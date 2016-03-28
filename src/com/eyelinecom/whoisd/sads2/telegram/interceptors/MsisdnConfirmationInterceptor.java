package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.personalization.helpers.PersonalizationClient;
import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.common.Initable;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.content.ContentRequest;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import com.eyelinecom.whoisd.sads2.telegram.ServiceSessionManager;
import org.apache.commons.logging.Log;

import java.util.Properties;

import static com.eyelinecom.whoisd.sads2.telegram.interceptors.TelegramStartLinkInterceptor.SESSION_VAR_MSISDN;
import static com.eyelinecom.whoisd.sads2.telegram.interceptors.TelegramStartLinkInterceptor.VAR_CHAT2MSISDN;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class MsisdnConfirmationInterceptor extends BlankInterceptor implements Initable {

  /**
   * Content page attribute marking it as requiring MSISDN verification.
   */
  public static final String ATTR_MSISDN_REQUIRED = "msisdn-required";
  public static final String VAR_MSISDN_CONFIRMATION_REDIRECTED = "MSISDN_CONFIRMATION_REDIRECTED";

  public static final String CONF_MSISDN_CONFIRMATION_ENABLED = "telegram.msisdn.confirmation.enabled";
  public static final String CONF_MSISDN_CONFIRMATION_FORCED = "telegram.msisdn.confirmation.forced";

  private PersonalizationClient client;
  private ServiceSessionManager sessionManager;

    @Override
    public void beforeContentRequest(SADSRequest request, ContentRequest contentRequest, RequestDispatcher dispatcher) throws InterceptionException {
        if (InitUtils.getBoolean(CONF_MSISDN_CONFIRMATION_FORCED, false, request.getServiceScenario().getAttributes())) {
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

                if (msisdn == null) {
                    redirectConfirmMsisdn(request, dispatcher, log);

                } else if (client.isExists(chatId, VAR_MSISDN_CONFIRMATION_REDIRECTED)) {
                    redirectBack(msisdn, request, dispatcher, log);
                }

            } catch (Exception e) {
                throw new InterceptionException(e);
            }
        }
    }

    @Override
  public void afterContentResponse(SADSRequest request,
                                   ContentRequest contentRequest,
                                   ContentResponse content,
                                   RequestDispatcher dispatcher) throws InterceptionException {
        if (!InitUtils.getBoolean(CONF_MSISDN_CONFIRMATION_FORCED, false, request.getServiceScenario().getAttributes())) {
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

              if ((content.getAttributes().get(ATTR_MSISDN_REQUIRED) != null) && (msisdn == null)) {
                  redirectConfirmMsisdn(request, dispatcher, log);

              } else if ((msisdn != null) &&
                      client.isExists(chatId, VAR_MSISDN_CONFIRMATION_REDIRECTED)) {
                  redirectBack(msisdn, request, dispatcher, log);
              }

          } catch (Exception e) {
              throw new InterceptionException(e);
          }
      }
  }

  private boolean isEnabled(SADSRequest request) {
      return InitUtils.getBoolean(CONF_MSISDN_CONFIRMATION_ENABLED, false, request.getServiceScenario().getAttributes()) ||
              InitUtils.getBoolean(CONF_MSISDN_CONFIRMATION_FORCED, false, request.getServiceScenario().getAttributes());
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
    final String redirectUri =
        request.getServiceScenario().getAttributes().getProperty("msisdn-confirmation-uri");
    final String prevUri = request.getResourceURI();

    client.set(chatId, VAR_MSISDN_CONFIRMATION_REDIRECTED, prevUri);
    request.setResourceURI(redirectUri);

    if (log.isDebugEnabled()) {
      log.debug("Redirecting to MSISDN verification page:" +
          " chatId = [" + chatId + "]," +
          " redirect to = [" + redirectUri + "]," +
          " from = [" + prevUri + "]");
    }

    dispatcher.processRequest(request);
  }

  private void redirectBack(String msisdn,
                            SADSRequest request,
                            RequestDispatcher dispatcher,
                            Log log) throws Exception {

    final String chatId = request.getAbonent();

    final String originalUrl = client.getString(chatId, VAR_MSISDN_CONFIRMATION_REDIRECTED);
    client.remove(chatId, VAR_MSISDN_CONFIRMATION_REDIRECTED);

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
