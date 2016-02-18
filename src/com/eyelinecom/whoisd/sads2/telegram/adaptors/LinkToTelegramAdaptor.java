package com.eyelinecom.whoisd.sads2.telegram.adaptors;

import com.eyelinecom.whoisd.personalization.helpers.PersonalizationManager;
import com.eyelinecom.whoisd.sads2.adaptor.URLAdaptor;
import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.content.ContentResponseUtils;
import com.eyelinecom.whoisd.sads2.telegram.registry.WebHookConfigListener;
import com.eyelinecom.whoisd.sads2.telegram.resource.TelegramApi;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;

import java.util.Properties;

/**
 * Created by jeck on 18/02/16
 */
public class LinkToTelegramAdaptor extends URLAdaptor {
    public static final String PERS_VAR_TELEGRAM_HASH_PREFIX = "tg-hash-";
    private String telegramAddress;
    private PersonalizationManager personalization;
    private TelegramApi api;
    private int hashLength;

    @Override
    protected String adaptLink(String linkUri, ContentResponse content) throws Exception {
        String service = content.getServiceScenario().getId();
        String token = content.getServiceScenario().getAttributes().getProperty(WebHookConfigListener.CONF_TOKEN);
        if (linkUri.startsWith("protocol://telegram")) {
            String subscriber = ContentResponseUtils.getAbonent(content);
            Log log = SADSLogger.getLogger(service, subscriber, this.getClass());
            String hash = RandomStringUtils.randomAlphanumeric(hashLength);
            String var = PERS_VAR_TELEGRAM_HASH_PREFIX+hash;
            personalization.set(var, subscriber);
            String link = telegramAddress+api.getMe(token).getUserName()+"?start="+hash;
            //String link = telegramAddress+"UruruBot?start="+hash;
            if (log.isInfoEnabled()) {
                log.info("Saved to personalization '"+var+"' value '"+subscriber+"', result link: "+link);
            }
            return link;
        }
        return linkUri;
    }

    @Override
    public void init(Properties config) throws Exception {
        super.init(config);
        this.telegramAddress = InitUtils.getString("telegram-url", "https://telegram.me/",config);
        this.personalization = (PersonalizationManager) SADSInitUtils.getResource("personalization", config);
        this.hashLength = InitUtils.getInt("hash-length", 5, config);
        this.api = (TelegramApi) SADSInitUtils.getResource("telegram-api", config);
    }
}
