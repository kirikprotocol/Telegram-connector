package com.eyelinecom.whoisd.sads2.telegram.mock

class Conf {

  String dbUrl = 'jdbc:mysql://192.168.2.112/profile_storage?allowMultiQueries=true'
  String dbUser = 'tester'
  String dbPassword = 'tester'

  String logFile = '/tmp/tg-mock.log'

  int targetRps = 10
  int warmupMessages = 1000
  int totalMessages = warmupMessages + 1000

  String token = '123:MyToken'
  String service = 'eyeline.demo'

  // Mobilizer address
  String webhookUri = "http://192.168.2.111:8080/telegram/$service/$token"

  // Where TG mock binds to
  int mockPort = 9090

}
