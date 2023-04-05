package pl.futurecollars.invoicing.db


import pl.futurecollars.invoicing.model.Invoice
import spock.lang.Specification

import static pl.futurecollars.invoicing.helpers.TestHelpers.invoice
import static pl.futurecollars.invoicing.helpers.TestHelpers.resetIds

abstract class AbstractDatabaseTest extends Specification {

    protected List<Invoice> invoices = (1..12).collect { invoice(it) }

    abstract Database<Invoice> getDatabaseInstance()

    Database<Invoice> database

    def setup() {
        database = getDatabaseInstance()
        database.getAll().forEach {
            invoice -> database.delete(invoice.getId())
        }

        assert database.getAll().isEmpty()
    }

    def "should save invoices returning sequential id"() {
        when:
        def ids = invoices.collect({ it.id = database.save(it) })

        then:
        (1L..invoices.size() - 1).forEach {assert ids[it] == ids[0] + it }
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
        def sizeBeforeSave = database.getAll().size()
        def id = database.save(invoices.get(0))

        expect:
        database.getAll().size() == sizeBeforeSave + 1

        when:
        database.delete(id)

        then:
        database.getAll().size() == sizeBeforeSave
    }

    def deleteAllInvoices() {
        database.getAll().forEach({ invoice -> database.delete(invoice.getId()) })

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
        def originalInvoice = invoices.get(0)
        originalInvoice.id = database.save(originalInvoice)

        def expectedInvoice = invoices.get(1)
        expectedInvoice.id = originalInvoice.id

        when:
        def result = database.update(originalInvoice.id, expectedInvoice)

        then:
        def invoiceAfterUpdate = database.getById(originalInvoice.id).get()
        def invoiceAfterUpdateAsString = resetIds(invoiceAfterUpdate).toString()
        def expectedInvoiceAfterUpdateAsString = resetIds(expectedInvoice).toString()
        invoiceAfterUpdateAsString == expectedInvoiceAfterUpdateAsString

        and:
        def invoiceBeforeUpdateAsString = resetIds(result.get()).toString()
        def expectedInvoiceBeforeUpdateAsString = resetIds(originalInvoice).toString()
        invoiceBeforeUpdateAsString == expectedInvoiceBeforeUpdateAsString
    }

    def "updating not existing invoice returns Optional.empty()"() {
        expect:
        database.update(666, invoices.get(1)) == Optional.empty()
    }
}
