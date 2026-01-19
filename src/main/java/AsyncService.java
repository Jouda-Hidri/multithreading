package main.java;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class AsyncService {
    public CompletableFuture<String> fetchData(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            return "Data-" + id;
        }, Executors.newCachedThreadPool());
    }

    public static void main(String[] args) {
        AsyncService service = new AsyncService();
        for (int i = 0; i < 5; i++) {
            int id = i;
            service.fetchData(id).thenAccept(result ->
                    System.out.println("Processed: " + result));
        }
    }
}

