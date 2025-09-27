/* Created on 27.09.2025 */
package org.javerland.dataanonymizer.model.config;

import java.util.List;

/**
 * Model class representing an address configuration.
 *
 * @author Juraj Pacolt
 */
public class Address {

    private List<String> city = List.of();
    private List<String> county = List.of();
    private List<String> region = List.of();
    private List<String> country = List.of();
    private List<String> postalCode = List.of();
    private List<String> street = List.of();

    public List<String> getCity() {
        return city;
    }

    public void setCity(List<String> city) {
        this.city = city;
    }

    public List<String> getCounty() {
        return county;
    }

    public void setCounty(List<String> county) {
        this.county = county;
    }

    public List<String> getRegion() {
        return region;
    }

    public void setRegion(List<String> region) {
        this.region = region;
    }

    public List<String> getCountry() {
        return country;
    }

    public void setCountry(List<String> country) {
        this.country = country;
    }

    public List<String> getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(List<String> postalCode) {
        this.postalCode = postalCode;
    }

    public List<String> getStreet() {
        return street;
    }

    public void setStreet(List<String> street) {
        this.street = street;
    }
}
