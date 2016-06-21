package com.eyelinecom.whoisd.sads2.telegram.mock

class Main {

  static void main(String[] args) {
    def confs = [
        new Conf(nActors: 1)
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
