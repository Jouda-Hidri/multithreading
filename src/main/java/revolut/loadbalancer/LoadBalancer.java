package revolut.loadbalancer;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadBalancer {

    private final CopyOnWriteArrayList<Server> servers = new CopyOnWriteArrayList<>();
    private final AtomicInteger currentServer = new AtomicInteger(0);
    private final AtomicInteger nextServer = new AtomicInteger(1);

    public void addServer() {
        servers.add(new Server(String.valueOf(nextServer.getAndIncrement())));
    }

    public void removeServer(String serverId) {
        servers.removeIf(server -> server.getId().equals(serverId));
    }

    public void removeServer() {
        try {
            servers.remove(0);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalStateException("No servers");
        }
    }

    public Server getServer() {
        // use snapshot
        Object[] snapshot = servers.toArray();
        int length = snapshot.length;
        if (length == 0) {
            throw new IllegalStateException("No servers");
        }
        return (Server) snapshot[currentServer.getAndIncrement() % length];
    }

}
