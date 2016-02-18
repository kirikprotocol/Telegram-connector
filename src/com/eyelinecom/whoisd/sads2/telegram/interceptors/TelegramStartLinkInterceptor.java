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

/**
 * Created by jeck on 18/02/16
 */
public class TelegramStartLinkInterceptor extends BlankConnectorInterceptor implements Initable{
    private static final String START_MESSAGE = "/start";
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
            final String chatId = request.getAbonent();
            final Session session = sessionManager.getSession(chatId);
            String rawSubscriberData = TelegramRequestUtils.getMessageText(((StoredHttpRequest) outerRequest).getContent());
            if (rawSubscriberData.startsWith(START_MESSAGE) && rawSubscriberData.length() > START_MESSAGE.length()+1) {
                String payload = rawSubscriberData.substring(START_MESSAGE.length()+1);
                String var = LinkToTelegramAdaptor.PERS_VAR_TELEGRAM_HASH_PREFIX+payload;
                if (personalization.isExists(var)) {
                    String subscriberData = personalization.getString(var);
                    Log log = SADSLogger.getLogger(request.getServiceId(), request.getServiceId(), this.getClass());
                    if (StringUtils.isNotBlank(subscriberData)) {
                        personalization.remove(var);
                        client.set(subscriberData, VAR_MSISDN2CHAT, chatId, log);
                        client.set(chatId, VAR_CHAT2MSISDN, subscriberData, log);
                        session.setAttribute(SESSION_VAR_MSISDN, subscriberData);
                    }
                }
            } else if (session.getAttribute(SADSExecutor.ATTR_SESSION_PREVIOUS_PAGE)==null){
                String msisdn = client.getString(chatId, VAR_CHAT2MSISDN);
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
