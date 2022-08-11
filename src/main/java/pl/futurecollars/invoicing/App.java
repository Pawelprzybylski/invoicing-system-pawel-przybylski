
package pl.futurecollars.invoicing;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import pl.futurecollars.invoicing.db.Database;
import pl.futurecollars.invoicing.db.memory.InMemoryDatabase;
import pl.futurecollars.invoicing.model.Company;
import pl.futurecollars.invoicing.model.Invoice;
import pl.futurecollars.invoicing.model.InvoiceEntry;
import pl.futurecollars.invoicing.model.Vat;
import pl.futurecollars.invoicing.service.InvoiceService;

public class App {

  public static void main(String[] args) {

    Database database = new InMemoryDatabase();
    InvoiceService service = new InvoiceService(database);

    Company buyer = new Company("244124", "Warszawa", "First Company");
    Company seller = new Company("2525251", "Poznań", "Second Company");

    List<InvoiceEntry> products = List.of(new InvoiceEntry("First position", BigDecimal.valueOf(2442), BigDecimal.valueOf(124), Vat.VAT_23));

    Invoice invoice = new Invoice(LocalDate.now(), buyer, seller, products);

    long id = service.save(invoice);

    service.getById(id).ifPresent(System.out::println);

    System.out.println(service.getAll());

    service.delete(id);

  }

}
