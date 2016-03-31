package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by jeck on 30/03/16
 */
public class File extends AbstractFile<File> {
    @JsonProperty(value = "file_path")
    private String filePath;

    @JsonIgnore
    private String url;

    public File() {
    }

    public String getUrl() {
        return url;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
