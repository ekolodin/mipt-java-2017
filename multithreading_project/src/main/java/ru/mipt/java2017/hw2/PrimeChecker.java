package ru.mipt.java2017.hw2;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * this class parallelizes calculating sum of primes on the given segment of numbers.
 */

class PrimeChecker {

  private ExecutorService executorService;

  PrimeChecker(int numberOfThreads) {
    this.executorService = Executors.newFixedThreadPool(numberOfThreads);
  }

  /**
   * Checks if the number is prime.
   */
  private boolean isPrime(long number) {
    if (number == 1) {
      return false;
    }
    if (number == 2 || number == 3) {
      return true;
    }
    for (int test = 2; test * test <= number; ++test) {
      if (number % test == 0) {
        return false;
      }
    }
    return true;
  }

  private Long SumOfPrimes(long left, long right) {
    long Sum = 0;
    for (long i = left; i <= right; ++i) {
      if (isPrime(i)) {
        Sum += i;
      }
    }
    return Sum;
  }

  Future<Long> isPrimePromise(final long left, final long right) {
    return executorService.submit(() -> SumOfPrimes(left, right));
  }
}
