package com.eyelinecom.whoisd.sads2.telegram.connector;

import com.eyelinecom.whoisd.sads2.Protocol;
import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.common.SADSUrlUtils;
import com.eyelinecom.whoisd.sads2.common.UrlUtils;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.SADSResponse;
import com.eyelinecom.whoisd.sads2.connector.Session;
import com.eyelinecom.whoisd.sads2.exception.NotFoundResourceException;
import com.eyelinecom.whoisd.sads2.executors.connector.AbstractHTTPPushConnector;
import com.eyelinecom.whoisd.sads2.executors.connector.LazyMessageConnector;
import com.eyelinecom.whoisd.sads2.executors.connector.MessageConnector;
import com.eyelinecom.whoisd.sads2.executors.connector.SADSExecutor;
import com.eyelinecom.whoisd.sads2.registry.ServiceConfig;
import com.eyelinecom.whoisd.sads2.telegram.ServiceSessionManager;
import com.eyelinecom.whoisd.sads2.telegram.SessionManager;
import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.api.methods.SendChatAction;
import com.eyelinecom.whoisd.sads2.telegram.api.types.User;
import com.eyelinecom.whoisd.sads2.telegram.resource.TelegramApi;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import static com.eyelinecom.whoisd.sads2.telegram.connector.TelegramRequestUtils.getChatId;
import static java.util.concurrent.Executors.newScheduledThreadPool;

public class TelegramMessageConnector extends HttpServlet {

  private final static Log log = new Log4JLogger(Logger.getLogger(TelegramMessageConnector.class));

  public static final String ATTR_SESSION_PREVIOUS_PAGE_URI = "SADS-previous-page-uri";

  private MessageConnector<StoredHttpRequest, SADSResponse> connector;

  @Override
  public void destroy() {
    super.destroy();
    connector.destroy();
  }

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    connector = new TelegramMessageConnectorImpl();

    try {
      final Properties properties = AbstractHTTPPushConnector.buildProperties(servletConfig);
      connector.init(properties);

    } catch (Exception e) {
      throw new ServletException(e);
    }
  }

  @Override
  protected void service(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {

    final StoredHttpRequest request = new StoredHttpRequest(req);

    SADSResponse response = connector.process(request);
    fillHttpResponse(resp, response);
  }

  private void fillHttpResponse(HttpServletResponse httpResponse,
                                SADSResponse sadsResponse) throws IOException {

    httpResponse.setStatus(sadsResponse.getStatus());
    String contentType = sadsResponse.getMimeType();
    if (sadsResponse.getEncoding() != null && !contentType.contains("charset=")) {
      contentType += "; charset=" + sadsResponse.getEncoding();
    }
    httpResponse.setContentType(contentType);
    Map<String, String> headers = sadsResponse.getHeaders();
    for (String key : headers.keySet()) {
      httpResponse.setHeader(key, headers.get(key));
    }
    httpResponse.setContentLength(sadsResponse.getData().length);
    httpResponse.getOutputStream().write(sadsResponse.getData());
    httpResponse.flushBuffer();
  }

  private class TelegramMessageConnectorImpl
      extends LazyMessageConnector<StoredHttpRequest, SADSResponse> {

    private static final int DEFAULT_DELAYED_JOBS_POLL_SIZE = 10;

    private String executorResourceName;
    private ScheduledExecutorService delayedExecutor;

    @Override
    public void init(Properties config) throws Exception {
      super.init(config);

      executorResourceName = InitUtils.getString("thread-pool", null, config);

      if (getLogger().isDebugEnabled()) {
        getLogger().debug("executor resourcename=" + executorResourceName);
      }

      if (StringUtils.isBlank(executorResourceName)) {
        this.delayedExecutor =
            newScheduledThreadPool(InitUtils.getInt("pool-size", DEFAULT_DELAYED_JOBS_POLL_SIZE, config));
        getLogger().debug("created a new delayed executor");
      }
    }

    @Override
    protected ExecutorService getExecutor(ServiceConfig config, String subscriber) {
      final Log log = getLogger();

      if (delayedExecutor == null) {
        try {
          final String executorResource =
              InitUtils.getString("executor", executorResourceName, config.getAttributes());
          final ScheduledExecutorService executorService =
              (ScheduledExecutorService) getResource(executorResource);

          if (log.isDebugEnabled()) {
            log.debug("Used resource executor: " + executorResource);
          }
          return executorService;

        } catch (NotFoundResourceException e) {
          if (log.isDebugEnabled()) {
            log.debug("Used internal executor",e);
          }
          return delayedExecutor;
        }

      } else {
        if (log.isDebugEnabled()) {
          log.debug("Used internal executor (it's not null)");
        }
        return delayedExecutor;
      }
    }

    /**
     * Response to a WebHook update.
     */
    @Override
    protected SADSResponse buildQueuedResponse(StoredHttpRequest req,
                                               SADSRequest sadsRequest) {
      try {
        return buildWebhookResponse(
            200,
            new SendChatAction(
                sadsRequest.getAbonent(),
                SendChatAction.ChatAction.TYPING).marshalAsWebHookResponse()
        );

      } catch (TelegramApiException e) {
        getLog(req).error(e.getMessage(), e);
        return buildQueueErrorResponse(e, req, sadsRequest);
      }
    }

    @Override
    protected SADSResponse buildQueueErrorResponse(Exception e,
                                                   StoredHttpRequest httpServletRequest,
                                                   SADSRequest sadsRequest) {
      return buildWebhookResponse(500);
    }

    @Override
    protected Log getLogger() {
      return TelegramMessageConnector.log;
    }

    @Override
    protected boolean isUSSDInitiator() {
      // Always FALSE.
      return false;
    }

    @Override
    protected String getSubscriberId(StoredHttpRequest req) throws Exception {
      return getChatId(req.getContent());
    }

    @Override
    protected String getServiceId(StoredHttpRequest req) throws Exception {
      // Extract service ID as a part of registered WebHook URL.
      final String[] parts = req.getRequestURI().split("/");
      return parts[parts.length - 2];
    }

    protected String getServiceToken(StoredHttpRequest req) throws Exception {
      // Extract service token as a part of registered WebHook URL.
      final String[] parts = req.getRequestURI().split("/");
      return parts[parts.length - 1];
    }

    @Override
    protected String getGateway() {
      // Arbitrary description, passed to content Provider via headers (brief)
      return "Telegram";
    }

    @Override
    protected String getGatewayRequestDescription(StoredHttpRequest httpServletRequest) {
      // Arbitrary description, passed to content Provider via headers (detailed)
      return "Telegram";
    }

    @Override
    protected Protocol getRequestProtocol(ServiceConfig config,
                                          String subscriberId,
                                          StoredHttpRequest httpServletRequest) {
      return Protocol.TELEGRAM;
    }

    @Override
    protected String getRequestUri(ServiceConfig config,
                                   String subscriberId,
                                   StoredHttpRequest message) throws Exception {

      final String serviceId = config.getId();
      final String incoming = TelegramRequestUtils.getMessageText(message.getContent());

      Session session = getSessionManager(serviceId).getSession(subscriberId);

      if ("/reset".equals(incoming)) {
        // Invalidate the current session.
        session.close();
        session = getSessionManager(serviceId).getSession(subscriberId);

      } else if ("/who".equals(incoming)) {
        final String serviceToken = getServiceToken(message);
        final User me = getClient().getMe(serviceToken);
        getClient().sendMessage(
            getSessionManager(serviceId),
            serviceToken,
            subscriberId,
            "Hello from " + me.getUserName() + "!"
        );
      }

      final String prevUri = (String) session.getAttribute(ATTR_SESSION_PREVIOUS_PAGE_URI);
      if (prevUri == null) {
        // No previous page means this is an initial request, thus serve the start page.
        return super.getRequestUri(config, subscriberId, message);

      } else {
        final Document prevPage =
            (Document) session.getAttribute(SADSExecutor.ATTR_SESSION_PREVIOUS_PAGE);

        String href = null;
        String inputName = null;

        // Look for a button with a corresponding label.
        //noinspection unchecked
        for (Element e : (List<Element>) prevPage.getRootElement().elements("button")) {
          if (StringUtils.equals(e.getTextTrim(), incoming)) {
            final String btnHref = e.attributeValue("href");
            href = btnHref != null ? btnHref : e.attributeValue("target");
          }
        }

        // Look for input field if any.
        if (href == null) {
          final Element input = prevPage.getRootElement().element("input");
          if (input != null) {
            href = input.attributeValue("href");
            inputName = input.attributeValue("name");
          }
        }

        // Nothing suitable to handle user input found, consider it a bad command.
        if (href == null) {
          final String badCommandPage =
              InitUtils.getString("bad-command-page", "", config.getAttributes());
          href = UrlUtils.merge(prevUri, badCommandPage);
          href = UrlUtils.addParameter(href, "bad_command", incoming);
        }

        href = SADSUrlUtils.processUssdForm(href, StringUtils.trim(incoming));
        if (inputName != null) {
          href = UrlUtils.addParameter(href, inputName, incoming);
        }

        return UrlUtils.merge(prevUri, href);
      }
    }

    @Override
    protected SADSResponse getSavedSADSResponse(StoredHttpRequest httpServletRequest) {
      // Always NULL.
      return null;
    }

    /**
     * @param request   Request to the content provider
     * @param response  Response from content provider
     */
    @Override
    protected SADSResponse getOuterResponse(StoredHttpRequest req,
                                            SADSRequest request,
                                            SADSResponse response) {
      return buildWebhookResponse(200);
    }

    private SADSResponse buildWebhookResponse(int statusCode) {
      return buildWebhookResponse(statusCode, "{}");
    }

    private SADSResponse buildWebhookResponse(int statusCode, String body) {
      final SADSResponse rc = new SADSResponse();
      rc.setStatus(statusCode);
      rc.setHeaders(Collections.<String, String>emptyMap());
      rc.setMimeType("application/json");
      rc.setData(body.getBytes());
      return rc;
    }

    @Override
    protected SADSResponse sadsRequestBuildError(Exception e,
                                                 StoredHttpRequest req) {
      getLog(req).error("SADSRequest build error", e);
      return null;
    }

    @Override
    protected SADSResponse sadsResponseBuildError(Exception e,
                                                  StoredHttpRequest req,
                                                  SADSRequest sadsRequest) {
      getLog(req).error("SADSResponse build error", e);
      return null;
    }

    @Override
    protected SADSResponse messageProcessingError(Exception e,
                                                  StoredHttpRequest req,
                                                  SADSRequest sadsRequest,
                                                  SADSResponse response) {
      getLog(req).error("Message processing error", e);
      return null;
    }

    private TelegramApi getClient() throws NotFoundResourceException {
      return (TelegramApi) getResource("telegram-api");
    }

    private SessionManager getSessionManager(String serviceId) throws Exception {
      final ServiceSessionManager serviceSessionManager =
          (ServiceSessionManager) getResource("telegram-session-manager");
      return serviceSessionManager.getSessionManager(serviceId);
    }

    private Log getLog(StoredHttpRequest req) {
      try{
        return SADSLogger.getLogger(getServiceId(req), getSubscriberId(req), getClass());

      } catch (Exception e) {
        return getLogger();
      }
    }
  }

}
