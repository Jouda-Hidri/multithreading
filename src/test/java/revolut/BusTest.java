package revolut;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BusTest {

    @Test
    public void shouldUpdateDriver() {
        List<Driver> drivers = List.of(
                new Driver(1L, List.of(3, 1, 2, 3)),
                new Driver(2L, List.of(3, 2, 3, 1)),
                new Driver(3L, List.of(4, 2, 3, 4, 5))
        );
        Bus bus = new Bus(drivers);
        //assertThat(bus.getMinutes()).isEqualTo(4);
        // 334
        assertThat(drivers.get(0).gossip(drivers)).isEqualTo(2);
        assertThat(drivers.get(1).gossip(drivers)).isEqualTo(2);
        assertThat(drivers.get(2).gossip(drivers)).isEqualTo(1);
        drivers.get(0).move(); // minutes 1
        drivers.get(1).move();
        drivers.get(2).move();
        // 122
        assertThat(drivers.get(0).gossip(drivers)).isEqualTo(2);
        assertThat(drivers.get(1).gossip(drivers)).isEqualTo(3);
        assertThat(drivers.get(2).gossip(drivers)).isEqualTo(3);
        drivers.get(0).move(); // minutes 2
        drivers.get(1).move();
        drivers.get(2).move();
        // 233
        assertThat(drivers.get(0).gossip(drivers)).isEqualTo(2);
        assertThat(drivers.get(1).gossip(drivers)).isEqualTo(3);
        assertThat(drivers.get(2).gossip(drivers)).isEqualTo(3);
        drivers.get(0).move(); // minutes 3
        drivers.get(1).move();
        drivers.get(2).move();
        // 314
        assertThat(drivers.get(0).gossip(drivers)).isEqualTo(2);
        assertThat(drivers.get(1).gossip(drivers)).isEqualTo(3);
        assertThat(drivers.get(2).gossip(drivers)).isEqualTo(3);
        drivers.get(0).move(); // minutes 4
        drivers.get(1).move();
        drivers.get(2).move();
        // 335
        assertThat(drivers.get(0).gossip(drivers)).isEqualTo(3);
        assertThat(drivers.get(1).gossip(drivers)).isEqualTo(3);
        assertThat(drivers.get(2).gossip(drivers)).isEqualTo(3);
        assertTrue(bus.allDriversHaveAllGossip(drivers));
    }
}