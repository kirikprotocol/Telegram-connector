package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.Initable;
import com.eyelinecom.whoisd.sads2.common.PageBuilder;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.common.UrlUtils;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.content.ContentRequest;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import com.eyelinecom.whoisd.sads2.telegram.connector.ExtendedSadsRequest;
import com.eyelinecom.whoisd.sads2.wstorage.profile.Profile.Query.PropertyQuery;
import org.apache.commons.logging.Log;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.util.Properties;

import static com.eyelinecom.whoisd.sads2.telegram.interceptors.TelegramPushInterceptor.getText;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

public class PasswordInputInterceptor extends BlankInterceptor implements Initable {

  @Override
  public void afterContentResponse(SADSRequest request,
                                   ContentRequest contentRequest,
                                   ContentResponse content,
                                   RequestDispatcher dispatcher) throws InterceptionException {

    final ExtendedSadsRequest tgRequest = (ExtendedSadsRequest) request;

    final String serviceId = request.getServiceId();
    final Log log = SADSLogger.getLogger(serviceId, getClass());

    final Document doc = (Document) content.getAttributes().get(PageBuilder.VALUE_DOCUMENT);
    final Element inputElement = (Element) doc
        .getRootElement()
        .selectSingleNode("//input[@type='password']");

    if (inputElement == null) {
      return;
    }

    final PropertyQuery passwordProp =
        tgRequest.getProfile().query().property("services", "password-" + serviceId, "value");
    final String passwordValue =  passwordProp.getValue();

    if (passwordValue != null) {
      // Password value is already known in profile storage.
      // Submit the value using input `name` and `href`.

      passwordProp.delete();

      String href = inputElement.attributeValue("href");
      final String inputName = inputElement.attributeValue("name");

      href = UrlUtils.addParameter(href, inputName, passwordValue);

      try {
        final String submitUri = UrlUtils.merge(request.getResourceURI(), href);
        request.setResourceURI(submitUri);

        dispatcher.processRequest(request);
      } catch (Exception e) {
        throw new InterceptionException(e);
      }

    } else {

      // Set:
      //  - password-sid: current service ID (the one which need the password)
      //  - password-prompt: message text
      //
      // Redirect to service property "password-input-uri".
      //
      final String prevUri = request.getResourceURI();

      request.getParameters().put("password-sid", serviceId);

      try {
        request.getParameters().put(
            "password-prompt",
            printBase64Binary(getText(doc).getBytes(UTF_8))
        );
      } catch (DocumentException e) {
        throw new InterceptionException(e);
      }

      final String redirectUri =
          request.getServiceScenario().getAttributes().getProperty("password-input-uri");
      request.setResourceURI(redirectUri);

      if (log.isDebugEnabled()) {
        log.debug("Redirecting to password input:" +
            " wnumber = [" + tgRequest.getProfile().getWnumber() + "]," +
            " redirect to = [" + redirectUri + "]," +
            " from = [" + prevUri + "]");
      }

      try {
        dispatcher.processRequest(request);
      } catch (Exception e) {
        throw new InterceptionException(e);
      }
    }
  }

  @Override
  public void init(Properties config) {

  }

  @Override
  public void destroy() {

  }
}
