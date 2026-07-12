package revolut;

import java.util.List;

public class Bus {
    List<Driver> drivers;

    public Bus(List<Driver> drivers) {
        this.drivers = drivers;
    }

    public int getMinutes() {
        int minutes = 0;
        while (minutes < 480) {
            for (Driver driver : drivers) {
                driver.gossip(drivers);
                driver.move();
                if (allDriversHaveAllGossip(drivers)) {
                    return minutes;
                }

            }
            minutes++;
        }
        return minutes;
    }

    public boolean allDriversHaveAllGossip(List<Driver> drivers1) {
        return drivers1.stream()
                .filter(d -> d.hasAllGossip(drivers1.size())).count() == drivers1.size();
    }


}
