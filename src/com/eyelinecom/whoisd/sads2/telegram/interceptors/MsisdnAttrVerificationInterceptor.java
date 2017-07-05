package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.Protocol;
import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.content.ContentRequest;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import com.eyelinecom.whoisd.sads2.profile.Profile.PropertyQuery;
import org.apache.commons.logging.Log;

import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import static com.eyelinecom.whoisd.sads2.Protocol.FACEBOOK;
import static com.eyelinecom.whoisd.sads2.Protocol.LINE;
import static com.eyelinecom.whoisd.sads2.Protocol.SKYPE;
import static com.eyelinecom.whoisd.sads2.Protocol.TELEGRAM;
import static com.eyelinecom.whoisd.sads2.Protocol.VIBER;
import static com.eyelinecom.whoisd.sads2.Protocol.VKONTAKTE;
import static com.eyelinecom.whoisd.sads2.Protocol.XHTML_MP;
import static java.lang.Boolean.parseBoolean;
import static java.util.Arrays.asList;

public class MsisdnAttrVerificationInterceptor extends BlankInterceptor {

  /**
   * Content page attribute marking it as requiring MSISDN verification.
   */
  private static final String ATTR_MSISDN_REQUIRED  = "msisdn-required";

  static final String VAR_MSISDN_CONFIRMATION_REDIRECTED  = "MSISDN_CONFIRMATION_REDIRECTED";

  private static final Collection<Protocol> PROTOCOLS_SUPPORTED = new HashSet<>(asList(
      TELEGRAM,
      SKYPE,
      FACEBOOK,
      VKONTAKTE,
      LINE,
      VIBER
  ));

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
      final String wnumber = request.getProfile().getWnumber();

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

  @SuppressWarnings("SimplifiableIfStatement")
  boolean isEnabled(SADSRequest request) {
    final Properties attrs = request.getServiceScenario().getAttributes();
    final Protocol protocol = request.getProtocol();

    if ((protocol == XHTML_MP) || (protocol == Protocol.USSD)) {
      // No need for verification as these protocols always have MSISDN.
      return false;

    } else if (PROTOCOLS_SUPPORTED.contains(protocol)) {
      return parseBoolean(
          attrs.getProperty(
              protocol.getProtocolName().toLowerCase() + ".msisdn.confirmation.enabled",
              "false"
          )
      );

    } else {
      // Unsupported protocol.
      return false;
    }
  }

  private void redirectConfirmMsisdn(SADSRequest request,
                                     RequestDispatcher dispatcher,
                                     Log log) throws Exception {

    final String prevUri = request.getResourceURI();
    redirectTo(request, dispatcher, log, prevUri);
  }

  void redirectTo(SADSRequest request,
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

  void redirectBack(SADSRequest request,
                    RequestDispatcher dispatcher,
                    String originalUrl) throws Exception {
    request.setResourceURI(originalUrl);
    dispatcher.processRequest(request);
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
}
