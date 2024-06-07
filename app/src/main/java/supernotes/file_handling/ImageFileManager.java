package supernotes.file_handling;

import supernotes.notes.ImageNote;
import supernotes.helpers.ImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageFileManager {

    private static final String IMAGES_DIRECTORY = "images";

    public void saveImage(ImageNote imageNote) {
        try {
            createDirectoryIfNotExists(IMAGES_DIRECTORY);

            String filePath = imageNote.getPath();
            System.out.println(filePath);
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(imageNote.getContent());
            fos.close();

            System.out.println("Image enregistrée avec succès : " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de l'enregistrement de l'image : " + e.getMessage());
        }
    }

    private void createDirectoryIfNotExists(String directoryName) throws IOException {
        File directory = new File(directoryName);
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                System.out.println("Répertoire créé avec succès : " + directoryName);
            } else {
                throw new IOException("Impossible de créer le répertoire : " + directoryName);
            }
        }
    }

    public void createOrUpdateImageNote(String filePath, byte[] imageData, String tag, String parentPageId, String pageId) {
        try {
            String fileName = new File(filePath).getName(); // Récupérer seulement le nom du fichier
            String destinationPath = IMAGES_DIRECTORY + "/" + fileName; // Chemin relatif à partir du répertoire de destination

            ImageNote imageNote = new ImageNote(destinationPath, imageData, tag, parentPageId, pageId);
            System.out.println(destinationPath);
            saveImage(imageNote); // Utiliser directement la méthode saveImage de cette classe
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Une erreur s'est produite lors de la création ou de la mise à jour de l'image : " + e.getMessage());
        }
    }
}