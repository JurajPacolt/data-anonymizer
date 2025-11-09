/* Created on 09.11.2025 */
package org.javerland.dataanonymizer.anonymizer;

/**
 * Enum representing different types of anonymization strategies.
 *
 * @author Juraj Pacolt
 */
public enum AnonymizationType {
    EMAIL,
    NAME,
    SURNAME,
    USERNAME,
    PHONE,
    BIRTH_DATE,
    CITY,
    COUNTY,
    REGION,
    COUNTRY,
    POSTAL_CODE,
    STREET,
    
    // Personal identification
    SSN,
    PASSPORT_NUMBER,
    DRIVER_LICENSE,
    TAX_ID,
    NATIONAL_ID,
    
    // Financial
    CREDIT_CARD,
    IBAN,
    BANK_ACCOUNT,
    
    // Company
    COMPANY_NAME,
    JOB_TITLE,
    DEPARTMENT,
    
    // Internet
    IP_ADDRESS,
    MAC_ADDRESS,
    URL,
    DOMAIN,
    
    // Additional personal
    FULL_NAME,
    GENDER,
    BLOOD_TYPE,
    
    CUSTOM
}
