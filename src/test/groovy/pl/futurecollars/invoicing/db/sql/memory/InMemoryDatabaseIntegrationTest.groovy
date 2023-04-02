package pl.futurecollars.invoicing.db.sql.memory

import pl.futurecollars.invoicing.db.AbstractDatabaseTest
import pl.futurecollars.invoicing.db.Database
import pl.futurecollars.invoicing.db.memory.InMemoryDatabase

class InMemoryDatabaseIntegrationTest extends AbstractDatabaseTest {

    @Override
    Database getDatabaseInstance() {
        return new InMemoryDatabase()
    }
}