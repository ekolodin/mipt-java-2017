package ru.mipt.java2017.hw3;

import java.io.InputStream;
import java.util.Objects;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class ExcelDataSource {

  private final static Logger logger = LoggerFactory.getLogger("excel");
  private final Workbook workbook;

  List<String> getFromExcel(String column) {
    Sheet sheet = workbook.getSheetAt(0);
    int cellNumber = 0;
    if (Objects.equals(column, sheet.getRow(0).getCell(0).getStringCellValue())) {
      cellNumber = 0;
    } else if (Objects.equals(column, sheet.getRow(0).getCell(1).getStringCellValue())) {
      cellNumber = 1;
    } else if (Objects.equals(column, sheet.getRow(0).getCell(2).getStringCellValue())) {
      cellNumber = 2;
    }
    int maxRowNumber = sheet.getLastRowNum();
    List<String> list = new ArrayList<>(maxRowNumber);
    for (int i = 1; i <= maxRowNumber; ++i) {
      Row row = sheet.getRow(i);
      Cell titleCell = row.getCell(cellNumber);
      String info = titleCell.getStringCellValue();

      list.add(info);
    }
    return list;
  }

  static ExcelDataSource createExcelDataSource(String fileName) {
    try {
      return new ExcelDataSource(fileName);
    } catch (FileNotFoundException e) {
      logger.error("File not found: {}", fileName);
      return null;
    } catch (IOException e) {
      logger.error("IOError: {}", e.getMessage());
      return null;
    }
  }

  static ExcelDataSource createExcelDataSource(InputStream inputStream) {
    try {
      return new ExcelDataSource(inputStream);
    } catch (IOException e) {
      logger.error("IOError: {}", e.getMessage());
      return null;
    }
  }

  private ExcelDataSource(String fileName) throws IOException {
    FileInputStream fileInputStream = new FileInputStream(
        new File(fileName)
    );
    workbook = new XSSFWorkbook(fileInputStream);
  }

  private ExcelDataSource(InputStream inputStream) throws IOException {
    workbook = new XSSFWorkbook(inputStream);
  }

}
