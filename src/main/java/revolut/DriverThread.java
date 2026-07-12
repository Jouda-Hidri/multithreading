package revolut;

import java.util.*;

public class DriverThread {
    private int stop = 0;
    Set<DriverThread> driversHavingAllGossip;
    Set<Long> gossip = new HashSet<>();
    List<Integer> route;
    List<DriverThread> drivers = new ArrayList<>();

    public DriverThread(Long gossip, List<Integer> route, Set<DriverThread> driversHavingAllGossip) {
        this.route = route;
        this.gossip.add(gossip); // the driver has his own gossip
        this.driversHavingAllGossip = driversHavingAllGossip;
    }

    public void setDrivers(List<DriverThread> drivers) {
        this.drivers = drivers;
    }

    public int moveAndGossip(List<DriverThread> drivers) throws InterruptedException {
        while (stop < 480) {
            // gossip first then move
            if (gossip(drivers) == drivers.size()) {
                // this driver has all gossip
                this.driversHavingAllGossip.add(this);
                // all drivers have all gossip
                if (this.driversHavingAllGossip.size() == drivers.size()) {
                    return stop;
                }
            }
            // each driver has his own gossip
            // but stop is shared resource;
            // driver updates his stop
            // other drivers read the stop value
            wait(); // the other drivers shouldnt read stop of this drivers
            stop++;
            notifyAll();
        }
        return stop;
    }

    public int getStop() throws InterruptedException {
        wait(); // dont let this driver to update the current stop now
        int routeStop = stop % route.size();
        notifyAll();
        return this.route.get(routeStop);
    }

    public int gossip(List<DriverThread> drivers) throws InterruptedException {
        if (gossip.size() < drivers.size()) {
            for (DriverThread driver : drivers) {
                if (Objects.equals(driver.getStop(), this.getStop())) {
                    this.gossip.addAll(driver.gossip);
                }
            }
        }
        return gossip.size();
    }

}


// TODO read solution for this exercise
// TODO read advises of the github repo
// do further exercies and take note: which var are disputed ? which read/write should be atomic ? which deadlock is likely to be?
