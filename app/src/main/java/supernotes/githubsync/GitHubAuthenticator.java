package supernotes.githubsync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

public class GitHubAuthenticator {
    private static final String GITHUB_API_URL = "https://api.github.com/user";


    private static String authToken;

    public static String getAuthToken() {
        return authToken;
    }

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static boolean isTokenSet() {
        return authToken != null && !authToken.isEmpty();
    }

    public static boolean authenticate(String token) {
        try {
            if (isTokenSet()) {
                return true; // Si le token est déjà enregistré, l'authentification est réussie
            }

            URL url = new URL(GITHUB_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestProperty("Authorization", "Bearer " + token);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                setAuthToken(token); // Enregistrer le token si l'authentification réussit
                System.out.println("Authentification réussie");
                return true;
            } else {
                System.out.println("Échec de l'authentification");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /*public static void main(String[] args) {
        // Exemple d'utilisation
        String token = "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"; // Remplacez par votre token GitHub

        if (authenticate(token)) {
            setAuthToken(token);
            System.out.println("Authentication successful!");
            // Code pour la synchronisation avec GitHub
        } else {
            System.out.println("Authentication failed!");
        }
    }*/
}
