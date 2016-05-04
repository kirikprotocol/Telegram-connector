package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.Initable;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.content.ContentRequest;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import com.eyelinecom.whoisd.sads2.profile.Profile.PropertyQuery;
import com.eyelinecom.whoisd.sads2.registry.ServiceConfig;
import org.apache.commons.logging.Log;

import java.util.Properties;

public class MsisdnConfirmationInterceptor extends BlankInterceptor implements Initable {

  /**
   * Content page attribute marking it as requiring MSISDN verification.
   */
  public static final String ATTR_MSISDN_REQUIRED = "msisdn-required";
  public static final String VAR_MSISDN_CONFIRMATION_REDIRECTED = "MSISDN_CONFIRMATION_REDIRECTED";

  public static final String CONF_MSISDN_CONFIRMATION_ENABLED = "telegram.msisdn.confirmation.enabled";

  @Override
  public void afterContentResponse(SADSRequest request,
                                   ContentRequest contentRequest,
                                   ContentResponse content,
                                   RequestDispatcher dispatcher) throws InterceptionException {

    final Log log = SADSLogger.getLogger(request.getServiceId(), getClass());
    final String serviceId = request.getServiceId();

    if (!isEnabled(request)) {
      return;
    }

    try {
      final String wnumber = request.getAbonent();

      final String msisdn = request.getProfile()
          .property("mobile", "msisdn")
          .getValue();

      if (log.isDebugEnabled()) {
        log.debug("Processing wnumber = [" + wnumber + "], stored msisdn = [" + msisdn + "]");
      }

      if ((content.getAttributes().get(ATTR_MSISDN_REQUIRED) != null) && (msisdn == null)) {
        redirectConfirmMsisdn(request, dispatcher, log);

      } else if ((msisdn != null) &&
          request.getProfile()
              .property("services", "auth-" + serviceId.replace(".", "_"), VAR_MSISDN_CONFIRMATION_REDIRECTED).get() != null) {
        redirectBack(msisdn, request, dispatcher, log);
      }

    } catch (Exception e) {
      throw new InterceptionException(e);
    }
  }

  private boolean isEnabled(SADSRequest request) {
    final ServiceConfig config = request.getServiceScenario();
    return Boolean.parseBoolean(
        config.getAttributes().getProperty(CONF_MSISDN_CONFIRMATION_ENABLED, "false")
    );
  }

  private void redirectConfirmMsisdn(SADSRequest request,
                                     RequestDispatcher dispatcher,
                                     Log log) throws Exception {

    final String prevUri = request.getResourceURI();
    redirectTo(request, dispatcher, log, prevUri);
  }

  protected void redirectTo(SADSRequest request,
                            RequestDispatcher dispatcher,
                            Log log,
                            String onSuccess) throws Exception {

    final String serviceId = request.getServiceId();
    final String redirectUri =
        request.getServiceScenario().getAttributes().getProperty("msisdn-confirmation-uri");

    request.getProfile()
        .property("services", "auth-" + serviceId.replace(".", "_"), VAR_MSISDN_CONFIRMATION_REDIRECTED)
        .set(onSuccess);

    request.setResourceURI(redirectUri);

    if (log.isDebugEnabled()) {
      log.debug("Redirecting to MSISDN verification page:" +
          " wnumber = [" + request.getProfile().getWnumber() + "]," +
          " redirect to = [" + redirectUri + "]," +
          " from = [" + onSuccess + "]");
    }

    dispatcher.processRequest(request);
  }

  private void redirectBack(String msisdn,
                            SADSRequest request,
                            RequestDispatcher dispatcher,
                            Log log) throws Exception {

    final String originalUrl = popOrigUrl(msisdn, request, log);

    redirectBack(request, dispatcher, originalUrl);
  }

  String redirectBack(SADSRequest request,
                      RequestDispatcher dispatcher,
                      String originalUrl) throws Exception {
    request.setResourceURI(originalUrl);
    dispatcher.processRequest(request);

    return originalUrl;
  }

  String popOrigUrl(String msisdn, SADSRequest request, Log log) {
    final String serviceId = request.getServiceId();

    final PropertyQuery property = request.getProfile()
        .property("services", "auth-" + serviceId.replace(".", "_"), VAR_MSISDN_CONFIRMATION_REDIRECTED);

    final String originalUrl = property.getValue();
    property.delete();

    if (log.isDebugEnabled()) {
      log.debug("Redirecting back to content:" +
          " wnumber = [" + request.getProfile().getWnumber() + "]," +
          " msisdn = [" + msisdn + "]," +
          " redirect to = [" + originalUrl + "]," +
          " from = [" + request.getResourceURI() + "]");
    }

    return originalUrl;
  }

  @Override
  public void init(Properties config) throws Exception {
  }

  @Override
  public void destroy() {

  }
}
