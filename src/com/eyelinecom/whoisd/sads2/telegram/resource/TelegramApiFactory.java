package com.eyelinecom.whoisd.sads2.telegram.resource;

import com.eyelinecom.whoisd.sads2.common.HttpDataLoader;
import com.eyelinecom.whoisd.sads2.common.SADSInitUtils;
import com.eyelinecom.whoisd.sads2.resource.ResourceFactory;
import org.apache.commons.configuration.HierarchicalConfiguration;

import java.util.Properties;

public class TelegramApiFactory implements ResourceFactory {

  @Override
  public TelegramApi build(String id,
                           Properties properties,
                           HierarchicalConfiguration config) throws Exception {

    final HttpDataLoader loader =
        (HttpDataLoader) SADSInitUtils.getResource("loader", properties);

    final String publicKey =
        config.getString("pubkey").replaceAll("\n\\s+", "\n");


    return new TelegramApiImpl(loader, publicKey, properties);
  }

  @Override public boolean isHeavyResource() { return false; }
}
