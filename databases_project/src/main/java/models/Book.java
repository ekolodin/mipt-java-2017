package models;

import java.math.BigDecimal;
import javax.persistence.*;

@Entity
@Table(name = "books")
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "title", length = 100)
  private String title;

  @Column(name = "isbn", precision = 0)
  private BigDecimal isbn;

  @Column(name = "cover", length = 400)
  private String url;

  public Long getId() {
    return id;
  }

  public BigDecimal getIsbn() {
    return isbn;
  }

  public String getTitle() {
    return title;
  }

  public String getUrl() {
    return url;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setIsbn(BigDecimal isbn) {
    this.isbn = isbn;
  }
}
