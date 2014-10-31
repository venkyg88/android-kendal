package com.staples.mobile.common.access.easyopen.model.cart;

/**
 * Created by Avinash Dodda.
 */
public class Address {

    private String address1;
    private String address2;
    private String city;
    private String emailAddress;
    private String firstName;
    private String lastName;
    private String organizationName;
    private String phoneExtension;
    private String phoneNumber;
    private String state;
    private String zipCode;

    // default constructor
    public Address() { }

    // DLS: temporary conversion constructor (until models are consolidated)
    public Address(com.staples.mobile.common.access.easyopen.model.member.Address profileAddress) {
        this.address1 = profileAddress.getAddress1();
        this.address2 = profileAddress.getAddress2();
        this.city = profileAddress.getCity();
        this.emailAddress = profileAddress.getEmailAddress();
        this.firstName = profileAddress.getFirstname();
        this.lastName = profileAddress.getLastname();
        this.organizationName = profileAddress.getOrganizationName();
        this.phoneExtension = profileAddress.getPhoneExtension();
        this.phoneNumber = profileAddress.getPhone1();
        this.state = profileAddress.getState();
        this.zipCode = profileAddress.getZipcode();
    }


    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getPhoneExtension() {
        return phoneExtension;
    }

    public void setPhoneExtension(String phoneExtension) {
        this.phoneExtension = phoneExtension;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
}
