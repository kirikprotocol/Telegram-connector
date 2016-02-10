package com.eyelinecom.whoisd.sads2.telegram;

import com.eyelinecom.whoisd.sads2.connector.Session;

/**
 * Created by jeck on 10/02/16
 */
public interface SessionManager {
    public Session getSession(String id) throws Exception;
}
