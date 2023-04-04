package pl.futurecollars.invoicing.service.invoice;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import pl.futurecollars.invoicing.db.Database;
import pl.futurecollars.invoicing.model.Invoice;

@Service
public class InvoiceService {

  private final Database<Invoice> database;

  public InvoiceService(Database<Invoice> database) {
    this.database = database;
  }

  public long save(Invoice invoice) {
    return database.save(invoice);
  }

  public Optional<Invoice> getById(long id) {
    return database.getById(id);
  }

  public List<Invoice> getAll() {
    return database.getAll();
  }

  public Optional<Invoice> update(long id, Invoice data) {
    data.setId(id); // just in case it was not set
    return database.update(id, data);
  }

  public Optional<Invoice> delete(long id) {
    return database.delete(id);
  }

}
