package com.eyelinecom.whoisd.sads2.telegram.mock

import groovy.json.JsonSlurper

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit

class TgMock implements Logger {

  /** chatId -> sent millis */
  private final ConcurrentHashMap<Long, Long> requests = new ConcurrentHashMap<>()

  private final Set<Long> results = Collections.newSetFromMap(new ConcurrentHashMap<Long, Boolean>())

  private final Conf conf

  private final ExecutorService actors

  private final NettyHttpClient client
  private final NettyHttpServer server

  TgMock(Conf conf) {
    this.conf = conf
    this.actors = Executors.newFixedThreadPool(
        conf.nActors,
        new ThreadFactory() {
          private volatile int ct = 0
          @Override Thread newThread(Runnable r) {
            new Thread(r, "actor-${++ct}").with {
              daemon = true; it
            }
          }
        }
    )

    client = new NettyHttpClient(conf.nActors, actors)
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

          _print "  << $chatId"
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
    _print "Actors = $conf.nActors"
    _print "Messages per actor, average = $conf.messagesPerActor"
    _print "Warmup messages per actor, average = $conf.warmupMessagesPerActor"
    _print ""
  }

  void run() {
    _printHeader "Warming up"

    int warmupCount = conf.nActors * conf.warmupMessagesPerActor

    _print "Scheduling $warmupCount messages"
    schedule(0..warmupCount)

    await warmupCount
    results.clear()

    _printHeader "Running"

    final count = conf.nActors * conf.messagesPerActor

    _print "Scheduling $count messages"
    schedule((warmupCount + 1)..(warmupCount + 1 + count))

    await count
    reportResults count

    results.clear()
  }

  void reportResults(int nActors) {
    _print "+ Actors: $nActors"

    final avg = results.sum() / results.size()
    _print "+ Response time: $avg"
  }

  void schedule(Range<Integer> chatIds) {
    chatIds.each { chatId -> actors.execute { send chatId } }
  }

  void stop() {
    client.stop()

    actors.shutdown()
    actors.awaitTermination(1, TimeUnit.MINUTES)

    server.stop()
  }

  void await(int nRequests) {
    _print "Awaiting for $nRequests responses to arrive"

    while (true) {
      Thread.sleep(TimeUnit.SECONDS.toMillis(5))

      final size = results.size()

      if (size < nRequests)   _print "  Got $size"
      else                    return
    }
  }

  void send(Long chatId) {
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

    requests.put(chatId, System.currentTimeMillis())
    client.request(conf.webhookHost, conf.webhookPort, conf.webhookPath, msg)

    _print "  >> $chatId"
  }

}
