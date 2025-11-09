/* Created on 09.11.2025 */
package org.javerland.dataanonymizer.anonymizer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Anonymizer class.
 *
 * @author Juraj Pacolt
 */
class AnonymizerTest {

    private Anonymizer anonymizer;

    @BeforeEach
    void setUp() {
        anonymizer = new Anonymizer();
    }

    @Test
    void testAnonymizeEmail() {
        String email = anonymizer.anonymizeEmail();
        assertNotNull(email);
        assertTrue(email.contains("@"));
        assertTrue(email.contains("."));
    }

    @Test
    void testAnonymizeName() {
        String name = anonymizer.anonymizeName();
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }

    @Test
    void testAnonymizeSurname() {
        String surname = anonymizer.anonymizeSurname();
        assertNotNull(surname);
        assertFalse(surname.isEmpty());
    }

    @Test
    void testAnonymizeFullName() {
        String fullName = anonymizer.anonymizeFullName();
        assertNotNull(fullName);
        assertFalse(fullName.isEmpty());
    }

    @Test
    void testAnonymizeUsername() {
        String username = anonymizer.anonymizeUsername();
        assertNotNull(username);
        assertFalse(username.isEmpty());
    }

    @Test
    void testAnonymizePhone() {
        String phone = anonymizer.anonymizePhone();
        assertNotNull(phone);
        assertFalse(phone.isEmpty());
    }

    @Test
    void testAnonymizeBirthDate() {
        Date birthDate = anonymizer.anonymizeBirthDate();
        assertNotNull(birthDate);
        assertTrue(birthDate.before(new Date(System.currentTimeMillis())));
    }

    @Test
    void testAnonymizeCity() {
        String city = anonymizer.anonymizeCity();
        assertNotNull(city);
        assertFalse(city.isEmpty());
    }

    @Test
    void testAnonymizeCountry() {
        String country = anonymizer.anonymizeCountry();
        assertNotNull(country);
        assertFalse(country.isEmpty());
    }

    @Test
    void testAnonymizePostalCode() {
        String postalCode = anonymizer.anonymizePostalCode();
        assertNotNull(postalCode);
        assertFalse(postalCode.isEmpty());
    }

    @Test
    void testAnonymizeStreet() {
        String street = anonymizer.anonymizeStreet();
        assertNotNull(street);
        assertFalse(street.isEmpty());
    }

    @Test
    void testAnonymizeSsn() {
        String ssn = anonymizer.anonymizeSsn();
        assertNotNull(ssn);
        assertFalse(ssn.isEmpty());
    }

    @Test
    void testAnonymizePassportNumber() {
        String passport = anonymizer.anonymizePassportNumber();
        assertNotNull(passport);
        assertEquals(9, passport.length());
        assertTrue(passport.matches("[A-Z]{2}[0-9]{7}"));
    }

    @Test
    void testAnonymizeDriverLicense() {
        String license = anonymizer.anonymizeDriverLicense();
        assertNotNull(license);
        assertEquals(8, license.length());
        assertTrue(license.matches("[A-Z]{2}[0-9]{6}"));
    }

    @Test
    void testAnonymizeTaxId() {
        String taxId = anonymizer.anonymizeTaxId();
        assertNotNull(taxId);
        assertEquals(9, taxId.length());
        assertTrue(taxId.matches("[0-9]{9}"));
    }

    @Test
    void testAnonymizeNationalId() {
        String nationalId = anonymizer.anonymizeNationalId();
        assertNotNull(nationalId);
        assertFalse(nationalId.isEmpty());
    }

    @Test
    void testAnonymizeCreditCard() {
        String creditCard = anonymizer.anonymizeCreditCard();
        assertNotNull(creditCard);
        assertFalse(creditCard.isEmpty());
    }

    @Test
    void testAnonymizeIban() {
        String iban = anonymizer.anonymizeIban();
        assertNotNull(iban);
        assertFalse(iban.isEmpty());
    }

    @Test
    void testAnonymizeBankAccount() {
        String bankAccount = anonymizer.anonymizeBankAccount();
        assertNotNull(bankAccount);
        assertFalse(bankAccount.isEmpty());
    }

    @Test
    void testAnonymizeCompanyName() {
        String company = anonymizer.anonymizeCompanyName();
        assertNotNull(company);
        assertFalse(company.isEmpty());
    }

    @Test
    void testAnonymizeJobTitle() {
        String jobTitle = anonymizer.anonymizeJobTitle();
        assertNotNull(jobTitle);
        assertFalse(jobTitle.isEmpty());
    }

    @Test
    void testAnonymizeDepartment() {
        String department = anonymizer.anonymizeDepartment();
        assertNotNull(department);
        assertFalse(department.isEmpty());
    }

    @Test
    void testAnonymizeIpAddress() {
        String ip = anonymizer.anonymizeIpAddress();
        assertNotNull(ip);
        assertTrue(ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"));
    }

    @Test
    void testAnonymizeMacAddress() {
        String mac = anonymizer.anonymizeMacAddress();
        assertNotNull(mac);
        assertFalse(mac.isEmpty());
    }

    @Test
    void testAnonymizeUrl() {
        String url = anonymizer.anonymizeUrl();
        assertNotNull(url);
        assertTrue(url.startsWith("http"));
    }

    @Test
    void testAnonymizeDomain() {
        String domain = anonymizer.anonymizeDomain();
        assertNotNull(domain);
        assertTrue(domain.contains("."));
    }

    @Test
    void testAnonymizeGender() {
        String gender = anonymizer.anonymizeGender();
        assertNotNull(gender);
        assertFalse(gender.isEmpty());
    }

    @Test
    void testAnonymizeBloodType() {
        String bloodType = anonymizer.anonymizeBloodType();
        assertNotNull(bloodType);
        assertTrue(bloodType.matches("(A|B|AB|O)[+-]"));
    }

    @Test
    void testAnonymizeCustomExpression() {
        String custom = anonymizer.anonymizeCustom("#{Name.firstName}");
        assertNotNull(custom);
        assertFalse(custom.isEmpty());
    }

    @Test
    void testAnonymizerWithDifferentLocale() {
        Anonymizer germanAnonymizer = new Anonymizer(Locale.GERMAN);
        String name = germanAnonymizer.anonymizeName();
        assertNotNull(name);
        assertFalse(name.isEmpty());
    }
}
