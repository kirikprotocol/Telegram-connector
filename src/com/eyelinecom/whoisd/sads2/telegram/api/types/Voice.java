package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by jeck on 30/03/16
 */
public class Voice extends AbstractFile<Voice> {
    private Integer duration;
    @JsonProperty(value = "mime_type")
    private String mimeType;

    public Voice() {
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
