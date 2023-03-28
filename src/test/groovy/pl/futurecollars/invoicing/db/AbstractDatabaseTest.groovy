package pl.futurecollars.invoicing.db


import pl.futurecollars.invoicing.model.Invoice
import spock.lang.Specification

import static pl.futurecollars.invoicing.helpers.TestHelpers.invoice

abstract class AbstractDatabaseTest extends Specification {

    protected List<Invoice> invoices = (1..12).collect { invoice(it) }

    abstract Database getDatabaseInstance()

    Database database

    def setup() {
        database = getDatabaseInstance()
        database.reset()

        assert database.getAll().isEmpty()
    }

    def "should save invoices returning sequential id, invoice should have id set to correct value, get by id returns saved invoice"() {
        when:
        def ids = invoices.collect({ it.id = database.save(it) })

        then:
        ids == (1L..invoices.size()).collect()
        ids.forEach({ assert database.getById(it).isPresent() })
        ids.forEach({ assert database.getById(it).get().getId() == it })
        ids.forEach({
            def expectedInvoice = resetIds(invoices.get((int) it - 1))
            def invoiceFromDb = resetIds(database.getById(it).get())
            assert invoiceFromDb.toString() ==  expectedInvoice.toString()})
    }

    def "get by id returns empty optional when there is no invoice with given id"() {
        expect:
        !database.getById(1).isPresent()
    }

    def "get all returns empty collection if there were no invoices"() {
        expect:
        database.getAll().isEmpty()
    }

    def "get all returns all invoices in the database, deleted invoice is not returned"() {
        given:
        invoices.forEach({ it.id = database.save(it) })

        expect:
        database.getAll().size() == invoices.size()
        database.getAll().eachWithIndex{ invoice, index ->
            def invoiceAsString = resetIds(invoice).toString()
            def expectedInvoiceAsString = resetIds(invoices.get(index)).toString()
            assert invoiceAsString == expectedInvoiceAsString
        }

        when:
        def firstInvoiceId = database.getAll().get(0).getId()
        database.delete(firstInvoiceId)

        then:
        database.getAll().size() == invoices.size() - 1
        database.getAll().eachWithIndex { invoice, index ->
            assert resetIds(invoice).toString() == resetIds(invoices.get(index + 1)).toString()
        }
        database.getAll().forEach({ assert it.getId() != firstInvoiceId })
    }

    def "can delete all invoices"() {
        given:
        invoices.forEach({ it.id = database.save(it) })

        when:
        invoices.forEach({ database.delete(it.getId()) })

        then:
        database.getAll().isEmpty()
    }

    def "deleting not existing invoice returns Optional.empty()"() {
        expect:
        database.delete(987) == Optional.empty()
    }

    def "updating the existing invoice returns old invoice"() {
        given:
        def oldInvoice = invoices.get(0)
        oldInvoice.id = database.save(oldInvoice)

        def newInvoice = invoices.get(1)
        newInvoice.id = oldInvoice.id

        when:
        def result = database.update(oldInvoice.id, newInvoice)

        then:
        resetIds(database.getById(oldInvoice.id).get()).toString() == resetIds(newInvoice).toString()
        resetIds(result.get()).toString() == resetIds(oldInvoice).toString()
    }

    def "updating not existing invoice returns Optional.empty()"() {
        expect:
        database.update(666, invoices.get(1)) == Optional.empty()
    }

    private static resetIds(Invoice invoice) {
        invoice.getBuyer().id = null as long
        invoice.getSeller().id = null as long
        invoice.entries.forEach {
            it.id = null
        }
        invoice
    }
}
