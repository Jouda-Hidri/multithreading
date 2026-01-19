public class MySingleton {

    private static MySingleton instance;

    private MySingleton() {
    }

    public static synchronized MySingleton create() throws InterruptedException {
        if (instance == null) {
            instance = new MySingleton();
        }
        return instance;
    }

    public static void reset() {
        instance = null;
    }
}
