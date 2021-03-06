package models;

import javax.persistence.*;

@Entity
@Table(name = "books_authors")
public class BookAuthor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne
  @JoinColumn(name = "books_id")
  private Book book;

  @ManyToOne
  @JoinColumn(name = "authors_id")
  private Author author;

  @Column(name = "num")
  private int num;

  public void setBook(Book book) {
    this.book = book;
  }

  public void setAuthor(Author author) {
    this.author = author;
  }

  public void setNum(int num) {
    this.num = num;
  }

  public Long getId() {
    return id;
  }

  public Book getBook() {
    return book;
  }

  public Author getAuthor() {
    return author;
  }

  public int getNum() {
    return num;
  }
}
