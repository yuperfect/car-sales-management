package com.carsales.util;

import com.carsales.entity.Car;
import com.carsales.enums.CarStatus;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
 */
public class ExcelImportUtil {

    private ExcelImportUtil() {
        // utility class
    }

    /**
     * 解析 Excel 文件，返回车辆列表
     *
     * @param file 上传的 Excel 文件 (.xlsx)
     * @return 车辆实体列表
     * @throws IOException 读取文件异常
     */
    public static List<Car> parseCars(MultipartFile file) throws IOException {
        List<Car> cars = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);

            // 从第2行开始（跳过表头）
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                Car car = new Car();

                // 品牌
                car.setBrand(getStringCellValue(row.getCell(0)));

                // 车型名称
                car.setModel(getStringCellValue(row.getCell(1)));

                // 排量（可选）
                car.setDisplacement(getStringCellValue(row.getCell(2)));

                // 变速箱（可选）
                car.setTransmission(getStringCellValue(row.getCell(3)));

                // 颜色（可选）
                car.setColor(getStringCellValue(row.getCell(4)));

                // 售价
                car.setPrice(BigDecimal.valueOf(getNumericCellValue(row.getCell(5))));

                // 库存（可选，默认0）
                Cell stockCell = row.getCell(6);
                if (stockCell != null) {
                    car.setStock((int) getNumericCellValue(stockCell));
                } else {
                    car.setStock(0);
                }

                // 默认在售状态
                car.setStatus(CarStatus.on_sale);

                cars.add(car);
            }
        }

        return cars;
    }

    private static String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // 避免 "1.0" 这样的数值
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    return String.valueOf((long) val);
                }
                return String.valueOf(val);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }

    private static double getNumericCellValue(Cell cell) {
        if (cell == null) {
            return 0;
        }
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue().trim());
                } catch (NumberFormatException e) {
                    return 0;
                }
            case FORMULA:
                return cell.getNumericCellValue();
            default:
                return 0;
        }
    }
}
