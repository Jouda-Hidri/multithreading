//package revolut;
//
//import revolut.kata1.Account2;
//
//public class Transfer {
//
//    public void transfer(Account2 from, Account2 to, long amount) {
//        if (from.hashCode() < to.hashCode()) {
//            from.lock.lock();
//            to.lock.lock();
//        } else {
//            to.lock.lock();
//            from.lock.lock();
//        }
//        try {
//            from.withdraw(amount);
//            to.deposit(amount);
//        } finally {
//            from.lock.unlock();
//            to.lock.unlock();
//        }
//    }
//}
