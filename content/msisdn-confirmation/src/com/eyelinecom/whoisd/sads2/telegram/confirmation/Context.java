package com.eyelinecom.whoisd.sads2.telegram.confirmation;

import com.eyelinecom.whoisd.sads2.wstorage.profile.ProfileStorage;
import com.eyelinecom.whoisd.sads2.wstorage.resource.DbProfileStorage;

import java.util.Properties;

public class Context {

  private DbProfileStorage profileStorage;

  public static Context getInstance() {
    return Holder.INSTANCE.self;
  }

  void init() throws Exception {
    final Properties storageProperties = new Properties();
    storageProperties.load(getClass().getResourceAsStream("/profile_storage.properties"));

    profileStorage = (DbProfileStorage) new DbProfileStorage.Factory().build(
        null, storageProperties, null
    );
  }

  void destroy() {
    profileStorage.destroy();
  }

  public ProfileStorage getProfileStorage() {
    return profileStorage;
  }


  //
  //
  //

  private enum Holder {
    INSTANCE(new Context());
    final Context self;

    Holder(Context context) { this.self = context; }
  }
}


