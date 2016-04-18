package com.eyelinecom.whoisd.sads2.input;

import com.eyelinecom.whoisd.sads2.telegram.api.types.ApiType;

/**
 * Created by jeck on 31/03/16
 */
public abstract class AbstractInputType<T extends AbstractInputType> extends ApiType<T> {
    protected String type = getTypeValue();

    protected abstract String getTypeValue();

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
