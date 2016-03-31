package com.eyelinecom.whoisd.sads2.input;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by jeck on 31/03/16
 */
public class InputFile extends AbstractInputType<InputFile> {
    @JsonProperty(value = "media_type")
    private String mediaType; //voice, audio, video, document, photo, sticker

    private String url;

    @JsonProperty(value = "content_type")
    private String contentType;

    private Integer size;
    @Override
    protected String getTypeValue() {
        return "file";
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}
