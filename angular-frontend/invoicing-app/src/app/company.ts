export class Company {

    public editMode: boolean = false;
    public editedCompany: Company= new Company(22,"dsg","fds","dsf",2,1);

    constructor(
        public id: number,
        public taxIdentificationNumber: string,
        public address: string,
        public name: string,
        public healthInsurance: number,
        public pensionInsurance: number
    ) {
    }

}
