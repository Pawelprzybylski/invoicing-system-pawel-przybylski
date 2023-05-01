package pl.futurecollars.invoicing.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.security.test.context.support.WithMockUser
import pl.futurecollars.invoicing.model.Company
import pl.futurecollars.invoicing.model.Invoice
import pl.futurecollars.invoicing.service.tax.TaxCalculatorResult
import pl.futurecollars.invoicing.utils.JsonService
import spock.lang.Specification

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static pl.futurecollars.invoicing.helpers.TestHelpers.company
import static pl.futurecollars.invoicing.helpers.TestHelpers.invoice

@WithMockUser
@AutoConfigureMockMvc
@SpringBootTest
class AbstractControllerTest extends Specification {

    static final String INVOICE_ENDPOINT = "/invoices"
    static final String COMPANY_ENDPOINT = "/companies"
    static final String TAX_CALCULATOR_ENDPOINT = "/tax"

    @Autowired
    MockMvc mockMvc

    @Autowired
    JsonService jsonService

    def setup() {
        getAllInvoices().each { invoice -> deleteInvoice(invoice.id) }
        getAllCompanies().each { company -> deleteCompany(company.id) }
    }

    int addInvoiceAndReturnId(Invoice invoice) {
        addAndReturnId(invoice, INVOICE_ENDPOINT)
    }

    int addCompanyAndReturnId(Company company) {
        addAndReturnId(company, COMPANY_ENDPOINT)
    }

    List<Invoice> getAllInvoices() {
        getAll(Invoice[], INVOICE_ENDPOINT)
    }
    List<Company> getAllCompanies() {
        getAll(Company[], COMPANY_ENDPOINT)
    }

    List<Invoice> addUniqueInvoices(int count) {
        (1..count).collect { id ->
            def invoice = invoice(id)
            invoice.id = addInvoiceAndReturnId(invoice)
            return invoice
        }
    }
    List<Company> addUniqueCompanies(int count) {
        (1..count).collect { id ->
            def company = company(id)
            company.id = addCompanyAndReturnId(company)
            company
        }
    }

    void deleteInvoice(long id) {
        mockMvc.perform(delete("$INVOICE_ENDPOINT/$id").with(csrf()))
                .andExpect(status().isNoContent())
    }
    void deleteCompany(long id) {
        mockMvc.perform(delete("$COMPANY_ENDPOINT/$id").with(csrf()))
                .andExpect(status().isNoContent())
    }

    Invoice getInvoiceById(long id) {
        getById(id, Invoice, INVOICE_ENDPOINT)
    }
    Company getCompanyById(long id) {
        getById(id, Company, COMPANY_ENDPOINT)
    }

    String invoiceAsJson(long id) {
        jsonService.toJson(invoice(id))
    }
    String companyAsJson(long id) {
        jsonService.toJson(company(id))
    }

    TaxCalculatorResult calculateTax(Company company) {
        def response = mockMvc.perform(
                post("$TAX_CALCULATOR_ENDPOINT")
                        .content(jsonService.toJson(company))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
        )
                .andExpect(status().isOk())
                .andReturn()
                .response
                .contentAsString

        jsonService.toObject(response, TaxCalculatorResult)
    }
    private <T> int addAndReturnId(T item, String endpoint) {
        Integer.valueOf(
                mockMvc.perform(
                        post(endpoint)
                                .content(jsonService.toJson(item))
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf())
                )
                        .andExpect(status().isOk())
                        .andReturn()
                        .response
                        .contentAsString
        )
    }

    private <T> T getAll(Class<T> clazz, String endpoint) {
        def response = mockMvc.perform(get(endpoint))
                .andExpect(status().isOk())
                .andReturn()
                .response
                .contentAsString

        jsonService.toObject(response, clazz)
    }

    private <T> T getById(long id, Class<T> clazz, String endpoint) {
        def invoiceAsString = mockMvc.perform(get("$endpoint/$id"))
                .andExpect(status().isOk())
                .andReturn()
                .response
                .contentAsString

        jsonService.toObject(invoiceAsString, clazz)
    }
}
