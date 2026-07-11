package com.carsales.util;

import com.carsales.entity.Car;
import com.carsales.enums.CarStatus;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Excel 导入工具类
 * 用于从 .xlsx 文件批量导入车辆数据
 *
 * Excel 列顺序：
 * 0 - 品牌 (brand)
 * 1 - 车型名称 (model)
 * 2 - 排量 (displacement) - 可选
 * 3 - 变速箱 (transmission) - 可选
 * 4 - 颜色 (color) - 可选
 * 5 - 售价 (price)
 * 6 - 库存 (stock) - 可选，默认0
 * 7 - 车辆图片 - 可选，将图片插入对应行的单元格位置
 */
public class ExcelImportUtil {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    private ExcelImportUtil() {
        // utility class
    }

    /**
     * 一行导入结果：车辆数据 + 可选的原生图片字节
     */
    public static class CarImportRow {
        private final Car car;
        private final byte[] imageData;
        private final String imageExtension;

        public CarImportRow(Car car, byte[] imageData, String imageExtension) {
            this.car = car;
            this.imageData = imageData;
            this.imageExtension = imageExtension;
        }

        public Car getCar() { return car; }
        public byte[] getImageData() { return imageData; }
        public String getImageExtension() { return imageExtension; }
        public boolean hasImage() { return imageData != null && imageData.length > 0; }
    }

    /**
     * 解析 Excel 文件（不含图片）— InputStream 版本
     */
    public static List<Car> parseCars(InputStream inputStream) throws IOException {
        List<Car> cars = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                cars.add(parseRow(row));
            }
        }
        return cars;
    }

    /**
     * 解析 Excel 文件，包含车辆图片
     * 返回 CarImportRow 列表，每行包含车辆 + 该行嵌入的图片（如果有）
     */
    public static List<CarImportRow> parseCarsWithImages(InputStream inputStream) throws IOException {
        List<CarImportRow> rows = new ArrayList<>();

        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            XSSFSheet sheet = workbook.getSheetAt(0);

            // 1. 按行解析车辆数据
            Map<Integer, Integer> indexToRow = new HashMap<>();
            List<Car> cars = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Car car = parseRow(row);
                if (car.getBrand() == null || car.getBrand().isBlank()) continue;
                int dataIndex = cars.size();
                cars.add(car);
                indexToRow.put(dataIndex, i);
            }

            // 2. 读取 Excel 中嵌入的图片，按行匹匹配
            Map<Integer, PictureInfo> rowPictures = extractPicturesByRow(sheet);

            // 2b. 如果标准方式没找到图片，尝试 WPS Office 格式（cellimages.xml + DISPIMG 公式）
            if (rowPictures.isEmpty()) {
                Map<Integer, PictureInfo> wpsPictures = extractWpsPicturesByRow(workbook, sheet);
                if (!wpsPictures.isEmpty()) {
                    rowPictures = wpsPictures;
                }
            }

            // 3. 合并
            for (int idx = 0; idx < cars.size(); idx++) {
                int excelRowNum = indexToRow.get(idx);
                PictureInfo pic = rowPictures.get(excelRowNum);
                Car car = cars.get(idx);

                if (pic != null && ALLOWED_EXTENSIONS.contains(pic.extension)) {
                    rows.add(new CarImportRow(car, pic.data, pic.extension));
                } else {
                    rows.add(new CarImportRow(car, null, null));
                }
            }
        }

        return rows;
    }

    /**
     * 从 sheet 中提取所有图片，按行号分组（每行取第一张）
     * 适用于标准 Excel 嵌入图片（XSSFDrawing）
     */
    private static Map<Integer, PictureInfo> extractPicturesByRow(XSSFSheet sheet) {
        Map<Integer, PictureInfo> result = new HashMap<>();
        XSSFDrawing drawing = sheet.getDrawingPatriarch();
        if (drawing == null) return result;

        for (XSSFShape shape : drawing.getShapes()) {
            if (!(shape instanceof XSSFPicture)) continue;
            XSSFPicture pic = (XSSFPicture) shape;

            int rowIndex = -1;
            if (pic.getAnchor() instanceof XSSFClientAnchor) {
                rowIndex = ((XSSFClientAnchor) pic.getAnchor()).getRow1();
            }
            if (rowIndex < 0) continue;

            if (result.containsKey(rowIndex)) continue;

            byte[] data = pic.getPictureData().getData();
            String extension = pic.getPictureData().suggestFileExtension();
            if ("jpeg".equalsIgnoreCase(extension)) extension = "jpg";
            result.put(rowIndex, new PictureInfo(data, extension));
        }

        return result;
    }

    /**
     * 从 WPS Office 格式的 Excel 中提取图片
     * WPS 使用 cellimages.xml + DISPIMG 公式，不走标准 XSSFDrawing
     *
     * 解析流程：
     * 1. 读取 xl/cellimages.xml → 获取 DISPIMG ID → blip rId 映射
     * 2. 读取 xl/_rels/cellimages.xml.rels → 获取 rId → media 文件路径
     * 3. 从 media 文件读取图片字节
     * 4. 扫描每行的 H 列，如含 DISPIMG 公式则提取 ID，关联图片
     */
    private static Map<Integer, PictureInfo> extractWpsPicturesByRow(XSSFWorkbook workbook, XSSFSheet sheet) {
        Map<Integer, PictureInfo> result = new HashMap<>();
        try {
            OPCPackage pkg = workbook.getPackage();

            // 查找 cellimages.xml 和其 .rels 部分
            PackagePart cellImagesPart = null;
            PackagePart cellImagesRelsPart = null;
            Map<String, byte[]> mediaParts = new HashMap<>(); // fileName → bytes

            for (PackagePart part : pkg.getParts()) {
                String name = part.getPartName().toString();
                if (name.contains("cellimages.xml") && !name.endsWith(".rels")) {
                    cellImagesPart = part;
                } else if (name.contains("cellimages.xml.rels")) {
                    cellImagesRelsPart = part;
                } else if (name.startsWith("/xl/media/")) {
                    String fileName = name.substring("/xl/media/".length());
                    try (InputStream is = part.getInputStream()) {
                        mediaParts.put(fileName, readAllBytes(is));
                    }
                }
            }

            if (cellImagesPart == null) return result;

            // 解析 cellimages.xml.rels → rId → media 文件路径
            Map<String, String> ridToTarget = new HashMap<>();
            if (cellImagesRelsPart != null) {
                DocumentBuilderFactory relsFactory = DocumentBuilderFactory.newInstance();
                relsFactory.setNamespaceAware(true);
                DocumentBuilder relsBuilder = relsFactory.newDocumentBuilder();
                Document relsDoc = relsBuilder.parse(cellImagesRelsPart.getInputStream());
                NodeList relationships = relsDoc.getDocumentElement().getChildNodes();
                for (int i = 0; i < relationships.getLength(); i++) {
                    if (relationships.item(i) instanceof Element) {
                        Element rel = (Element) relationships.item(i);
                        String id = rel.getAttribute("Id");
                        String target = rel.getAttribute("Target");
                        ridToTarget.put(id, target);
                    }
                }
            }

            // 解析 cellimages.xml → DISPIMG ID → rId
            Map<String, String> dispimgIdToRid = new HashMap<>();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(cellImagesPart.getInputStream());

            // 查找 etc:cellImage 或 cellImage 元素
            NodeList cellImageNodes = doc.getDocumentElement().getChildNodes();
            for (int i = 0; i < cellImageNodes.getLength(); i++) {
                if (cellImageNodes.item(i) instanceof Element) {
                    Element cellImageElem = (Element) cellImageNodes.item(i);
                    // 查找 cNvPr 元素获取 name 属性（即 DISPIMG ID）
                    NodeList nvPrList = cellImageElem.getElementsByTagNameNS("*", "cNvPr");
                    for (int j = 0; j < nvPrList.getLength(); j++) {
                        Element nvPr = (Element) nvPrList.item(j);
                        String dispimgId = nvPr.getAttribute("name");
                        if (dispimgId != null && !dispimgId.isEmpty()) {
                            // 查找同级的 blip 元素获取 r:embed
                            NodeList blipList = cellImageElem.getElementsByTagNameNS("*", "blip");
                            for (int k = 0; k < blipList.getLength(); k++) {
                                Element blip = (Element) blipList.item(k);
                                String embed = blip.getAttributeNS("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "embed");
                                if (embed != null && !embed.isEmpty()) {
                                    dispimgIdToRid.put(dispimgId, embed);
                                }
                            }
                        }
                    }
                }
            }

            if (dispimgIdToRid.isEmpty()) return result;

            // 构建 DISPIMG ID → 图片字节 映射
            Map<String, byte[]> dispimgToImage = new HashMap<>();
            for (Map.Entry<String, String> entry : dispimgIdToRid.entrySet()) {
                String rid = entry.getValue();
                String target = ridToTarget.get(rid);
                if (target != null) {
                    // target 是相对路径，如 "media/image1.jpeg"
                    String fileName = target;
                    if (fileName.contains("/")) {
                        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
                    }
                    byte[] imageBytes = mediaParts.get(fileName);
                    if (imageBytes != null) {
                        dispimgToImage.put(entry.getKey(), imageBytes);
                    }
                }
            }

            if (dispimgToImage.isEmpty()) return result;

            // 扫描每一行 H 列，检查 DISPIMG 公式
            Pattern dispimgPattern = Pattern.compile("DISPIMG\\(\"([^\"]+)\"");
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Cell hCell = row.getCell(7); // H 列
                if (hCell == null) continue;

                String formula = null;
                try {
                    if (hCell.getCellType() == CellType.FORMULA) {
                        formula = hCell.getCellFormula();
                    }
                } catch (Exception ignored) {}

                if (formula == null || !formula.contains("DISPIMG")) continue;

                Matcher matcher = dispimgPattern.matcher(formula);
                if (matcher.find()) {
                    String id = matcher.group(1);
                    byte[] imageBytes = dispimgToImage.get(id);
                    if (imageBytes != null) {
                        result.put(i, new PictureInfo(imageBytes, "jpg"));
                    }
                }
            }
        } catch (Exception e) {
            // WPS 格式解析失败时不抛异常，静默返回空
            System.err.println("WPS图片解析失败: " + e.getMessage());
        }

        return result;
    }

    private static byte[] readAllBytes(InputStream is) throws IOException {
        byte[] buf = new byte[8192];
        int pos = 0;
        int read;
        while ((read = is.read(buf, pos, buf.length - pos)) != -1) {
            pos += read;
            if (pos == buf.length) {
                byte[] newBuf = new byte[buf.length * 2];
                System.arraycopy(buf, 0, newBuf, 0, buf.length);
                buf = newBuf;
            }
        }
        byte[] result = new byte[pos];
        System.arraycopy(buf, 0, result, 0, pos);
        return result;
    }

    private static class PictureInfo {
        final byte[] data;
        final String extension;
        PictureInfo(byte[] data, String extension) {
            this.data = data;
            this.extension = extension;
        }
    }

    /**
     * 解析 Excel 的一行数据为 Car 对象
     */
    private static Car parseRow(Row row) {
        Car car = new Car();
        car.setBrand(getStringCellValue(row.getCell(0)));
        car.setModel(getStringCellValue(row.getCell(1)));
        car.setDisplacement(getStringCellValue(row.getCell(2)));
        car.setTransmission(getStringCellValue(row.getCell(3)));
        car.setColor(getStringCellValue(row.getCell(4)));
        car.setPrice(BigDecimal.valueOf(getNumericCellValue(row.getCell(5))));

        Cell stockCell = row.getCell(6);
        if (stockCell != null) {
            car.setStock((int) getNumericCellValue(stockCell));
        } else {
            car.setStock(0);
        }

        car.setStatus(CarStatus.on_sale);
        return car;
    }

    private static String getStringCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue().trim();
            case NUMERIC:
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    return String.valueOf((long) val);
                }
                return String.valueOf(val);
            case BOOLEAN:  return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try { return cell.getStringCellValue().trim(); }
                catch (Exception e) { return String.valueOf(cell.getNumericCellValue()); }
            default: return null;
        }
    }

    private static double getNumericCellValue(Cell cell) {
        if (cell == null) return 0;
        switch (cell.getCellType()) {
            case NUMERIC:  return cell.getNumericCellValue();
            case STRING:
                try { return Double.parseDouble(cell.getStringCellValue().trim()); }
                catch (NumberFormatException e) { return 0; }
            case FORMULA:  return cell.getNumericCellValue();
            default: return 0;
        }
    }
}
