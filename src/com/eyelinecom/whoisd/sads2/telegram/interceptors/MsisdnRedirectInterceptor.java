package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.personalization.helpers.PersonalizationClient;
import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.common.Initable;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import org.apache.commons.logging.Log;

import java.util.Properties;

import static com.eyelinecom.whoisd.sads2.telegram.interceptors.TelegramStartLinkInterceptor.VAR_CHAT2MSISDN;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

public class MsisdnRedirectInterceptor extends BlankInterceptor implements Initable {

  public static final String CONF_MSISDN_CONFIRMATION_FORCED = "telegram.msisdn.verification";

  private PersonalizationClient client;

    @Override
    public void onRequest(SADSRequest request, RequestDispatcher dispatcher) throws InterceptionException {
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

  @Override
  public void init(Properties config) throws Exception {
    client = (PersonalizationClient) SADSInitUtils.getResource("personalization-client", config);
  }

  @Override
  public void destroy() {

  }
}
