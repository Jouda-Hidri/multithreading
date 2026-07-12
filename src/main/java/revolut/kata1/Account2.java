package revolut.kata1;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Account2 {
    long balance = 0L;
    private final List<String> statements = new ArrayList<>();
    final Lock lock = new ReentrantLock();

    public void deposit(long amount) {
        appendStatements(amount);
    }

    public void withdraw(long amount) {
        appendStatements(-amount);
    }

    private void appendStatements(Long amount) {
        lock.lock();
        try {
            balance+=amount;
            statements.add(
                    String.format("%s %s", LocalDateTime.now(), balance)
            );
        } finally {
            lock.unlock();
        }
    }
    public String printStatement() {
        lock.lock();
        try {
            return String.join("\n", statements);
        } finally {
            lock.unlock();
        }
    }


}
