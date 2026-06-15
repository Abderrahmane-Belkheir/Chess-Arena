package org.Core.Auth;


import com.github.javakeyring.Keyring;
import lombok.Getter;


import java.util.Optional;

public class TokenStorage {

    private static final String SERVICE_NAME = "ChessArenaLauncher";
    private static final String ACCOUNT_NAME = "ClerkAccessToken";

    @Getter
    private String accessToken;

    /**
     * Saves the token directly into the hardware-backed OS Vault.
     */
    public  void saveRefreshToken(String token) {
        try (Keyring keyring = Keyring.create();){
            keyring.setPassword(SERVICE_NAME, ACCOUNT_NAME, token);
        } catch (Exception e) {
            System.err.println("[OS VAULT ERROR] " + e.getClass().getSimpleName()
                    + " - " + e.getMessage());
        }
    }

    /**
     * Retrieves the saved token string from the vault.
     * Returns null if no token exists.
     */
    public  Optional<String> loadRefreshToken()  {

        try (  Keyring keyring=Keyring.create()){
            return Optional.of(keyring.getPassword(SERVICE_NAME, ACCOUNT_NAME));
        } catch (Exception e) {
            return Optional.empty();
        }
    }


    public  void clearRefreshToken() {
        try (Keyring keyring = Keyring.create()) {
            keyring.deletePassword(SERVICE_NAME, ACCOUNT_NAME);
        } catch (Exception e) {

        }
    }

    protected void setAccessToken(String accessToken){
        this.accessToken=accessToken;
    }

}
