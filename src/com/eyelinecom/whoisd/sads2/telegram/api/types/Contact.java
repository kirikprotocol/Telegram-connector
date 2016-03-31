package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by jeck on 30/03/16
 */
public class Contact extends ApiType<Contact> {
    @JsonProperty(value = "phone_number")
    private String phoneNumber;

    @JsonProperty(value = "first_name")
    private String firstName;

    @JsonProperty(value = "last_name")
    private String lastName;

    @JsonProperty(value = "user_id")
    private Integer userId;

    public Contact() {
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}
