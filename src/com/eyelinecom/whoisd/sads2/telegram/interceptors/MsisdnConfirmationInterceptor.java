package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.personalization.helpers.PersonalizationClient;
import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.Initable;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.common.SADSUrlUtils;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.content.ContentRequest;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import org.apache.commons.lang.StringUtils;

import java.util.Properties;

import static com.eyelinecom.whoisd.sads2.telegram.interceptors.TelegramStartLinkInterceptor.VAR_CHAT2MSISDN;

public class MsisdnConfirmationInterceptor extends BlankInterceptor implements Initable {

  public static final String ATTR_MSISDN_REQUIRED = "msisdn-required";

  private PersonalizationClient client;


  @Override
  public void afterContentResponse(SADSRequest request,
                                   ContentRequest contentRequest,
                                   ContentResponse content,
                                   RequestDispatcher dispatcher) throws InterceptionException {

    try {
      if (content.getAttributes().get(ATTR_MSISDN_REQUIRED) != null) {
        final String chatId = request.getAbonent();

        if (client.isExists(chatId, VAR_CHAT2MSISDN)) {
          final String msisdn = client.getString(chatId, VAR_CHAT2MSISDN);
          if (StringUtils.isBlank(msisdn)) {
            doRedirect(request, dispatcher);
          }
        }
      }

    } catch (Exception e) {
      throw new InterceptionException(e);
    }
  }

  private void doRedirect(SADSRequest request, RequestDispatcher dispatcher) throws Exception {
    final String redirectUri =
        request.getServiceScenario().getAttributes().getProperty("msisdn-confirmation-uri");

    request.setResourceURI(SADSUrlUtils.getResourceUriFromFullUri(redirectUri));
    request.setSourceName(SADSUrlUtils.getSourceNameFromFullUri(redirectUri));
    request.setParameters(SADSUrlUtils.getParametersMap(redirectUri));

    dispatcher.processRequest(request);
  }

  @Override
  public void init(Properties config) throws Exception {
    client = (PersonalizationClient) SADSInitUtils.getResource("personalization-client", config);
  }

  @Override
  public void destroy() {

  }
}
