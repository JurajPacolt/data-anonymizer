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

    public List<String> getCustoms() {
        return customs;
    }

    public void setCustoms(List<String> customs) {
        this.customs = customs;
    }
}
