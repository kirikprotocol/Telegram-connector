package com.eyelinecom.whoisd.sads2.telegram;

import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.resource.ResourceFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.configuration.HierarchicalConfiguration;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by jeck on 10/02/16
 */
public class InMemorySessionManager implements SessionManager {
    LoadingCache<String, Session> storage;

    @Override
    public Session getSession(String id) throws ExecutionException {
        return storage.get(id);
    }
    public static class Factory implements ResourceFactory {

        @Override
        public Object build(String id, Properties properties, HierarchicalConfiguration config) throws Exception {
            LoadingCache<String, Session> storage = CacheBuilder.newBuilder()
                    .expireAfterAccess(InitUtils.getLong("life-time", 1000*60*15, properties), TimeUnit.MILLISECONDS)
                    .build(
                            new CacheLoader<String, Session>() {
                                public Session load(String key){
                                    return new SimpleSession(key);
                                }
                            });
            return storage;
        }

        @Override
        public boolean isHeavyResource() {
            return false;
        }
    }
}


