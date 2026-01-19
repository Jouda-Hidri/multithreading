import static java.util.Comparator.comparingInt;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.averagingLong;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.groupingByConcurrent;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.reducing;
import static java.util.stream.Collectors.summingDouble;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toConcurrentMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.junit.jupiter.api.Test;

class InvoiceGenTest {

    @Test
    public void shouldCreateInvoice() throws InterruptedException { // one transaction per user
        // we should have as much invoices as the created transactions
        // GIVEN invoice generator
        var invoiceGen = new main.java.InvoiceGen();
        // WHEN exec transactions and gen invoices
        ExecutorService executor = Executors.newFixedThreadPool(10); //todo correct only for 1&2 thread pool
        for (int i = 0; i < 100; i++) {
            executor.submit(() -> invoiceGen.pay("user1", BigDecimal.valueOf(50)));
            executor.submit(() -> invoiceGen.pay("user2", BigDecimal.valueOf(100)));
            executor.submit(() -> invoiceGen.getOrCreate("user1"));
            executor.submit(() -> invoiceGen.getOrCreate("user2"));
        }
        // todo, how does it work incase we have a future reuslt ?
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        // todo parallel stream
        // THEN return invoice per user
        // no duplicate transaction ID
        assertThat(invoiceGen.getTransactionId().get()).isEqualTo(200);
        assertThat(invoiceGen.getInvoiceId().get()).isEqualTo(200);
//        assertThat(invoiceGen.getTransactionId().get()).isEqualTo(invoiceGen.getInvoiceId().get());
    }
// todo one user has many transactions
    // todo users have orders, orders have transactions
    // java stream to get transactions of one order from list of users
    // todo pay with date
    // invoice in the last 1week/month


    public int sum(List<Integer> numbers) {
        // even number when rest division of 2 is zero
        return numbers.stream().filter(n -> n % 2 == 0).reduce((x, y) -> x + y).get();
    }

    public List<String> upperCase(List<String> input) {
        return input.stream().map(String::toUpperCase).toList();
    }

    public Map<Character, Set<String>> groupByFirstLetter(List<String> input) {
        //input.stream().collect(Collectors.toMap(s -> s.charAt(0), ))
        return input.stream().collect(groupingBy(s -> s.charAt(0), toSet())); //groupingBy
    }

    public List<String> distinctWords(List<String> input) {
        return input.stream().flatMap(s -> Arrays.stream(s.split(" ")))
                .map(String::toLowerCase)
                .distinct()
                .toList();
    }

    public Map<Character, String> groupByLongestWord(List<String> input) {
        return input.stream().collect(
                groupingBy(
                        s -> s.charAt(0),
                        // todo remember collector and then
                        Collectors.collectingAndThen(
                                Collectors.maxBy(comparingInt(String::length)),
                                opt -> opt.orElse(null)
                        )));

    }

    public Map<Integer, Set<String>> groupByLength(List<String> input) {
        return input.stream().collect(groupingBy(String::length, toSet()));
    }


    public Map<String, Long> frequencies(List<String> input) {
        return input.stream().collect(groupingBy(s -> s, counting()));
    }

    public Map<Boolean, List<String>> partition(List<String> input) {
        return input.stream().collect(partitioningBy(s -> s.length() > 5, toList()));
    }

    public String joinList(List<String> input) {
        return input.stream()
                .filter(s -> s.length() > 5)
                .collect(joining(","));
    }

    private Map<Character, Long> countWordsOfSameLetter(List<String> input) {
        return input.stream().collect(groupingBy(s -> s.charAt(0), counting()));
    }

    public Map<Character, String> longestWordByLetter(List<String> input) {
        return input.stream().collect(
                Collectors.toMap(
                        s -> s.charAt(0),
                        Function.identity(),
                        BinaryOperator.maxBy(comparingInt(String::length)))
        );
    }

    public Map<Boolean, Double> averageAge(List<Long> input) {
        return input.stream().collect(
                partitioningBy(
                        age -> age >= 18,
                        averagingLong(age -> age)));
    }

    public Map<String, Double> totalTransaction(List<Transaction> input) {
        return input.stream().collect(
                groupingBy(
                        Transaction::getCustomerId,
                        summingDouble(Transaction::getAmount)));
    }

    public Map<Character, List> nLongestWords(List<String> input, int n) {
        return input.stream().collect(
                groupingBy(
                        s -> s.charAt(0),
                        collectingAndThen(
                                toList(),
                                list -> list.stream()
                                        .sorted(comparingInt(String::length).reversed())
                                        .limit(n)
                                        .toList())
                )
        );
    }

    public Map<String, Integer> wordCount(List<String> input) { //how to transform string into list of chars
        return input.stream().collect(groupingBy(
                w -> w,
                reducing(0, w -> 1, Integer::sum))); //todo what does this mean ?
    }

    public Map<String, Map<String, List<Employee>>> nestedGrouping(List<Employee> input) {
        // todo collectingAndThen : I still dont master
        // keep in mind first is the list, then transformation on the list
        // collectingAndThen(toList, list ->list...)
//        input.stream().collect(
//                groupingBy(
//                        e -> e.dept,
//                        collectingAndThen(e -> e.age,
//                                list -> list.stream().map(Employee::getEmployee)))
//        );
        return input.stream().collect(
                groupingBy(
                        e -> e.dept,
                        groupingBy(e -> e.age >= 50 ? "Senior" : "Junior") // todo interesting example
                )
        );
    }

    public Map<String, List<String>> productByCategory(List<Product> products) {
        return products.stream().collect(
                groupingBy(
                        p -> p.category,
                        // todo also Collectors.mapping(Product::getName, Collectors.toList())
                        collectingAndThen(
                                toList(),
                                list -> list.stream().map(p -> p.name).toList()))
        );
    }

    public Map<Integer, String> wordByLength(List<String> input) {
        return input.stream().collect(groupingBy(
                String::length,
                joining()));
    }

    public Map<Character, String> firstWordByStartingLetter(List<String> input) {
        return input.stream().collect(groupingBy(
                        s -> s.charAt(0),
                        collectingAndThen(
                                reducing((w1, w2) -> w1),
                                Optional::get
                        )
                )
        );
    }

    /**
     * Flatten all items across all orders.
     * <p>
     * Total quantity per product.
     * <p>
     * Most popular product.
     */

    public Map<String, Long> mostPopularProduct(List<Order> orders) {
        List<OrderItem> items = orders.stream()
                .flatMap(order -> order.items.stream())
                .toList();
        Map<String, Long> count = items.stream().collect(
                groupingBy(
                        i -> i.product,
                        counting())
        );
        Map<String, Long> top = count.entrySet().stream()
                .sorted(Entry.<String, Long>comparingByValue().reversed())
                .limit(1)
                .collect(
                        // todo remember toMap should be inside of collect
                        toMap(
                                Map.Entry<String, Long>::getKey,
                                Map.Entry<String, Long>::getValue,
                                (a, b) -> a, // todo remember this
                                LinkedHashMap::new
                        )
                );
        return top;
    }

    /**
     * Compute total amount per customer.
     * <p>
     * Get the top 3 spenders.
     * <p>
     * Sum of all transactions in the last 7 days.
     * <p>
     * Monthly average spend per customer.
     */

    public void totalByCustomer(List<Transaction> transactions) {
        Map<String, Double> total = transactions.stream().collect(
                groupingBy(
                        Transaction::getCustomerId,
                        summingDouble(Transaction::getAmount)));
        Map<String, Double> top3 = total.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed()) // todo remmeber sort map
                .limit(3)
                .collect(Collectors.toMap(
                        Entry::getKey,
                        Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new // preserve order
                ));
        double sum = transactions.stream().filter(
                        transaction -> transaction.date.isAfter(LocalDate.now().minus(7, ChronoUnit.DAYS)))
                .mapToDouble(Transaction::getAmount).sum(); // TODO remember mapToDouble().sum
        Map<String, Map<Month, Double>> avg = transactions.stream().collect(groupingBy(
                Transaction::getCustomerId,
                groupingBy(
                        t -> t.date.getMonth(),
                        averagingDouble(Transaction::getAmount)
                ))

        );
    }

    /**
     * Implement a custom collector to count word frequencies in a large text.
     */
    public Map<String, Long> countFreq(List<String> input) {
        return input.stream().collect(groupingBy(
                Function.identity(),
                counting()// todo simpler reducing(0, w -> 1, Integer::sum)
        ));
    }

    public Map<String, Long> countWords(List<String> input) {
        return input.stream().collect(
                collectingAndThen( // todo collectAndThen should be inside collect
                        groupingBy(Function.identity(), counting()),
                        Collections::unmodifiableMap)

                // todo
//                        toMap(
//                                Entry::getKey,
//                                Entry::getValue,
//                                (a, b) -> a,
//                                UnmodifiableMap::new
//                        )

        );
    }

    /**
     * longest word in a list
     */

    public String longestWord(List<String> input) {
        return input.stream()
                .max(comparingInt(String::length))
                .orElse(null);
    }

    public String longestWordReducing(List<String> input) {
        //todo collect(reducing) -> reduce
        return input.stream().reduce("", (w1, w2) -> w1.length() > w2.length() ? w1 : w2);
    }

    public void avgByDept(List<Employee> input) {
        input.stream()
                .collect(groupingBy(
                                e -> e.dept,
                                collectingAndThen(
                                        averagingDouble(e -> e.age),
                                        Math::round
// todo fix next: no nead for list map etc. just tell the actual mapping ...
//                                list -> list.stream().map(
//                                        avg -> Math.round(avg)
//                                ).toList()
                                )
                        )
                );
    }

    public void fct1(List<Employee> input) {
        // group by age
        // count frequency for each age
        // sort reversed
        // limit 3
        HashMap<Long, Long> x = input.stream().collect(groupingBy(e -> e.age, counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(3)
                .collect(toMap(
                        Entry<Long, Long>::getKey,
                        Entry<Long, Long>::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

    }

    /**
     * Collect Top 3 Words by Frequency
     */

    public LinkedHashMap<String, Long> topFreq(List<String> input) {
        return input.stream().collect(
                collectingAndThen(
                        groupingBy(
                                Function.identity(),
                                counting()
                        ),
                        list -> list.entrySet().stream()
                                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                                .limit(3)
                                // todo  in case we want words only without the freq
                                //  .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
                                .collect(toMap(
                                        Entry::getKey,
                                        Entry::getValue,
                                        (a, b) -> a,
                                        LinkedHashMap::new)
                                ))

        );
    }

    public int reduceToSum(List<String> input) {
        // TODO difference between reduce and reducing:
//             return input.stream().collect(Collectors.reducing(
//                    0,
//                    String::length,
//                    Integer::sum
//            ));
        return input.stream().map(String::length).reduce(0, Integer::sum);
    }

    public void fct3(List<String> input) {
        // longest by group =>
        // group by first letter and return max lenght
        Map<Character, Optional<String>> x = input.stream().filter(s -> !s.isEmpty())
                .collect(groupingBy(
                        s -> s.charAt(0), maxBy(comparingInt(String::length))));
    }


    public Map<Character, String> longestByGroup(List<String> input) {
        return input.stream().collect(
                groupingBy(
                        s -> s.charAt(0),
                        // todo or also
                        // Collectors.reducing((w1, w2) -> w1.length() >= w2.length() ? w1 : w2)
                        collectingAndThen(
                                maxBy(comparingInt(String::length)),
                                Optional::get
                        )

                )
        );
    }

    @Test
    public void testStream() {

        assertThat(sum(List.of(1, 2, 3, 4, 5, 6))).isEqualTo(12);

        assertThat(upperCase(List.of("alice", "bob", "carol"))).isEqualTo(List.of("ALICE", "BOB", "CAROL"));

        assertThat(groupByFirstLetter(List.of("apple", "banana", "apricot", "blueberry")))
                .isEqualTo(Map.of('a', Set.of("apple", "apricot"),
                        'b', Set.of("banana", "blueberry")));

        assertThat(distinctWords(List.of("Hello world", "world of Java", "hello Java")))
                .isEqualTo(List.of("hello", "world", "of", "java"));

        assertThat(groupByLength(List.of("one", "two", "three", "four", "five")))
                .isEqualTo(
                        Map.of(
                                3, Set.of("one", "two"),
                                5, Set.of("three"),
                                4, Set.of("four", "five")));

        assertThat(frequencies(List.of("apple", "banana", "apple", "orange", "banana", "apple")))
                .isEqualTo(Map.of(
                        "apple", 3L,
                        "banana", 2L,
                        "orange", 1L
                ));
        assertThat(partition(List.of("apple", "banana", "pear", "cherry")))
                .isEqualTo(Map.of(
                        true, List.of("banana", "cherry"),
                        false, List.of("apple", "pear")));

        assertThat(joinList(List.of("apple", "banana", "pear", "cherry")))
                .isEqualTo("banana,cherry");

        assertThat(countWordsOfSameLetter(List.of("apple", "apricot", "banana", "blueberry")))
                .isEqualTo(Map.of(
                        'a', 2L,
                        'b', 2L));

        assertThat(totalTransaction(List.of(
                new Transaction("c1", 50d),
                new Transaction("c2", 100d),
                new Transaction("c1", 25d)
        ))).isEqualTo(
                Map.of("c1", 75.0,
                        "c2", 100.0)
        );

        assertThat(nLongestWords(List.of("apple", "banana", "beans", "apricot", "acajou", "ba"), 2))
                .isEqualTo(
                        Map.of('a', List.of("apricot", "acajou"), // 7, 6
                                'b', List.of("banana", "beans")) // 6, 5
                );

        assertThat(longestWord(List.of("a", "ab", "abc", "a", "cc"))).isEqualTo("abc");
        assertThat(longestWordReducing(List.of("a", "ab", "abc", "a", "cc"))).isEqualTo("abc");
        assertThat(topFreq(
                List.of("aa", "ab", "ab", "cc", "cc", "cc", "dbc", "dbc", "dbc", "dbc"))
        ).isEqualTo(
                Map.of(
                        "dbc", 4L,
                        "cc", 3L,
                        "ab", 2L
                )
        );
        assertThat(longestByGroup(
                List.of("aa", "ab", "ab", "cc", "cc", "cc", "dbc", "db", "dbcd", "dd"))
        ).isEqualTo(
                Map.of(
                        'a', "aa", // or ab
                        'c', "cc",
                        'd', "dbcd"
                )
        );
    }


    class Order {
        String id;
        List<OrderItem> items;
    }

    class OrderItem {
        String product;
        int quantity;
    }

    List<Order> orders;


    class Transaction {
        String customerId;
        double amount;
        LocalDate date;

        public Transaction(String customerId, Double amount) {
            this.customerId = customerId;
            this.amount = amount;
        }

        public String getCustomerId() {
            return customerId;
        }

        public double getAmount() {
            return amount;
        }

        public LocalDate getDate() {
            return date;
        }
    }


    public class Product {
        String category;
        String name;
    }

    public class Employee {
        String dept;
        Long age;
        String employee;


    }

    /**
     * Parallel stream exercices
     * Compute the sum of squares from 1 to N.
     */
    public double squaresSum(int n) {
        DoubleStream.of(0, 5, 6, 10).map(Math::sqrt).sum();
        return IntStream.rangeClosed(1, n) // todo remember IntStream.rangeClosed
                .boxed()
                .parallel()
//                .map(Math::sqrt)
                .mapToDouble(Math::sqrt)// todo fix cannot call sum on .map(Math::sqrt)
                .sum();// todo fix .reduce(0d, Double::sum);

    }

    // todo test with and without ConcurrentHashMap
    public Map<String, Long> countByWord(List<String> input) {
        return input.stream().parallel()
                .collect(groupingBy(
                        Function.identity(),
                        ConcurrentHashMap::new, //todo remember mapping here for grouping, reducing etc.
                        counting()
                ));
    }

    public void fct(List<Transaction> transactions) {
        String maxCustomer = transactions.stream().collect(
                        groupingBy(
                                Transaction::getCustomerId,
                                summingDouble(Transaction::getAmount)
                        )
                ).entrySet().stream().max(Entry.<String, Double>comparingByValue().reversed())
                .map(Entry::getKey)
                .orElse(null);
        Optional<Transaction> t = transactions.stream().max(Comparator.comparingDouble(Transaction::getAmount));
    }

    /**
     * Find the max transaction amount per customer in parallel.
     */
    public Transaction maxTransaction(List<Transaction> transactions) {
// todo fix: for reducing grouping etc. mapping is optional and identity is also optional
        Optional<Transaction> optionalResult = transactions.parallelStream()
                .reduce((t1, t2) -> t1.amount > t2.amount ? t1 : t2);
        return transactions.parallelStream()
                .reduce((t1, t2) -> t1.amount > t2.amount ? t1 : t2)
                .orElse(null); // handle empty list case
//        return transactions.stream().parallel()
//                .reduce(new Transaction(null, 0d), (t1, t2) -> t1.amount > t2.amount ? t1 : t2);
    }

    /*
    * TODO interesting example
    *  // Count vowels in a large list of strings
public Map<Character, Long> countVowels(List<String> input) {
    return input.parallelStream()
            .flatMapToInt(String::chars)
            .mapToObj(c -> (char) c)
            .filter(ch -> "aeiouAEIOU".indexOf(ch) >= 0)
            .collect(Collectors.groupingBy(
                    Function.identity(),
                    ConcurrentHashMap::new,
                    Collectors.counting()
            ));
}

    * */

    @Test
    public void testParallelStream() {
        assertThat(maxTransaction(
                        List.of(
                                new Transaction("c", 100d),
                                new Transaction("c", 50d),
                                new Transaction("c", 150d),
                                new Transaction("c", 300d),
                                new Transaction("c", 50d),
                                new Transaction("c", 10d),
                                new Transaction("c", 1000d),
                                new Transaction("c", 5000d), //
                                new Transaction("c", 700d),
                                new Transaction("c", 30d)
                        )
                )
                        .amount
        )
                .isEqualTo(5000d);
    }

    /**
     * ✅ Exercise 1: Detect Top 10 Most Active Users Across Logs
     * Problem: You are given a List<LogEntry> where each entry contains a userId and an actionTime. Logs are from multiple servers and possibly contain duplicates.
     * <p>
     * Goal: Use parallel stream to count unique actions per user and return top 10 most active users (by count). Ensure thread-safety without using external mutable state.
     */

    public Set<String> top10Users(List<LogEntry> input) {
        // count unique actions per user
        ConcurrentMap<String, Integer> countByUser = input.stream().parallel()
                .collect(groupingByConcurrent(
                        logEntry -> logEntry.userId,
                        collectingAndThen(
                                mapping(logEntry -> logEntry.actionTime, toSet()),
                                Set::size
                        )

                        // FIXME    toList() is not thread-safe or parallel-optimized.
                        //  It collects all entries per user before streaming again.
                        //  It works but might be inefficient for large data.
//                        collectingAndThen(
//                                toList(),
//                                e -> e.stream().map(logEntry -> logEntry.actionTime)
//                                        .distinct().count()
//                        )
                ));
        // top 10 most active users
        return countByUser.entrySet().stream().parallel()
                .sorted(Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(Entry::getKey)
                .collect(toCollection(LinkedHashSet::new));
        // TODO improvement
//                .collect(toMap(
//                        Entry::getKey,
//                        Entry::getValue,
//                        (a, b) -> a,
//                        ConcurrentHashMap::new
//                ))
//                .keySet();
    }

    public class LogEntry {
        private String userId;
        private String actionTime;
    }

// TODO I didnt do exercice 2 where we are doing for loops uisng intStream


    public class Pair {
        public final String left;
        public final String right;

        public Pair(String left, String right) {
            this.left = left;
            this.right = right;
        }
    }

    /**
     * Goal: Compute monthly average per customer, in parallel, with thread safety.
     */

    public Map<String, Map<String, Double>> monthlyAvg(List<Transaction> input) {
        var result = input.stream().parallel()
                .collect(
                        groupingByConcurrent(
                                Transaction::getCustomerId,
                                groupingByConcurrent(
                                        t -> t.date.getMonth().toString(),
                                        averagingDouble(Transaction::getAmount)
                                        // todo toMap(Entry::getKey, ...) only works on a Stream<Map.Entry>
//                                        toMap(
//                                                Entry::getKey,
//                                                Entry::getValue,
//                                                (a, b) -> a,
//                                                HashMap::new
//                                        )

                                )
                        )
                );
        // TODO how to convert ConcurrentMap to Map;
        //  Java’s type system doesn't allow this assignment directly,
        //  even though ConcurrentMap is a subtype of Map.
        //  This is due to Java’s generics being invariant — meaning:
        //  ConcurrentMap<K, V> is not a subtype of Map<K, V2> unless V == V2.
        return result.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new HashMap<>(e.getValue()) // wrap inner ConcurrentMap as a HashMap
                ));
    }


    /*ForkJoinPool*/

//    class FileSearchTask extends RecursiveTask<List<File>> {
//        private final File directory;
//        private final String keyword;
//
//        // Recursive split and join
//    }

    /*
    *
    *
    * ⚙️ Exercise 5: Parallel Price Aggregator with ExecutorService
Problem: Given a list of APIs to call (simulate them with Callable<Double>), fetch all prices in parallel, and return the average.

Constraints:

Limit concurrency (e.g., 10 threads max)

Collect failed tasks separately

ExecutorService executor = Executors.newFixedThreadPool(10);
List<Future<Double>> futures = tasks.map(executor::submit);
Handle timeout, exception, and average only successful results.
*/

    public class PriceAggregator {

        public static double fetchAveragePrice(List<Callable<Double>> tasks, int timeoutSeconds) throws InterruptedException {
            ExecutorService executor = Executors.newFixedThreadPool(10);
            List<Double> successful = new ArrayList<>();
            List<Exception> failures = new ArrayList<>();

            try {
                List<Future<Double>> futures = tasks.stream()
                        .map(executor::submit)
                        .collect(Collectors.toList());

                for (Future<Double> future : futures) {
                    try {
                        Double result = future.get(timeoutSeconds, TimeUnit.SECONDS);
                        successful.add(result);
                    } catch (TimeoutException | ExecutionException e) {
                        failures.add(e);
                    }
                }

                return successful.stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);

            } finally {
                executor.shutdownNow();
            }
        }

        // Example usage
//        public static void main(String[] args) throws InterruptedException {
//            List<Callable<Double>> tasks = List.of(
//                    () -> 100.0,
//                    () -> 200.0,
//                    () -> { Thread.sleep(2000); return 300.0; }, // this will timeout
//                    () -> { throw new RuntimeException("API failed"); }
//            );
//
//            double avg = fetchAveragePrice(tasks, 1);
//            System.out.println("Average successful price: " + avg);
//        }
    }

/*
🔁 Exercise 6: Retry Logic with ScheduledExecutor
Goal: Retry failed tasks (like API call or DB query) with delay and exponential backoff.

Create a reusable retry(Runnable task, int maxRetries) method using ScheduledExecutorService.
    *
    * */

    public class RetryExecutor {

        private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        public void retry(Runnable task, int maxRetries) {
            AtomicInteger attempts = new AtomicInteger(0);
            long initialDelay = 1; // seconds

            Runnable retryTask = new Runnable() {
                @Override
                public void run() {
                    try {
                        task.run();
                        System.out.println("Task succeeded on attempt " + attempts.incrementAndGet());
                    } catch (Exception e) {
                        int attempt = attempts.incrementAndGet();
                        if (attempt <= maxRetries) {
                            long delay = (long) Math.pow(2, attempt); // exponential backoff
                            System.out.println("Retrying in " + delay + " seconds (attempt " + attempt + ")");
                            scheduler.schedule(this, delay, TimeUnit.SECONDS);
                        } else {
                            System.out.println("Task failed after " + maxRetries + " retries");
                        }
                    }
                }
            };

            scheduler.schedule(retryTask, initialDelay, TimeUnit.SECONDS);
        }

        // Example usage
//        public static void main(String[] args) {
//            RetryExecutor re = new RetryExecutor();
//            AtomicInteger counter = new AtomicInteger(0);
//
//            re.retry(() -> {
//                if (counter.incrementAndGet() < 3) {
//                    throw new RuntimeException("Fail");
//                }
//                System.out.println("Done on attempt " + counter.get());
//            }, 5);
//        }
    }

/*
*
| Feature            | Exercise 5                        | Exercise 6                  |
| ------------------ | --------------------------------- | --------------------------- |
| Thread pool limit  | ✅ `FixedThreadPool(10)`           | ✅ `ScheduledThreadPool(1)`  |
| Exception handling | ✅ `try/catch` on `Future.get()`   | ✅ `try/catch` in `Runnable` |
| Timeout            | ✅ `future.get(timeout, TimeUnit)` | ❌ (optional enhancement)    |
| Retry              | ❌                                 | ✅ With exponential backoff  |
| Result aggregation | ✅ average of successful           | ❌ (add logging if needed)   |

*
* */



    /*
Here are some practical exercises to practice **CompletableFuture** in Java, focusing on asynchronous programming, combining futures, handling exceptions, and working with thread pools:

---

### Exercise 1: Simple Async Task

**Task:**
Create a method that returns a `CompletableFuture<String>` which asynchronously returns `"Hello, World!"` after a 1-second delay.

```java
public CompletableFuture<String> sayHelloAsync();
```

---

*/

    public CompletableFuture<String> sayHelloAsync() {
        return supplyAsync(() -> {
            try {
                Thread.sleep(1000); // simulate delay
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "Hello, World!";
        });
        // TODO this is the fix
//        CompletableFuture<String> cf = new CompletableFuture();
//        cf.completeOnTimeout("Hello World", 1, TimeUnit.SECONDS);
//        return cf.get();
    }

    @Test
    public void shouldReturnAfterTimeout() throws ExecutionException, InterruptedException {
        assertThat(sayHelloAsync().get()).isEqualTo("Hello, World!");
    }

    /*

### Exercise 2: Combining Two Async Tasks

**Task:**
Given two asynchronous tasks returning `CompletableFuture<Integer>` for fetching prices from two different stores, combine their results to get the sum of prices.

```java
CompletableFuture<Integer> fetchPriceFromStoreA();
CompletableFuture<Integer> fetchPriceFromStoreB();

// Implement
CompletableFuture<Integer> totalPriceAsync();
```

---

*/
    CompletableFuture<Integer> fetchPriceFromStoreA() {
        return CompletableFuture.completedFuture(50);
    }

    CompletableFuture<Integer> fetchPriceFromStoreB() {
        return CompletableFuture.completedFuture(150);
    }

    CompletableFuture<Integer> totalPriceAsync() throws ExecutionException, InterruptedException {
        return fetchPriceFromStoreA()
                .thenCombine(fetchPriceFromStoreB(), Integer::sum); // or (a, b) -> a + b
        // TODO fix
//        CompletableFuture<Void> x = fetchPriceFromStoreA()
//                .thenAcceptBoth(
//                        fetchPriceFromStoreB(),
//                        (s1, s2) -> s1 + s2);
    }


    /*

### Exercise 3: Exception Handling with CompletableFuture

**Task:**
Create a method that asynchronously divides two numbers. If a division by zero occurs, the future should complete exceptionally and be handled gracefully by returning `-1`.

```java
CompletableFuture<Integer> divideAsync(int a, int b);
```

---

*/
    public CompletableFuture<Integer> divide(int a, int b) {
        return supplyAsync(() -> a / b)
                .exceptionally(ex -> {
                    System.out.println("Exception occurred: " + ex.getMessage());
                    return -1;
                });
        // TODO fix this
//        CompletableFuture.supplyAsync(
//                () -> a/b
//        ).exceptionally(() -> -1);
    }

    /*



### Exercise 4: Parallel Calls and Combining Results

**Task:**
Fetch a list of user IDs asynchronously (simulate with a `CompletableFuture<List<String>>`), then for each user ID, asynchronously fetch the user’s profile (`CompletableFuture<User>`). Finally, combine all results into a single list of users.

Hint: Use `CompletableFuture.allOf()` and then combine the results.

---

*/

    public CompletableFuture<List<String>> fetchIds() {
        return supplyAsync(() -> List.of("id1", "id2", "id3"));
    }

    public CompletableFuture<String> fetchId() {
        return supplyAsync(() -> "id1");
    }

    public CompletableFuture<User> fetchProfile(String id) {
        return CompletableFuture.supplyAsync(() -> new User(id));
        // user, ids -> ids.stream().map(id -> new User(id)).collect(toSet())
    }

    public void fetch() {
        CompletableFuture<Object> future = fetchIds()
                .thenCompose(list -> {
                    List<CompletableFuture<User>> result = list.stream().map(id -> fetchProfile(id)).toList();
                    CompletableFuture<Void> done = allOf(result.toArray(new CompletableFuture[0]));
                    return done.thenApply(v -> result.stream().map(CompletableFuture::join).collect(toSet()));
                });
    }

    public class User {
        private String userId;

        public User(String userId) {
            this.userId = userId;
        }
    }

    /*

### Exercise 5: CompletableFuture with Custom Executor

**Task:**
Create a `CompletableFuture` that runs an expensive computation asynchronously, but use a **custom thread pool** (ExecutorService with 5 threads). Return the result of the computation.

---

### Exercise 6: Sequential Async Calls

**Task:**
Create two async methods:

* `fetchUserIdAsync()` returns a user ID asynchronously.
* `fetchUserProfileAsync(String userId)` fetches user profile given a user ID asynchronously.

Use `thenCompose` to first get the user ID and then fetch the user profile, chaining the async calls.

---

*/


    public void fetchAsync() {
        CompletableFuture<User> future = fetchId().thenCompose(this::fetchProfile);
    }

    /*

### Exercise 7: Timeout on CompletableFuture

**Task:**
Create a method that returns a `CompletableFuture<String>`. It should complete with the string `"Done"` after 5 seconds, but if it takes longer than 2 seconds, it should timeout and complete exceptionally with a `TimeoutException`.

---

*/
    public CompletableFuture<String> doneWithTimeout() {
        return supplyAsync(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
            }
            return "Done";
        }).orTimeout(2, TimeUnit.SECONDS);
    }


    @Test
    public void shouldTimeout() {
        assertThrows(CompletionException.class, () -> {
            doneWithTimeout().join();
        });
    }

    /*

### Exercise 8: Combining CompletableFuture with Stream API

**Task:**
Given a list of URLs, asynchronously fetch the content length for each URL using `CompletableFuture<Integer>`. Use parallel streams and then combine all results to get the total sum of content lengths.

---

Would you like me to provide solutions for any or all of these?

    * */


    public CompletableFuture<Integer> fetchContentLengthAsync(String url) {
        return CompletableFuture.supplyAsync(() -> {
            // simulate delay or actual network call here
            return url.length(); // Replace with real content length
        });
    }

    public Integer parallelLength() {
        List<String> urls = List.of("a", "ab", "abc", "abcd", "abcde");

        // Asynchronously fetch content lengths in parallel
        List<CompletableFuture<Integer>> futures = urls.parallelStream()
                .map(this::fetchContentLengthAsync)
                .collect(Collectors.toList());

        // Combine all futures into one
        CompletableFuture<Void> allDone = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        // After all are done, sum the results
        return allDone.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .reduce(0, Integer::sum)
        ).join();
        // TODO fix
//        CompletableFuture<List<String>> urls = CompletableFuture.completedFuture(List.of("a", "ab", "abc"));
//        CompletableFuture<Integer> max = urls.thenApply(s -> s.stream().parallel().max(comparingInt(String::length)).map(String::length).orElse(0));
//        CompletableFuture<Integer> sum = urls.thenApply(s -> s.stream().parallel().map(String::length).reduce(Integer::sum).orElse(0));
//        return sum.join();

    }

    /*
    *
    * Task:
You are given a list of cities. For each city, simulate an async API call to fetch the temperature. Use CompletableFuture.allOf() to wait for all temperatures to be fetched and return a Map<String, Integer> (city → temperature).
    * */
    public Map<String, Integer> temperature() {
        List<String> cities = List.of("Paris", "Moscow", "Milano", "Berlin");
        Map<String, CompletableFuture<Integer>> futures = cities.parallelStream()
                .collect(
                        toMap(
                                city -> city,
                                this::fetch
                        ));
        CompletableFuture<Void> all = allOf(futures.values().toArray(new CompletableFuture[0]));
        all.join();
        return futures.entrySet().stream().collect(
                toMap(
                        Entry::getKey,
                        entry -> entry.getValue().join()

                )
        );
    }

    public CompletableFuture<Integer> fetch(String city) {
        return CompletableFuture.supplyAsync(() -> {
            return switch (city) {
                case "Paris" -> 25;
                case "Moscow" -> -5;
                case "Milano" -> 20;
                case "Berlin" -> 10;
                default -> throw new IllegalStateException("Unexpected value: " + city);
            };
        });
    }


    /*
    * Exercise 2: Fetch & Combine User Data
Task:
For a list of user IDs, perform two parallel API calls per user:

fetchUserInfo(String userId)

fetchUserSettings(String userId)

Combine them into a UserProfile object and return the list of all user profiles.
    *
    * */

    CompletableFuture<String> fetchUserInfo(String userId) {
        return supplyAsync(() -> {
            return userId + "-Info";
        });
    }

    CompletableFuture<String> fetchUserSettings(String userId) {
        return supplyAsync(() -> {
            return userId + "-Setting";
        });
    }

    Map<String, UserProfile> createUserProfile() {
        List<String> users = List.of("user1", "user2");
        /*
        *
               List<CompletableFuture<UserProfile>> userProfileFutures = users.stream()
                .map(userId ->
                        fetchUserInfo(userId)
                                .thenCombine(fetchUserSettings(userId),
                                        (info, settings) -> new UserProfile(userId, info, settings))
                )
                .collect(Collectors.toList());

        // Wait for all to complete
        CompletableFuture<Void> allDone = allOf(userProfileFutures.toArray(new CompletableFuture[0]));

        // Combine results
        return allDone.thenApply(v ->
                userProfileFutures.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
        ).join();
        *
        * */
        Map<String, CompletableFuture<String>> infoFutures = users.stream()
                .collect(
                        toMap(
                                user -> user,
                                this::fetchUserInfo
                        ));
        Map<String, CompletableFuture<String>> settingFutures = users.stream()
                .collect(
                        toMap(
                                user -> user,
                                this::fetchUserSettings
                        ));
        CompletableFuture<Void> allProfiles = allOf(infoFutures.values().toArray(new CompletableFuture[0]));
        CompletableFuture<Void> allSettings = allOf(settingFutures.values().toArray(new CompletableFuture[0]));
        allProfiles.join();
        allSettings.join();
        return infoFutures.entrySet().stream().collect(toMap(
                Entry::getKey,
                entry -> new UserProfile(
                        entry.getKey(),
                        infoFutures.get(entry.getKey()).join(),
                        settingFutures.get(entry.getKey()).join())
        ));


    }

    public class UserProfile {
        private String id;
        private String info;
        private String settings;

        UserProfile(String id, String info, String settings) {
            this.id = id;
            this.info = info;
            this.settings = settings;
        }
    }

    /*
    * 🔧 Exercise 1: Format Dates in Parallel (Avoid SimpleDateFormat)
Task:
You are given a list of LocalDateTime objects. Format them to "yyyy-MM-dd HH:mm" using a parallel stream.
    * */

    public String dateFormat(LocalDateTime time) {
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    @Test
    public void shouldFormat() {
        assertThat(dateFormat(LocalDateTime.now())).isEqualTo("2025-08-04 14:16");
    }

    /*
    *
    * 🔧 Exercise 2: Parse and Filter Future Dates in Parallel
Task:
Given a list of date strings ("yyyy-MM-dd"), parse them to LocalDate and filter only the ones after today — in parallel.

*
*/

    public Map<LocalDateTime, Long> aggregateTimestamps() {
        // we aggregate by hour
        // count per aggregate
        return LongStream.of(1760864400, 1760866200, 1760868000)
                .mapToObj(t -> LocalDateTime.ofEpochSecond(t, 0, ZoneOffset.UTC))
                // extract hours
                .map(d -> d.withSecond(0).withMinute(0).withNano(0))
                .collect(
                        groupingBy(
                                Function.identity(),
                                counting()
                        )
                );
    }

    @Test
    public void aggregateByHour() {
        Map<LocalDateTime, Long> map = aggregateTimestamps();
        assertThat(map.entrySet()).hasSize(2);
        assertThat(map.values()).containsAll(List.of(1l,2l));
    }

    public List<LocalDate> filterFutureDates(List<String> dateStrings) {
        return dateStrings.parallelStream().map(
                s -> LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        ).filter(
                d -> d.isAfter(LocalDate.now())
        ).toList();
    }

    @Test
    public void shouldFormatFromString() {
        ArrayList<String> actual = new ArrayList<>();
        ArrayList<LocalDate> expected = new ArrayList<>();
        for (int j = 0; j < 100000; j++) {
            for (int i = 1; i < 31; i++) {
                actual.add("2025-07-" + (i < 10 ? "0" + i : i));
            }
            for (int i = 1; i < 31; i++) {
                actual.add("2025-09-" + (i < 10 ? "0" + i : i));
                expected.add(LocalDate.of(2025, Month.SEPTEMBER, i));
            }
        }
        assertThat(filterFutureDates(actual)).isEqualTo(expected);
    }


    /*
    *
    * 🔧 Exercise 3: Safely Use SimpleDateFormat in Threads
Task:
Write a method that uses SimpleDateFormat safely in a multithreaded context using ThreadLocal<SimpleDateFormat>.
*/
    public Date parseWithThreadLocal(String date) throws ParseException { // todo Date is thread-safe ? I think DateTime is threadsafe and not Date
//    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
//    LocalDate.parse(date, format);
//    return Date.from(Instant.parse(date)).setTime();
        // TODO
        ThreadLocal<SimpleDateFormat> formatter =
                ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
        return formatter.get().parse(date);
    }
//Use "yyyy-MM-dd" format.


    /*
    *
    * Exercise 4: Count Events Per Day Using ZonedDateTime in Parallel
Task:
You have a list of ZonedDateTime event timestamps (from multiple time zones).
Group and count how many events occurred on each local day.

*/
    public Map<LocalDate, Long> countEventsPerDay(List<ZonedDateTime> timestamps) {
        return timestamps.parallelStream()
                .collect(
                        groupingByConcurrent(
                                ZonedDateTime::toLocalDate,
                                counting()
                        )
                );
    }
//Must work with parallel streams and ensure safe zone conversions.

    @Test
    public void shouldGroupEvents() {
        ZonedDateTime date1 = ZonedDateTime.parse("2025-08-04T00:00:00+02:00");
        ZonedDateTime date2 = ZonedDateTime.parse("2025-08-03T00:00:00+02:00");
        List<ZonedDateTime> actual = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            for (long j = 0; j < 2; j++) {
                actual.add(date1);
            }
            for (long j = 0; j < 1; j++) {
                actual.add(date2);
            }
        }

        assertThat(countEventsPerDay(actual)).isEqualTo(Map.of(
                date1.toLocalDate(), 2000L,
                date2.toLocalDate(), 1000L
        ));
    }

    /*
    *
    * 🔧 Exercise 5: Generate a Calendar Map
Task:
Generate a map for the next 30 days where each day is mapped to false (indicating no appointments booked yet).
    * */

    public Map<LocalDate, Boolean> initializeEmptyCalendar() {
        LocalDate today = LocalDate.now();
        return LongStream.range(1, 30)
                .boxed()
                .parallel().collect(
                        toConcurrentMap(
                                today::plusDays,
                                i -> false
                        )
                );
    }

    @Test
    public void shouldBook() {
        ExecutorService service = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 1000; i++) {
            service.submit(() -> {
                initializeEmptyCalendar();
            });
        }
        service.shutdown();
        try {
            service.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd");
    private ThreadLocal<SimpleDateFormat> threadFormat =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));

    @Test
    public void shouldParseSimpleFormat() throws ParseException, ExecutionException, InterruptedException {
        List<Future> futures = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(5); //todo 5 threads
        for (int i = 0; i < 100; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    return parseSimpleFormat("2025-08-04");
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }));
            futures.add(executorService.submit(() -> {
                try {
                    return parseSimpleFormat("2025-08-05");
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }));
            futures.add(executorService.submit(() -> {
                try {
                    return parseSimpleFormat("2025-08-06");
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        assertThat(parseSimpleFormat("2025-08-06")).isEqualTo(from(LocalDate.of(2025, Month.AUGUST, 06)));
        assertThat(futures.get(0).get()).isEqualTo(from(LocalDate.of(2025, Month.AUGUST, 04)));
        assertThat(futures.get(1).get()).isEqualTo(from(LocalDate.of(2025, Month.AUGUST, 05)));
        assertThat(futures.get(2).get()).isEqualTo(from(LocalDate.of(2025, Month.AUGUST, 06)));
        assertThat(futures.get(3).get()).isEqualTo(from(LocalDate.of(2025, Month.AUGUST, 04)));
        assertThat(futures.get(4).get()).isEqualTo(from(LocalDate.of(2025, Month.AUGUST, 05)));
        assertThat(futures.get(5).get()).isEqualTo(from(LocalDate.of(2025, Month.AUGUST, 06)));
    }

    private Date from(LocalDate d) {
        return Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public Date parseSimpleFormat(String date) throws ParseException {
        // TODO remember simpleFormat.parse(date) -> from string to date
        // simpleFormate.format(date) -> from date to string
        return threadFormat.get().parse(date); // or simpleFormat & test fails
    }

    @Test
    public void shouldShareCalendar() throws ExecutionException, InterruptedException {
        List<Future> futures = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(5); //todo 5 threads
        for (int i = 0; i < 2; i++) {
            futures.add(executorService.submit(() -> {
                return shareCalendar(1754294400L); // August 6th, 2025
            }));
            futures.add(executorService.submit(() -> {
                return shareCalendar(1754419200L); // 2025-08-05
            }));
            futures.add(executorService.submit(() -> {
                return shareCalendar(1754265600L); // August 4, 2025
            }));
        }
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        assertThat(shareCalendar(1754265600L)).isEqualTo("2025-08-04");
        assertThat(futures.get(0).get()).isEqualTo("2025-08-04");
        assertThat(futures.get(1).get()).isEqualTo("2025-08-05");
        assertThat(futures.get(2).get()).isEqualTo("2025-08-04");
        assertThat(futures.get(3).get()).isEqualTo("2025-08-04");
        assertThat(futures.get(4).get()).isEqualTo("2025-08-05");
        assertThat(futures.get(5).get()).isEqualTo("2025-08-04");
    }

    private Calendar calendar = Calendar.getInstance();

    public String shareCalendar(Long timestamp) {
        synchronized (calendar) {
            calendar.setTimeInMillis(timestamp * 1000);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            return format.format(calendar.getTime());
        }
    }

    /*
    *
    * Exercise 3: Concurrent Formatting with DateTimeFormatterBuilder
Task:
DateTimeFormatter instances are immutable and thread-safe, but a DateTimeFormatterBuilder is not.

Simulate multiple threads concurrently building a DateTimeFormatter with the same DateTimeFormatterBuilder instance.

Explain why this causes problems.

Refactor the code to ensure thread safety by instantiating a new builder per thread.

Exercise 4: Thread-Safe Event Scheduler with LocalDateTime
Task:
You have a shared map of LocalDateTime to event descriptions representing a schedule. Multiple threads concurrently add, remove, and query events.

Implement the event scheduler using a non-thread-safe HashMap and show how race conditions lead to inconsistencies.

Fix it using ConcurrentHashMap.

Bonus: Handle concurrent updates atomically, e.g., using computeIfAbsent.

Exercise 5: Parallel Date Range Filter with Shared Clock
Task:
You use a shared mutable clock instance (e.g., java.time.Clock) to get the current time inside a parallel stream that filters events happening after the current time.

Show why sharing a mutable clock (if you implement one) leads to inconsistent filtering.

Fix the design by passing immutable snapshots or thread-safe clocks.
    * */


    // Parse ISO 8601 timestamps like "2025-08-01T10:15:30Z" into Instant and convert to local zone.
    @Test
    public void shouldFizzBuzz() throws InterruptedException {
        FizzBuzz fizzBuzz = new FizzBuzz(15);
        ExecutorService executorService = Executors.newFixedThreadPool(1000); //  TODO increase pool
        for (int i = 0; i < 1000; i++) {
            executorService.submit(() -> {
                try {
                    fizzBuzz.fizz();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            executorService.submit(() -> {
                try {
                    fizzBuzz.buzz();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            executorService.submit(() -> {
                try {
                    fizzBuzz.fizzbuzz();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            executorService.submit(() -> {
                try {
                    fizzBuzz.number();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);
        assertThat(fizzBuzz.getList().stream().toList()).isEqualTo(
                List.of("1", "2", "fizz", "4", "buzz",
                        "fizz", "7", "8", "fizz", "buzz",
                        "11", "fizz", "13", "14", "fizzbuzz")
        );

    }

    @Test
    public void revision() {
        // string - Date conversion
        String s = "2025-08-06T11:30";
//        LocalDateTime d = LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
//        assertThat(d).isEqualTo(LocalDateTime.of(2025, Month.AUGUST, 6, 11, 30, 00));
        ;
        assertThat(LocalDateTime.parse(s)).isEqualTo(LocalDateTime.of(2025, Month.AUGUST, 6, 11, 30, 00));
//        System.out.println(d.toString());

        ;
        assertThat(LocalDate.parse("2025-08-06")).isEqualTo(LocalDate.of(2025, Month.AUGUST, 6));

        // zoned datetime

//        ZonedDateTime zdt = LocalDateTime.now().atZone(ZoneId.systemDefault());
//        System.out.println(zdt);
        ZonedDateTime actual = ZonedDateTime.parse("2025-08-10T15:52:43.635309+02:00[America/Marigot]");
        ZonedDateTime expected = ZonedDateTime.of(2025, 8, 10, 15, 52, 43, 635309000, ZoneId.of("Europe/Berlin"));

//        System.out.println(actual);
//        System.out.println(expected);

        assertThat(actual).isEqualTo(expected);

//        Set<String> allZones = ZoneId.getAvailableZoneIds();
//        allZones.forEach(System.out::println);


        // Date - long conversion

        // todo: is it in sec or milli sec?

        long nowMs = Instant.now().toEpochMilli();
        Instant instantMs = Instant.ofEpochMilli(nowMs);
        Instant instant = Instant.ofEpochSecond(nowMs / 1000);
//        System.out.println(instantMs);
//        System.out.println(instant);
        LocalDateTime dms = LocalDateTime.ofInstant(instantMs, ZoneId.systemDefault());
        LocalDateTime d = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
//        System.out.println(dms);
//        System.out.println(d);
        // todo string format all date types

        final var inputString = "2025-08-10T15:52";
        String inputPattern = "yyyy-MM-dd'T'HH:mm";
        var parsedDateTime = LocalDateTime.parse(inputString, DateTimeFormatter.ofPattern(inputPattern));
        var parsedTime = LocalTime.parse(inputString, DateTimeFormatter.ofPattern(inputPattern));
        var parsedDate = LocalDate.parse(inputString, DateTimeFormatter.ofPattern(inputPattern));
//        var parsedZoned = ZonedDateTime.parse(inputString, DateTimeFormatter.ofPattern(inputPattern));
        // fails because zone is missing
        System.out.println(parsedDateTime);
        System.out.println(parsedDate);
        System.out.println(parsedTime);
        System.out.println("2025-08-10T15:52:00+02:00");
        System.out.println(Instant.parse("2025-08-10T15:52:00.000Z"));

        // todo convert all date types from instant

        OffsetDateTime zonedInstant = instantMs.atOffset(ZoneOffset.ofHours(-5));
//        System.out.println("-5offset - " + zonedInstant); // shows time of that timezone
        var x = LocalDateTime.ofInstant(zonedInstant.toInstant(), ZoneOffset.ofHours(0));
        // should be same as
        var y = LocalDateTime.ofInstant(instantMs, ZoneOffset.ofHours(0)); // offset zero, means the time of UTC
        // FIXME offset from UTC or local ? => utc

        System.out.printf("%s \n== \n%s%n", x, y);
        // StringBuffer


    }
}