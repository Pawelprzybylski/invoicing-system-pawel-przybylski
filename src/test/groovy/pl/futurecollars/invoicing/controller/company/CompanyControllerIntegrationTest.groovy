package pl.futurecollars.invoicing.controller.company

import org.springframework.http.MediaType
import pl.futurecollars.invoicing.controller.AbstractControllerTest
import spock.lang.Unroll

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import static pl.futurecollars.invoicing.helpers.TestHelpers.company

@Unroll
class CompanyControllerIntegrationTest extends AbstractControllerTest {

    def "empty array is returned when no companies were added"() {
        expect:
        getAllCompanies() == []
    }

    def "add company returns sequential id"() {
        expect:
        def firstId = addCompanyAndReturnId(company(1))
        addCompanyAndReturnId(company(2)) == firstId + 1
        addCompanyAndReturnId(company(3)) == firstId + 2
        addCompanyAndReturnId(company(4)) == firstId + 3
        addCompanyAndReturnId(company(5)) == firstId + 4
    }

    def "all companies are returned when getting all companies"() {
        given:
        def numberOfCompanies = 3
        def expectedCompanies = addUniqueCompanies(numberOfCompanies)

        when:
        def companies = getAllCompanies()

        then:
        companies.size() == numberOfCompanies
        companies == expectedCompanies
    }

    def "correct company is returned when getting by id"() {
        given:
        def expectedCompanies = addUniqueCompanies(5)
        def expectedCompany = expectedCompanies.get(2)

        when:
        def company = getCompanyById(expectedCompany.getId())

        then:
        company == expectedCompany
    }

    def "404 is returned when company id is not found when getting by id [#id]"() {
        given:
        addUniqueCompanies(11)

        expect:
        mockMvc.perform(get("$COMPANY_ENDPOINT/$id"))
                .andExpect(status().isNotFound())

        where:
        id << [-100, -2, -1, 0, 168, 1256]
    }

    def "404 is returned when company id is not found when deleting company [#id]"() {
        given:
        addUniqueCompanies(11)

        expect:
        mockMvc.perform(
                delete("$COMPANY_ENDPOINT/$id")
                        .with(csrf())
        )
                .andExpect(status().isNotFound())

        where:
        id << [-100, -2, -1, 0, 12, 13, 99, 102, 1000]
    }

    def "404 is returned when company id is not found when updating company [#id]"() {
        given:
        addUniqueCompanies(11)

        expect:
        mockMvc.perform(
                put("$COMPANY_ENDPOINT/$id")
                        .content(companyAsJson(1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
        )
                .andExpect(status().isNotFound())

        where:
        id << [-100, -2, -1, 0, 12, 13, 99, 102, 1000]
    }

    def "company can be modified"() {
        given:
        def id = addCompanyAndReturnId(company(4))
        def updatedCompany = company(1)
        updatedCompany.id = id

        expect:
        mockMvc.perform(
                put("$COMPANY_ENDPOINT/$id")
                        .content(jsonService.toJson(updatedCompany))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
        )
                .andExpect(status().isNoContent())

        def companyFromDbAfterUpdate = getCompanyById(id).toString()
        def expectedCompany = updatedCompany.toString()
        companyFromDbAfterUpdate == expectedCompany
    }

    def "company can be deleted"() {
        given:
        def companies = addUniqueCompanies(69)

        expect:
        companies.each { company -> deleteCompany(company.getId()) }
        getAllCompanies().size() == 0
    }

}