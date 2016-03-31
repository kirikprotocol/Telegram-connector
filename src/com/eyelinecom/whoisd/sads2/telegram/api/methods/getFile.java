package com.eyelinecom.whoisd.sads2.telegram.api.methods;

import com.eyelinecom.whoisd.sads2.telegram.api.types.File;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by jeck on 30/03/16
 */
public class getFile extends ApiMethod<getFile, File> {
    @JsonProperty(value = "file_id", required = true)
    private String fileId;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public getFile() {
    }

    public getFile(String fileId) {
        this.fileId = fileId;
    }
}
