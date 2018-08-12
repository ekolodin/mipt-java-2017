package ru.mipt.java2017.hw3;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import models.Author;
import models.Book;
import models.BookAuthor;

public class DatabaseUpdater {

  private final EntityManager entityManager;
  private final CriteriaBuilder builder;

  private DatabaseUpdater(DatabaseAccess access) {
    entityManager = access.entityManager;
    builder = entityManager.getCriteriaBuilder();
  }

  private void addAuthors(List<Author> authors) {
    entityManager.getTransaction().begin();
    for (Author author1 : authors) {
      CriteriaQuery<Author> query = builder.createQuery(Author.class);
      Root<Author> table = query.from(Author.class);
      Predicate sameAuthor = builder.equal(table.get("name"), author1.getName());
      query.where(sameAuthor);

      List<Author> author = entityManager.createQuery(query).getResultList();
      if (author.isEmpty()) {
        entityManager.persist(author1);
      }
    }
    entityManager.getTransaction().commit();
  }

  private List<Author> getAuthors(String string) {
    ArrayList<String> names = new ArrayList<>(Arrays.asList(string.split(",\\s+")));
    List<Author> list = new ArrayList<>(100);
    for (String name : names) {
      Author author = new Author();
      author.setName(name);
      list.add(author);
    }
    return list;
  }

  private void addBookAuthors(BigDecimal isbn, List<Author> authors) {
    entityManager.getTransaction().begin();
    for (int i = 0; i < authors.size(); ++i) {
      CriteriaQuery<Book> queryForBook = builder.createQuery(Book.class);
      Root<Book> tableForBook = queryForBook.from(Book.class);
      Predicate sameISBN = builder.equal(tableForBook.get("isbn"), isbn);
      queryForBook.where(sameISBN);

      CriteriaQuery<Author> queryForAuthor = builder.createQuery(Author.class);
      Root<Author> tableForAuthor = queryForAuthor.from(Author.class);
      Predicate sameAuthor = builder.equal(tableForAuthor.get("name"), authors.get(i).getName());
      queryForAuthor.where(sameAuthor);

      Book book = entityManager.createQuery(queryForBook).getSingleResult();
      Author author = entityManager.createQuery(queryForAuthor).getSingleResult();

      BookAuthor bookAuthor = new BookAuthor();
      bookAuthor.setAuthor(author);
      bookAuthor.setBook(book);
      bookAuthor.setNum(i + 1);
      entityManager.persist(bookAuthor);
    }
    entityManager.getTransaction().commit();
  }

  private void correctTypos(BigDecimal isbn, String correctTitle) {
    CriteriaQuery<Book> query = builder.createQuery(Book.class);
    Root<Book> table = query.from(Book.class);
    Predicate sameISBN = builder.equal(table.get("isbn"), isbn);
    query.where(sameISBN);

    entityManager.getTransaction().begin();
    Book book;
    try {
      book = entityManager.createQuery(query).getSingleResult();
      book.setTitle(correctTitle);
      entityManager.merge(book);
    } catch (NoResultException exception) {
      book = new Book();
      book.setIsbn(isbn);
      book.setTitle(correctTitle);
      entityManager.persist(book);
    }

    entityManager.getTransaction().commit();
  }

  private List<Book> getAllBooks() {
    CriteriaQuery<Book> query = builder.createQuery(Book.class);
    Root<Book> table = query.from(Book.class);
    Predicate everyBook = builder.isNotNull(table.get("id"));
    query.where(everyBook);

    entityManager.getTransaction().begin();
    List<Book> books = entityManager.createQuery(query).getResultList();
    entityManager.getTransaction().commit();

    return books;
  }

  private List<Author> getAllAuthors() {
    CriteriaQuery<Author> query = builder.createQuery(Author.class);
    Root<Author> table = query.from(Author.class);
    Predicate everyAuthor = builder.isNotNull(table.get("id"));
    query.where(everyAuthor);

    entityManager.getTransaction().begin();
    List<Author> books = entityManager.createQuery(query).getResultList();
    entityManager.getTransaction().commit();

    return books;
  }

  private List<BookAuthor> getAllBookAuthors() {
    CriteriaQuery<BookAuthor> query = builder.createQuery(BookAuthor.class);
    Root<BookAuthor> table = query.from(BookAuthor.class);
    Predicate everyBookAuthor = builder.isNotNull(table.get("id"));
    query.where(everyBookAuthor);

    entityManager.getTransaction().begin();
    List<BookAuthor> bookAuthors = entityManager.createQuery(query).getResultList();
    entityManager.getTransaction().commit();

    return bookAuthors;
  }

  public static void main(String[] args) {
    ExcelDataSource sheet = ExcelDataSource.createExcelDataSource(args[1]);
    assert sheet != null;
    List<String> titles = sheet.getFromExcel("Title");
    List<String> authors = sheet.getFromExcel("Authors");
    List<String> isbnInStr = sheet.getFromExcel("ISBN");
    List<BigDecimal> isbn = new ArrayList<>(isbnInStr.size());
    for (String anIsbnInStr : isbnInStr) {
      BigDecimal singleIsbn = new BigDecimal(anIsbnInStr.substring(8, 21));
      isbn.add(singleIsbn);
    }

    DatabaseAccess access = new DatabaseAccess(args[0]);
    DatabaseUpdater updater = new DatabaseUpdater(access);
    for (int i = 0; i < isbn.size(); ++i) {
      updater.correctTypos(isbn.get(i), titles.get(i));
    }

    for (int i = 0; i < authors.size(); ++i) {
      List<Author> listOfAuthors = updater.getAuthors(authors.get(i));
      updater.addAuthors(listOfAuthors);
      updater.addBookAuthors(isbn.get(i), listOfAuthors);
    }

    ExcelOutput outputSheet = ExcelOutput.createExcelOutput(args[2]);
    try {
      assert outputSheet != null;
      outputSheet.makeBookTable(updater.getAllBooks());
      outputSheet.makeAuthorTable(updater.getAllAuthors());
      outputSheet.makeBookAuthorTable(updater.getAllBookAuthors());
      outputSheet.complete();
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.exit(0);
  }
}
