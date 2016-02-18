package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.Initable;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.content.ContentRequest;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import com.eyelinecom.whoisd.sads2.telegram.SessionManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import java.util.Properties;

/**
 * Created by jeck on 18/02/16
 */
public class Chat2MsisdnInterceptor extends BlankInterceptor implements Initable{
    private SessionManager sessionManager;

    @Override
    public void beforeContentRequest(SADSRequest request, ContentRequest contentRequest, RequestDispatcher dispatcher) throws InterceptionException {
        Log log = SADSLogger.getLogger(request.getServiceId(), request.getServiceId(), this.getClass());
        try {
            Session session = sessionManager.getSession(request.getAbonent());
            String msisdn = (String) session.getAttribute(TelegramStartLinkInterceptor.SESSION_VAR_MSISDN);
            if (StringUtils.isNotBlank(msisdn)) {
                contentRequest.setAbonent(msisdn);
            }
        } catch (Exception e) {
            log.warn("", e);
        }

    }

    @Override
    public void init(Properties config) throws Exception {
        this.sessionManager = (SessionManager) SADSInitUtils.getResource("session-manager", config);
    }

    @Override
    public void destroy() {

    }
}
