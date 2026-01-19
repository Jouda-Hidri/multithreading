import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class Session {
    private final Map<String, UserSession> sessions = new ConcurrentHashMap();
    private final Deque<String> errors = new ConcurrentLinkedDeque<>();
    private final Set<UserSession> createdSession = new HashSet<>();

    public void login(String userId) {
//        sessions.putIfAbsent(userId, new UserSession());
        sessions.computeIfAbsent(userId, u -> new UserSession());
    }

    public int onlineUsers(List<String> userIds) {
        return userIds.stream()
                .filter(sessions::containsKey)
                .collect(Collectors.toSet()).size();
    }

    public void logout(String user) {
        if (sessions.containsKey(user)) {
            sessions.remove(user);
        } else {
            final var message = String.format("user %s is offline", user);
            System.out.println(message);
            errors.add(message);
            throw new RuntimeException();
        }
    }

    public Set<UserSession> getCreatedSession() {
        return createdSession;
    }

    public int getErrors() {
        return errors.size();
    }

    public class UserSession {
        private final long created;

        public UserSession() {
            this.created = System.currentTimeMillis();
            createdSession.add(this);
        }
    }
}
