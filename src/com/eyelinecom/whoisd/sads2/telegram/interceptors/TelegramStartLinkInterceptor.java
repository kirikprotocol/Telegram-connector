package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.personalization.helpers.PersonalizationClient;
import com.eyelinecom.whoisd.personalization.helpers.PersonalizationManager;
import com.eyelinecom.whoisd.sads2.Protocol;
import com.eyelinecom.whoisd.sads2.common.Initable;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.executors.connector.SADSExecutor;
import com.eyelinecom.whoisd.sads2.executors.interceptor.BlankConnectorInterceptor;
import com.eyelinecom.whoisd.sads2.telegram.SessionManager;
import com.eyelinecom.whoisd.sads2.telegram.adaptors.LinkToTelegramAdaptor;
import com.eyelinecom.whoisd.sads2.telegram.connector.StoredHttpRequest;
import com.eyelinecom.whoisd.sads2.telegram.connector.TelegramRequestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import java.util.Properties;

import static com.eyelinecom.whoisd.sads2.telegram.resource.TelegramApi.START_MESSAGE;

/**
 * Created by jeck on 18/02/16
 */
public class TelegramStartLinkInterceptor extends BlankConnectorInterceptor implements Initable {

  public static final String VAR_MSISDN2CHAT = "telegram-chat-id";
  public static final String VAR_CHAT2MSISDN = "telegram-msisdn";
  public static final String SESSION_VAR_MSISDN = "msisdn";

  private PersonalizationManager personalization;
  private PersonalizationClient client;
  private SessionManager sessionManager;

  @Override
  public void init(Properties config) throws Exception {
    personalization = (PersonalizationManager) SADSInitUtils.getResource("personalization-service", config);
    client = (PersonalizationClient) SADSInitUtils.getResource("personalization-client", config);
    sessionManager = (SessionManager) SADSInitUtils.getResource("session-manager", config);
  }

  @Override
  public void onOuterRequest(SADSRequest request, Object outerRequest) throws Exception {
    if (request.getProtocol() == Protocol.TELEGRAM && outerRequest instanceof StoredHttpRequest) {
      onTelegramRequest(request, (StoredHttpRequest) outerRequest);
    }
  }

  private void onTelegramRequest(SADSRequest request,
                                 StoredHttpRequest outerRequest) throws Exception {

    final String chatId = request.getAbonent();
    final Session session = sessionManager.getSession(chatId);
    final String rawSubscriberData = TelegramRequestUtils.getMessageText(outerRequest.getContent());

    if (rawSubscriberData.startsWith(START_MESSAGE) &&
        rawSubscriberData.length() > START_MESSAGE.length() + 1) {
      // Got payload along with the start message.

      final String payload = rawSubscriberData.substring(START_MESSAGE.length() + 1);
      final String var = LinkToTelegramAdaptor.PERS_VAR_TELEGRAM_HASH_PREFIX + payload;

      if (personalization.isExists(var)) {
        final String subscriberData = personalization.getString(var);
        if (StringUtils.isNotBlank(subscriberData)) {
          // Persist MSISDN <-> ChatID mapping.

          final Log log = SADSLogger.getLogger(request.getServiceId(), request.getServiceId(), this.getClass());

          personalization.remove(var);
          client.set(subscriberData, VAR_MSISDN2CHAT, chatId, log);
          client.set(chatId, VAR_CHAT2MSISDN, subscriberData, log);

          session.setAttribute(SESSION_VAR_MSISDN, subscriberData);
        }
      }

    } else if (session.getAttribute(SADSExecutor.ATTR_SESSION_PREVIOUS_PAGE) == null) {

      if (client.isExists(chatId, VAR_CHAT2MSISDN)) {
        final String msisdn = client.getString(chatId, VAR_CHAT2MSISDN);
        if (StringUtils.isNotBlank(msisdn)) {
          session.setAttribute(SESSION_VAR_MSISDN, msisdn);
        }
      }
    }
  }

  @Override
  public void destroy() {

  }
}
