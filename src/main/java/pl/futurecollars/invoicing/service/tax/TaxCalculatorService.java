package pl.futurecollars.invoicing.service.tax;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.futurecollars.invoicing.db.Database;
import pl.futurecollars.invoicing.model.Car;
import pl.futurecollars.invoicing.model.Company;
import pl.futurecollars.invoicing.model.Invoice;
import pl.futurecollars.invoicing.model.InvoiceEntry;
import pl.futurecollars.invoicing.service.tax.TaxCalculatorResult;

@Service
@AllArgsConstructor
public class TaxCalculatorService {

  private final Database<Invoice> database;

  public TaxCalculatorResult calculateTaxes(Company company) {

    String taxIdentificationNumber = company.getTaxIdentificationNumber();;

    BigDecimal incomeMinusCosts = getEarnings(taxIdentificationNumber);
    BigDecimal incomeMinusCostsMinusPensionInsurance = incomeMinusCosts.subtract(company.getPensionInsurance());
    BigDecimal incomeMinusCostsMinusPensionInsuranceRounded = incomeMinusCostsMinusPensionInsurance.setScale(0, RoundingMode.HALF_DOWN);
    BigDecimal incomeTax = incomeMinusCostsMinusPensionInsuranceRounded.multiply(BigDecimal.valueOf(19, 2));
    BigDecimal healthInsuranceToSubtract =
        company.getHealthInsurance().multiply(BigDecimal.valueOf(775)).divide(BigDecimal.valueOf(900), RoundingMode.HALF_UP);
    BigDecimal incomeTaxMinusHealthInsurance = incomeTax.subtract(healthInsuranceToSubtract);

    return TaxCalculatorResult.builder()
        .income(getIncome(taxIdentificationNumber))
        .costs(getCosts(taxIdentificationNumber))
        .incomeMinusCosts(incomeMinusCosts)
        .pensionInsurance(company.getPensionInsurance())
        .incomeMinusCostsMinusPensionInsurance(incomeMinusCostsMinusPensionInsurance)
        .incomeMinusCostsMinusPensionInsuranceRounded(incomeMinusCostsMinusPensionInsuranceRounded)
        .incomeTax(incomeTax)
        .healthInsurancePaid(company.getHealthInsurance())
        .healthInsuranceToSubtract(healthInsuranceToSubtract)
        .incomeTaxMinusHealthInsurance(incomeTaxMinusHealthInsurance)
        .finalIncomeTax(incomeTaxMinusHealthInsurance.setScale(0, RoundingMode.DOWN))

        // vat
        .collectedVat(getIncomingVat(taxIdentificationNumber))
        .paidVat(getOutgoingVat(taxIdentificationNumber))
        .vatToReturn(getVatToReturn(taxIdentificationNumber))
        .build();
  }

  private BigDecimal getVatValueTakingIntoConsiderationPersonalCarUsage(InvoiceEntry invoiceEntry) {
    return Optional.ofNullable(invoiceEntry.getExpenseRelatedToCar())
        .map(Car::isPersonalUse)
        .map(personalCarUsage -> personalCarUsage ? BigDecimal.valueOf(5, 1) : BigDecimal.ONE)
        .map(proportion -> invoiceEntry.getVatValue().multiply(proportion))
        .map(value -> value.setScale(2, RoundingMode.FLOOR))
        .orElse(invoiceEntry.getVatValue());
  }

  private BigDecimal getIncomeValueTakingIntoConsiderationPersonalCarUsage(InvoiceEntry invoiceEntry) {
    return invoiceEntry.getNetPrice()
        .add(invoiceEntry.getVatValue())
        .subtract(getVatValueTakingIntoConsiderationPersonalCarUsage(invoiceEntry));
  }

  private BigDecimal getIncome(String taxIdentificationNumber) {
    return visit(sellerPredicate(taxIdentificationNumber), InvoiceEntry::getNetPrice);
  }

  private BigDecimal getCosts(String taxIdentificationNumber) {
    return visit(buyerPredicate(taxIdentificationNumber), this::getIncomeValueTakingIntoConsiderationPersonalCarUsage);
  }

  private BigDecimal getIncomingVat(String taxIdentificationNumber) {
    return visit(sellerPredicate(taxIdentificationNumber), InvoiceEntry::getVatValue);
  }

  private BigDecimal getOutgoingVat(String taxIdentificationNumber) {
    return visit(buyerPredicate(taxIdentificationNumber), this::getVatValueTakingIntoConsiderationPersonalCarUsage);
  }

  private BigDecimal getEarnings(String taxIdentificationNumber) {
    return getIncome(taxIdentificationNumber).subtract(getCosts(taxIdentificationNumber));
  }

  private BigDecimal getVatToReturn(String taxIdentificationNumber) {
    return getIncomingVat(taxIdentificationNumber).subtract(getOutgoingVat(taxIdentificationNumber));
  }

  private Predicate<Invoice> sellerPredicate(String taxIdentificationNumber) {
    return invoice -> taxIdentificationNumber.equals(invoice.getSeller().getTaxIdentificationNumber());
  }

  private Predicate<Invoice> buyerPredicate(String taxIdentificationNumber) {
    return invoice -> taxIdentificationNumber.equals(invoice.getBuyer().getTaxIdentificationNumber());
  }

  private BigDecimal visit(
      Predicate<Invoice> invoicePredicate,
      Function<InvoiceEntry, BigDecimal> invoiceEntryToValue
  ) {
    return database.getAll().stream()
        .filter(invoicePredicate)
        .flatMap(i -> i.getEntries().stream())
        .map(invoiceEntryToValue)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

}
