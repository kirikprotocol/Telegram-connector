package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.personalization.helpers.PersonalizationClient;
import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.Initable;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.SADSResponse;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import java.util.Properties;

/**
 * Created by jeck on 18/02/16
 */
public class Msisdn2ChatInterceptor extends BlankInterceptor implements Initable{
    PersonalizationClient client;
    @Override
    public void afterResponseRender(SADSRequest request, ContentResponse content, SADSResponse response, RequestDispatcher dispatcher) throws InterceptionException {
        Log log = SADSLogger.getLogger(request.getServiceId(), request.getServiceId(), this.getClass());
        try {
            if (client.isExists(request.getAbonent(), TelegramStartLinkInterceptor.VAR_MSISDN2CHAT, log)) {
                String chatId = client.getString(request.getAbonent(), TelegramStartLinkInterceptor.VAR_MSISDN2CHAT, log);
                if (StringUtils.isNotBlank(chatId)) {
                    request.setAbonent(chatId);
                }
            }
        } catch (Exception e) {
            log.warn("",e);
        }
    }

    @Override
    public void init(Properties config) throws Exception {
        this.client = (PersonalizationClient) SADSInitUtils.getResource("personalization-client", config);
    }

    @Override
    public void destroy() {

    }
}
