/* Created on 09.11.2025 */
package org.javerland.dataanonymizer.anonymizer;

import net.datafaker.Faker;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;

/**
 * Main anonymizer class responsible for generating fake data.
 *
 * @author Juraj Pacolt
 */
public class Anonymizer {

    private final Faker faker;

    public Anonymizer() {
        this.faker = new Faker(Locale.ENGLISH);
    }

    public Anonymizer(Locale locale) {
        this.faker = new Faker(locale);
    }

    public String anonymizeEmail() {
        return faker.internet().emailAddress();
    }

    public String anonymizeName() {
        return faker.name().firstName();
    }

    public String anonymizeSurname() {
        return faker.name().lastName();
    }

    public String anonymizeFullName() {
        return faker.name().fullName();
    }

    public String anonymizeUsername() {
        return faker.internet().username();
    }

    public String anonymizePhone() {
        return faker.phoneNumber().phoneNumber();
    }

    public Date anonymizeBirthDate() {
        LocalDate birthDate = faker.timeAndDate().birthday(18, 80);
        return Date.valueOf(birthDate);
    }

    public String anonymizeCity() {
        return faker.address().city();
    }

    public String anonymizeCounty() {
        return faker.address().city();
    }

    public String anonymizeRegion() {
        return faker.address().state();
    }

    public String anonymizeCountry() {
        return faker.address().country();
    }

    public String anonymizePostalCode() {
        return faker.address().zipCode();
    }

    public String anonymizeStreet() {
        return faker.address().streetAddress();
    }

    public String anonymizeCustom(String expression) {
        return faker.expression(expression);
    }

    public String anonymizeSsn() {
        return faker.idNumber().valid();
    }

    public String anonymizePassportNumber() {
        return faker.regexify("[A-Z]{2}[0-9]{7}");
    }

    public String anonymizeDriverLicense() {
        return faker.regexify("[A-Z]{2}[0-9]{6}");
    }

    public String anonymizeTaxId() {
        return faker.regexify("[0-9]{9}");
    }

    public String anonymizeNationalId() {
        return faker.idNumber().valid();
    }

    public String anonymizeCreditCard() {
        return faker.finance().creditCard();
    }

    public String anonymizeIban() {
        return faker.finance().iban();
    }

    public String anonymizeBankAccount() {
        return faker.regexify("[0-9]{12}");
    }

    public String anonymizeCompanyName() {
        return faker.company().name();
    }

    public String anonymizeJobTitle() {
        return faker.job().title();
    }

    public String anonymizeDepartment() {
        return faker.company().industry();
    }

    public String anonymizeIpAddress() {
        return faker.internet().ipV4Address();
    }

    public String anonymizeMacAddress() {
        return faker.internet().macAddress();
    }

    public String anonymizeUrl() {
        return faker.internet().url();
    }

    public String anonymizeDomain() {
        return faker.internet().domainName();
    }

    public String anonymizeGender() {
        return faker.gender().binaryTypes();
    }

    public String anonymizeBloodType() {
        String[] bloodTypes = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        return bloodTypes[faker.random().nextInt(bloodTypes.length)];
    }
}
