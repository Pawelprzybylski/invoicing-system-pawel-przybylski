package pl.futurecollars.invoicing.db.memory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.futurecollars.invoicing.db.Database;
import pl.futurecollars.invoicing.model.Company;
import pl.futurecollars.invoicing.model.Invoice;

@Configuration
@ConditionalOnProperty(name = "invoicing-system.database", havingValue = "memory")
public class InMemoryDatabaseConfiguration {

  @Bean
  public Database<Invoice> invoiceInMemoryDatabase() {
    return new InMemoryDatabase<>();
  }

  @Bean
  public Database<Company> companyInMemoryDatabase() {
    return new InMemoryDatabase<>();
  }

}
