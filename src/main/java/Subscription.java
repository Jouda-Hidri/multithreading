package main.java;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Subscription {

    CopyOnWriteArrayList<Subscriber> subscribers;

    public void subscribe(Subscriber s) {
        subscribers.addIfAbsent(s); //  prevents duplicate subscriptions.
        // similar to computeIfAbsent
    }
    public void unsubscribe(Subscriber s) {
        subscribers.remove(s);
    }
    public void publish(String event) {
        subscribers.forEach( s -> s.notify(event));
    }

    public static class Subscriber {
        private List<String> events = new ArrayList<>();
        public void notify(String event) {
            events.add(event);
        }
    }

}
