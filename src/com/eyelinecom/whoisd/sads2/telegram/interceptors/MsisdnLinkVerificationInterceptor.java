package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.common.UrlUtils;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.content.ContentRequest;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import org.apache.commons.logging.Log;

public class MsisdnLinkVerificationInterceptor extends MsisdnAttrVerificationInterceptor {

  /**
   * Request url must start with {@code verify://msisdn?success_url=...} to mark that
   * verification is needed.
   */
  private static final String MSISDN_REQUIRED_PREFIX          = "verify://msisdn";

  private static final String SUCCESS_REDIRECT_URL_PARAM      = "success_url";
  private static final String PREVIOUS_PAGE_URL_SESSION_PARAM = "previous-page-url";

  /** Request parameter to return MSISDN as a result of plugin execution */
  private static final String MSISDN_URL_PARAM                = "msisdn";

  @Override
  public void afterContentResponse(SADSRequest request,
                                   ContentRequest contentRequest,
                                   ContentResponse content,
                                   RequestDispatcher dispatcher) throws InterceptionException {

    if (!isEnabled(request)) {
      return;
    }

    request.getSession().setAttribute(PREVIOUS_PAGE_URL_SESSION_PARAM, request.getResourceURI());
  }

  @Override
  public void beforeContentRequest(SADSRequest request,
                                   ContentRequest contentRequest,
                                   RequestDispatcher dispatcher) throws InterceptionException {

    final Log log = SADSLogger.getLogger(request.getServiceId(), getClass());
    final String requestUri = request.getResourceURI();
    if (requestUri == null || !requestUri.startsWith(MSISDN_REQUIRED_PREFIX)){
       return;
    }

    if (!isEnabled(request)) {
      // Proceed directly to success_url.
      try {
        String successUrl = UrlUtils.getParameter(requestUri, SUCCESS_REDIRECT_URL_PARAM);
        successUrl = UrlUtils.merge(
            request.getServiceScenario().getAttributes().getProperty("start-page"),
            successUrl);

        if (log.isDebugEnabled()) {
          log.debug("Protocol = [" + request.getProtocol() + "]," +
              " redirecting to success_url = [" + successUrl + "]");
        }

        request.setResourceURI(successUrl);
        dispatcher.processRequest(request);

      } catch (Exception e) {
        throw new InterceptionException(e);
      }

    } else {

      final String serviceId = request.getServiceId();

      try {
        final String wnumber = request.getProfile().getWnumber();
        final String msisdn = request.getProfile()
            .property("mobile", "msisdn")
            .getValue();

        if (log.isDebugEnabled()) {
          log.debug("Processing wnumber = [" + wnumber + "], stored msisdn = [" + msisdn + "]");
        }

        if (msisdn == null) {
          log.debug("Redirecting to MSISDN verification");

          redirectConfirmMsisdn(request, dispatcher, log);

        } else if (
            request.getProfile()
                .property("services", "auth-" + serviceId.replace(".", "_"), VAR_MSISDN_CONFIRMATION_REDIRECTED).get() != null) {
          log.debug("Redirecting FROM MSISDN verification");

          redirectBack(msisdn, request, contentRequest, dispatcher, log);

        } else {
          log.debug("MSISDN is already verified");

          String successUrl =
              UrlUtils.getParameter(request.getResourceURI(), SUCCESS_REDIRECT_URL_PARAM);
          successUrl = resolveForwardUrl(request, successUrl);
          successUrl = UrlUtils.addParameter(successUrl, MSISDN_URL_PARAM, msisdn);

          super.redirectBack(request, dispatcher, successUrl);
        }

      } catch (Exception e) {
        throw new InterceptionException(e);
      }
    }
  }

  private void redirectConfirmMsisdn(SADSRequest request,
                                     RequestDispatcher dispatcher,
                                     Log log) throws Exception {

    String successUrl = UrlUtils.getParameter(request.getResourceURI(), SUCCESS_REDIRECT_URL_PARAM);
    successUrl = resolveForwardUrl(request, successUrl);
    redirectTo(request, dispatcher, log, successUrl);
  }

  private void redirectBack(String msisdn,
                            SADSRequest request,
                            ContentRequest contentRequest,
                            RequestDispatcher dispatcher,
                            Log log) throws Exception {

    String originalUrl = popOrigUrl(msisdn, request, log);
    originalUrl = UrlUtils.addParameter(originalUrl, MSISDN_URL_PARAM, msisdn);

    contentRequest.setResourceURI(originalUrl);
    super.redirectBack(request, dispatcher, originalUrl);
  }

  private String resolveForwardUrl(SADSRequest request, String forwardUrl) {
    if (UrlUtils.isAbsoluteUrl(forwardUrl)) {
      return forwardUrl;
    }

    String previousUrl = (String) request.getSession().getAttribute(PREVIOUS_PAGE_URL_SESSION_PARAM);
    if (previousUrl != null) {
      try {
        return UrlUtils.merge(previousUrl, forwardUrl);

      } catch (Exception e) {
        return forwardUrl;
      }
    }

    return forwardUrl;
  }

}
