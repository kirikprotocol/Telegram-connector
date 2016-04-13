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
   * request url must start with app://verify-msisdn?success_url=... to mark that verification is needed.
   */
  public static final String MSISDN_REQUIRED_SIGN = "app://verify-msisdn";
  public static final String SUCCESS_REDIRECT_URL_PARAM = "success_url";
  public static final String PREVIOUS_PAGE_URL_SESSION_PARAM = "previous-page-url";

  public static final String CONF_MSISDN_CONFIRMATION_ENABLED = "telegram.msisdn.confirmation.enabled";


  @Override
  public void afterContentResponse(SADSRequest request, ContentRequest contentRequest, ContentResponse content, RequestDispatcher dispatcher) throws InterceptionException {
    if (!isEnabled(request)) {
      return;
    }
    ExtendedSadsRequest extendedRequest = (ExtendedSadsRequest)request;
    extendedRequest.getSession().setAttribute(PREVIOUS_PAGE_URL_SESSION_PARAM,request.getResourceURI());
  }

  @Override
  public void beforeContentRequest(SADSRequest request,
                                   ContentRequest contentRequest,
                                   RequestDispatcher dispatcher) throws InterceptionException {
    if (!isEnabled(request)) {
      return;
    }
    String requestUri = request.getResourceURI();
    if(requestUri == null || !requestUri.startsWith(MSISDN_REQUIRED_SIGN)){
       return;
    }

    final Log log = SADSLogger.getLogger(request.getServiceId(), getClass());
    final String serviceId = request.getServiceId();
    final ExtendedSadsRequest tgRequest = (ExtendedSadsRequest) request;

    try {
      final String wnumber = request.getAbonent();
      final String msisdn = tgRequest.getProfile()
          .query()
          .property("mobile", "msisdn")
          .getValue();

      if (log.isDebugEnabled()) {
        log.debug("Processing wnumber = [" + wnumber + "], stored msisdn = [" + msisdn + "]");
      }

      if ((msisdn == null)) {
        if(log.isDebugEnabled()){log.debug("redirect to confirm msisdn");}
        redirectConfirmMsisdn(tgRequest, dispatcher, log);

      } else if (
          tgRequest.getProfile()
              .query()
              .property("services", "auth-" + serviceId, VAR_MSISDN_CONFIRMATION_REDIRECTED).get() != null)
      {
        if(log.isDebugEnabled()){log.debug("redirect from confirm msisdn");}
        redirectBack(msisdn, tgRequest, contentRequest, dispatcher, log);
      }

      else {
        if(log.isDebugEnabled()){log.debug("already verified msisdn");}
        String originalUrl = UrlUtils.getParameter(request.getResourceURI(),SUCCESS_REDIRECT_URL_PARAM);
        originalUrl = getForwardUrl(tgRequest,originalUrl);
        super.redirectBack(tgRequest,dispatcher,originalUrl);
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

    String verificationForwardUri = UrlUtils.getParameter(request.getResourceURI(),SUCCESS_REDIRECT_URL_PARAM);
    verificationForwardUri = getForwardUrl(request,verificationForwardUri);
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

  private String getForwardUrl(ExtendedSadsRequest request,String forwardUrl){
    if(UrlUtils.isAbsoluteUrl(forwardUrl)){
      return forwardUrl;
    }
    String previousUrl = (String)request.getSession().getAttribute(PREVIOUS_PAGE_URL_SESSION_PARAM);
    if(previousUrl!=null){
      try {
        return UrlUtils.merge(previousUrl,forwardUrl);
      } catch (Exception e) {
        return forwardUrl;
      }
    }
    return forwardUrl;
  }

  @Override
  public void init(Properties config) throws Exception {
  }

  @Override
  public void destroy() {

  }
}
