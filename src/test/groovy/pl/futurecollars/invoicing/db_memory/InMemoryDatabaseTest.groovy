package pl.futurecollars.invoicing.db_memory

import pl.futurecollars.invoicing.db.Database
import pl.futurecollars.invoicing.db.memory.InMemoryDatabase
import pl.futurecollars.invoicing.model.Invoice
import spock.lang.Specification

import static pl.futurecollars.invoicing.TestHelpers.invoice

class InMemoryDatabaseTest extends Specification {

    private Database database
    private List<Invoice> invoices

    def setup() {
        database = new InMemoryDatabase();

        invoices = (1..12).collect { invoice(it) }
    }

    def "get by id returns empty optional when there is no invoice with given id"() {
        expect:
        !database.getById(1).isPresent()
    }

    def "deleting not existing invoice is not causing any error"() {
        expect:
        database.delete(123);
    }

    def "updating not existing invoice throws exception"() {
        when:
        database.update(213, invoices.get(1))

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Id 213 does not exist"
    }

}
