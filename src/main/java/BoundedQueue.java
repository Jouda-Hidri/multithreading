package main.java;

import java.util.concurrent.locks.ReentrantLock;
import java.util.LinkedList;


/**
 *  Bounded Blocking Queue
 * */
public class BoundedQueue<T> {
    private final int maxSize;
    private final LinkedList<T> queue = new LinkedList<>();
    private final ReentrantLock lock = new ReentrantLock();

    public BoundedQueue(int maxSize) {
        this.maxSize = maxSize;
    }

    public boolean enqueue(T item) {
        lock.lock();
        try {
            if (queue.size() < maxSize) {
                queue.add(item);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public T dequeue() {
        lock.lock();
        try {
            return queue.isEmpty() ? null : queue.removeFirst();
        } finally {
            lock.unlock();
        }
    }
}

