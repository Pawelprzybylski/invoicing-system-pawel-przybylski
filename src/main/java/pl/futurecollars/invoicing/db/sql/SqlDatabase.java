package pl.futurecollars.invoicing.db.sql;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;
import pl.futurecollars.invoicing.db.Database;
import pl.futurecollars.invoicing.model.Car;
import pl.futurecollars.invoicing.model.Company;
import pl.futurecollars.invoicing.model.Invoice;
import pl.futurecollars.invoicing.model.InvoiceEntry;
import pl.futurecollars.invoicing.model.Vat;

@AllArgsConstructor
public class SqlDatabase implements Database {

  private final JdbcTemplate jdbcTemplate;

  private final String selectQuery = "select i.id, i.date, i.number, "
      + "c1.name as seller_name, c1.tax_identification_number as seller_tax_id, c1.address as seller_address, "
      + "c1.health_insurance as seller_health_insurance, c1.pension_insurance as seller_pension_insurance, "
      + "c1.id as seller_id, "
      + "c2.name as buyer_name, c2.tax_identification_number as buyer_tax_id , c2.address as buyer_address, "
      + "c2.health_insurance as buyer_health_insurance, c2.pension_insurance as buyer_pension_insurance, "
      + "c2.id as buyer_id "
      + "from invoice i "
      + "inner join company c1 on i.seller = c1.id "
      + "inner join company c2 on i.buyer = c2.id";

  private final Map<Vat, Integer> vatToId = new HashMap<>();
  private final Map<Integer, Vat> idToVat = new HashMap<>();

  @PostConstruct
  void initVatRatesMap() {
    jdbcTemplate.query("select * from vat", rs -> {
      Vat vat = Vat.valueOf("VAT_" + rs.getString("name"));
      int id = rs.getInt("id");

      vatToId.put(vat, id);
      idToVat.put(id, vat);
    });
  }

  private Integer insertCarAndGetItId(Car car) {
    if (car == null) {
      return null;
    }

    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(
          "insert into car (registration_number, personal_use) values (?, ?);", new String[]{"id"});
      ps.setString(1, car.getRegistrationNumber());
      ps.setBoolean(2, car.isPersonalUse());
      return ps;
    }, keyHolder);

    return keyHolder.getKey().intValue();
  }

  private int insertCompany(Company company) {
    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement("insert into company "
          + "(tax_identification_number, address, name, health_insurance, pension_insurance) "
          + "values (?, ?, ?, ?, ?);", new String[]{"id"});
      ps.setString(1, company.getTaxIdentificationNumber());
      ps.setString(2, company.getAddress());
      ps.setString(3, company.getName());
      ps.setBigDecimal(4, company.getHealthInsurance());
      ps.setBigDecimal(5, company.getPensionInsurance());
      return ps;
    }, keyHolder);

    return keyHolder.getKey().intValue();
  }

  private int insertInvoice(Invoice invoice, int buyerId, int sellerId) {
    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement("insert into invoice (date, number, buyer, seller) values (?, ?, ?, ?);",
          new String[]{"id"});
      ps.setDate(1, Date.valueOf(invoice.getDate()));
      ps.setString(2, invoice.getNumber());
      ps.setLong(3, buyerId);
      ps.setLong(4, sellerId);
      return ps;
    }, keyHolder);

    return keyHolder.getKey().intValue();
  }

  private void addEntriesToInvoice(int invoiceId, Invoice invoice) {
    GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

    invoice.getEntries().forEach(entry -> {
      jdbcTemplate.update(connection -> {
        PreparedStatement ps = connection.prepareStatement(
            "insert into invoice_entry (description, net_price, vat_value, vat_rate, expense_related_to_car) values (?, ?, ?, ?, ?);",
            new String[]{"id"});
        ps.setString(1, entry.getDescription());
        ps.setBigDecimal(2, entry.getNetPrice());
        ps.setBigDecimal(3, entry.getVatValue());
        ps.setInt(4, vatToId.get(entry.getVatRate()));
        ps.setObject(5, insertCarAndGetItId(entry.getExpenseRelatedToCar()));
        return ps;
      }, keyHolder);
      int invoiceEntryId = keyHolder.getKey().intValue();

      jdbcTemplate.update(connection -> {
        PreparedStatement ps = connection.prepareStatement(
            "insert into invoice_invoice_entry (invoice_id, invoice_entry_id) values (?, ?);");
        ps.setInt(1, invoiceId);
        ps.setInt(2, invoiceEntryId);
        return ps;
      });
    });
  }

  private RowMapper<Invoice> invoiceRowMapper() {
    return (rs, rowNr) -> {
      int invoiceId = rs.getInt("id");

      List<InvoiceEntry> invoiceEntries = jdbcTemplate.query(
          "select * from invoice_invoice_entry iie "
              + "inner join invoice_entry e on iie.invoice_entry_id = e.id "
              + "left outer join car c on expense_related_to_car = c.id "
              + "where invoice_id = " + invoiceId,
          (response, ignored) -> InvoiceEntry.builder()
              .description(response.getString("description"))
              .netPrice(response.getBigDecimal("net_price"))
              .vatValue(response.getBigDecimal("vat_value"))
              .vatRate(idToVat.get(response.getInt("vat_rate")))
              .expenseRelatedToCar(response.getObject("registration_number") != null
                  ? Car.builder()
                  .registrationNumber(response.getString("registration_number"))
                  .personalUse(response.getBoolean("personal_use"))
                  .build()
                  : null)
              .build());

      return Invoice.builder()
          .id(rs.getInt("id"))
          .date(rs.getDate("date").toLocalDate())
          .number(rs.getString("number"))
          .buyer(Company.builder()
              .id(rs.getInt("buyer_id"))
              .taxIdentificationNumber(rs.getString("buyer_tax_id"))
              .name(rs.getString("buyer_name"))
              .address(rs.getString("buyer_address"))
              .healthInsurance(rs.getBigDecimal("buyer_health_insurance"))
              .pensionInsurance(rs.getBigDecimal("buyer_pension_insurance"))
              .build())
          .seller(Company.builder()
              .id(rs.getInt("seller_id"))
              .taxIdentificationNumber(rs.getString("seller_tax_id"))
              .name(rs.getString("seller_name"))
              .address(rs.getString("seller_address"))
              .healthInsurance(rs.getBigDecimal("seller_health_insurance"))
              .pensionInsurance(rs.getBigDecimal("seller_pension_insurance"))
              .build())
          .entries(invoiceEntries)
          .build();
    };
  }

  private void updateCompany(Company updated, Company toUpdate) {
    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement("update company "
          + "set tax_identification_number = ?, address = ?, name = ?, health_insurance = ?, pension_insurance = ? "
          + "where id = ?");

      ps.setString(1, updated.getTaxIdentificationNumber());
      ps.setString(2, updated.getAddress());
      ps.setString(3, updated.getName());
      ps.setBigDecimal(4, updated.getHealthInsurance());
      ps.setBigDecimal(5, updated.getPensionInsurance());
      ps.setInt(6, toUpdate.getId());
      return ps;
    });
  }

  private void deleteCars(int invoiceId) {
    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement("delete from car where id in "
          + "(select expense_related_to_car from invoice_entry where id in "
          + "(select invoice_entry_id from invoice_invoice_entry where invoice_id = ?));");
      ps.setInt(1, invoiceId);
      return ps;
    });
  }

  private void deleteEntries(int invoiceId) {
    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement("delete from invoice_entry where id in "
          + "(select invoice_entry_id from invoice_invoice_entry where invoice_id = ?);");
      ps.setInt(1, invoiceId);
      return ps;
    });
  }

  @Override
  @Transactional
  public int save(Invoice invoice) {
    int buyerId = insertCompany(invoice.getBuyer());
    int sellerId = insertCompany(invoice.getSeller());
    int invoiceId = insertInvoice(invoice, buyerId, sellerId);
    addEntriesToInvoice(invoiceId, invoice);

    return invoiceId;
  }

  @Override
  public Optional<Invoice> getById(int id) {
    List<Invoice> invoices = jdbcTemplate.query(selectQuery + " where i.id = " + id, invoiceRowMapper());
    return invoices.isEmpty() ? Optional.empty() : Optional.of(invoices.get(0));
  }

  @Override
  public List<Invoice> getAll() {
    return jdbcTemplate.query(selectQuery, invoiceRowMapper());
  }

  @Override
  @Transactional
  public Optional<Invoice> update(int id, Invoice updatedInvoice) {
    Optional<Invoice> optionalInvoice = getById(id);

    if (optionalInvoice.isEmpty()) {
      return optionalInvoice;
    }

    updateCompany(updatedInvoice.getBuyer(), optionalInvoice.get().getBuyer());
    updateCompany(updatedInvoice.getSeller(), optionalInvoice.get().getSeller());

    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement("update invoice "
          + "set date = ?, number = ? "
          + "where id = ?");

      ps.setDate(1, Date.valueOf(updatedInvoice.getDate()));
      ps.setString(2, updatedInvoice.getNumber());
      ps.setInt(3, id);
      return ps;
    });

    deleteCars(id);
    deleteEntries(id);
    addEntriesToInvoice(id, updatedInvoice);

    return optionalInvoice;
  }

  @Override
  public Optional<Invoice> delete(int id) {
    Optional<Invoice> optionalInvoice = getById(id);

    if (optionalInvoice.isEmpty()) {
      return optionalInvoice;
    }

    final Invoice invoice = optionalInvoice.get();

    deleteCars(id);
    deleteEntries(id);

    jdbcTemplate.update(con -> {
      PreparedStatement ps = con.prepareStatement(
          "delete from invoice where id = ?;");
      ps.setInt(1, id);
      return ps;
    });

    jdbcTemplate.update(con -> {
      PreparedStatement ps = con.prepareStatement(
          "delete from company where id in (?, ?)");
      ps.setInt(1, invoice.getBuyer().getId());
      ps.setInt(2, invoice.getSeller().getId());
      return ps;
    });

    return optionalInvoice;
  }
}
