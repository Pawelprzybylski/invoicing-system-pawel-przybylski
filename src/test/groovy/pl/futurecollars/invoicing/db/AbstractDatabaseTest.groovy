package pl.futurecollars.invoicing.db


import pl.futurecollars.invoicing.model.Invoice
import spock.lang.Specification

import static pl.futurecollars.invoicing.helpers.TestHelpers.invoice

abstract class AbstractDatabaseTest extends Specification {

    List<Invoice> invoices = (1..12).collect { invoice(it) }

    abstract Database getDatabaseInstance()

    Database database

    def setup() {
        database = getDatabaseInstance()
        database.reset()

        assert database.getAll().isEmpty()
    }

    def "should save invoices returning sequential id, invoice should have id set to correct value, get by id returns saved invoice"() {
        when:
        def ids = invoices.collect { it.id = database.save(it) }

        then:
        ids == (1L..invoices.size()).collect()
        ids.forEach { assert database.getById(it).isPresent() }
        ids.forEach { assert database.getById(it).get().getId() == it }
        ids.forEach {
            def expectedInvoice = resetIds(invoices.get((int) it - 1))
            def invoiceFromDb = resetIds(database.getById(it).get())
            assert invoiceFromDb.toString() == expectedInvoice.toString()
        }
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
        invoices.forEach { it.id = database.save(it) }

        expect:
        database.getAll().size() == invoices.size()
        database.getAll().eachWithIndex { invoice, index ->
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
        database.getAll().forEach { assert it.getId() != firstInvoiceId }
    }

    def "can delete all invoices"() {
        given:
        database.getAll().isEmpty()

        when:
        invoices.forEach { it.id = database.save(it) }
        invoices.forEach { database.delete(it.getId()) }

        then:
        database.getAll().isEmpty()
    }

    def "deleting not existing invoice returns optional empty"() {
        expect:
        database.delete(123) == Optional.empty()
    }

    def "it's possible to update the invoice, original invoice is returned"() {
        given:
        def originalInvoice = invoices.get(0)
        originalInvoice.id = database.save(originalInvoice)

        def expectedInvoice = invoices.get(0)
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
        database.update(213, invoices.get(1)) == Optional.empty()
    }

    // resetting is necessary because database query returns ids while we don't know ids in original invoice
    private static Invoice resetIds(Invoice invoice) {
        invoice.getBuyer().id = null
        invoice.getSeller().id = null
        invoice.entries.forEach {
            it.id = null
        }
    }

}