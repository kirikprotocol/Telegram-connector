package com.eyelinecom.whoisd.sads2.telegram;

import com.eyelinecom.whoisd.sads2.connector.Session;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jeck on 08/02/16
 */
public class SimpleSession implements Session {
    private String id;
    private Map<String,Object> attributes = new HashMap<String, Object>();
    private Date startDate = new Date();

    public SimpleSession(String id) {
        this.id = id;
    }

    public SimpleSession() {

    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Object getAttribute(String id) {
        return attributes.get(id);
    }

    @Override
    public void setAttribute(String id, Object value) {
        attributes.put(id, value);
    }

    @Override
    public Collection<String> getAttributesNames() {
        return attributes.keySet();
    }

    @Override
    public Object removeAttribute(String id) {
        return attributes.remove(id);
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}

