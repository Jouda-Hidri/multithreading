package revolut;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerTest {

    @Test
    public void shouldTransfer() {

        Map<Long, Account> accounts = Map.of(
                1L, new Account(new BigDecimal(1000)),
                2L, new Account(new BigDecimal(1000)),
                3L, new Account(new BigDecimal(1000))
        );

        Ledger ledger = new Ledger(accounts);

        ExecutorService executor =
                Executors.newFixedThreadPool(100);


        for (int i = 0; i < 10000; i++) {

            executor.submit(() ->
                    ledger.transfer(
                            1,
                            2,
                            new BigDecimal("1.00")
                    )
            );


            executor.submit(() ->
                    ledger.transfer(
                            2,
                            3,
                            new BigDecimal("1.00")
                    )
            );


            executor.submit(() ->
                    ledger.transfer(
                            3,
                            1,
                            new BigDecimal("1.00")
                    )
            );
        }

        assertThat(ledger.totalBalance())
                .isEqualTo(new BigDecimal("3000")); // todo why not 3000.0?
    }

}