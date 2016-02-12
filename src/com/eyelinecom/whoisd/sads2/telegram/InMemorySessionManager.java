package com.eyelinecom.whoisd.sads2.telegram;

import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.resource.ResourceFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.configuration.HierarchicalConfiguration;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by jeck on 10/02/16
 */
public class InMemorySessionManager implements SessionManager {
    Cache<String, Session> storage;

    @Override
    public Session getSession(final String id) throws ExecutionException {
        return storage.get(id, new Callable<Session>() {
            @Override
            public Session call() throws Exception {
                return new MemorySession(id, InMemorySessionManager.this.storage);
            }
        });

    }

    public static class Factory implements ResourceFactory {
        @Override
        public Object build(String id, Properties properties, HierarchicalConfiguration config) throws Exception {
            return CacheBuilder.newBuilder()
                    .expireAfterAccess(InitUtils.getLong("life-time", 1000*60*15, properties), TimeUnit.MILLISECONDS)
                    .<String, Session>build();
        }

        @Override
        public boolean isHeavyResource() {
            return false;
        }
    }

    private static class MemorySession implements Session {
        private String id;
        private Map<String,Object> attributes = new HashMap<>();
        private Date startDate = new Date();
        private Cache<String,Session> storage;

        public MemorySession(String id, Cache<String,Session> storage) {
            this.id = id;
            this.storage = storage;
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

        @Override
        public void close() {
            storage.invalidate(this.id);
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

}


