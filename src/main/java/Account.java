package main.java;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe Bank Account Transfer
 * */

public class Account {
    private final ReentrantLock lock = new ReentrantLock();
    private int balance;

    public Account(int balance) {
        this.balance = balance;
    }

    public void debit(int amount) {
        balance -= amount;
    }

    public void credit(int amount) {
        balance += amount;
    }

    public int getBalance() {
        return balance;
    }

    public ReentrantLock getLock() {
        return lock;
    }

    public static void transfer(Account from, Account to, int amount) {
        Account first = from.hashCode() < to.hashCode() ? from : to;
        Account second = from.hashCode() < to.hashCode() ? to : from;

        first.getLock().lock();
        second.getLock().lock();
        try {
            if (from.getBalance() >= amount) {
                from.debit(amount);
                to.credit(amount);
            }
        } finally {
            second.getLock().unlock();
            first.getLock().unlock();
        }
    }
}

