package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.common.Initable;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import com.eyelinecom.whoisd.sads2.profile.Profile;
import com.eyelinecom.whoisd.sads2.profile.ProfileStorage;
import org.apache.commons.logging.Log;

import java.util.Properties;

import static org.apache.commons.lang.StringUtils.isBlank;

public class MsisdnRedirectInterceptor extends BlankInterceptor implements Initable {

  public static final String CONF_MSISDN_CONFIRMATION_FORCED = "telegram.msisdn.verification";

  private ProfileStorage profileStorage;

  @Override
  public void onRequest(SADSRequest request, RequestDispatcher dispatcher) throws InterceptionException {
    if (InitUtils.getBoolean(CONF_MSISDN_CONFIRMATION_FORCED, false, request.getServiceScenario().getAttributes())) {
      final Log log = SADSLogger.getLogger(request.getServiceId(), getClass());
      if (!isEnabled(request) || request.getProfile() == null) {
        return;
      }

      try {
        final Profile profile = request.getProfile();
        final String msisdn = getMsisdn(profile);
        if (log.isDebugEnabled()) {
          log.debug("Processing wnumber = [" + profile.getWnumber() + "], stored msisdn = [" + msisdn + "]");
        }
        if (isBlank(msisdn)) {
          String redirectURL = request.getServiceScenario().getAttributes().getProperty("msisdn-redirect-uri");
          request.setResourceURI(redirectURL);
          if (log.isInfoEnabled()) {
            log.info("Request redirected to "+redirectURL);
          }
        }
      } catch (Exception e) {
        throw new InterceptionException(e);
      }
    }
  }

  private boolean isEnabled(SADSRequest request) {
    return InitUtils.getBoolean(CONF_MSISDN_CONFIRMATION_FORCED, false, request.getServiceScenario().getAttributes());
  }

  private String getMsisdn(Profile profile) {
    if (profile == null) {
      return null;
    }
    final Profile.Property msisdn = profile
        .property("mobile", "msisdn")
        .get();
    return msisdn == null ? null : msisdn.getValue();
  }

  @Override
  public void init(Properties config) throws Exception {
    profileStorage = SADSInitUtils.getResource("profile-storage", config);
  }

  @Override
  public void destroy() {

  }
}
