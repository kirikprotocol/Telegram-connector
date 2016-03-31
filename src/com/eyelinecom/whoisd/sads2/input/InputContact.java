package com.eyelinecom.whoisd.sads2.input;

/**
 * Created by jeck on 31/03/16
 */
public class InputContact extends AbstractInputType<InputLocation>  {
    private String msisdn;

    private String name;

    public InputContact() {
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected String getTypeValue() {
        return "contact";
    }
}
