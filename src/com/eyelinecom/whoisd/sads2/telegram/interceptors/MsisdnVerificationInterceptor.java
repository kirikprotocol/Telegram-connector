package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.personalization.helpers.PersonalizationClient;
import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.Initable;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.common.UrlUtils;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.content.ContentRequest;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import com.eyelinecom.whoisd.sads2.registry.ServiceConfig;
import com.eyelinecom.whoisd.sads2.telegram.ServiceSessionManager;
import org.apache.commons.logging.Log;

import java.util.Properties;

import static com.eyelinecom.whoisd.sads2.telegram.interceptors.TelegramStartLinkInterceptor.SESSION_VAR_MSISDN;
import static com.eyelinecom.whoisd.sads2.telegram.interceptors.TelegramStartLinkInterceptor.VAR_CHAT2MSISDN;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class MsisdnVerificationInterceptor extends BlankInterceptor implements Initable {

  /**
   * request url must start with verify://msisdn?success_url=... to mark that verification is needed.
   */
  public static final String MSISDN_REQUIRED_SIGN = "verify://msisdn";
  public static final String SUCCESS_REDIRECT_URL_PARAM = "success_url";
  public static final String VAR_MSISDN_VERIFICATION_REDIRECTED = "MSISDN_VERIFICATION_REDIRECTED";
  public static final String MSISDN_CONFIRMATION_URI = "msisdn-confirmation-uri";

  public static final String CONF_MSISDN_CONFIRMATION_ENABLED = "telegram.msisdn.confirmation.enabled";

  private PersonalizationClient client;
  private ServiceSessionManager sessionManager;

  @Override
  public void beforeContentRequest(SADSRequest request, ContentRequest contentRequest, RequestDispatcher dispatcher) throws InterceptionException {
    final Log log = SADSLogger.getLogger(request.getServiceId(), getClass());

        if (!isEnabled(request)) {
          return;
        }

        try {
          final String chatId = request.getAbonent();
          final String msisdn = getMsisdn(chatId);

          if (log.isDebugEnabled()) {
            log.debug("Processing chatId = [" + chatId + "], stored msisdn = [" + msisdn + "]");
          }

          String requestUri = request.getResourceURI();
          if ((requestUri != null && requestUri.startsWith(MSISDN_REQUIRED_SIGN)) && (msisdn == null)) {

            redirectConfirmMsisdn(request, dispatcher, log);

          } else if ((msisdn != null) &&
              client.isExists(chatId, VAR_MSISDN_VERIFICATION_REDIRECTED)) {
              redirectBack(msisdn, request, contentRequest, dispatcher, log);
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

      private String getMsisdn(String chatId) throws Exception {
        if (client.isExists(chatId, VAR_CHAT2MSISDN)) {
          final String msisdn = client.getString(chatId, VAR_CHAT2MSISDN);
          if (isNotBlank(msisdn)) {
            // Okay, MSISDN is known.
            return msisdn;
          }
        }
        return null;
      }

      private void redirectConfirmMsisdn(SADSRequest request,
                                         RequestDispatcher dispatcher,
                                         Log log) throws Exception {

        final String chatId = request.getAbonent();
        final String redirectUri = request.getServiceScenario().getAttributes().getProperty(MSISDN_CONFIRMATION_URI);
        final String verificationForwardUri = UrlUtils.getParameter(request.getResourceURI(),SUCCESS_REDIRECT_URL_PARAM);

        client.set(chatId, VAR_MSISDN_VERIFICATION_REDIRECTED, verificationForwardUri);
        request.setResourceURI(redirectUri);

        if (log.isDebugEnabled()) {
          log.debug("Redirecting to MSISDN verification page:" +
              " chatId = [" + chatId + "]," +
              " redirect to = [" + redirectUri + "]," +
              " from = [" + request.getResourceURI() + "]");
        }

        dispatcher.processRequest(request);
      }

      private void redirectBack(String msisdn,
                                SADSRequest request, ContentRequest contentRequest,
                                RequestDispatcher dispatcher,
                                Log log) throws Exception {

        final String chatId = request.getAbonent();

        final String originalUrl = client.getString(chatId, VAR_MSISDN_VERIFICATION_REDIRECTED);
        client.remove(chatId, VAR_MSISDN_VERIFICATION_REDIRECTED);

        if (log.isDebugEnabled()) {
          log.debug("Redirecting back to content:" +
              " chatId = [" + chatId + "]," +
              " msisdn = [" + msisdn + "]," +
              " redirect to = [" + originalUrl + "]," +
              " from = [" + request.getResourceURI() + "]");
        }

        final Session session =
            sessionManager.getSessionManager(request.getServiceId()).getSession(chatId);
        if (session.getAttribute(SESSION_VAR_MSISDN) == null) {
          session.setAttribute(SESSION_VAR_MSISDN, msisdn);
        }

    //    request.setAbonent(msisdn);
        request.setResourceURI(originalUrl);
        contentRequest.setResourceURI(originalUrl);
        dispatcher.processRequest(request);
      }

      @Override
      public void init(Properties config) throws Exception {
        client = (PersonalizationClient) SADSInitUtils.getResource("personalization-client", config);
        sessionManager = (ServiceSessionManager) SADSInitUtils.getResource("session-manager", config);
      }

      @Override
      public void destroy() {

      }
}
