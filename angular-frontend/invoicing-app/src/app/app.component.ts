import { Component, OnInit } from '@angular/core';
import { Company } from "./company";
import { CompanyService } from './company.service';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})

export class AppComponent implements OnInit {

    companies: Company[] = [];

    newCompany: Company = new Company(0, "", "", "", 0, 0);

    constructor(
        private companiesService: CompanyService
    ) {
    }

    ngOnInit(): void {
        this.companiesService.getCompanies()
            .subscribe(companies => {
                this.companies = companies;
            });
    }

    addCompany() {
        this.companiesService.addCompany(this.newCompany)
            .subscribe(id => {
                this.newCompany.id = id;
                this.companies.push(this.newCompany);

                this.newCompany = new Company(0, "", "", "", 0, 0);
            });
    }

    deleteCompany(companyToDelete: Company) {
        this.companiesService.deleteCompany(companyToDelete.id)
            .subscribe(() => {
                this.companies = this.companies.filter(company => company !== companyToDelete);
            })
    }

    triggerUpdate(company: Company) {
        company.editedCompany = new Company(
            company.id,
            company.taxIdentificationNumber,
            company.address,
            company.name,
            company.healthInsurance,
            company.pensionInsurance
        )

        company.editMode = true
    }

    cancelCompanyUpdate(company: Company) {
        company.editMode = false;
    }

    updateCompany(updatedCompany: Company) {
        this.companiesService.editCompany(updatedCompany.editedCompany)
            .subscribe(() => {
                updatedCompany.taxIdentificationNumber = updatedCompany.editedCompany.taxIdentificationNumber
                updatedCompany.address = updatedCompany.editedCompany.address
                updatedCompany.name = updatedCompany.editedCompany.name
                updatedCompany.healthInsurance = updatedCompany.editedCompany.healthInsurance
                updatedCompany.pensionInsurance = updatedCompany.editedCompany.pensionInsurance

                updatedCompany.editMode = false;
            })
    }
}
