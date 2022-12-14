package pl.futurecollars.invoicing.db.file;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import pl.futurecollars.invoicing.db.Database;
import pl.futurecollars.invoicing.model.Invoice;
import pl.futurecollars.invoicing.utils.FilesService;
import pl.futurecollars.invoicing.utils.JsonService;

@AllArgsConstructor
public class FileBasedDatabase implements Database {

  private final Path databasePath;
  private final IdProvider idProvider;
  private final FilesService filesService;
  private final JsonService jsonService;

  @Override
  public int save(Invoice invoice) {
    try {
      invoice.setId(idProvider.getNextIdAndIncrement());
      filesService.appendLineToFile(databasePath, jsonService.toJson(invoice));

      return invoice.getId();
    } catch (IOException ex) {
      throw new RuntimeException("Database failed to save invoice", ex);
    }
  }

  @Override
  public Optional<Invoice> getById(int id) {
    try {
      return filesService.readAllLines(databasePath)
          .stream()
          .filter(line -> containsId(line, id))
          .map(line -> jsonService.toObject(line, Invoice.class))
          .findFirst();
    } catch (IOException ex) {
      throw new RuntimeException("Database failed to get invoice with id: " + id, ex);
    }
  }

  @Override
  public List<Invoice> getAll() {
    try {
      return filesService.readAllLines(databasePath)
          .stream()
          .map(line -> jsonService.toObject(line, Invoice.class))
          .collect(Collectors.toList());
    } catch (IOException ex) {
      throw new RuntimeException("Failed to read invoices from file", ex);
    }
  }

  @Override
  public Optional<Invoice> update(int id, Invoice data) {
    try {
      List<String> allInvoices = filesService.readAllLines(databasePath);

      Optional<String> invoiceAsJson = allInvoices
          .stream()
          .filter(line -> containsId(line, id))
          .findFirst();

      if (invoiceAsJson.isPresent()) {
        allInvoices.remove(invoiceAsJson.get());
        Invoice invoice = jsonService.toObject(invoiceAsJson.get(), Invoice.class);
        updateInvoiceData(invoice, data);
        allInvoices.add(jsonService.toJson(invoice));
        filesService.writeLinesToFile(databasePath, allInvoices);
        return Optional.of(invoice);
      } else {
        return Optional.empty();
      }

    } catch (IOException ex) {
      throw new RuntimeException("Failed to update invoice with id: " + id, ex);
    }

  }

  private void updateInvoiceData(Invoice invoice, Invoice data) {
    invoice.setDate(data.getDate());
    invoice.setBuyer(data.getBuyer());
    invoice.setSeller(data.getSeller());
    invoice.setEntries(data.getEntries());
  }

  @Override
  public Optional<Invoice> delete(int id) {
    try {
      var allInvoices = filesService.readAllLines(databasePath);

      Optional<String> invoiceAsJson = allInvoices
          .stream()
          .filter(line -> containsId(line, id))
          .findFirst();

      if (invoiceAsJson.isPresent()) {
        allInvoices.remove(invoiceAsJson.get());
        filesService.writeLinesToFile(databasePath, allInvoices);
        return Optional.of(jsonService.toObject(invoiceAsJson.get(), Invoice.class));
      } else {
        return Optional.empty();
      }
    } catch (IOException ex) {
      throw new RuntimeException("Failed to delete invoice with id: " + id, ex);
    }
  }

  private boolean containsId(String line, int id) {
    return line.contains("\"id\":" + id + ",");
  }
}
