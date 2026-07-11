package com.carsales.util;

import com.carsales.entity.Car;
import com.carsales.enums.CarStatus;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * 解析 Excel 文件（不含图片），向后兼容
     */
    public static List<Car> parseCars(MultipartFile file) throws IOException {
        List<Car> cars = new ArrayList<>();
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
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
    public static List<CarImportRow> parseCarsWithImages(MultipartFile file) throws IOException {
        List<CarImportRow> rows = new ArrayList<>();

        try (InputStream is = file.getInputStream(); XSSFWorkbook workbook = new XSSFWorkbook(is)) {
            XSSFSheet sheet = workbook.getSheetAt(0);

            // 1. 按行解析车辆数据
            Map<Integer, Integer> indexToRow = new HashMap<>(); // rows索引 → Excel行号
            List<Car> cars = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Car car = parseRow(row);
                if (car.getBrand() == null || car.getBrand().isBlank()) continue; // 空行跳过
                int dataIndex = cars.size();
                cars.add(car);
                indexToRow.put(dataIndex, i);
            }

            // 2. 读取 Excel 中嵌入的图片，按行拼装
            Map<Integer, PictureInfo> rowPictures = extractPicturesByRow(sheet);

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
     */
    private static Map<Integer, PictureInfo> extractPicturesByRow(XSSFSheet sheet) {
        Map<Integer, PictureInfo> result = new HashMap<>();
        XSSFDrawing drawing = sheet.getDrawingPatriarch();
        if (drawing == null) return result;

        for (XSSFShape shape : drawing.getShapes()) {
            if (!(shape instanceof XSSFPicture)) continue;
            XSSFPicture pic = (XSSFPicture) shape;

            // 获取图片锚定的行号
            int rowIndex = -1;
            if (pic.getAnchor() instanceof XSSFClientAnchor) {
                rowIndex = ((XSSFClientAnchor) pic.getAnchor()).getRow1();
            }
            if (rowIndex < 0) continue;

            // 如果该行已有图片，跳过（只取第一张）
            if (result.containsKey(rowIndex)) continue;

            byte[] data = pic.getPictureData().getData();
            String extension = pic.getPictureData().suggestFileExtension();
            // "jpeg" → "jpg"
            if ("jpeg".equalsIgnoreCase(extension)) {
                extension = "jpg";
            }
            result.put(rowIndex, new PictureInfo(data, extension));
        }

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
