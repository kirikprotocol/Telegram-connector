package com.eyelinecom.whoisd.sads2.telegram.mock

import groovy.json.JsonSlurper
import groovy.sql.Sql
import org.apache.http.client.entity.EntityBuilder
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.RequestBuilder

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

import static FiberClient.*

class TgMock implements Logger {

  /** chatId -> sent millis */
  private final ConcurrentHashMap<Long, Long> requests = new ConcurrentHashMap<>()

  private final Set<Long> results = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>())

  private final Conf conf

  private final FiberClient client
  private final NettyHttpServer server

  TgMock(Conf conf) {
    this.conf = conf

    client = new FiberClient()
    server = new NettyHttpServer(conf.mockPort, new NettyHttpServer.RequestHandler() {
      @Override
      String handle(String content) {
        final obj = new JsonSlurper().parseText(content)

        if (obj.method == 'sendChatAction') {
          // Ignore

        } else {
          final chatId = obj.chat_id

          final sent = requests.remove chatId.toLong()
          if (sent == null) {
            throw new RuntimeException()
          }
          results << (System.currentTimeMillis() - sent)

          _debug "  << $chatId"
        }

        '''
        {
          "ok": true,
          "result": {}
        }
        '''
      }
    })

    Thread.start('server') {
      server.await()
    }

    printInit conf.logFile

    _printHeader "Initialized"
    _print "Target RPS = $conf.targetRps"
    _print "Messages = $conf.totalMessages, of them warmup = $conf.warmupMessages"
    _print ""
  }

  void run() {
    ({
      _print "Running cold"

      _print "Scheduling $conf.totalMessages messages"

      schedule().join()

      await conf.totalMessages
      reportResults()

      results.clear()
    })()

    _printLine()

    ({
      _print " Running hot "

      _print "Scheduling $conf.totalMessages messages"

      schedule().join()

      await conf.totalMessages
      reportResults()

      results.clear()
    })()
  }

  void reportResults() {
    def actual = conf.totalMessages - conf.warmupMessages

    _print "+ Target RPS, client: $conf.targetRps"
    _print "+ Actual RPS, client: ${(endMillis - startMillis) / actual}"
    _print "+ Messages: ${actual}"

    final avg = results.sum() / results.size()
    _print "+ AVG time, ms: $avg"
    _print "+ MIN time, ms: ${results.min()}"
    _print "+ MAX time, ms: ${results.max()}"
  }

  int idx2ChatId(int idx) {
    -(idx + 1)
  }

  int chatId2Idx(int chatId) {
    -(chatId - 1)
  }

  volatile long startMillis
  volatile long endMillis

  Thread schedule() {
    Thread.start 'sender', {
      client.run(
          conf.targetRps,
          conf.totalMessages,

          new RequestProducer() {
            @Override
            Request produce(int idx) {
              final chatId = idx2ChatId idx
              final msg = """{
                "update_id":657656097,
                "message":{
                  "message_id":38,
                  "from":{"id":$chatId,"first_name":"tester","username":"Tester"},
                  "chat":{"id":$chatId,"first_name":"tester","username":"Tester","type":"private"},
                  "date":1455522117,
                  "text":"\\/start"
                  }
                }"""

              final req = RequestBuilder
                  .post()
                  .setUri(conf.webhookUri)
                  .setEntity(EntityBuilder.create().setText(msg).build())
                  .build()

              new Request(req as HttpPost, idx)
            }
          },

          new ProgressTracker() {
            @Override
            void onExecuted(int idx) {
              if (idx > conf.warmupMessages) {
                if (startMillis == 0) {
                  startMillis = System.currentTimeMillis()
                }

                requests.put idx2ChatId(idx), System.currentTimeMillis()

                endMillis = System.currentTimeMillis()
              }
            }
          }
      )
    }
  }

  void stop() {
    _printHeader "Clearing DB"

    final sql = Sql.newInstance(conf.dbUrl, conf.dbUser, conf.dbPassword, 'com.mysql.jdbc.Driver')
    try {
      sql.execute '''
        START TRANSACTION;
        CALL sample_clear_profiles('test-%');
        COMMIT;
      '''
    } finally {
      sql.close()
    }

    _print "DB cleared"

    client.stop()

    actors.shutdown()
    actors.awaitTermination(1, TimeUnit.MINUTES)

    server.stop()
  }

  void await(int nRequests) {
    _print " Awaiting for $nRequests responses to arrive..."

    while (true) {
      Thread.sleep(TimeUnit.SECONDS.toMillis(1))

      final size = requests.size()

      if (size != 0) {
        _prints " $size"
      } else {
        _print ''
        return
      }
    }

  }

}
