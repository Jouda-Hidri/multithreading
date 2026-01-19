import main.java.Account;
import main.java.Counter;
import main.java.LRUCache;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Linked* preserve order
// concurrent* thread safe
// synchronised* blocking
// Deque : addFirst, addLast, removeFirst, removeLast

// TODO revise dates,
//  revise long int double BigDecimal etc.,
// revise time and space complexity
// revise java streaming, min max map reduce etc. for ex. transform list into a map
// computable future
// todo Executors, forkJoin (and all kind of executors)

/*

| Type      | Bits | Bytes                     | Range                          |
| --------- | ---- | ------------------------- | ------------------------------ |
| `byte`    | 8    | 1                         | -128 to 127                    |
| `short`   | 16   | 2                         | -32,768 to 32,767              |
---------------------------------------------------------------------------------
| `int`     | 32   | 4                         | \~ -2.1B to 2.1B               | 2^32 -1 unsigned
| `long`    | 64   | 8                         | \~ ±9.22×10¹⁸                  |
| `float`   | 32   | 4                         | \~ 7 decimal digits precision  |
| `double`  | 64   | 8                         | \~ 15 decimal digits precision |
---------------------------------------------------------------------------------
| `char`    | 16   | 2                         | Unicode 0–65,535               |
| `boolean` | 1\*  | \~1 byte (depends on JVM) |                                |


BigDecimal
Variable size — it’s an object that internally stores:
A BigInteger (arbitrary precision integer)
A scale (int)
Minimum overhead for an empty BigDecimal:
~ 32 bytes (object header + fields) + memory for digits array (char[] or int[])
Actual size grows with the number of digits.

| Space Complexity | Operations                                                                          | Notes                                                   |
| ---------------- | ----------------------------------------------------------------------------------- | ------------------------------------------------------- |
| **O(1)**         | `scale()`, `precision()`, `signum()`, `intValue()`, `longValue()`                   | No extra storage besides input object                   |
| **O(n)**         | `add()`, `subtract()`, `negate()`, `setScale()`, `toString()`, `BigDecimal(String)` | Creates new BigDecimal or string proportional to digits |
| **O(n)**         | `multiply()`, `divide()`, `remainder()`, `pow()`                                    | Result size proportional to operands                    |


| Time Complexity | Operations                                                                                         | Notes                                      |
| --------------- | -------------------------------------------------------------------------------------------------- | ------------------------------------------ |
| **O(1)**        | `scale()`, `precision()`, `signum()` (usually), `intValue()`, `longValue()`                        | Simple getters or minimal scans            |
| **O(n)**        | `add()`, `subtract()`, `compareTo()`, `negate()`, `setScale()`, `toString()`, `BigDecimal(String)` | Linear in number of digits                 |
| **O(n²)**       | `multiply()` (schoolbook), `divide()`, `remainder()`, `pow()` (with repeated multiplication)       | Quadratic due to digit-by-digit operations |
| **O(n log n)**  | `multiply()` (Karatsuba/FFT for large n)                                                           | Optimized multiplication algorithms        |


sorting algorithms

| Algorithm                           | Best       | Average    | Worst      | Space    | Stable? | Notes                                |
| ----------------------------------- | ---------- | ---------- | ---------- | -------- | ------- | ------------------------------------ |
| **Bubble Sort**                     | O(n)       | O(n²)      | O(n²)      | O(1)     | ✅       | Rarely used in practice              |
| **Selection Sort**                  | O(n²)      | O(n²)      | O(n²)      | O(1)     | ❌       | Simple but inefficient               |
| **Insertion Sort**                  | O(n)       | O(n²)      | O(n²)      | O(1)     | ✅       | Good for small/mostly-sorted arrays  |
| **Merge Sort**                      | O(n log n) | O(n log n) | O(n log n) | O(n)     | ✅       | Divide & conquer, stable             |
| **Quick Sort**                      | O(n log n) | O(n log n) | O(n²)      | O(log n) | ❌       | Fast, but not stable                 |
| **Heap Sort**                       | O(n log n) | O(n log n) | O(n log n) | O(1)     | ❌       | Uses heap, good for in-place sorting |
| **Timsort (used in Java & Python)** | O(n)       | O(n log n) | O(n log n) | O(n)     | ✅       | Hybrid of merge + insertion sort     |



Date and thread safety

Immutable = Thread-safe → All java.time classes.
Mutable = Not thread-safe → All pre–Java 8 date/time classes (Date, Calendar, TimeZone, SimpleDateFormat).
java.sql.Date, java.sql.Timestamp, java.sql.Time
These are subclasses of java.util.Date and inherit its mutability.
>>>Best practice:
Use java.time API for new code.
If stuck with old API, create a new instance per thread or use ThreadLocal.

Default Date format

| Class            | Default `.toString()` Format                 | Example Output                                | Notes                                   |
| ---------------- | -------------------------------------------- | --------------------------------------------- | --------------------------------------- |
| `LocalDate`      | `yyyy-MM-dd`                                 | `2025-08-04`                                  | Date only                               |
| `LocalTime`      | `HH:mm:ss.SSS` (if milliseconds present)     | `13:45:30.123`                                | Time only                               |
| `LocalDateTime`  | `yyyy-MM-dd'T'HH:mm:ss.SSS`                  | `2025-08-04T13:45:30.123`                     | Date + Time                             |
| `Instant`        | `yyyy-MM-dd'T'HH:mm:ss.SSSZ` (in UTC)        | `2025-08-04T11:45:30.123Z`                    | UTC (Zulu time)                         |
| `OffsetDateTime` | `yyyy-MM-dd'T'HH:mm:ss.SSSXXX`               | `2025-08-04T13:45:30.123+02:00`               | UTC offset                              |
| `ZonedDateTime`  | `yyyy-MM-dd'T'HH:mm:ss.SSSXXX['['ZoneId']']` | `2025-08-04T13:45:30.123+02:00[Europe/Paris]` | UTC offset + zone ID                    |

Unix timestamps are typically seconds.
Java & JavaScript timestamps are typically milliseconds.

| Feature       | StringBuffer       | StringBuilder   |
| ------------- | ------------------ | --------------- |
| Thread Safety | Yes (synchronized) | No              |
| Performance   | Slower             | Faster          |
| Introduced In | Java 1.0           | Java 5          |
| Use Case      | Multi-threaded     | Single-threaded |

| Data Structure | Thread-Safe Variant                            | Description                          |
| -------------- | ---------------------------------------------- | ------------------------------------ |
| List           | `CopyOnWriteArrayList`                         | Good for frequent reads, rare writes |
| Set            | `ConcurrentSkipListSet`, `CopyOnWriteArraySet` | Sorted & thread-safe alternatives    |
| Map            | `ConcurrentHashMap`                            | Supports atomic operations           |
| Queue          | `ConcurrentLinkedQueue`, `LinkedBlockingQueue` | Lock-free or blocking                |
| Deque          | `ConcurrentLinkedDeque`, `LinkedBlockingDeque` | For double-ended queue ops           |
| Counter        | `AtomicInteger`, `LongAdder`                   | For atomic increment/decrement       |
| Locking        | `ReentrantLock`, `StampedLock`                 | Explicit lock control                |


| Method         | Behavior when queue is **full**       | Returns?                |
| -------------- | ------------------------------------- | ----------------------- |
| **put(E e)**   | **Blocks** until space is available   | void                    |
| **add(E e)**   | Throws **IllegalStateException**      | boolean (true if added) |
| **offer(E e)** | Returns **false** immediately if full | boolean (true if added) |


| Method       | Behavior when queue is **empty**           | Returns                  |
| ------------ | ------------------------------------------ | ------------------------ |
| **take()**   | **Blocks** until an element is available   | The head element (E)     |
| **remove()** | Throws **NoSuchElementException** if empty | The head element (E)     |
| **poll()**   | Returns **null** immediately if empty      | The head element or null |

| Mode                | Description                                        | Acquiring method      | Releasing method                    |
| ------------------- | -------------------------------------------------- | --------------------- | ----------------------------------- |
| **Write Lock**      | Exclusive lock for writing                         | `writeLock()`         | `unlockWrite(stamp)`                |
| **Read Lock**       | Shared lock for reading                            | `readLock()`          | `unlockRead(stamp)`                 |
| **Optimistic Read** | Non-blocking read, might be invalidated by writers | `tryOptimisticRead()` | `validate(stamp)` to check validity |

Parallel Stream

| Rule                                              | Explanation                                                                      |
| ------------------------------------------------- | -------------------------------------------------------------------------------- |
| ❌ **Avoid `forEach`**                             | `forEach` mutates external state — not safe in parallel. Use collectors instead. |
| ✅ **Use `ConcurrentHashMap`**                     | Thread-safe for concurrent access during collection.                             |
| ✅ **Use `ConcurrentSkipListSet`**                 | Sorted + thread-safe set, better than `TreeSet` in concurrent context.           |
| ✅ **Use `groupingByConcurrent`**                  | Built-in collector optimized for parallel streams.                               |
| ❌ **Avoid mutable identity in `reduce()`**        | Use only immutable identity values like `0`, `""`, etc.                          |
| ✅ **Use `collect()` for mutable data structures** | Like `List`, `Map`, `Set` safely and efficiently.                                |

Sample test

    @Test
    public void shouldBook() {
        ExecutorService service = Executors.newFixedThreadPool(100);
        for(int i=0; i<1000; i++) {
            service.submit(() -> {
                initializeEmptyCalendar();
            });
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }

executor.scheduleAtFixedRate(() -> {...}, 1, 1, TimeUnit.SECONDS); // initialDelay = 1s, period = 1s

* Examples of ForkJoin

| Method         | Description      | Blocks | Returns                |
| -------------- | -----------------| -------| ---------------------- |
| `invoke()`     | sync             | Yes    | Result (T)             |
| `submit()`     | async            | No     | Future<T>              |
| `execute()`    | async            | No     | void                   |
| parallelStream | internally async | No     | Stream pipeline result |

ForkJoinPool pool = new ForkJoinPool();

        int result = pool.invoke(task);

        pool.execute(() -> {
            System.out.println("Running some async task");
        });

        Future<Integer> future = pool.submit(task);
        // do other stuff here...
        // wait for result
        int result = future.get();

         List<Integer> result = pool.submit(() ->
                IntStream.rangeClosed(1, 10)
                        .parallel()
                        .map(i -> i * i)
                        .boxed()
                        .toList()
        ).get();

* Completable future

| Method             | CompletableFuture Equivalent                                                     |
| ------------------ | -------------------------------------------------------------------------------- |
| `invoke()`         | `CompletableFuture.get()` (blocking) or `join()`                                 |
| `submit()`         | `CompletableFuture.supplyAsync()`                                                |
| `execute()`        | `CompletableFuture.runAsync()`                                                   |
| `parallelStream()` | `parallelStream()` itself, or `CompletableFuture` with `.thenApplyAsync()` chain |


*
* | Feature            | `get()`                             | `join()`                          |
| ------------------ | ----------------------------------- | --------------------------------- |
| Exception Type     | Checked                             | Unchecked (`CompletionException`) |
| Interrupted?       | Yes – throws `InterruptedException` | No                                |
| Cleaner in Streams | ❌                                   | ✅                                 |
| Use Case           | Traditional blocking calls          | Functional-style chaining         |

* Thread

        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                example.append("x");
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);

        t1.start();
        t2.start();

        t1.join();
        t2.join();

* If your code under test uses:
* a singleton
* static caches
* background threads you’ll need to explicitly reset them in @BeforeEach or @AfterEach.

* */

public class Main {
    public static void main(String[] args) throws InterruptedException {
        simulateCounter();
        simulateLRU();
        simulateTransfer();
    }

    static void simulateCounter() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Counter counter = new Counter();

        for (int i = 0; i < 1000; i++) {
            executor.submit(counter::increment);
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        System.out.println("Final counter value: " + counter.get());
    }

    static void simulateLRU() throws InterruptedException {
        LRUCache<Integer, String> cache = new LRUCache<>(5);
        ExecutorService executor = Executors.newFixedThreadPool(5);

        for (int i = 0; i < 10; i++) {
            final int key = i;
            executor.submit(() -> {
                cache.put(key, "Val" + key);
                cache.get(key);
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        // System.out.println("Final LRU keys: " + cache.keys());
    }

    static void simulateTransfer() throws InterruptedException {
        Account acc1 = new Account(1000);
        Account acc2 = new Account(1000);

        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 100; i++) {
            executor.submit(() -> Account.transfer(acc1, acc2, 10));
            executor.submit(() -> Account.transfer(acc2, acc1, 10));
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        System.out.println("Account1: " + acc1.getBalance());
        System.out.println("Account2: " + acc2.getBalance());
        System.out.println("Total: " + (acc1.getBalance() + acc2.getBalance()));
    }
}
