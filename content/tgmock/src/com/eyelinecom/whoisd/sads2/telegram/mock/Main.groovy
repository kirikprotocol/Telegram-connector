package com.eyelinecom.whoisd.sads2.telegram.mock

class Main {

  static void main(String[] args) {

    if (args) {
      final cli = new CliBuilder()

      cli.r(longOpt: 'rps', required: true, 'Client RPS', type: int, numberOfArgs: 1)
      cli.n(longOpt: 'messages', required: true, 'Total number of messages', type: int, numberOfArgs: 1)
      cli.w(longOpt: 'warmup', required: true, 'Number of warmup messages', type: int, numberOfArgs: 1)
      
      final opts = cli.parse(args)
      if (!opts) {
        return
      }

      if (opts.h) {
        cli.usage()
        return
      }

      new TgMock(
          new Conf(
              targetRps:        opts.r.toInteger(),
              warmupMessages:   opts.w.toInteger(),
              totalMessages:    opts.n.toInteger()
          )).with { _ ->

        try {
          _.run()
        } finally {
          _.stop()
        }
      }

    } else {
      final confs = [
          // Warmup round with a small number of messages.
          new Conf(targetRps: 20, warmupMessages: 100, totalMessages: 200),

          new Conf(targetRps: 10, totalMessages: 3000),
          new Conf(targetRps: 20),
          new Conf(targetRps: 30),
          new Conf(targetRps: 40),
          new Conf(targetRps: 50),
          new Conf(targetRps: 70),
          new Conf(targetRps: 100),
          new Conf(targetRps: 200),
          new Conf(targetRps: 300),
          new Conf(targetRps: 400),
          new Conf(targetRps: 1000),
          new Conf(targetRps: 1000),
          new Conf(targetRps: 10_000)
      ]

      confs.each { conf ->
        new TgMock(conf).with { _ ->
          try {
            _.run()
          } finally {
            _.stop()
          }
        }
      }
    }

  }

}
