package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.Protocol;
import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.common.Initable;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.executors.interceptor.BlankConnectorInterceptor;
import com.eyelinecom.whoisd.sads2.telegram.connector.ExtendedSadsRequest;
import com.eyelinecom.whoisd.sads2.telegram.connector.TelegramWebhookRequest;
import com.eyelinecom.whoisd.sads2.telegram.session.ServiceSessionManager;
import com.eyelinecom.whoisd.sads2.wstorage.profile.Profile;
import com.eyelinecom.whoisd.sads2.wstorage.profile.ProfileStorage;

import java.util.Properties;

import static com.eyelinecom.whoisd.sads2.executors.registry.ShortcutsStorage.CONF_STARTPAGE;
import static com.eyelinecom.whoisd.sads2.telegram.resource.TelegramApi.START_MESSAGE;
import static com.eyelinecom.whoisd.sads2.wstorage.profile.QueryRestrictions.property;

/**
 * Created by jeck on 18/02/16
 */
public class TelegramStartLinkInterceptor extends BlankConnectorInterceptor implements Initable {

  private ProfileStorage profileStorage;
  private ServiceSessionManager serviceSessionManager;

  @Override
  public void init(Properties config) throws Exception {
    profileStorage = (ProfileStorage) SADSInitUtils.getResource("profile-storage", config);
    serviceSessionManager =
        (ServiceSessionManager) SADSInitUtils.getResource("session-manager", config);
  }

  @Override
  public void onOuterRequest(SADSRequest request, Object outerRequest) throws Exception {
    if (request.getProtocol() == Protocol.TELEGRAM && outerRequest instanceof TelegramWebhookRequest) {
      onTelegramRequest((ExtendedSadsRequest) request, (TelegramWebhookRequest) outerRequest);
    }
  }

  private void onTelegramRequest(ExtendedSadsRequest request,
                                 TelegramWebhookRequest outerRequest) throws Exception {

    final String token = outerRequest.getServiceToken();
    final String rawSubscriberData = outerRequest.getMessageText();

    if (rawSubscriberData != null && rawSubscriberData.startsWith(START_MESSAGE)) {

      closeSession(request, request.getProfile().getWnumber());

      if (rawSubscriberData.length() > START_MESSAGE.length() + 1) {
        // Got payload along with the start message.

        final String payload = rawSubscriberData.substring(START_MESSAGE.length() + 1);

        final Profile profile = profileStorage
            .query()
            .where(property("telegram-hashes", token).eq(payload))
            .get();  //TODO filter by date (link life-time: 10 min)

        if (profile != null) {
          profile.property("telegram-hashes", token).delete();
          final String msisdn = profile
              .property("mobile", "msisdn")
              .getValue();

          if (msisdn != null) {
            request.getProfile()
                .property("mobile", "msisdn")
                .set(msisdn);
          }

          if (!profile.getWnumber().equals(request.getProfile().getWnumber())) profile.delete();
          closeSession(request, profile.getWnumber());
        }
      }

    }

  }

  private void closeSession(ExtendedSadsRequest request, String wnumber) throws Exception {
    request.getSession().close();

    final Session session = serviceSessionManager
        .getSessionManager(request.getServiceId())
        .getSession(wnumber, false);
    if (session != null) {
      session.close();
    }

    final Session currentSession = serviceSessionManager
        .getSessionManager(request.getServiceId())
        .getSession(wnumber);
    request.setSession(currentSession);

    // Start page is already set by now in message connector, but session got suddenly invalidated.
    // Fix that.
    request.setResourceURI(
        InitUtils.getString(CONF_STARTPAGE, "", request.getServiceScenario().getAttributes())
    );
  }

  @Override
  public void destroy() {

  }
}
