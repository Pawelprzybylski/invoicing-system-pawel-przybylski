package pl.futurecollars.invoicing.controller.invoice

import com.mongodb.client.MongoDatabase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import pl.futurecollars.invoicing.model.Invoice
import pl.futurecollars.invoicing.utils.JsonService
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import pl.futurecollars.invoicing.helpers.TestHelpers

import java.time.LocalDate

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@AutoConfigureMockMvc
@SpringBootTest
@Stepwise
class InvoiceControllerStepwiseTest extends Specification {

    private final Invoice originalInvoice = TestHelpers.invoice(1)

    private final LocalDate updatedDate = LocalDate.of(2020, 02, 28)

    @Shared
    private int invoiceId

    @Autowired
    private MockMvc mockMvc

    @Autowired
    private JsonService jsonService

    @Autowired
    private ApplicationContext context

    @Requires({ System.getProperty('spring.profiles.active', 'memory').contains("mongo") })
    def "database is dropped to ensure clean state"() {
        expect:
        MongoDatabase mongoDatabase = context.getBean(MongoDatabase)
        mongoDatabase.drop()
    }

    def "empty array is returned when no invoices were added"() {
        when:
        def response = mockMvc.perform(get("/invoices"))
                .andExpect(status().isOk())
                .andReturn()
                .response
                .contentAsString

        then:
        response == "[]"
    }

    def "add single invoice"() {
        given:
        def invoiceAsJson = jsonService.toJson(originalInvoice)

        when:
        invoiceId = Integer.valueOf(
                mockMvc.perform(
                        post("/invoices")
                                .content(invoiceAsJson)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn()
                        .response
                        .contentAsString
        )

        then:
        invoiceId > 0
    }

    def "one invoice is returned when getting all invoices"() {
        given:
        def expectedInvoice = originalInvoice
        expectedInvoice.id = invoiceId

        when:
        def response = mockMvc.perform(get("/invoices"))
                .andExpect(status().isOk())
                .andReturn()
                .response
                .contentAsString

        def invoices = jsonService.toObject(response, Invoice[])

        then:
        invoices.size() == 1
        invoices[0] == expectedInvoice
    }

    def "invoice is returned correctly when getting by id"() {
        given:
        def expectedInvoice = originalInvoice
        expectedInvoice.id = invoiceId

        when:
        def response = mockMvc.perform(get("/invoices/$invoiceId"))
                .andExpect(status().isOk())
                .andReturn()
                .response
                .contentAsString

        def invoice = jsonService.toObject(response, Invoice)

        then:
        invoice == expectedInvoice
    }

    def "invoice date can be modified"() {
        given:
        def modifiedInvoice = originalInvoice
        modifiedInvoice.date = updatedDate

        def invoiceAsJson = jsonService.toJson(modifiedInvoice)

        expect:
        mockMvc.perform(
                put("/invoices/$invoiceId")
                        .content(invoiceAsJson)
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andDo(print())
                .andExpect(status().isNoContent())
    }

    def "updated invoice is returned correctly when getting by id"() {
        given:
        def expectedInvoice = originalInvoice
        expectedInvoice.id = invoiceId
        expectedInvoice.date = updatedDate

        when:
        def response = mockMvc.perform(get("/invoices/$invoiceId"))
                .andExpect(status().isOk())
                .andReturn()
                .response
                .contentAsString

        def invoices = jsonService.toObject(response, Invoice)

        then:
        invoices == expectedInvoice
    }

    def "invoice can be deleted"() {
        expect:
        mockMvc.perform(delete("/invoices/$invoiceId"))
                .andExpect(status().isNoContent())

        and:
        mockMvc.perform(delete("/invoices/$invoiceId"))
                .andExpect(status().isNotFound())

        and:
        mockMvc.perform(get("/invoices/$invoiceId"))
                .andExpect(status().isNotFound())
    }

}
