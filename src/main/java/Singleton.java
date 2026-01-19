package main.java;


/**
 * Singleton Initialization
 * */
public class Singleton {
    private static volatile Singleton instance;
    private static final Object lock = new Object();

    private Singleton() {}

    public static Singleton getInstance() {
        if (instance == null) {
            // synchronised method or synchronised object + volatile
            synchronized (lock) {
                if (instance == null) {
                    instance = new Singleton();
                }
            }
        }
        return instance;
    }
}

