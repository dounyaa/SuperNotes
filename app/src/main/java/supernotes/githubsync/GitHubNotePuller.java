package supernotes.githubsync;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import supernotes.githubsync.GitHubAuthenticator;
import supernotes.githubsync.GitHubRepositoryManager;
import supernotes.management.NoteManagerDataBase;
import supernotes.management.SQLiteDBManager;
import supernotes.notes.ImageNote;
import supernotes.notes.Note;
import supernotes.notes.TextNote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static supernotes.githubsync.GitHubAuthenticator.authenticate;
import static supernotes.githubsync.GitHubAuthenticator.setAuthToken;

/*public class GitHubNotePuller {
    public void pullNotesFromGitHub (String repoName, String fileName) {
        try {
            GitHub github = new GitHubBuilder().build();
            GitHubRepositoryManager manager = new GitHubRepositoryManager();
            String owner = manager.getUsername();
            GHRepository repository = github.getRepository(owner + "/" + repoName);

            // Récupérer le contenu du fichier .adoc
            String adocContent = getFileContent(repository, fileName);

            // Extraire les notes à partir du contenu du fichier .adoc
            ArrayList<Note> notes = extractNotesFromAdoc(adocContent);

            // Affichage des notes
            System.out.println("Notes extraites :");
            for (Note note : notes) {
                if (note instanceof TextNote) {
                    TextNote textNote = (TextNote) note;
                    System.out.println("TextNote: " + textNote.getContent());
                } else if (note instanceof ImageNote) {
                    ImageNote imageNote = (ImageNote) note;
                    System.out.println("ImageNote: " + imageNote.getPath());
                }
            }


            // Sauvegarder les notes dans la base de données SQLite
            saveNotesToSQLite(notes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getFileContent(GHRepository repository, String filePath) throws IOException {
        GHContent content = repository.getFileContent(filePath);
        return content.getContent();
    }

    private static ArrayList<Note> extractNotesFromAdoc(String adocContent) {
        ArrayList<Note> notes = new ArrayList<>();

        Pattern pattern = Pattern.compile("== (Image)? Note (\\d+) ==\n(.*?)(?=\n== (?:Image)? Note \\d+ ==|\n\\z)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(adocContent);

        while (matcher.find()) {
            String type = matcher.group(1);
            int id = Integer.parseInt(matcher.group(2));
            String content = matcher.group(3).trim();

            if ("Image".equals(type)) {
                // Si c'est une note image, récupérer le chemin de l'image
                String imagePath = getImagePath(content);
                if (imagePath != null) {
                    try {
                        byte[] imageBytes = Files.readAllBytes(Paths.get(imagePath));
                        notes.add(new ImageNote(imagePath, imageBytes, "github-pull", null, null));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                // Si c'est une note texte, ajouter le contenu directement
                notes.add(new TextNote(content, "github-pull", null, null));
            }
        }

        return notes;
    }

    private static String getImagePath(String content) {
        Pattern pattern = Pattern.compile("image::(.*?)\\[.*?]");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }


    private static void saveNotesToSQLite(ArrayList<Note> notes) {
        SQLiteDBManager dbManager = new SQLiteDBManager(); // Créer une instance de votre gestionnaire de base de données

        for (Note note : notes) {
            if (note instanceof TextNote) {
                TextNote textNote = (TextNote) note;
                dbManager.addTextNote(textNote.getContent(), textNote.getTag(), null, null, null);
            } else if (note instanceof ImageNote) {
                ImageNote imageNote = (ImageNote) note;
                dbManager.addImageNote( imageNote.getPath(), imageNote.getContent(), imageNote.getTag(), null, null, null);
            }
        }
    }

    public static void main(String[] args) {

        String token = "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"; // Remplacez par votre token GitHub

        if (authenticate(token)) {
            setAuthToken(token);
            System.out.println("Authentication successful!");
            // Nom du référentiel GitHub
            String repoName = "dounyaa-notes";
            // Nom du fichier à récupérer
            String fileName = "notes.adoc";

            // Création d'une instance de GitHubNotePuller
            GitHubNotePuller notePuller = new GitHubNotePuller();

            // Appel de la méthode pullNotesFromGitHub pour récupérer et sauvegarder les notes
            notePuller.pullNotesFromGitHub(repoName, fileName);

            System.out.println("Notes récupérées et sauvegardées avec succès !");
        } else {
            System.out.println("Authentication failed!");
        }

    }

}*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import supernotes.management.SQLiteDBManager;
import supernotes.notes.ImageNote;
import supernotes.notes.Note;
import supernotes.notes.TextNote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubNotePuller {
    private static final String GITHUB_API_URL = "https://api.github.com";

    public static void pullAndSaveNotesFromGitHub(String owner, String repoName, String fileName) {
        try {
            String asciiDocContent = downloadAsciiDocFile(owner, repoName, fileName);
            ArrayList<Note> notes = extractNotesFromAsciiDoc(asciiDocContent);
            saveNotesToDatabase(notes);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de la récupération des notes depuis GitHub.");
        }
    }

    private static String downloadAsciiDocFile(String owner, String repoName, String fileName) throws IOException {
        URL url = new URL(GITHUB_API_URL + "/repos/" + owner + "/" + repoName + "/contents/" + fileName);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return content.toString();
        }
    }

    private static ArrayList<Note> extractNotesFromAsciiDoc(String asciiDocContent) {
        ArrayList<Note> notes = new ArrayList<>();
        Pattern pattern = Pattern.compile("== (?:Image)? Note (\\d+) ==\\n(.*?)(?=\n== (?:Image)? Note \\d+ ==|\n\\z)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(asciiDocContent);
        while (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            String content = matcher.group(2).trim();
            if (matcher.group().startsWith("== Image")) {
                // Si c'est une note d'image
                notes.add(new ImageNote(null, null, content, "github-pull", null));
            } else {
                // Si c'est une note de texte
                notes.add(new TextNote(content, "github-pull", null, null));
            }
        }
        return notes;
    }

    private static void saveNotesToDatabase(ArrayList<Note> notes) {
        SQLiteDBManager dbManager = new SQLiteDBManager();
        for (Note note : notes) {
            if (note instanceof TextNote) {
                TextNote textNote = (TextNote) note;
                dbManager.addTextNote(textNote.getContent(), textNote.getTag(), null, null, null);
            } else if (note instanceof ImageNote) {
                ImageNote imageNote = (ImageNote) note;
                dbManager.addImageNote(imageNote.getPath(), imageNote.getContent(), imageNote.getTag(), null, null, null);
            }
        }
        System.out.println("Les notes ont été récupérées depuis GitHub et sauvegardées dans la base de données.");
    }

    public static void main(String[] args) {
        // Spécifiez le propriétaire du dépôt, le nom du dépôt et le nom du fichier AsciiDoc
        String owner = "dounyaa";
        String repoName = "repo-test";
        String fileName = "test.adoc";
        // Appel de la fonction pour récupérer et sauvegarder les notes depuis GitHub
        pullAndSaveNotesFromGitHub(owner, repoName, fileName);
    }
}

