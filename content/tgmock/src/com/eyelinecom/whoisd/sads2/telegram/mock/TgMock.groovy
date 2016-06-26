package com.eyelinecom.whoisd.sads2.telegram.mock

import co.paralleluniverse.fibers.SuspendExecution
import groovy.json.JsonSlurper
import groovy.sql.Sql
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import org.apache.commons.math3.stat.descriptive.rank.Percentile
import org.apache.http.client.entity.EntityBuilder
import org.apache.http.client.methods.HttpPost

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import static com.eyelinecom.whoisd.sads2.telegram.mock.FiberClient.ProgressTracker
import static com.eyelinecom.whoisd.sads2.telegram.mock.FiberClient.RequestProducer

// Note: there's some UGLY (and even uglier) code, and it's written this way FOR A REASON.
//
// Comsat java agent performs some bytecode-level tricks and doesn't handle Groovy dynamic code
// well, so some hacks and workarounds are required here and there.
class TgMock implements Logger {

  /** chatId -> sent millis */
  private final ConcurrentHashMap<Integer, Long> requests

  /** chatId -> async call duration, millis */
  private final ConcurrentHashMap<Integer, Long> results

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
          // Ignore, async response.

        } else {
          final Integer chatId = obj.chat_id.toInteger()

          _debug "<< $chatId [${Thread.currentThread().getName()}]"

          final sent = requests.remove chatId
          if (sent == null) {
            // Must be a response to a warm-up message

          } else {
            results.put(chatId, System.currentTimeMillis() - sent)
          }
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

    requests = new ConcurrentHashMap<>(conf.totalMessages, 0.75F, 32)
    results = new ConcurrentHashMap<>(conf.totalMessages, 0.75F, 32)

    _printHeader "Initialized"
    _print "Target RPS = $conf.targetRps"
    _print "Messages = $conf.totalMessages, of them warmup = $conf.warmupMessages"
    _print ""

    clearDb()
  }

  void run() {

    firstActualRequestSent = lastActualRequestSent = 0

    firstActualRequestSent = lastActualRequestSent = 0

    ({
      def start = new Date()
      _print "Running cold at ${start.format('dd.MM.yyyy HH:mm:ss')}"

      _print "Scheduling $conf.totalMessages messages"

      schedule().join()

      await()
      reportResults(start)

      results.clear()
    })()

    _printLine()

    firstActualRequestSent = lastActualRequestSent = 0

    ({
      def start = new Date()
      _print " Running hot at ${start.format('dd.MM.yyyy HH:mm:ss')}"

      _print "Scheduling $conf.totalMessages messages"

      schedule().join()

      await()
      reportResults(start)

      results.clear()
    })()
  }

  static double rps(int count, Long millis) { count / (millis / 1000.0) }

  void reportResults(Date start) {
    def actualRequests = conf.totalMessages - conf.warmupMessages
    def now = System.currentTimeMillis()

    _print "Round ends at ${new Date(now).format('dd.MM.yyyy HH:mm:ss')}"
    _print "+ Target RPS, client: $conf.targetRps"
    _print "+ Actual RPS, client [SyncResponse]:" +
        " ${rps(actualRequests, lastActualRequestSent - firstActualRequestSent)}"

    _print "+ Overall RPS [AsyncResponse]:" +
        " ${rps(actualRequests, now - firstActualRequestSent)}"
    _print "+ Messages: ${actualRequests}"
    _print "+ Results: ${results.size()}"

    final avg = results.values().sum() / results.size()

    final sortedResults = results.values().sort().toArray(new Long[0])
    float median = median(sortedResults)

    _print "+ AVG time, ms: $avg"
    _print "+ MED time, ms: $median"
    _print "+ 95%, ms: ${new Percentile().evaluate(sortedResults as double[], 95.0)}"
    _print "+ MIN time, ms: ${results.values().min()}"
    _print "+ MAX time, ms: ${results.values().max()}"
  }

  private static float median(Long[] sortedResults) {
    final mid = (sortedResults.size() / 2) as int
    final float median =
        sortedResults.size() % 2 != 0 ?
            sortedResults[mid] :
            ((sortedResults[mid] + sortedResults[mid - 1]) / 2.0)
    median
  }

  static int idx2ChatId(int idx) { -(idx + 1) }

  static int chatId2Idx(int chatId) { -(chatId - 1) }

  volatile long firstActualRequestSent
  volatile long lastActualRequestSent

  @CompileStatic
  @InheritConstructors
  static class HttpPostImpl extends HttpPost {
    int reqIdx
  }

  Thread schedule() {
    Thread.start 'sender', {
      client.run(
          conf.targetRps,
          conf.totalMessages,

          new RequestProducer() {
            @Override
            HttpPostImpl produce(int idx) throws SuspendExecution {
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

              final post = new HttpPostImpl(conf.webhookUriUri)
              post.reqIdx = idx
              post.setEntity(EntityBuilder.create().setText(msg).build())
              post
            }
          },

          new ProgressTracker() {
            private AtomicInteger cIdx = new AtomicInteger(0)

            @Override
            void onExecuted(int idx) {
              _debug ">> ${idx2ChatId(idx)}"

              if ((cIdx.incrementAndGet()) > conf.warmupMessages) {
                if (TgMock.this.firstActualRequestSent == 0L) {
                  firstActualRequestSent = System.currentTimeMillis()
                }

                def prev = requests.put idx2ChatId(idx), System.currentTimeMillis()
                if (prev) {
                  throw new RuntimeException("Duplicate key [$idx]")
                }

                TgMock.this.lastActualRequestSent = System.currentTimeMillis()
              }
            }
          }
      )
    }
  }

  void stop() {
    clearDb()

    server.stop()
  }

  private void clearDb() {
    _printHeader "Clearing DB"

    final sql = Sql.newInstance(conf.dbUrl, conf.dbUser, conf.dbPassword, 'com.mysql.jdbc.Driver')
    try {
      //noinspection SqlNoDataSourceInspection,SqlDialectInspection
      sql.execute '''
        START TRANSACTION;
        CALL sample_clear_profiles('test-%');
        COMMIT;
      '''
    } finally {
      sql.close()
    }

    _print "DB cleared"
  }

  @CompileStatic
  void await() {
    final actualMessages = conf.totalMessages - conf.warmupMessages
    _print " Awaiting for ${actualMessages} responses..."

    while (true) {
      final size = results.size()

      if (size < actualMessages) {
        _prints " $size"

      } else {
        _print ''
        return
      }

      Thread.sleep(TimeUnit.SECONDS.toMillis(1))
    }

  }

}
