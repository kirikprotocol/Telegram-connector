package com.eyelinecom.whoisd.sads2.telegram.mock

class Conf {

  String logFile = '/tmp/tg-mock.log'

  int nActors

  String token = '123:MyToken'
  String service = 'eyeline.demo'

  // Mobilizer address
  String webhookHost = '192.168.2.111'
  int webhookPort = 8080
  String webhookPath = "/telegram/$service/$token"

  // Where TG mock binds to
  int mockPort = 9090

  int messagesPerActor = 10
  int warmupMessagesPerActor = 1

}
