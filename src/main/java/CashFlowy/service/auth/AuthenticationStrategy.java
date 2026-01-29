package CashFlowy.service.auth;

public interface AuthenticationStrategy {
    boolean authenticate(String username, String password);
}
