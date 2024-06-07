package supernotes.githubsync;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import supernotes.management.SQLiteDBManager;
import supernotes.notes.ImageNote;
import supernotes.notes.Note;
import supernotes.notes.TextNote;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubNotePuller {
    private final String token;

    public GitHubNotePuller() {
        this.token = GitHubAuthenticator.getAuthToken();
    }

    public void pullNotesFromGitHub(String repoName, String fileName) {
        try {
            GitHub github = new GitHubBuilder()
                    .withOAuthToken(token)
                    .build();
            String owner = github.getMyself().getLogin();
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
        try (InputStream inputStream = content.read()) {
            return new String(inputStream.readAllBytes());
        }
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
    }
}