package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.Initable;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.SADSResponse;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import com.eyelinecom.whoisd.sads2.profile.Profile;
import com.eyelinecom.whoisd.sads2.profile.Profile.Property;
import com.eyelinecom.whoisd.sads2.profile.ProfileStorage;
import com.eyelinecom.whoisd.sads2.telegram.registry.WebHookConfigListener;
import org.apache.commons.logging.Log;

import java.util.Properties;

import static com.eyelinecom.whoisd.sads2.wstorage.profile.QueryRestrictions.property;

/**
 * Created by jeck on 18/02/16
 */
public class Msisdn2ChatInterceptor extends BlankInterceptor implements Initable{
  private ProfileStorage profileStorage;

  @Override
  public void afterResponseRender(SADSRequest request,
                                  ContentResponse content,
                                  SADSResponse response,
                                  RequestDispatcher dispatcher) throws InterceptionException {

    Log log = SADSLogger.getLogger(request.getServiceId(), request.getServiceId(), this.getClass());

    try {

      final String msisdn = request.getAbonent();

      final Profile profile = profileStorage
          .query()
          .where(property("mobile", "msisdn").eq(msisdn))
          .get();

      if (profile != null) {
        final String token =
            request.getServiceScenario().getAttributes().getProperty(WebHookConfigListener.CONF_TOKEN);

        if (token != null) {
          final Property chatId = profile
              .property("telegram-chats", token)
              .get();

          if (chatId != null) {
            request.setAbonent(chatId.getValue());
          }
        }
      }

    } catch (Exception e) {
      log.warn("",e);
    }
  }

  @Override
  public void init(Properties config) throws Exception {
    profileStorage = SADSInitUtils.getResource("profile-storage", config);
  }

  @Override
  public void destroy() {

  }
}
