package revolut;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Driver {
    private int stop = 0;
    Set<Long> gossip = new HashSet<>();
    List<Integer> route;

    public Driver(Long gossip, List<Integer> route) {
        this.route = route;
        this.gossip.add(gossip); // the driver has his own gossip
    }

    public void move() {
        stop++;
    }

    public int getStop() {
        return this.route.get(stop % route.size());
    }

    public int gossip(List<Driver> drivers) {
        if (gossip.size() < drivers.size()) {
            for (Driver driver : drivers) {
                if (Objects.equals(driver.getStop(), this.getStop())) {
                    this.gossip.addAll(driver.gossip);
                }
            }
        }
        return gossip.size();
    }

    public boolean hasAllGossip(int driversCount) {
        return this.gossip.size() == driversCount;
    }
}


// TODO read solution for this exercise
// TODO read advises of the github repo
// do further exercies and take note: which var are disputed ? which read/write should be atomic ? which deadlock is likely to be?
