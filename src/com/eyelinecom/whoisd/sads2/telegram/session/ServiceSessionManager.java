package com.eyelinecom.whoisd.sads2.telegram.session;

/**
 * Provides {@linkplain SessionManager} specific to a service.
 */
public interface ServiceSessionManager {

  SessionManager getSessionManager(String serviceId) throws Exception;

  /**
   * Registers a listener instance that should be notified each time a session is
   * initialized or closed.
   * <br/>
   * Note that notifications are not synchronous, and the only thing guaranteed is that
   * the listener will be called some time in the future after the corresponding event occurs.
   */
  void addSessionEventListener(SessionEventListener listener);

  /**
   * Removes {@linkplain SessionEventListener event listener} set by
   * {@linkplain #addSessionEventListener(SessionEventListener) addSessionEventListener}.
   * <br/>
   * Does nothing in case the specified listener was not found among the registered ones.
   */
  void removeSessionEventListener(SessionEventListener listener);


  interface SessionEventListener {

    /**
     * Called in case a new {@linkplain com.eyelinecom.whoisd.sads2.connector.Session session}
     * is initialized.
     */
    void onSessionOpened(String serviceId, String sessionId);

    /**
     * Called in case a {@linkplain com.eyelinecom.whoisd.sads2.connector.Session session}
     * is closed due to whatever reason.
     */
    void onSessionClosed(String serviceId, String sessionId);
  }
}
