package pl.futurecollars.invoicing.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.futurecollars.invoicing.db.Database;
import pl.futurecollars.invoicing.db.file.FileBasedDatabase;
import pl.futurecollars.invoicing.db.file.IdProvider;
import pl.futurecollars.invoicing.db.memory.InMemoryDatabase;
import pl.futurecollars.invoicing.service.InvoiceService;
import pl.futurecollars.invoicing.service.TaxCalculatorService;
import pl.futurecollars.invoicing.utils.FilesService;
import pl.futurecollars.invoicing.utils.JsonService;

@Slf4j
@Configuration
public class InvoiceConfiguration {

  private static final String INVOICES_FILE_NAME = "invoices.json";
  private static final String ID_FILE_NAME = "id.txt";
  private static final String DATABASE_LOCATION = "db";

  @Bean
  public JsonService jsonService() {
    return new JsonService();
  }

  @Bean
  public FilesService filesService() {
    return new FilesService();
  }

  @Bean
  @ConditionalOnProperty(name = "invoicing-system.database", havingValue = "memory")
  public InMemoryDatabase inMemoryDatabase() {
    return new InMemoryDatabase();
  }

  @Bean
  public InvoiceService invoiceService(@Qualifier("fileBasedDatabase") Database database) {
    return new InvoiceService(database);
  }

  @Bean
  public IdProvider idService(FilesService filesService) throws IOException {
    Path idFilePath = Files.createTempFile(DATABASE_LOCATION, ID_FILE_NAME);
    return new IdProvider(idFilePath, filesService);
  }

  @Bean
  @ConditionalOnProperty(name = "invoicing-system.database", havingValue = "file")
  public Database fileBasedDatabase(IdProvider idService, FilesService filesService, JsonService jsonService) throws IOException {
    Path databaseFilePath = Files.createTempFile(DATABASE_LOCATION, INVOICES_FILE_NAME);
    return new FileBasedDatabase(databaseFilePath, idService, filesService, jsonService);
  }

  @Bean
  public TaxCalculatorService taxCalculatorService(Database database) {
    return new TaxCalculatorService(database);
  }

}
