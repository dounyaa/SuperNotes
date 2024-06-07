package supernotes.githubsync;

import supernotes.githubsync.GitHubAuthenticator;
import supernotes.githubsync.GitHubRepositoryManager;
import supernotes.management.NoteManagerDataBase;
import supernotes.notes.Note;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GitHubRepositoryHandler {
    private static final String GITHUB_API_URL = "https://api.github.com";

    private final String token;

    public GitHubRepositoryHandler() {
        this.token = GitHubAuthenticator.getAuthToken();
    }

    public void createRepository(String repoName, ArrayList<Note> notes, String fileName, List<String> collaborators) {
        try {
            if (GitHubAuthenticator.authenticate(token)) {
                GitHubRepositoryManager manager = new GitHubRepositoryManager();
                String owner = manager.getUsername();
                manager.createRepo(repoName, true);
                manager.uploadNotesAsAsciiDoc(notes, fileName, repoName);
                for (String collaborator : collaborators) {
                    addCollaborator(owner, repoName, collaborator);
                }
            } else {
                System.out.println("L'authentification a échoué !");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de la création du référentiel.");
        }
    }




    private void addCollaborator(String owner, String repoName, String collaboratorUsername) {
        try {
            URL url = new URL(GITHUB_API_URL + "/repos/" + owner + "/" + repoName + "/collaborators/" + collaboratorUsername);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Authorization", "Bearer " + token);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                System.out.println("Collaborateur ajouté avec succès !");
            } else {
                System.out.println("Échec de l'ajout du collaborateur.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'ajout du collaborateur : " + e.getMessage());
        }
    }
}
