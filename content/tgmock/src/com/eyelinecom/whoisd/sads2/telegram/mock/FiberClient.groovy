package com.eyelinecom.whoisd.sads2.telegram.mock

import co.paralleluniverse.fibers.Fiber
import co.paralleluniverse.strands.SuspendableRunnable
import co.paralleluniverse.strands.Timeout
import co.paralleluniverse.strands.channels.Channel
import co.paralleluniverse.strands.channels.Channels
import co.paralleluniverse.strands.channels.SendPort
import com.pinterest.jbender.events.TimingEvent
import com.pinterest.jbender.executors.Validator
import com.pinterest.jbender.executors.http.FiberApacheHttpClientRequestExecutor
import com.pinterest.jbender.intervals.ConstantIntervalGenerator
import com.pinterest.jbender.intervals.IntervalGenerator
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.methods.HttpRequestBase

import java.util.concurrent.TimeUnit

import static com.pinterest.jbender.JBender.loadTestThroughput

public class FiberClient {

  private final Validator<CloseableHttpResponse> validator = new Validator<CloseableHttpResponse>() {
    @Override
    void validate(CloseableHttpResponse res) {
      if (res == null) {
        throw new AssertionError("Response is null")
      }

      final int status = res.getStatusLine().getStatusCode()
      if (status != 200) {
        throw new AssertionError("Status is " + status)
      }
      println 'ok'
    }
  }


  //
  //
  //

  static class Request extends HttpPost {
    @Delegate(interfaces = true)
    final HttpPost delegate
    final int idx

    Request(HttpPost delegate, int idx) {
      this.delegate = delegate
      this.idx = idx
    }
  }

  static interface RequestProducer {
    Request produce(int idx)
  }

  static interface ProgressTracker {
    void onExecuted(int idx)
  }


  //
  //
  //

  void run(int targetRps, int totalRequests, RequestProducer producer, ProgressTracker tracker) {
    final IntervalGenerator intervalGenerator = new ConstantIntervalGenerator(
        (TimeUnit.SECONDS.toNanos(1) / targetRps).intValue()
    )

    final requestExecutor =
        new FiberApacheHttpClientRequestExecutor<Request>(validator, 1_000_000) {
          @Override
          CloseableHttpResponse execute(long nanoTime, Request request) {
            final rc = super.execute(nanoTime, request)
            tracker.onExecuted request.idx
            return rc
          }
        }

    try {
      final Channel<HttpRequestBase> requestCh = Channels.newChannel(1000)

      new Fiber<Void>("req-gen", new SuspendableRunnable() {
        @Override
        void run() {
          (0..totalRequests).each { requestCh.send(producer.produce(it)) }
          requestCh.close()
        }
      }).start()

      new Fiber<Void>("client", new SuspendableRunnable() {
        @Override
        void run() {
          loadTestThroughput(
              intervalGenerator,
              0,
              requestCh,
              requestExecutor,
              new NullSendPort<TimingEvent<CloseableHttpResponse>>()
          )
        }
      }).start().join()

    } finally {
      requestExecutor.close()
    }
  }

  private static class NullSendPort<Message> implements SendPort<Message> {
    @Override void send(Message timingEvent) { }
    @Override boolean send(Message timingEvent, long timeout, TimeUnit unit) { true }
    @Override boolean send(Message timingEvent, Timeout timeout) { true }
    @Override boolean trySend(Message timingEvent) { true }
    @Override void close() { }
    @Override void close(Throwable t) { }
  }
}