package revolut;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.Collectors;

public class DriverThread {
    private volatile int stop = 0;
    Set<Long> gossip = ConcurrentHashMap.newKeySet();
    List<Integer> route;
    List<DriverThread> drivers = new ArrayList<>();
    CyclicBarrier startGossip;
    CyclicBarrier finishGossip;
    private static final Object gossipLock = new Object();

    public DriverThread(Long gossip, List<Integer> route) {
        this.route = route;
        this.gossip.add(gossip); // the driver has his own gossip
    }

    public void setDrivers(List<DriverThread> drivers,
                           CyclicBarrier startGossip,
                           CyclicBarrier finishGossip) {
        this.drivers = drivers;
        this.startGossip = startGossip;
        this.finishGossip = finishGossip;
    }

    public int moveAndGossip() throws InterruptedException, BrokenBarrierException {
        while (stop < 480) {
            gossip();
            if (allDriversHaveAllGossip()) {
                return stop;
            }
            stop++;
        }
        return stop;
    }

    public int gossip() throws BrokenBarrierException, InterruptedException {
        startGossip.await(); // wait for all to start gossip
        synchronized (gossipLock) { // one driver gossips at a time
            // get gossip from all drivers of this stop
            Set<Long> gossipOfStop = drivers.stream()
                    .filter(d -> d.getStop() == this.getStop())
                    .map(d -> d.gossip)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            // give gossip to all drivers of this stop
            drivers.stream()
                    .filter(d -> d.getStop() == this.getStop())
                    .forEach(d -> {
                                d.gossip.clear();
                                d.gossip.addAll(gossipOfStop);
                            }
                    );
        }
        finishGossip.await(); // wait for all to finish gossip
        return gossip.size();
    }

    private boolean allDriversHaveAllGossip() {
        return drivers.stream()
                .allMatch(d -> d.gossip.size() == drivers.size());
    }

    private int getStop() {
        int routeStop = stop % route.size();
        return this.route.get(routeStop);
    }

}
