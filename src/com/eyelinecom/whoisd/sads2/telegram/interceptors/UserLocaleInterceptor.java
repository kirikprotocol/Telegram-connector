package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.content.ContentRequest;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import com.eyelinecom.whoisd.sads2.profile.Profile;

/**
 * Passes current client language to content provider via request param.
 */
public class UserLocaleInterceptor extends BlankInterceptor {

  public static final String LANG_PARAM = "lang";

  @Override
  public void beforeContentRequest(SADSRequest request,
                                   ContentRequest contentRequest,
                                   RequestDispatcher dispatcher) throws InterceptionException {

    final String lang = getLangParam(request);
    if (lang != null) {
      contentRequest.getParameters().put(LANG_PARAM, lang);
    }

    super.beforeContentRequest(request, contentRequest, dispatcher);
  }

  private String getLangParam(SADSRequest request) {

    // 1. Check current session.
    final Session session = request.getSession();
    if (session != null && !session.isClosed()) {
      final String sessionParam = (String) session.getAttribute(LANG_PARAM);
      if (sessionParam != null) {
        return sessionParam;
      }
    }

    final Profile profile = request.getProfile();
    if (profile != null) {
      // 2. Check service-specific profile.
      final String safeSid = request.getServiceId().replace(".", "_");

      final String profileParam = profile
          .property("services", "lang-" + safeSid)
          .getValue();

      if (profileParam != null) {
        return profileParam;
      }

      // 3. Check global profile.
      final String globalProfileParam = profile
          .property(LANG_PARAM)
          .getValue();

      if (globalProfileParam != null) {
        return globalProfileParam;
      }
    }

    // 4. Fall back to service config.

    final String serviceParam =
        request.getServiceScenario().getAttributes().getProperty(LANG_PARAM, null);
    if (serviceParam != null) {
      return serviceParam;
    }

    // Well, we expect a global default value in scenario configuration, but that's okay.
    return null;
  }

}
