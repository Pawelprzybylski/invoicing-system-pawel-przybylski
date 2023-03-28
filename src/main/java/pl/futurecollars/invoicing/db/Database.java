package pl.futurecollars.invoicing.db;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import pl.futurecollars.invoicing.model.Invoice;
import pl.futurecollars.invoicing.model.InvoiceEntry;

public interface Database {

  long save(Invoice invoice);

  Optional<Invoice> getById(long id);

  List<Invoice> getAll();

  Optional<Invoice> update(long id, Invoice updatedInvoice);

  Optional<Invoice> delete(long id);

  default BigDecimal visit(
      Predicate<Invoice> invoicePredicate,
      Function<InvoiceEntry, BigDecimal> invoiceEntryToValue
  ) {
    return getAll().stream()
        .filter(invoicePredicate)
        .flatMap(i -> i.getEntries().stream())
        .map(invoiceEntryToValue)
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  default void reset() {
    getAll().forEach(invoice -> delete(invoice.getId()));
  }

}
