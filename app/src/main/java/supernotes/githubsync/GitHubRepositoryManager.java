package supernotes.githubsync;

import org.json.JSONObject;
import supernotes.management.NoteManager;
import supernotes.management.NoteManagerDataBase;
import supernotes.notes.Note;
import supernotes.notes.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.nio.file.Files;


public class GitHubRepositoryManager {
    private static final String GITHUB_API_URL = "https://api.github.com";
    private static final String GITHUB_REPOS_ENDPOINT = "/user/repos";
    private final String token;

    public GitHubRepositoryManager() {
        this.token = GitHubAuthenticator.getAuthToken();
    }

    public void createPrivateRepositoryIfNotExists() {
        try {
            if (GitHubAuthenticator.authenticate(token)) {
                if (!hasPrivateRepository()) {
                    String username = getUsername();
                    String repoName = username.toLowerCase() + "-notes"; // Nom du référentiel privé
                    createRepo(repoName, true);
                } else {
                    System.out.println("L'utilisateur possède déjà un référentiel privé.");
                }
            } else {
                System.out.println("L'authentification a échoué !");
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de la création du référentiel.");
        }
    }

    public boolean isRepositoryCreated() {
        try {
            return hasPrivateRepository();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de la vérification de l'existence du référentiel.");
        }
        return false;
    }

    /*public void uploadNotesAsAsciiDoc(ArrayList<Note> notes, String asciiDocFileName) throws IOException {
        if (notes == null || notes.isEmpty()) {
            System.out.println("Aucune note à sauvegarder.");
            return;
        }

        try {
            String username = getUsername();
            asciiDocFileName = asciiDocFileName.endsWith(".adoc") ? asciiDocFileName : asciiDocFileName + ".adoc";

            String owner = username; // Utilisez le nom d'utilisateur comme propriétaire du référentiel
            String repo = username.toLowerCase() + "-notes"; // Utilisez le nom d'utilisateur comme nom du référentiel

            String asciiDocContent = generateAsciiDocContent(notes, username);

            byte[] asciiDocBytes = asciiDocContent.getBytes();

            // Téléverser le fichier AsciiDoc
            uploadFile(owner, repo, asciiDocFileName, asciiDocBytes);
            uploadImages(owner, repo);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'upload du fichier AsciiDoc : " + e.getMessage());
            throw e; // Lancer l'exception IOException à l'appelant
        }
    }*/

    public void uploadNotesAsAsciiDoc(ArrayList<Note> notes, String asciiDocFileName, String repo) throws IOException {
        if (notes == null || notes.isEmpty()) {
            System.out.println("Aucune note à sauvegarder.");
            return;
        }

        try {
            String username = getUsername();
            asciiDocFileName = asciiDocFileName.endsWith(".adoc") ? asciiDocFileName : asciiDocFileName + ".adoc";

            String owner = username; // Utilisez le nom d'utilisateur comme propriétaire du référentiel

            // Vérifier si le fichier existe déjà
            if (fileExistsOnGitHub(owner, repo, asciiDocFileName)) {
                System.out.println("Le fichier existe déjà sur GitHub : " + asciiDocFileName);
                return;
            }

            String asciiDocContent = generateAsciiDocContent(notes, username);

            byte[] asciiDocBytes = asciiDocContent.getBytes();

            // Téléverser le fichier AsciiDoc
            uploadFile(owner, repo, asciiDocFileName, asciiDocBytes);
            uploadImages(owner, repo);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'upload du fichier AsciiDoc : " + e.getMessage());
            throw e; // Lancer l'exception IOException à l'appelant
        }
    }


    public void uploadImages(String owner, String repo) {
        String imagesDirectoryPath = "/Users/dounya/Desktop/SuperNotes_version/SuperNotes/app/images";
        File imagesDirectory = new File(imagesDirectoryPath);

        if (imagesDirectory.exists() && imagesDirectory.isDirectory()) {
            File[] files = imagesDirectory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String fileName = file.getName();
                        if (!fileExistsOnGitHub(owner, repo, fileName)) {
                            byte[] fileContent;
                            try {
                                fileContent = Files.readAllBytes(file.toPath());
                                uploadFile(owner, repo, "images/" + fileName, fileContent);
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.out.println("Une erreur s'est produite lors de la lecture du fichier : " + e.getMessage());
                            }
                        } else {
                            System.out.println("Le fichier existe déjà sur GitHub : " + fileName);
                        }
                    }
                }
            }
        } else {
            System.out.println("Le répertoire images n'existe pas ou n'est pas un répertoire.");
        }
    }

    private boolean fileExistsOnGitHub(String owner, String repo, String fileName) {
        try {
            URL url = new URL(GITHUB_API_URL + "/repos/" + owner + "/" + repo + "/contents/images/" + fileName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + token);

            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String generateAsciiDocContent(ArrayList<Note> notes, String username) {
        StringBuilder asciiDocContent = new StringBuilder();
        for (Note note : notes) {
            if (note instanceof TextNote) {
                asciiDocContent.append("== Note ").append(note.getId()).append(" ==\n");
                asciiDocContent.append(((TextNote) note).getContent()).append("\n\n");
            } else if (note instanceof ImageNote) {
                ImageNote imageNote = (ImageNote) note;
                asciiDocContent.append("== Image Note ").append(note.getId()).append(" ==\n");
                // Encodage du contenu de l'image en Base64
                String encodedImage = Base64.getEncoder().encodeToString(imageNote.getContent());
                asciiDocContent.append("image::").append("https://github.com/").append(username).append("/").append(username.toLowerCase() + "-notes").append("/blob/main/images/").append(new File(imageNote.getPath()).getName()).append("[]\n\n");
            }
        }
        return asciiDocContent.toString();
    }

    private boolean hasPrivateRepository() throws IOException {
        URL url = new URL(GITHUB_API_URL + GITHUB_REPOS_ENDPOINT);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setRequestProperty("Authorization", "Bearer " + token);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("\"private\": true")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void createRepo(String repoName, boolean isPrivate) throws IOException {
        URL url = new URL(GITHUB_API_URL + GITHUB_REPOS_ENDPOINT);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setRequestProperty("Authorization", "Bearer " + token);

        String postData = "{\"name\": \"" + repoName + "\", \"private\": " + isPrivate + "}";
        connection.setDoOutput(true);
        connection.getOutputStream().write(postData.getBytes());

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_CREATED) {
            System.out.println("Référentiel créé avec succès : " + repoName);
        } else {
            System.out.println("Échec de la création du référentiel.");
        }
    }

    public String getUsername() throws IOException {
        URL url = new URL(GITHUB_API_URL + "/user");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        connection.setRequestProperty("Authorization", "Bearer " + token);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString().split("\"login\":")[1].split(",")[0].replace("\"", "").trim();
            }
        }
        return null;
    }

    private void uploadFile(String owner, String repo, String fileName, byte[] fileContent) {
        try {
            URL url = new URL(GITHUB_API_URL + "/repos/" + owner + "/" + repo + "/contents/" + fileName);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestProperty("Authorization", "Bearer " + token);

            JSONObject requestBody = new JSONObject();
            requestBody.put("message", "Upload " + fileName);
            requestBody.put("content", Base64.getEncoder().encodeToString(fileContent));

            connection.setDoOutput(true);
            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(requestBody.toString().getBytes());
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_CREATED) {
                System.out.println("Fichier téléchargé avec succès : " + fileName);
            } else {
                System.out.println("Échec du téléchargement du fichier : " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors du téléchargement du fichier : " + e.getMessage());
        }
    }

}

