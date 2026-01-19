import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

class SessionTest {

    private Session session = new Session();

    @Test
    public void shouldLogin() {
        // given userId
        final String userId = "userId";
        // when login
        session.login(userId);
        // then sessions.contains(userId)
        assertThat(session.onlineUsers(List.of(userId))).isEqualTo(1);
    }

    @Test
    public void shouldLoginMultiple() throws InterruptedException {
        // given multiple userIds
        final String user1 = "user1";
        final String user2 = "user2";
        // when login concurrently
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        for(int i=0; i<100; i++) {
            executorService.submit(() -> session.login(user1));
            executorService.submit(() -> session.login(user1));
            executorService.submit(() -> session.login(user2));
        }
        // then sessions.containsAll(userIds)
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);
        assertThat(session.onlineUsers(List.of(user1, user2))).isEqualTo(2);
        assertThat(session.getCreatedSession().size()).isEqualTo(2); // fails if we use putIfAbsent
    }

    @Test
    public void testLogoutOfflineUser() {
        // given user online
        final String user = "user";
        assertThatThrownBy(() -> {
            // when logout
            session.logout(user);
        });
        // then exception
    }

    @Test
    public void shouldLogoutOnlineUSer() {
        // given user online
        final String user = "user";
        session.login(user);
        // when logout
        session.logout(user);
        // then sessions empty
        assertThat(session.onlineUsers(List.of(user))).isEqualTo(0);
    }

    @Test
    public void shouldLogoutMultiple() throws InterruptedException {
        // given 2 users
        final String user1 = "user1";
        final String user2 = "user2";
        // when both login and one logout
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.submit(() -> {
            session.login(user1);
            session.logout(user1);
        });
        executorService.submit(() -> {
            session.login(user2);
        });
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);
        // then onlineUsers is 1
        assertThat(session.onlineUsers(List.of(user1, user2))).isEqualTo(1);
    }

    @Test
    public void shouldLogoutManyTimes() throws InterruptedException {
        // given 1 user
        final String user1 = "user1";
        // when logout many times
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 1; i++) {
            executorService.submit(() -> {
                session.login(user1);
            });
            executorService.submit(() -> {
                session.logout(user1);
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);
        // then exception
        assertThat(session.getErrors()).isEqualTo(1);
    }

}