package pl.futurecollars.invoicing.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import pl.futurecollars.invoicing.db.Database;
import pl.futurecollars.invoicing.db.file.FileBasedDatabase;
import pl.futurecollars.invoicing.db.file.IdProvider;
import pl.futurecollars.invoicing.db.jpa.InvoiceRepository;
import pl.futurecollars.invoicing.db.jpa.JpaDatabase;
import pl.futurecollars.invoicing.db.memory.InMemoryDatabase;
import pl.futurecollars.invoicing.db.sql.SqlDatabase;
import pl.futurecollars.invoicing.utils.FilesService;
import pl.futurecollars.invoicing.utils.JsonService;

@Slf4j
@Configuration
public class InvoiceConfiguration {

  private static final String INVOICES_FILE_NAME = "invoices.json";
  private static final String ID_FILE_NAME = "id.txt";
  private static final String DATABASE_LOCATION = "db";

  @Bean
  @ConditionalOnProperty(name = "invoicing-system.database", havingValue = "memory")
  public InMemoryDatabase inMemoryDatabase() {
    log.info("loading in-memory database");
    return new InMemoryDatabase();
  }

  @Bean
  @ConditionalOnProperty(name = "invoicing-system.database", havingValue = "file")
  public IdProvider idService(FilesService filesService) throws IOException {
    Path idFilePath = Files.createTempFile(DATABASE_LOCATION, ID_FILE_NAME);
    return new IdProvider(idFilePath, filesService);
  }

  @Bean
  @ConditionalOnProperty(name = "invoicing-system.database", havingValue = "file")
  public Database fileBasedDatabase(IdProvider idService, FilesService filesService, JsonService jsonService,
                                    @Value("${database.path:/db/defaultDb.json}") String databasePath) throws IOException {
    log.info("loading filebased database");
    log.debug(databasePath);
    Path databaseFilePath = Files.createTempFile(DATABASE_LOCATION, INVOICES_FILE_NAME);
    return new FileBasedDatabase(databaseFilePath, idService, filesService, jsonService);
  }

  @Bean
  @ConditionalOnProperty(name = "invoicing-system.database", havingValue = "sql")
  public Database sqlDatabase(JdbcTemplate jdbcTemplate) {
    return new SqlDatabase(jdbcTemplate);
  }


  @Bean
  @ConditionalOnProperty(name = "invoicing-system.database", havingValue = "jpa")
  public Database jpaDatabase(InvoiceRepository invoiceRepository) {
    return new JpaDatabase(invoiceRepository);
  }

}

