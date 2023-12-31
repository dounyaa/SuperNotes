package supernotes.notionAPI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Scanner;

public class NotionAPI implements NotionApiManager{

    private final String baseUrl = "https://api.notion.com/v1/";
    private final HttpClient httpClient;
    private String apiKey; 
    
    public NotionAPI() {
        this.httpClient = HttpClients.createDefault();
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String createNotionPage(String parentPageId, String propertiesJson) {

        if (apiKey == null || apiKey.isEmpty()) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Veuillez saisir votre clé API Notion : ");
            String userApiKey = scanner.nextLine();
            setApiKey(userApiKey);
        }

        String endpoint = "pages";
        HttpPost request = new HttpPost(baseUrl + endpoint);

        request.addHeader("Authorization", "Bearer " + apiKey);
        request.addHeader("Notion-Version", "2021-05-13");
        request.addHeader("Content-Type", "application/json");

        try {
            // Créez le JSON pour les propriétés de la nouvelle page
            StringEntity requestBody = new StringEntity(propertiesJson);
            request.setEntity(requestBody);

            // Exécution de la requête et récupération de la réponse
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                return EntityUtils.toString(response.getEntity());
            } else {
                // Gestion des erreurs si la requête n'est pas réussie (statut différent de 200)
                System.err.println("Erreur lors de la création de la note sur notion: " + statusCode);
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String retrievePageContent(String pageId) {
        if (apiKey == null || apiKey.isEmpty()) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Veuillez saisir votre clé API Notion : ");
            String userApiKey = scanner.nextLine();
            setApiKey(userApiKey);
        }

        String endpoint = "pages/" + pageId;
        HttpGet request = new HttpGet(baseUrl + endpoint);

        request.addHeader("Authorization", "Bearer " + apiKey);
        request.addHeader("Notion-Version", "2021-05-13");

        try {
            // Exécution de la requête et récupération de la réponse
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                return EntityUtils.toString(response.getEntity());
            } else {
                // Gestion des erreurs si la requête n'est pas réussie (statut différent de 200)
                System.err.println("Erreur lors de la récupération de la page: " + statusCode);
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null; 
        }
    }

    public String updatePageProperties(String pageId, String propertiesJson) {

        if (apiKey == null || apiKey.isEmpty()) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Veuillez saisir votre clé API Notion : ");
            String userApiKey = scanner.nextLine();
            setApiKey(userApiKey);
        }

        String endpoint = "pages/" + pageId;
        HttpPatch request = new HttpPatch(baseUrl + endpoint);

        request.addHeader("Authorization", "Bearer " + apiKey);
        request.addHeader("Notion-Version", "2021-05-13");
        request.addHeader("Content-Type", "application/json");

        try {
            // Ajoutez le JSON des nouvelles propriétés à mettre à jour
            StringEntity requestBody = new StringEntity(propertiesJson);
            request.setEntity(requestBody);

            // Exécution de la requête et récupération de la réponse
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200) {
                return EntityUtils.toString(response.getEntity());
            } else {
                // Gestion des erreurs si la requête n'est pas réussie (statut différent de 200)
                System.err.println("Erreur lors de la mise à jour de la page: " + statusCode);
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}




