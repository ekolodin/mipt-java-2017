package ru.mipt.java2017.hw2;

import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.io.IOException;

/**
 * This class takes the segment from client and tries to calculate sum of primes.
 */

public class Server {

  private static final Logger logger = LoggerFactory.getLogger("Server");
  private io.grpc.Server server;

  public static void main(String[] args) throws IOException, InterruptedException {
    final Server server = new Server();
    server.start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
    server.blockUntilShutdown();
  }

  private void start(int numberOfThreads, int port) throws IOException {
    server = ServerBuilder.forPort(port)
        .addService(new PrimeSumCalcImpl(numberOfThreads))
        .build()
        .start();
    logger.info("Server started, listening on " + port);
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      System.err.println("*** shutting down gRPC server since JVM is shutting down");
      Server.this.stop();
      System.err.println("*** server shut down");
    }));
  }

  private void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  static class PrimeSumCalcImpl extends PrimeSumCalcGrpc.PrimeSumCalcImplBase {

    private final PrimeChecker primeChecker;
    private final int numberOfThreads;
    PrimeSumCalcImpl(int numberOfThreads) {
      this.primeChecker = new PrimeChecker(numberOfThreads);
      this.numberOfThreads = numberOfThreads;
    }

    @Override
    public void solve(Range range, StreamObserver<Answer> responseObserver) {
      long Sum = 0;
      long Length = (range.getRight() - range.getLeft() + 1) / numberOfThreads;
      long currentRight = range.getLeft() + Length, currentLeft = range.getLeft();

      if (range.getRight() - range.getLeft() + 1 > 0) {
        List<Future<Long>> promises = new ArrayList<>(numberOfThreads);
        for (int i = 0; i < numberOfThreads; ++i) {
          if (i == numberOfThreads - 1) {
            currentRight = range.getRight();
          }
          Future<Long> promise = primeChecker.isPrimePromise(currentLeft, currentRight);
          promises.add(promise);
          currentLeft = currentRight + 1;
          currentRight = currentRight + Length;
        }

        for (int i = 0; i < numberOfThreads; ++i) {
          Future<Long> promise = promises.get(i);
          try {
            Sum += promise.get();
          } catch (InterruptedException | ExecutionException exception) {
            logger.error("Error in ", exception.getCause());
          }
        }
      }

      Answer sum = Answer.newBuilder().setSum(Sum).build();
      responseObserver.onNext(sum);
      responseObserver.onCompleted();
    }
  }
}
