package com.eyelinecom.whoisd.sads2.telegram.api.types;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.reflect.ParameterizedType;

/**
 * Created by jeck on 30/03/16
 */
public abstract class AbstractFile <T extends AbstractFile> extends ApiType<T>{
    @JsonProperty(value = "file_id")
    private String fileId;
    @JsonProperty(value = "file_size")
    private Integer fileSize;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    protected Class<T> getEntityClass() {
        final ParameterizedType genericSuperclass =
                (ParameterizedType) getClass().getGenericSuperclass();
        //noinspection unchecked
        return (Class<T>) genericSuperclass.getActualTypeArguments()[0];
    }
}
