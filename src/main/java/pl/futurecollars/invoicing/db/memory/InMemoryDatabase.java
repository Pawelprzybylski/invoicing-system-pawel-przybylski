package pl.futurecollars.invoicing.db.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import pl.futurecollars.invoicing.db.Database;
import pl.futurecollars.invoicing.model.Invoice;

public class InMemoryDatabase implements Database {

  private final Map<Long, Invoice> invoices = new HashMap<>();
  private long nextID = 1;

  @Override
  public long save(Invoice invoice) {
    long newId = nextID++;
    invoice.setId(newId);
    invoices.put(newId, invoice);
    return nextID++;
  }

  @Override
  public Optional<Invoice> getById(long id) {
    return Optional.of(invoices.get(id));
  }

  @Override
  public List<Invoice> getAll() {
    return new ArrayList<>(invoices.values());
  }

  @Override
  public void update(long id, Invoice updatedInvoice) {
    if (!invoices.containsKey(id)) {
      throw new IllegalArgumentException("Id " + id + " does not exist");
    }

    updatedInvoice.setId(id);
    invoices.put(id, updatedInvoice);
  }

  @Override
  public void delete(long id) {
    invoices.remove(id);
  }

}
