package pl.futurecollars.invoicing.db.file

import pl.futurecollars.invoicing.utils.FilesService
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class IdProviderTest extends Specification {

    private final Path nextIdDbPath = File.createTempFile('nextId', '.txt').toPath()

    def "next id starts from 1 if file was empty"() {
        given:
        IdProvider idService = new IdProvider(nextIdDbPath, new FilesService())

        expect:
        Files.readAllLines(nextIdDbPath) == ['1']

        and:
        idService.getNextIdAndIncrement() == 1
        Files.readAllLines(nextIdDbPath) == ['2']

        and:
        idService.getNextIdAndIncrement() == 2
        Files.readAllLines(nextIdDbPath) == ['3']

        and:
        idService.getNextIdAndIncrement() == 3
        Files.readAllLines(nextIdDbPath) == ['4']
    }

    def "next id starts from last number if file was not empty"() {
        given:
        Files.writeString(nextIdDbPath, "17")
        IdProvider idService = new IdProvider(nextIdDbPath, new FilesService())

        expect:
        Files.readAllLines(nextIdDbPath) == ['17']

        and:
        idService.getNextIdAndIncrement() == 17
        Files.readAllLines(nextIdDbPath) == ['18']

        and:
        idService.getNextIdAndIncrement() == 18
        Files.readAllLines(nextIdDbPath) == ['19']

        and:
        idService.getNextIdAndIncrement() == 19
        Files.readAllLines(nextIdDbPath) == ['20']
    }

}
