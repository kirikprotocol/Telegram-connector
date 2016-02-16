package com.eyelinecom.whoisd.sads2.telegram;

import com.eyelinecom.whoisd.sads2.connector.Session;

/**
 * Created by jeck on 10/02/16
 */
public interface SessionManager {

  /**
   * Returns a session by identifier, creating one if needed (same as {@code getSession(id, true)}).
   */
  Session getSession(String id) throws Exception;

  Session getSession(String id, boolean createIfMissing) throws Exception;
}
