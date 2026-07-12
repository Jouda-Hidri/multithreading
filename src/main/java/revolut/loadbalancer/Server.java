package revolut.loadbalancer;

public class Server {

    private final String id;

    public Server(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
