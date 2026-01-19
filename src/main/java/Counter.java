package main.java;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe Counter
 * */
public class Counter {
    private final AtomicInteger count = new AtomicInteger(0);

    public void increment() {
        count.incrementAndGet();
    }

    public void reset() {
        count.set(0);
    }

    public int get() {
        return count.get();
    }
}

