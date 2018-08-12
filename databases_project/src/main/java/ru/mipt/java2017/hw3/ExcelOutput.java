package ru.mipt.java2017.hw3;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import models.Author;
import models.Book;
import models.BookAuthor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;

class ExcelOutput {

  private final FileOutputStream fileOutputStream;
  private final static Logger logger = LoggerFactory.getLogger("excel");
  private final Workbook workbook;

  private void setTable(Sheet sheet, List<String> gaps) throws IOException {
    Row headingRow = sheet.createRow(0);
    List<Cell> cells = new ArrayList<>(gaps.size());
    for (int i = 0; i < gaps.size(); ++i) {
      cells.add(headingRow.createCell(i));
      cells.get(i).setCellValue(gaps.get(i));
    }
    CellStyle style = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    style.setFont(font);

    headingRow.setRowStyle(style);
    sheet.setColumnWidth(0, 3000);
    sheet.setColumnWidth(1, 5000);
    sheet.setColumnWidth(2, 12000);
    sheet.setColumnWidth(3, 5000);
  }

  private void fillBookTable(Sheet sheet, List<Book> books) {
    int rowNumber = 1;
    for (Book book : books) {
      Long id = book.getId();
      String bookTitle = book.getTitle();
      BigDecimal bookIsbn = book.getIsbn();
      String url = book.getUrl();

      Row row = sheet.createRow(rowNumber++);
      Cell ID = row.createCell(0);
      Cell ISBN = row.createCell(1);
      Cell title = row.createCell(2);
      Cell cover = row.createCell(3);
      ID.setCellValue(id);
      title.setCellValue(bookTitle);
      ISBN.setCellValue(bookIsbn.toString());
      cover.setCellValue(url);
    }
  }

  private void fillAuthorTable(Sheet sheet, List<Author> authors) {
    int rowNumber = 1;
    for (Author author : authors) {
      Long id = author.getId();
      String name = author.getName();

      Row row = sheet.createRow(rowNumber++);
      Cell ID = row.createCell(0);
      Cell authorName = row.createCell(1);
      ID.setCellValue(id);
      authorName.setCellValue(name);
    }
  }

  private void fillBookAuthorTable(Sheet sheet, List<BookAuthor> bookAuthors) {
    int rowNumber = 1;
    for (BookAuthor bookAuthor : bookAuthors) {
      Long id = bookAuthor.getId();
      Long bookId = bookAuthor.getBook().getId();
      Long authorId = bookAuthor.getAuthor().getId();
      int num = bookAuthor.getNum();

      Row row = sheet.createRow(rowNumber++);
      Cell ID = row.createCell(0);
      Cell bookIdCell = row.createCell(1);
      Cell authorIdCell = row.createCell(2);
      Cell number = row.createCell(3);
      ID.setCellValue(id);
      bookIdCell.setCellValue(bookId);
      authorIdCell.setCellValue(authorId);
      number.setCellValue(num);
    }
  }

  void makeBookTable(List<Book> books) throws IOException {
    Sheet sheet = workbook.createSheet("Books");
    List<String> gaps = new ArrayList<>(4);
    gaps.add("ID");
    gaps.add("ISBN");
    gaps.add("title");
    gaps.add("cover");
    setTable(sheet, gaps);
    fillBookTable(sheet, books);
  }

  void makeAuthorTable(List<Author> authors) throws IOException {
    Sheet sheet = workbook.createSheet("Authors");
    List<String> gaps = new ArrayList<>(2);
    gaps.add("ID");
    gaps.add("name");
    setTable(sheet, gaps);
    fillAuthorTable(sheet, authors);
  }

  void makeBookAuthorTable(List<BookAuthor> authors) throws IOException {
    Sheet sheet = workbook.createSheet("Books_Authors");
    List<String> gaps = new ArrayList<>(4);
    gaps.add("ID");
    gaps.add("books_id");
    gaps.add("authors_id");
    gaps.add("num");
    setTable(sheet, gaps);
    fillBookAuthorTable(sheet, authors);
  }

  void complete() throws IOException {
    workbook.write(fileOutputStream);
  }

  static ExcelOutput createExcelOutput(String fileName) {
    try {
      return new ExcelOutput(fileName);
    } catch (FileNotFoundException e) {
      logger.error("File not found: {}", fileName);
      return null;
    } catch (IOException e) {
      logger.error("IOError: {}", e.getMessage());
      return null;
    }
  }

  private ExcelOutput(String fileName) throws IOException {
    fileOutputStream = new FileOutputStream(
        new File(fileName)
    );
    workbook = new XSSFWorkbook();
  }

}
