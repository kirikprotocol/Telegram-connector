package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.content.ContentRequest;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import com.eyelinecom.whoisd.sads2.telegram.connector.ExtendedSadsRequest;
import com.eyelinecom.whoisd.sads2.wstorage.profile.Profile;

/**
 * Passes current client language to content provider via request param.
 */
public class UserLocaleInterceptor extends BlankInterceptor {

  public static final String LANG_REQUEST_PARAM = "lang";

  @Override
  public void beforeContentRequest(SADSRequest request,
                                   ContentRequest contentRequest,
                                   RequestDispatcher dispatcher) throws InterceptionException {

    final String lang = getLangParam(request);
    if (lang != null) {
      contentRequest.getParameters().put(LANG_REQUEST_PARAM, lang);
    }

    super.beforeContentRequest(request, contentRequest, dispatcher);
  }

  private String getLangParam(SADSRequest request) {
    if (request instanceof ExtendedSadsRequest) {
      final ExtendedSadsRequest tgRequest = (ExtendedSadsRequest) request;

      // 1. Check current session.
      final Session session = tgRequest.getSession();
      if (session != null && !session.isClosed()) {
        final String sessionParam = (String) session.getAttribute("lang");
        if (sessionParam != null) {
          return sessionParam;
        }
      }

      final Profile profile = tgRequest.getProfile();
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
            .property("lang")
            .getValue();

        if (globalProfileParam != null) {
          return globalProfileParam;
        }
      }
    }

    // 4. Fall back to service config.

    final String serviceParam =
        request.getServiceScenario().getAttributes().getProperty("lang", null);
    if (serviceParam != null) {
      return serviceParam;
    }

    // Well, we expect a global default value in scenario configuration, but that's okay.
    return null;
  }

}
