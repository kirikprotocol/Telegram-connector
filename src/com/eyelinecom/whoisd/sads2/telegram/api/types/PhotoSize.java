package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by jeck on 30/03/16
 */
public class PhotoSize extends ApiType<PhotoSize> {
    @JsonProperty(value = "file_id")
    private String fileId;
    private Integer width;
    private Integer height;

    @JsonProperty(value = "file_size")
    private Integer fileSize;

    public PhotoSize() {
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }
}
