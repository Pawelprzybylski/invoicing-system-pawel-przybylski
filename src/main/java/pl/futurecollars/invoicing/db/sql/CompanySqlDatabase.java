package pl.futurecollars.invoicing.db.sql;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;
import pl.futurecollars.invoicing.db.Database;
import pl.futurecollars.invoicing.model.Company;

public class CompanySqlDatabase extends AbstractSqlDatabase implements Database<Company> {

  public static final String SELECT_QUERY = "select * from company";

  public CompanySqlDatabase(JdbcTemplate jdbcTemplate) {
    super(jdbcTemplate);
  }

  @Override
  @Transactional
  public long save(Company company) {
    return insertCompany(company);
  }

  @Override
  public List<Company> getAll() {
    return jdbcTemplate.query(SELECT_QUERY, companyRowMapper());
  }

  @Override
  public Optional<Company> getById(long id) {
    List<Company> companies = jdbcTemplate.query(SELECT_QUERY + " where id = " + id, companyRowMapper());

    return companies.isEmpty() ? Optional.empty() : Optional.of(companies.get(0));
  }

  @Override
  @Transactional
  public Optional<Company> update(long id, Company updatedCompany) {
    Optional<Company> originalCompany = getById(id);
    if (originalCompany.isEmpty()) {
      return Optional.empty();
    }

    updateCompany(updatedCompany);

    return originalCompany;
  }

  @Override
  @Transactional
  public Optional<Company> delete(long id) {
    Optional<Company> originalCompany = getById(id);
    if (originalCompany.isEmpty()) {
      return Optional.empty();
    }

    jdbcTemplate.update(connection -> {
      PreparedStatement ps = connection.prepareStatement(
          "delete from company where id = ?;");
      ps.setLong(1, id);
      return ps;
    });

    return originalCompany;
  }

  private RowMapper<Company> companyRowMapper() {
    return (rs, rowNr) ->
        Company.builder()
            .id(rs.getLong("id"))
            .taxIdentificationNumber(rs.getString("tax_identification_number"))
            .name(rs.getString("name"))
            .address(rs.getString("address"))
            .pensionInsurance(rs.getBigDecimal("pension_insurance"))
            .healthInsurance(rs.getBigDecimal("health_insurance"))
            .build();
  }

}
