package CashFlowy.service.auth;

/**
 * Strategy that authenticates against in-memory hardcoded credentials.
 * Keeps the same behavior as the previous implementation in LoginController.
 */
public class HardcodedAuthenticationStrategy implements AuthenticationStrategy {

    private final String expectedUsername;
    private final String expectedPassword;

    public HardcodedAuthenticationStrategy(String expectedUsername, String expectedPassword) {
        this.expectedUsername = expectedUsername;
        this.expectedPassword = expectedPassword;
    }

    @Override
    public boolean authenticate(String username, String password) {
        return expectedUsername.equals(username) && expectedPassword.equals(password);
    }
}
