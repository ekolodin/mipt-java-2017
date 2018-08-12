package ru.mipt.java2017.hw3;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import models.Author;
import models.Book;
import models.BookAuthor;
import org.hibernate.cfg.Configuration;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DatabaseAccess {

  final EntityManager entityManager;

  DatabaseAccess(String url) {
    String driverClassName = null;
    try {
      driverClassName = DriverManager.getDriver(url).getClass().getCanonicalName();
    } catch (SQLException exception) {
      Logger logger = LoggerFactory.getLogger("DatabaseAcc");
      logger.error("Cannot get the driver");
      exception.printStackTrace();
      System.exit(1);
    }

    Properties properties = new Properties();

    if (url.contains("sqlite")) {
      properties.setProperty("hibernate.dialect", "org.hibernate.dialect.SQLiteDialect");
    }

    properties.setProperty("hibernate.connection.driver_class", driverClassName);
    properties.setProperty("hibernate.connection.url", url);

    EntityManagerFactory entityManagerFactory = Persistence
        .createEntityManagerFactory("mydatabase", properties);
    entityManager = entityManagerFactory.createEntityManager();

    Runtime.getRuntime().addShutdownHook(new Thread(entityManagerFactory::close));
  }

  static {
    Configuration configuration = new Configuration();
    configuration.addAnnotatedClass(Book.class);
    configuration.addAnnotatedClass(Author.class);
    configuration.addAnnotatedClass(BookAuthor.class);

    configuration.configure();
  }
}
