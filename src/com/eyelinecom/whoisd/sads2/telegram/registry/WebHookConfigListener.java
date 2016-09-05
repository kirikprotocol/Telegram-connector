package com.eyelinecom.whoisd.sads2.telegram.registry;

import com.eyelinecom.whoisd.sads2.common.InitUtils;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.exception.ConfigurationException;
import com.eyelinecom.whoisd.sads2.registry.Config;
import com.eyelinecom.whoisd.sads2.registry.ServiceConfig;
import com.eyelinecom.whoisd.sads2.registry.ServiceConfigListener;
import com.eyelinecom.whoisd.sads2.resource.ResourceFactory;
import com.eyelinecom.whoisd.sads2.telegram.TelegramApiException;
import com.eyelinecom.whoisd.sads2.telegram.resource.TelegramApi;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("unused")
public class WebHookConfigListener extends ServiceConfigListener {

  public static final String CONF_TOKEN = "telegram.token";
  public static final String CONF_REGISTER_WEBHOOK = "telegram.register.webhook";

  private final Logger logger = Logger.getLogger(WebHookConfigListener.class);

  private final Map<String, String> serviceId2Token = new HashMap<>();
  private TelegramApi client;

  public WebHookConfigListener(TelegramApi client) {
    this.client = client;
  }

  @Override
  protected void process(Config config) throws ConfigurationException {
    final String serviceId = config.getId();

    if (config.isEmpty()) {
      unRegisterWebHook((ServiceConfig) config, serviceId);

    } else if (config instanceof ServiceConfig) {
      final ServiceConfig serviceConfig = (ServiceConfig) config;

      String token = InitUtils.getString(CONF_TOKEN, null, serviceConfig.getAttributes());
      token = StringUtils.trimToNull(token);

      if (token == null) {
        unRegisterWebHook(serviceConfig, serviceId);

      } else {
        if (!token.equals(serviceId2Token.get(serviceId))) {
          // Token changed, unregister previous one first.
          unRegisterWebHook(serviceConfig, serviceId);
        }
        registerWebHook(serviceConfig, serviceId, token);
      }
    }
  }

  private boolean shouldRegisterWebhook(ServiceConfig config) {
    return InitUtils.getBoolean(CONF_REGISTER_WEBHOOK, true, config.getAttributes());
  }

  private void registerWebHook(ServiceConfig config,
                               String serviceId,
                               String token) throws ConfigurationException {

    try {
      if (shouldRegisterWebhook(config)) {
        client.registerWebHook(token, client.getServiceUrl(serviceId, token));
      }
      serviceId2Token.put(serviceId, token);

    } catch (TelegramApiException e) {
      throw new ConfigurationException(serviceId, e.getMessage());
    }
  }

  private void unRegisterWebHook(ServiceConfig config,
                                 String serviceId) throws ConfigurationException {

    final String token = serviceId2Token.get(serviceId);
    if (token != null) {
      if (shouldRegisterWebhook(config)) {
        try {
          client.unRegisterWebHook(token);

        } catch (TelegramApiException e) {
          logger.warn("Failed to unbind webhook:" +
              " serviceId = [" + serviceId + "]: " + e.getMessage());
        }
      }

      serviceId2Token.remove(serviceId);
    }
  }

  public String getToken(String serviceId) {
    return serviceId2Token.get(checkNotNull(serviceId));
  }


  @SuppressWarnings("unused")
  public static class Factory implements ResourceFactory {

    @Override
    public WebHookConfigListener build(String id, Properties properties, HierarchicalConfiguration config) throws Exception {
      TelegramApi api = SADSInitUtils.getResource("telegram-api", properties);
      return new WebHookConfigListener(api);
    }

    @Override
    public boolean isHeavyResource() {
      return false;
    }
  }
}
