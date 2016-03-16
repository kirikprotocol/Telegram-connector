package com.eyelinecom.whoisd.sads2.telegram;

/**
 * Provides {@linkplain SessionManager} specific to a service.
 */
public interface ServiceSessionManager {

  SessionManager getSessionManager(String serviceId) throws Exception;

}
