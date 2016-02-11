package com.eyelinecom.whoisd.sads2.telegram.connector;

import com.eyelinecom.whoisd.sads2.Protocol;
import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.common.SADSLogger;
import com.eyelinecom.whoisd.sads2.connector.SADSRequest;
import com.eyelinecom.whoisd.sads2.connector.SADSResponse;
import com.eyelinecom.whoisd.sads2.exception.NotFoundResourceException;
import com.eyelinecom.whoisd.sads2.executors.connector.AbstractHTTPPushConnector;
import com.eyelinecom.whoisd.sads2.executors.connector.LazyMessageConnector;
import com.eyelinecom.whoisd.sads2.executors.connector.MessageConnector;
import com.eyelinecom.whoisd.sads2.registry.ServiceConfig;
import com.eyelinecom.whoisd.sads2.telegram.api.types.Update;
import com.eyelinecom.whoisd.sads2.telegram.resource.TelegramApi;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class TelegramMessageConnector extends HttpServlet {

  private final static Log log = new Log4JLogger(Logger.getLogger(TelegramMessageConnector.class));

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


//    try {
//      String serviceId = connector.getServiceId(req);
//      String scenarioId = connector.getScenarioId(req);
//      String subscriber = connector.getSubscriberId(req);
//
//      Registry sadsRegistry = SADSInitializer.getServiceRegistry();
//      ServiceConfig sc = sadsRegistry.getServiceConfig(serviceId, scenarioId);
//
//      String requestUri = connector.getRequestUri(sc, subscriber, req);
//      if (requestUri.trim().equals("/")) {
//        Properties prop = sc.getAttributes();
//        String startPage = InitUtils.getString(ShortcutsStorage.CONF_STARTPAGE, prop);
//        String resourceUri = UrlUtils.getPath(startPage);
//        resp.sendRedirect(resourceUri.substring(1));
//        return;
//      }
//
//    } catch (Exception e) {
//      log.warn("error", e);
//    }

    SADSResponse response = connector.process(request);
    this.fillHttpResponse(resp, response);
  }

  private void fillHttpResponse(HttpServletResponse httpResponse,
                                SADSResponse sadsResponse) throws IOException {

    httpResponse.setStatus(sadsResponse.getStatus());
    String contentType = sadsResponse.getMimeType();
    if (sadsResponse.getEncoding() != null && contentType.indexOf("charset=") == -1) {
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
        this.delayedExecutor = Executors.newScheduledThreadPool(InitUtils.getInt("pool-size", DEFAULT_DELAYED_JOBS_POLL_SIZE, config));
        getLogger().debug("created a new delayed executor");
      }
    }


    @Override
    protected ExecutorService getExecutor(ServiceConfig config, String subscriber) {
      Log log = this.getLogger();

      if (this.delayedExecutor == null) {

        try {
          String executorResource = InitUtils.getString("executor", executorResourceName, config.getAttributes());
          ScheduledExecutorService executorService = (ScheduledExecutorService) this.getResource(executorResource);
          if (log.isDebugEnabled()) log.debug("Used resource executor: " + executorResource);
          return executorService;

        } catch (NotFoundResourceException e) {
          if (log.isDebugEnabled()) log.debug("Used internal executor",e);
          return delayedExecutor;
        }

      } else {
        if (log.isDebugEnabled()) log.debug("Used internal executor (it's not null)");
        return delayedExecutor;
      }
    }

    @Override
    protected SADSResponse buildQueuedResponse(StoredHttpRequest httpServletRequest,
                                               SADSRequest sadsRequest) {
      // Response to WebHook update.
      final SADSResponse rc = new SADSResponse();
      rc.setStatus(200);
      rc.setMimeType("application/json");
      rc.setData(new byte[] {});
      return rc;
    }

    @Override
    protected SADSResponse buildQueueErrorResponse(Exception e,
                                                   StoredHttpRequest httpServletRequest,
                                                   SADSRequest sadsRequest) {
      final SADSResponse rc = new SADSResponse();
      rc.setStatus(500);
      rc.setMimeType("application/json");
      rc.setData(new byte[] {});
      return rc;
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
      // Chat ID
      final Update update = getClient().readUpdate(req.getContent());
      return String.valueOf(update.getMessage().getChat().getId());
    }

    @Override
    protected String getServiceId(StoredHttpRequest req) throws Exception {
      // Extract service ID as a part of registered WebHook URL.
      final String[] parts = req.getRequestURI().split("/");
      return parts[parts.length - 2];
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
      // All the processing
      // TODO
      return super.getRequestUri(config, subscriberId, message);
    }

    @Override
    protected SADSResponse getSavedSADSResponse(StoredHttpRequest httpServletRequest) {
      // Always NULL.
      return null;
    }

    @Override
    protected SADSResponse getOuterResponse(StoredHttpRequest httpServletRequest,
                                            SADSRequest request,
                                            SADSResponse response) {

      // Stuff to push to the user.
      // TODO
      return null;
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

    private Log getLog(StoredHttpRequest req) {
      try{
        return SADSLogger.getLogger(getServiceId(req), getSubscriberId(req), getClass());

      } catch (Exception e) {
        return getLogger();
      }
    }
  }

}
