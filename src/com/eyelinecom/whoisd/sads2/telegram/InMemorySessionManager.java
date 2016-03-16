package com.eyelinecom.whoisd.sads2.telegram;

import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.resource.ResourceFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.configuration.HierarchicalConfiguration;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import static java.util.Collections.unmodifiableSet;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Created by jeck on 10/02/16
 */
@SuppressWarnings("unused")
public class InMemorySessionManager {

  @SuppressWarnings("unused")
  public static class Factory implements ResourceFactory {

    @Override
    public ServiceSessionManager build(String id,
                                       Properties properties,
                                       HierarchicalConfiguration config) throws Exception {

      return new ServiceSessionManagerImpl(
          InitUtils.getLong("life-time", MINUTES.toMillis(15), properties)
      );
    }

    @Override
    public boolean isHeavyResource() {
      return false;
    }
  }

  private static class ServiceSessionManagerImpl
      extends ExpiringCacheBase<String, SessionManager>
      implements ServiceSessionManager {

    private final long expireAfterAccessMillis;

    public ServiceSessionManagerImpl(long expireAfterAccessMillis) {
      super(expireAfterAccessMillis);
      this.expireAfterAccessMillis = expireAfterAccessMillis;
    }

    @Override
    protected SessionManager init(String key) {
      return new SessionManagerImpl(expireAfterAccessMillis);
    }

    @Override
    public SessionManager getSessionManager(String serviceId) throws ExecutionException {
      return get(serviceId, true);
    }
  }

  private static class SessionManagerImpl
      extends ExpiringCacheBase<String, Session>
      implements SessionManager {

    public SessionManagerImpl(long expireAfterAccessMillis) {
      super(expireAfterAccessMillis);
    }

    @Override
    protected Session init(final String id) {
      return new MemorySession(id) {
        @Override public void close() { storage.invalidate(id); }
      };
    }

    @Override
    public Session getSession(final String id) throws ExecutionException {
      return getSession(id, true);
    }

    @Override
    public Session getSession(final String id,
                              boolean createIfMissing) throws ExecutionException {
      return get(id, createIfMissing);
    }

  }

  private static abstract class MemorySession implements Session {
    private final String id;
    private final Map<String,Object> attributes = new HashMap<>();
    private final Date startDate = new Date();

    public MemorySession(String id) {
      this.id = id;
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
      return unmodifiableSet(new HashSet<>(attributes.keySet()));
    }

    @Override
    public Object removeAttribute(String id) {
      return attributes.remove(id);
    }

    @Override
    public Date getStartDate() {
      return startDate;
    }
  }

  private static abstract class ExpiringCacheBase<K, V> {
    protected final Cache<K, V> storage;

    public ExpiringCacheBase(long expireAfterAccessMillis) {
      storage = CacheBuilder.newBuilder()
          .expireAfterAccess(expireAfterAccessMillis, MILLISECONDS)
          .build();
    }

    protected abstract V init(K key);

    protected V get(final K key,
                    boolean createIfMissing) throws ExecutionException {
      return createIfMissing ?
          storage.get(key, initWrapper(key)) : storage.getIfPresent(key);
    }

    private Callable<V> initWrapper(final K key) {
      return new Callable<V>() {
        @Override
        public V call() throws Exception {
          return init(key);
        }
      };
    }
  }

}


