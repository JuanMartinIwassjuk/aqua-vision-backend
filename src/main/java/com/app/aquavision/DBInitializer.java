package com.app.aquavision;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.Connection;

@Component
public class DBInitializer {

  @Autowired
  private DataSource dataSource;

  private static final Logger logger = LoggerFactory.getLogger(DBInitializer.class);

  @PostConstruct
  public void init() {
      try (Connection connection = dataSource.getConnection()) {
      ScriptUtils.executeSqlScript(connection, new ClassPathResource("dbScripts/initializeSchema.sql"));
      //ScriptUtils.executeSqlScript(connection, new ClassPathResource("dbScripts/homeDataMock.sql"));
      //ScriptUtils.executeSqlScript(connection, new ClassPathResource("dbScripts/measureDataMock.sql"));
    } catch (Exception e) {
        logger.error("Error al inicializar la base de datos", e);
        e.printStackTrace();
    }
  }
}