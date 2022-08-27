package pl.futurecollars.invoicing.db.file

import pl.futurecollars.invoicing.utils.FilesService
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

class IdProviderTest extends Specification {

    private Path nextIdDbPath = File.createTempFile('nextId', '.txt').toPath()

    def "next id starts from 1 if file was empty"() {
        given:
        IdProvider idProvider = new IdProvider(nextIdDbPath, new FilesService())

        expect:
        ['1'] == Files.readAllLines(nextIdDbPath)

        and:
        1 == idProvider.getNextIdAndIncrement()
        ['2'] == Files.readAllLines(nextIdDbPath)

        and:
        2 == idProvider.getNextIdAndIncrement()
        ['3'] == Files.readAllLines(nextIdDbPath)

        and:
        3 == idProvider.getNextIdAndIncrement()
        ['4'] == Files.readAllLines(nextIdDbPath)
    }

    def "next id starts from last number if file was not empty"() {
        given:
        Files.writeString(nextIdDbPath, "17")
        IdProvider idProvider = new IdProvider(nextIdDbPath, new FilesService())

        expect:
        ['17'] == Files.readAllLines(nextIdDbPath)

        and:
        17 == idProvider.getNextIdAndIncrement()
        ['18'] == Files.readAllLines(nextIdDbPath)

        and:
        18 == idProvider.getNextIdAndIncrement()
        ['19'] == Files.readAllLines(nextIdDbPath)

        and:
        19 == idProvider.getNextIdAndIncrement()
        ['20'] == Files.readAllLines(nextIdDbPath)
    }
}