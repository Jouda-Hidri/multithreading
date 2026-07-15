package revolut.parking;


import java.util.*;

public class Parker {

    private final Map<String, String> parkedCars = new HashMap<>();
    private final Queue<String> freeSpots = new ArrayDeque<>();

    public Parker() {
        for (int i = 0; i < 10; i++) {
            freeSpots.offer(String.valueOf(i));
        }
    }

    public synchronized Optional<String> parkIn(String car) {

        if (parkedCars.containsKey(car)) {
            return Optional.of(parkedCars.get(car));
        }

        String spot = freeSpots.poll();

        if (spot == null) {
            return Optional.empty();
        }

        parkedCars.put(car, spot);

        return Optional.of(spot);
    }

    public synchronized void parkOut(String car) {

        String spot = parkedCars.remove(car); // TODO learn, map remove O(1), queue remove O(n)

        if (spot != null) {
            freeSpots.offer(spot);
        }
    }
}
