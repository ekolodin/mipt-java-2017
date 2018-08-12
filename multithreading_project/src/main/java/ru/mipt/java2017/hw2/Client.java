package ru.mipt.java2017.hw2;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.TimeUnit;

/**
 * This class "implements" the client interface, distributing equal segments of numbers to the
 * servers.
 */

public class Client {

  private static final Logger logger = LoggerFactory.getLogger("Client");
  private final ArrayList<ManagedChannel> channel;
  private final ArrayList<PrimeSumCalcGrpc.PrimeSumCalcStub> stub;
  private ArrayList<Boolean> serverIsAlive;

  private Client(ArrayList<String> host, ArrayList<Integer> port) throws InterruptedException {
    channel = new ArrayList<>(host.size());
    stub = new ArrayList<>(port.size());
    serverIsAlive = new ArrayList<>(port.size());
    for (int i = 0; i < host.size(); ++i) {
      channel.add(
          ManagedChannelBuilder.forAddress(host.get(i), port.get(i)).usePlaintext(true).build());
      stub.add(PrimeSumCalcGrpc.newStub(channel.get(i)));
      serverIsAlive.add(Boolean.TRUE);
    }
  }

  public static void main(String[] args) throws Exception {
    int numberOfServers = (args.length - 2) / 2;
    ArrayList<String> hosts = new ArrayList<>(numberOfServers);
    ArrayList<Integer> ports = new ArrayList<>(numberOfServers);
    for (int i = 2; i < args.length; i += 2) {
      hosts.add(args[i]);
      ports.add(Integer.parseInt(args[i + 1]));
    }
    Client client = new Client(hosts, ports);
    try {
      System.out
          .println(client
              .serversGreeter(Long.parseLong(args[0]), Long.parseLong(args[1]), numberOfServers));
    } finally {
      client.shutdown();
    }
  }

  private void shutdown() throws InterruptedException {
    for (ManagedChannel aChannel : channel) {
      aChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }

  /**
   * This function tries to connect to all working servers and to give them a segment of numbers.
   * Also keeps the list of working servers.
   */
  private long serversGreeter(final long left, final long right, final int numberOfServers)
      throws InterruptedException {
    long Length = (right - left + 1) / numberOfServers;
    long currentLeft = left, currentRight = left + Length;
    Lock resultsLock = new ReentrantLock();
    ArrayList<Long> result = new ArrayList<>(numberOfServers);

    BlockingQueue<Pair<Long, Long>> ranges = new ArrayBlockingQueue<>(numberOfServers);
    for (int i = 0; i < stub.size(); ++i) {
      if (i == stub.size() - 1) {
        currentRight = right;
      }
      ranges.put(new Pair<>(currentLeft, currentRight));
      currentLeft = currentRight + 1;
      currentRight = currentRight + Length;
    }

    while (!ranges.isEmpty()) {
      int counterOfAliveServers = 0;
      for (Boolean aServerIsAlive : serverIsAlive) {
        if (aServerIsAlive) {
          ++counterOfAliveServers;
        }
      }
      CountDownLatch finishLatch = new CountDownLatch(counterOfAliveServers);
      try {
        for (int i = 0; i < numberOfServers; ++i) {
          final int currentIndex = i;
          if (ranges.isEmpty()) {
            break;
          }
          if (serverIsAlive.get(i)) {
            Range range = Range.newBuilder().setLeft(ranges.peek().getKey())
                .setRight(ranges.peek().getValue()).build();
            ranges.take();
            stub.get(i).solve(range, new StreamObserver<Answer>() {
              @Override
              public void onNext(Answer answer) {
                resultsLock.lock();
                result.add(answer.getSum());
                resultsLock.unlock();
              }

              @Override
              public void onError(Throwable throwable) {
                serverIsAlive.set(currentIndex, Boolean.FALSE);
                Status status = Status.fromThrowable(throwable);
                try {
                  ranges.put(new Pair<>(range.getLeft(), range.getRight()));
                } catch (InterruptedException exception) {
                  logger.error("Error: " + exception.getCause());
                }
                logger.warn("Failed: " + status);
                finishLatch.countDown();
              }

              @Override
              public void onCompleted() {
                finishLatch.countDown();
              }
            });
          }
        }
        finishLatch.await();
      } catch (StatusRuntimeException exception) {
        logger.error("RPC failed: " + exception.getStatus());
      }
    }
    long ans = 0;
    for (Long aResult : result) {
      ans += aResult;
    }
    return ans;
  }
}
