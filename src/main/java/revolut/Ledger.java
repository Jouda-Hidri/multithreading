package revolut;

import java.math.BigDecimal;
import java.util.Map;

public class Ledger {

    private final Map<Long, Account> accounts;

    public Ledger(Map<Long, Account> accounts) {
        this.accounts = accounts;
    }


    public void transfer(
            long fromId,
            long toId,
            BigDecimal amount
    ) {

        Account from = accounts.get(fromId);
        Account to = accounts.get(toId);


        // implement this

    }

    public BigDecimal totalBalance() {
        return accounts.values().stream()
                .map(Account::getBalance)
                .reduce(BigDecimal::add)
                .orElse(new BigDecimal(0));
    }
}