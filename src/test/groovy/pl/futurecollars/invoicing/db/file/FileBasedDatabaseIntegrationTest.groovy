package pl.futurecollars.invoicing.db.file

import pl.futurecollars.invoicing.db.AbstractDatabaseTest
import pl.futurecollars.invoicing.db.Database
import pl.futurecollars.invoicing.helpers.TestHelpers
import pl.futurecollars.invoicing.utils.FilesService
import pl.futurecollars.invoicing.utils.JsonService

import java.nio.charset.Charset
import java.nio.file.Files

class FileBasedDatabaseIntegrationTest extends AbstractDatabaseTest {

    def dbPath

    @Override
    Database getDatabaseInstance() {
        def filesService = new FilesService()

        def idPath = File.createTempFile('ids', '.txt').toPath()
        def idService = new IdProvider(idPath, filesService)

        dbPath = File.createTempFile('invoices', '.txt').toPath()
        return new FileBasedDatabase(dbPath, idService, filesService, new JsonService())
    }

    def "file based database writes invoices to correct file"() {
        given:
        def db = getDatabaseInstance()

        when:
        db.save(TestHelpers.invoice(4))

        then:
        1 == Files.readAllLines(dbPath, Charset.defaultCharset()).size()

        when:
        db.save(TestHelpers.invoice(5))

        then:
        2 == Files.readAllLines(dbPath, Charset.defaultCharset()).size()
    }
}
