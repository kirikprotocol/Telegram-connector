package com.eyelinecom.whoisd.sads2.telegram.util;

public abstract class RateLimiter {

  public abstract void acquire();

  /**
   * @param qps Upper limit of queries per second.
   *            No limit is imposed if the value is &le; 0,
   */
  public static RateLimiter create(final double qps) {
    return (qps <= 0) ?

        new RateLimiter() {
          @Override
          public void acquire() { /* Nothing here */ }
        }

        :

        new RateLimiter() {
          private final com.google.common.util.concurrent.RateLimiter impl =
              com.google.common.util.concurrent.RateLimiter.create(qps);

          @Override
          public void acquire() { impl.acquire(); }
        };
  }

}
