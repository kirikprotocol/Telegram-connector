package com.eyelinecom.whoisd.sads2.telegram.mock

import co.paralleluniverse.common.util.Exceptions
import co.paralleluniverse.fibers.Fiber
import co.paralleluniverse.fibers.SuspendExecution
import co.paralleluniverse.fibers.httpclient.FiberHttpClient
import co.paralleluniverse.strands.SuspendableCallable
import co.paralleluniverse.strands.SuspendableRunnable
import co.paralleluniverse.strands.Timeout
import co.paralleluniverse.strands.channels.Channel
import co.paralleluniverse.strands.channels.Channels
import co.paralleluniverse.strands.channels.SendPort
import com.pinterest.jbender.events.TimingEvent
import com.pinterest.jbender.executors.RequestExecutor
import com.pinterest.jbender.executors.Validator
import com.pinterest.jbender.intervals.ConstantIntervalGenerator
import com.pinterest.jbender.intervals.IntervalGenerator
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.apache.http.client.HttpRequestRetryHandler
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpRequestBase
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor
import org.apache.http.impl.nio.reactor.IOReactorConfig
import org.apache.http.nio.reactor.IOReactorException
import org.apache.http.protocol.HttpContext

import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

import static com.eyelinecom.whoisd.sads2.telegram.mock.TgMock.HttpPostImpl
import static com.pinterest.jbender.JBender.loadTestThroughput

//@CompileStatic
@PackageScope
class FiberClient {

  @CompileStatic
  static class ValidatorImpl implements Validator<CloseableHttpResponse> {

    @Override
    void validate(CloseableHttpResponse res) {
      if (res == null) {
        throw new AssertionError('Response is null' as Object)
      }

      final int status = res.getStatusLine().getStatusCode()
      if (status != 200) {
        throw new AssertionError("Status is $status" as Object)
      }
    }
  }


  //
  //
  //

  static interface RequestProducer {
    HttpPostImpl produce(int idx) throws SuspendExecution
  }

  static interface ProgressTracker {
    void onExecuted(int idx) throws SuspendExecution
  }


  //
  //
  //

  @CompileStatic
  static class FiberApacheHttpClientRequestExecutor<X extends HttpUriRequest>
      implements RequestExecutor<X, CloseableHttpResponse>, AutoCloseable {

    final Validator<CloseableHttpResponse> validator
    final FiberHttpClient client

    FiberApacheHttpClientRequestExecutor(Validator<CloseableHttpResponse> resValidator,
                                         int maxConnections) throws IOReactorException {

      final ioReactor = new DefaultConnectingIOReactor(
          IOReactorConfig.custom()
              .setConnectTimeout(0)
              .setIoThreadCount(Runtime.getRuntime().availableProcessors())
              .setSoTimeout(0)
              .build()
      )

      final mngr = new PoolingNHttpClientConnectionManager(ioReactor)
      mngr.defaultMaxPerRoute = maxConnections
      mngr.maxTotal = maxConnections
      final ahc = HttpAsyncClientBuilder.create()
          .setConnectionManager(mngr)
          .setDefaultRequestConfig(
            RequestConfig.custom().setLocalAddress((InetAddress)null).build()
          )
          .build()

      this.client = new FiberHttpClient(ahc, new HttpRequestRetryHandler() {
        @Override
        boolean retryRequest(IOException exception,
                             int executionCount,
                             HttpContext context) {
          true
        }
      })

      this.validator = resValidator
    }

    @Override
    public CloseableHttpResponse execute(long nanoTime,
                                         HttpUriRequest request) throws SuspendExecution, InterruptedException {
      CloseableHttpResponse ret

      try {
        ret = new Fiber(new FiberExecutor(client, request))
            .start()
            .get() as CloseableHttpResponse

      } catch (ExecutionException e) {
        throw Exceptions.rethrowUnwrap(e)
      }

      validator?.validate ret

      ret
    }

    @CompileStatic
    static class FiberExecutor implements SuspendableCallable {
      final FiberHttpClient client
      final HttpUriRequest request

      FiberExecutor(FiberHttpClient client, HttpUriRequest request) {
        this.client = client
        this.request = request
      }

      @Override
      Object run() throws SuspendExecution, InterruptedException {
        try {
          return client.execute(request)

        } catch (IOException e) {
          throw Exceptions.rethrowUnwrap(e)
        }
      }
    }

    void close() throws IOException { client.close() }
  }

  @CompileStatic
  static class ExecutorImpl extends FiberApacheHttpClientRequestExecutor<HttpPostImpl> {
    ProgressTracker tracker

    ExecutorImpl() throws IOReactorException {
      super(new ValidatorImpl(), 1_000_000)
    }

    @Override
    CloseableHttpResponse execute(long nanoTime,
                                  HttpUriRequest request) throws SuspendExecution, InterruptedException {
      final reqIdx = (request as HttpPostImpl).reqIdx

      final rc = super.execute(nanoTime, request)
      tracker.onExecuted(reqIdx)
      return rc
    }
  }

  @SuppressWarnings('GrMethodMayBeStatic')
  void run(int targetRps, int totalRequests, RequestProducer producer, ProgressTracker tracker) {
    final intervalGenerator = new ConstantIntervalGenerator(
        (TimeUnit.SECONDS.toNanos(1) / targetRps).intValue()
    )

    final requestExecutor = new ExecutorImpl(tracker: tracker)

    try {
      final Channel<HttpRequestBase> requestCh = Channels.newChannel(1000)

      new Fiber<Void>(
          'req-gen',
          new ProducerTask(producer: producer, totalRequests: totalRequests, requestCh: requestCh)
      ).start()

      new Fiber<Void>(
          "client",
          new ClientTask(
              intervalGenerator: intervalGenerator,
              requestCh: requestCh,
              executor: requestExecutor)
      ).start().join()

    } finally {
      requestExecutor.close()
    }
  }

  @CompileStatic
  static class ClientTask implements SuspendableRunnable {

    IntervalGenerator intervalGenerator
    Channel<HttpRequestBase> requestCh
    RequestExecutor executor

    @Override
    void run() throws SuspendExecution, InterruptedException {
      loadTestThroughput(
          intervalGenerator,
          0,
          requestCh,
          executor,
          new NullSendPort<TimingEvent<CloseableHttpResponse>>()
      )
    }
  }

  @CompileStatic
  static class ProducerTask implements SuspendableRunnable {
    RequestProducer producer
    int totalRequests
    Channel<HttpRequestBase> requestCh

    @Override
    void run() throws SuspendExecution {
      for (int i = 0; i < totalRequests; i++) {
//        System.out.println "Produced: $i"
        requestCh.send(producer.produce(i))
      }
      requestCh.close()
    }
  }

  @CompileStatic
  private static class NullSendPort<Message> implements SendPort<Message> {
    @Override void send(Message timingEvent) { }
    @Override boolean send(Message timingEvent, long timeout, TimeUnit unit) { true }
    @Override boolean send(Message timingEvent, Timeout timeout) { true }
    @Override boolean trySend(Message timingEvent) { true }
    @Override void close() { }
    @Override void close(Throwable t) { }
  }
}