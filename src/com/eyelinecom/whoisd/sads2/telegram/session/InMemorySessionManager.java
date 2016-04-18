package com.eyelinecom.whoisd.sads2.telegram.session;

import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.resource.ResourceFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Collections.unmodifiableSet;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Created by jeck on 10/02/16
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class InMemorySessionManager {

  private static final Log log = new Log4JLogger(Logger.getLogger(InMemorySessionManager.class));

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
    private final Set<SessionEventListener> eventListeners =
        Collections.newSetFromMap(new ConcurrentHashMap<SessionEventListener, Boolean>());

    ServiceSessionManagerImpl(long expireAfterAccessMillis) {
      super(expireAfterAccessMillis);
      this.expireAfterAccessMillis = expireAfterAccessMillis;
    }

    @Override
    protected SessionManager init(String serviceId) {
      return new SessionManagerImpl(serviceId, expireAfterAccessMillis);
    }

    @Override
    public SessionManager getSessionManager(String serviceId) throws ExecutionException {
      return get(serviceId, true);
    }

    @Override
    public void addSessionEventListener(SessionEventListener listener) {
      eventListeners.add(checkNotNull(listener));
    }

    @Override
    protected void onRemoval(String serviceId, SessionManager sessionManager) {
      // Ensure removal events are enqueued _now_ for all associated sessions.
      storage.invalidateAll();
    }


    private class SessionManagerImpl
        extends ExpiringCacheBase<String, Session>
        implements SessionManager {

      private final String serviceId;

      SessionManagerImpl(String serviceId, long expireAfterAccessMillis) {
        super(expireAfterAccessMillis);
        this.serviceId = serviceId;
      }

      @Override
      protected Session init(final String subscriberId) {
        for (SessionEventListener eventListener : eventListeners) {
          try {
            eventListener.onSessionOpened(serviceId, subscriberId);
          } catch (Exception e) {
            log.error("Session event listener execution failed", e);
          }
        }

        return new MemorySession(subscriberId) {
          @Override public void close() { storage.invalidate(subscriberId); }
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

      @Override
      protected void onRemoval(String subscriberId, Session value) {
        for (SessionEventListener eventListener : eventListeners) {
          try {
            eventListener.onSessionClosed(serviceId, subscriberId);
          } catch (Exception e) {
            log.error("Session event listener execution failed", e);
          }
        }
      }

    }
  }


  //
  //
  //

  private static abstract class MemorySession implements Session {
    private final String id;
    private final Map<String,Object> attributes = new HashMap<>();
    private final Date startDate = new Date();

    MemorySession(String id) {
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


  //
  //
  //

  private static abstract class ExpiringCacheBase<K, V> {
    protected final Cache<K, V> storage;

    ExpiringCacheBase(long expireAfterAccessMillis) {
      storage = CacheBuilder.newBuilder()
          .expireAfterAccess(expireAfterAccessMillis, MILLISECONDS)
          .removalListener(new RemovalListener<K, V>() {
            @Override
            public void onRemoval(
                @SuppressWarnings("NullableProblems") RemovalNotification<K, V> event) {
              ExpiringCacheBase.this.onRemoval(event.getKey(), event.getValue());
            }
          })
          .build();
    }

    protected abstract V init(K key);

    protected void onRemoval(K key, V value) {}

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


