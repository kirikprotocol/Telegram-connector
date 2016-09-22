package com.eyelinecom.whoisd.sads2.telegram.interceptors;

import com.eyelinecom.whoisd.sads2.RequestDispatcher;
import com.eyelinecom.whoisd.sads2.common.ArrayUtil;
import com.eyelinecom.whoisd.sads2.common.Initable;
import com.eyelinecom.whoisd.sads2.common.PageBuilder;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.common.UrlUtils;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.content.ContentRequest;
import com.eyelinecom.whoisd.sads2.content.ContentResponse;
import com.eyelinecom.whoisd.sads2.exception.InterceptionException;
import com.eyelinecom.whoisd.sads2.interceptor.BlankInterceptor;
import com.eyelinecom.whoisd.sads2.profile.Profile.PropertyQuery;
import com.eyelinecom.whoisd.sads2.telegram.connector.TelegramRequestUtils;
import com.eyelinecom.whoisd.sads2.telegram.connector.TelegramRequestUtils.ExtLink;
import com.eyelinecom.whoisd.sads2.telegram.util.MarshalUtils;
import com.google.common.base.Function;
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
  public void beforeContentRequest(SADSRequest request,
                                   ContentRequest contentRequest,
                                   RequestDispatcher dispatcher) throws InterceptionException {

    final String serviceId = request.getServiceId();
    final Session session = request.getSession();
    final String redirectUri =
        request.getServiceScenario().getAttributes().getProperty("password-input-uri");

    final PropertyQuery passwordProp = request
        .getProfile()
        .property("services", "password-" + serviceId.replace(".", "_"));

    final String passwordValue = passwordProp.getValue();

    if (passwordValue != null && !request.getResourceURI().startsWith(redirectUri)) {
      // Password value is already known in profile storage.
      //
      // Submit the value using session attrs:
      //  - password-uri: submit URL
      //  - password-name: input name
      //

      passwordProp.delete();

      String href = (String) session.getAttribute("password-uri");
      final String inputName = (String) session.getAttribute("password-name");

      href = UrlUtils.addParameter(href, inputName, passwordValue);
      request.setResourceURI(href);

      processRequest(request, dispatcher);

    }
  }

  @Override
  public void beforeResponseRender(SADSRequest request,
                                   ContentResponse content,
                                   RequestDispatcher dispatcher) throws InterceptionException {

    final Document doc = (Document) content.getAttributes().get(PageBuilder.VALUE_DOCUMENT);

    final Element inputElement = (Element) doc.getRootElement()
        .selectSingleNode("//input[@type='password']");

    final String redirectUri =
        request.getServiceScenario().getAttributes().getProperty("password-input-uri");

    if (inputElement != null) {
      redirectToPasswordInput(
          request,
          dispatcher,
          doc,
          inputElement,
          redirectUri);

    }
  }

  private void redirectToPasswordInput(final SADSRequest request,
                                       RequestDispatcher dispatcher,
                                       Document doc,
                                       Element inputElement,
                                       String redirectUri) throws InterceptionException {

    final Log log = SADSLogger.getLogger(request.getServiceId(), getClass());

    // Got an input[type=password].
    //
    // Set request attrs:
    //  - password-sid: current service ID (the one which needs the password)
    //  - password-prompt: message text
    //  - password-links: original message buttons, if any
    //
    // Set session attrs:
    //  - password-uri: submit URL, merged w/ the current content root
    //  - password-name: input name
    //
    // Redirect to content service using property "password-input-uri".
    //
    final String prevUri = request.getResourceURI();

    request.getParameters().put("password-sid", request.getServiceId());

    try {
      request.getParameters().put(
          "password-prompt",
          printBase64Binary(getText(doc).getBytes(UTF_8))
      );
    } catch (DocumentException e) {
      throw new InterceptionException(e);
    }

    final ExtLink[][] links = TelegramRequestUtils.collectExtLinks(doc);
    try {
      if (links != null) {

        ArrayUtil.forEach(links,
            new Function<ExtLink, ExtLink>() {
              @Override
              public ExtLink apply(ExtLink link) {
                try {
                  return new ExtLink(
                      UrlUtils.merge(request.getResourceURI(), link.href),
                      link.label
                  );

                } catch (Exception e) {
                  log.error(e.getMessage(), e);
                  return link;
                }
              }
            });

        request.getParameters().put(
            "password-links",
            printBase64Binary(MarshalUtils.marshal(links).getBytes(UTF_8))
        );
      }
    } catch (Exception e) {
      throw new InterceptionException(e);
    }

    String href = inputElement.attributeValue("href");
    try {
      href = UrlUtils.merge(request.getResourceURI(), href);

    } catch (Exception e) {
      throw new InterceptionException(e);
    }

    request.getSession().setAttribute("password-uri", href);
    request.getSession().setAttribute("password-name", inputElement.attributeValue("name"));

    request.setResourceURI(redirectUri);

    if (log.isDebugEnabled()) {
      log.debug("Redirecting to password input:" +
          " wnumber = [" + request.getProfile().getWnumber() + "]," +
          " redirect to = [" + redirectUri + "]," +
          " from = [" + prevUri + "]");
    }

    processRequest(request, dispatcher);
  }

  private void processRequest(SADSRequest request,
                              RequestDispatcher dispatcher) throws InterceptionException {
    try {
      dispatcher.processRequest(request);

    } catch (Exception e) {
      throw new InterceptionException(e);
    }
  }

  @Override
  public void init(Properties config) {

  }

  @Override
  public void destroy() {

  }
}
