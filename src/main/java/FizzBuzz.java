import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

class FizzBuzz {
    private int n;
    private AtomicInteger i = new AtomicInteger(1);
    private ConcurrentLinkedDeque<String> list = new ConcurrentLinkedDeque<>();

    public FizzBuzz(int n) {
        this.n = n;

    }

    public synchronized void fizz() throws InterruptedException {
        while (i.get() <= n) {
            if (i.get() % 15 != 0 && i.get() % 3 == 0) {
                list.add("fizz");
                i.incrementAndGet();
                notifyAll();
            } else {
                wait();
            }
        }

    }

    public synchronized void buzz() throws InterruptedException {
        while (i.get() <= n) {
            if (i.get() % 15 != 0 && i.get() % 5 == 0) {
                list.add("buzz");
                i.incrementAndGet();
                notifyAll();
            } else {
                wait();
            }
        }

    }

    public synchronized void fizzbuzz() throws InterruptedException {
        while (i.get() <= n) {
            if (i.get() % 15 == 0) {
                list.add("fizzbuzz");
                i.incrementAndGet();
                notifyAll();
            } else {
                wait();
            }
        }

    }

    public synchronized void number() throws InterruptedException {
        while (i.get() <= n) {
            if (i.get() % 3 == 0 || i.get() % 5 == 0) {
                wait();
            } else {
                list.add(i.get() + "");
                i.incrementAndGet();
                notifyAll();
            }
        }

    }

    public ConcurrentLinkedDeque<String> getList() {
        return list;
    }
}