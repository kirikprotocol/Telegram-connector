package com.eyelinecom.whoisd.sads2.telegram.api.types;

/**
 * Created by jeck on 31/03/16
 */
public class Location extends ApiType<Location> {
    private Double longitude;
    private Double latitude;

    public Location() {
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
}
