package revolut;

import java.math.BigDecimal;

public record Account(BigDecimal balance) {

    public BigDecimal getBalance() {
        return balance;
    }

}
