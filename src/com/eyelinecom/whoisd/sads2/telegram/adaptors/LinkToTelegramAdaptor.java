package com.eyelinecom.whoisd.sads2.telegram.adaptors;

import com.eyelinecom.whoisd.sads2.adaptor.URLAdaptor;
import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.content.ContentResponseUtils;
import com.eyelinecom.whoisd.sads2.executors.connector.SADSInitializer;
import com.eyelinecom.whoisd.sads2.telegram.registry.WebHookConfigListener;
import com.eyelinecom.whoisd.sads2.telegram.resource.TelegramApi;
import com.eyelinecom.whoisd.sads2.wstorage.profile.Profile;
import com.eyelinecom.whoisd.sads2.wstorage.profile.ProfileStorage;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;

import java.util.Properties;

import static com.eyelinecom.whoisd.sads2.wstorage.profile.QueryRestrictions.property;

/**
 * Created by jeck on 18/02/16
 */
public class LinkToTelegramAdaptor extends URLAdaptor {
    private static final String PREFIX_TELEGRAM = "protocol://telegram";
    public static final String PERS_VAR_TELEGRAM_HASH_PREFIX = "tg-hash-";
    private String telegramAddress;
    private TelegramApi api;
    private int hashLength;
    private ProfileStorage profileStorage;

    @Override
    protected String adaptLink(String linkUri, ContentResponse content) throws Exception {
        String service = content.getServiceScenario().getId();
        if (linkUri.startsWith(PREFIX_TELEGRAM)) {
            String subscriber = ContentResponseUtils.getAbonent(content);
            Log log = SADSLogger.getLogger(service, subscriber, this.getClass());
            String token;
            if (linkUri.length() > PREFIX_TELEGRAM.length()+1) {
                service = linkUri.substring(PREFIX_TELEGRAM.length()+1);
                token = SADSInitializer.getServiceRegistry().getProperties(service).getProperty(WebHookConfigListener.CONF_TOKEN);

            } else {
                token = content.getServiceScenario().getAttributes().getProperty(WebHookConfigListener.CONF_TOKEN);
            }

            final Profile profile = profileStorage
                .query()
                .where(property("mobile", "msisdn").eq(subscriber))
                .getOrCreate();

            final String hash = RandomStringUtils.randomAlphanumeric(hashLength);
            profile
                .property("telegram-hashes", token)
                .set(hash);

            String botUsername  = api.getMe(token).getUserName();
            String link = telegramAddress+botUsername+"?start="+hash;

            if (log.isInfoEnabled()) {
                log.info("Saved to profile storage '" + profile.dump() + "' value '" + subscriber + "', result link: "+link);
            }
            return link;
        }
        return linkUri;
    }

    @Override
    public void init(Properties config) throws Exception {
        super.init(config);
        this.telegramAddress = InitUtils.getString("telegram-url", "https://telegram.me/",config);
        this.profileStorage = (ProfileStorage) SADSInitUtils.getResource("profile-storage", config);

        this.hashLength = InitUtils.getInt("hash-length", 5, config);
        this.api = (TelegramApi) SADSInitUtils.getResource("telegram-api", config);
    }
}
