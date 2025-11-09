/* Created on 27.09.2025 */
package org.javerland.dataanonymizer.model.config;

import java.util.List;

/**
 * Model class representing search column terms configuration.
 *
 * @author Juraj Pacolt
 */
public class SearchColumnTerms {

    private List<String> email = List.of();
    private Name name;
    private List<String> surname = List.of();
    private List<String> username = List.of();
    private Address address;
    private List<String> phone = List.of();
    private List<String> birthDate = List.of();
    
    // Personal identification
    private List<String> ssn = List.of();
    private List<String> passportNumber = List.of();
    private List<String> driverLicense = List.of();
    private List<String> taxId = List.of();
    private List<String> nationalId = List.of();
    
    // Financial
    private List<String> creditCard = List.of();
    private List<String> iban = List.of();
    private List<String> bankAccount = List.of();
    
    // Company
    private List<String> companyName = List.of();
    private List<String> jobTitle = List.of();
    private List<String> department = List.of();
    
    // Internet
    private List<String> ipAddress = List.of();
    private List<String> macAddress = List.of();
    private List<String> url = List.of();
    private List<String> domain = List.of();
    
    // Additional personal
    private List<String> fullName = List.of();
    private List<String> gender = List.of();
    private List<String> bloodType = List.of();
    
    private List<String> customs = List.of();

    public List<String> getEmail() {
        return email;
    }

    public void setEmail(List<String> email) {
        this.email = email;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public List<String> getSurname() {
        return surname;
    }

    public void setSurname(List<String> surname) {
        this.surname = surname;
    }

    public List<String> getUsername() {
        return username;
    }

    public void setUsername(List<String> username) {
        this.username = username;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<String> getPhone() {
        return phone;
    }

    public void setPhone(List<String> phone) {
        this.phone = phone;
    }

    public List<String> getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(List<String> birthDate) {
        this.birthDate = birthDate;
    }

    public List<String> getSsn() {
        return ssn;
    }

    public void setSsn(List<String> ssn) {
        this.ssn = ssn;
    }

    public List<String> getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(List<String> passportNumber) {
        this.passportNumber = passportNumber;
    }

    public List<String> getDriverLicense() {
        return driverLicense;
    }

    public void setDriverLicense(List<String> driverLicense) {
        this.driverLicense = driverLicense;
    }

    public List<String> getTaxId() {
        return taxId;
    }

    public void setTaxId(List<String> taxId) {
        this.taxId = taxId;
    }

    public List<String> getNationalId() {
        return nationalId;
    }

    public void setNationalId(List<String> nationalId) {
        this.nationalId = nationalId;
    }

    public List<String> getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(List<String> creditCard) {
        this.creditCard = creditCard;
    }

    public List<String> getIban() {
        return iban;
    }

    public void setIban(List<String> iban) {
        this.iban = iban;
    }

    public List<String> getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(List<String> bankAccount) {
        this.bankAccount = bankAccount;
    }

    public List<String> getCompanyName() {
        return companyName;
    }

    public void setCompanyName(List<String> companyName) {
        this.companyName = companyName;
    }

    public List<String> getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(List<String> jobTitle) {
        this.jobTitle = jobTitle;
    }

    public List<String> getDepartment() {
        return department;
    }

    public void setDepartment(List<String> department) {
        this.department = department;
    }

    public List<String> getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(List<String> ipAddress) {
        this.ipAddress = ipAddress;
    }

    public List<String> getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(List<String> macAddress) {
        this.macAddress = macAddress;
    }

    public List<String> getUrl() {
        return url;
    }

    public void setUrl(List<String> url) {
        this.url = url;
    }

    public List<String> getDomain() {
        return domain;
    }

    public void setDomain(List<String> domain) {
        this.domain = domain;
    }

    public List<String> getFullName() {
        return fullName;
    }

    public void setFullName(List<String> fullName) {
        this.fullName = fullName;
    }

    public List<String> getGender() {
        return gender;
    }

    public void setGender(List<String> gender) {
        this.gender = gender;
    }

    public List<String> getBloodType() {
        return bloodType;
    }

    public void setBloodType(List<String> bloodType) {
        this.bloodType = bloodType;
    }

    public List<String> getCustoms() {
        return customs;
    }

    public void setCustoms(List<String> customs) {
        this.customs = customs;
    }
}
