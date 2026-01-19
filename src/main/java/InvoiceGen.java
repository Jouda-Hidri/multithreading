package main.java;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class InvoiceGen {

    private Set<Transaction> transactions = ConcurrentHashMap.newKeySet();
    private Set<Invoice> invoices = ConcurrentHashMap.newKeySet();
    private AtomicLong transactionId = new AtomicLong();
    private AtomicLong invoiceId = new AtomicLong();

    public void pay(String userId, BigDecimal amount) {
        transactions.add(new Transaction(transactionId.incrementAndGet(), userId, amount));
    }

    public synchronized void getOrCreate(String userId) {
        Set<Long> existing = invoices.stream()
                .map(i -> i.transactionId)
                .collect(Collectors.toSet());

        List<Invoice> created = transactions.stream()
                .filter(t -> t.getUserId().equals(userId))
                .filter(t -> !existing.contains(t.transactionId))
                .map(t -> new Invoice(invoiceId.incrementAndGet(), t.getTransactionId()))
                .toList();

        invoices.addAll(created);
    }

    public AtomicLong getTransactionId() {
        return transactionId;
    }

    public AtomicLong getInvoiceId() {
        return invoiceId;
    }

    public class Transaction {
        private Long transactionId;
        private String userId;
        private BigDecimal amount;

        public Transaction(Long transactionId, String userId, BigDecimal amount) { // TODO nonnul
            this.transactionId = transactionId;
            this.userId = userId;
            this.amount = amount;
        }

        public Long getTransactionId() {
            return transactionId;
        }

        public String getUserId() {
            return userId;
        }

    }

    public class Invoice {
        private Long invoiceId;
        private Long transactionId;

        public Invoice(Long invoiceId, Long transactionId) {
            this.invoiceId = invoiceId;
            this.transactionId = transactionId;
        }

        public Long getTransactionId() {
            return transactionId;
        }
    }

}
