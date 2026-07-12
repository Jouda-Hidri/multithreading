package revolut.kata1;

import java.time.LocalDateTime;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Account {

    private Long balance = 0L;
    private String statement = new String();
    private final Lock lock = new ReentrantLock();


    public void deposit(long amount) {
        appendStatement(amount);
    }

    public void withdraw(int amount) {
        appendStatement(-amount);
    }

    private void appendStatement(long amount) {
        lock.lock();
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(statement);
            sb.append("\n");
            sb.append(LocalDateTime.now());
            balance += amount;
            sb.append(balance);
            statement = sb.toString();
        } finally {
            lock.unlock();
        }
    }

    String printStatement() {
        return statement;
    }

}
