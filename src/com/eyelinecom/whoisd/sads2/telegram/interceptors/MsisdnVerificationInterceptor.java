package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.Initable;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.common.UrlUtils;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.content.ContentRequest;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.registry.ServiceConfig;
import com.eyelinecom.whoisd.sads2.telegram.connector.ExtendedSadsRequest;
import org.apache.commons.logging.Log;

import java.util.Properties;

public class MsisdnVerificationInterceptor extends MsisdnConfirmationInterceptor implements Initable {

  /**
   * request url must start with verify://msisdn?success_url=... to mark that verification is needed.
   */
  public static final String MSISDN_REQUIRED_SIGN = "verify://msisdn";
  public static final String SUCCESS_REDIRECT_URL_PARAM = "success_url";

  public static final String CONF_MSISDN_CONFIRMATION_ENABLED = "telegram.msisdn.confirmation.enabled";


  @Override
  public void afterContentResponse(SADSRequest request, ContentRequest contentRequest, ContentResponse content, RequestDispatcher dispatcher) throws InterceptionException {
    // Nothing here.
  }

  @Override
  public void beforeContentRequest(SADSRequest request,
                                   ContentRequest contentRequest,
                                   RequestDispatcher dispatcher) throws InterceptionException {

    final Log log = SADSLogger.getLogger(request.getServiceId(), getClass());
    final String serviceId = request.getServiceId();
    final ExtendedSadsRequest tgRequest = (ExtendedSadsRequest) request;

    if (!isEnabled(request)) {
      return;
    }

    try {
      final String wnumber = request.getAbonent();
      final String msisdn = tgRequest.getProfile()
          .query()
          .property("mobile", "msisdn")
          .getValue();

      if (log.isDebugEnabled()) {
        log.debug("Processing wnumber = [" + wnumber + "], stored msisdn = [" + msisdn + "]");
      }

      String requestUri = request.getResourceURI();
      if ((requestUri != null && requestUri.startsWith(MSISDN_REQUIRED_SIGN)) && (msisdn == null)) {
        redirectConfirmMsisdn(tgRequest, dispatcher, log);

      } else if ((msisdn != null) &&
          tgRequest.getProfile()
              .query()
              .property("services", "auth-" + serviceId, VAR_MSISDN_CONFIRMATION_REDIRECTED).get() != null) {
        redirectBack(msisdn, tgRequest, contentRequest, dispatcher, log);
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

  private void redirectConfirmMsisdn(ExtendedSadsRequest request,
                                     RequestDispatcher dispatcher,
                                     Log log) throws Exception {

    final String verificationForwardUri =
        UrlUtils.getParameter(request.getResourceURI(),SUCCESS_REDIRECT_URL_PARAM);

    redirectTo(request, dispatcher, log, verificationForwardUri);
  }

  private void redirectBack(String msisdn,
                            ExtendedSadsRequest request,
                            ContentRequest contentRequest,
                            RequestDispatcher dispatcher,
                            Log log) throws Exception {

    final String originalUrl = popOrigUrl(msisdn, request, log);
    contentRequest.setResourceURI(originalUrl);
    super.redirectBack(request, dispatcher, originalUrl);
  }

  @Override
  public void init(Properties config) throws Exception {
  }

  @Override
  public void destroy() {

  }
}
