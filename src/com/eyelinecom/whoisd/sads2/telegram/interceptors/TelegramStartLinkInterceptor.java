package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.Protocol;
import com.eyelinecom.whoisd.sads2.common.Initable;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.executors.interceptor.BlankConnectorInterceptor;
import com.eyelinecom.whoisd.sads2.telegram.connector.ExtendedSadsRequest;
import com.eyelinecom.whoisd.sads2.telegram.connector.TelegramWebhookRequest;
import com.eyelinecom.whoisd.sads2.wstorage.profile.Profile;
import com.eyelinecom.whoisd.sads2.wstorage.profile.ProfileStorage;

import java.util.Properties;

import static com.eyelinecom.whoisd.sads2.telegram.resource.TelegramApi.START_MESSAGE;
import static com.eyelinecom.whoisd.sads2.wstorage.profile.QueryRestrictions.property;

/**
 * Created by jeck on 18/02/16
 */
public class TelegramStartLinkInterceptor extends BlankConnectorInterceptor implements Initable {

  private ProfileStorage profileStorage;

  @Override
  public void init(Properties config) throws Exception {
    profileStorage = (ProfileStorage) SADSInitUtils.getResource("profile-storage", config);
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

    if (rawSubscriberData!=null &&
        rawSubscriberData.startsWith(START_MESSAGE) &&
        rawSubscriberData.length() > START_MESSAGE.length() + 1) {
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
      }

    }

  }

  @Override
  public void destroy() {

  }
}
