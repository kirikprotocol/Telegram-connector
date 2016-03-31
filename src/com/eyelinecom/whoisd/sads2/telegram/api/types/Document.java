package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by jeck on 30/03/16
 */
public class Document extends AbstractFile<Document> {
    private PhotoSize thumb;
    @JsonProperty(value = "file_name")
    private String fileName;
    @JsonProperty(value = "mime_type")
    private String mimeType;

    public Document() {
    }

    public PhotoSize getThumb() {
        return thumb;
    }

    public void setThumb(PhotoSize thumb) {
        this.thumb = thumb;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
