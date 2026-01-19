package main.java;

import java.util.LinkedList;

/**
 * Parallel Producer-Consumer
 * */
public class BlockingQueue<T> {
    private final int capacity;
    private final LinkedList<T> queue = new LinkedList<>();

    public BlockingQueue(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void produce(T item) throws InterruptedException {
        while (queue.size() == capacity) {
            wait();
        }
        queue.add(item); // ~ addLast
        notifyAll();
    }

    public synchronized T consume() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        T item = queue.removeFirst();
        notifyAll();
        return item;
    }
}

