package com.eyelinecom.whoisd.sads2.input;

/**
 * Created by jeck on 31/03/16
 */
public class InputLocation extends AbstractInputType<InputLocation> {
    private Double longitude;
    private Double latitude;

    public InputLocation() {
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @Override
    protected String getTypeValue() {
        return "location";
    }
}
