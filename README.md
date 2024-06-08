# SuperNotes

| __GitHub Actions__ | [![Build](https://github.com/dounyaa/SuperNotes/actions/workflows/gradle.yml/badge.svg)](https://github.com/dounyaa/SuperNotes/actions/workflows/gradle.yml) |
| :--- | :--- |
| __Version__ | [![Tag](https://img.shields.io/badge/Tag-v1.0-blue)](https://github.com/dounyaa/SuperNotes/compare/Ollama_API_Integration?expand=1) |
| __SonarCloud__ | [![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=dounyaa_SuperNotes&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=dounyaa_SuperNotes) [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=dounyaa_SuperNotes&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=dounyaa_SuperNotes) [![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=dounyaa_SuperNotes&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=dounyaa_SuperNotes) [![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=dounyaa_SuperNotes&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=dounyaa_SuperNotes) [![Bugs](https://sonarcloud.io/api/project_badges/measure?project=dounyaa_SuperNotes&metric=bugs)](https://sonarcloud.io/summary/new_code?id=dounyaa_SuperNotes) [![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=dounyaa_SuperNotes&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=dounyaa_SuperNotes) [![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=dounyaa_SuperNotes&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=dounyaa_SuperNotes) [![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=dounyaa_SuperNotes&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=dounyaa_SuperNotes) |
| __Licence__ | [![license: Apache 2.0](https://img.shields.io/badge/license-Apache_2.0-green)](LICENSE) |


## Description

SuperNote est une application de gestion de notes, offrant une gamme étendue de fonctionnalités pour répondre aux besoins de tous les utilisateurs, des particuliers aux professionnels. Cette application permet de créer, organiser, filtrer, exporter et synchroniser les notes sur différents supports, offrant ainsi une flexibilité maximale dans la gestion des informations.

---

## Exigences

- Java (version 21)
- Gradle (version 8.4)

---

## Installation

1. **Clonage du Projet :**

   ```bash
   git clone https://github.com/dounyaa/SuperNotes.git
   ```

2. **Compilation :**
- Sous (macOS/Linux)
   ```bash
   cd SuperNotes
   ./gradlew build
   ```
- Sous Windows
   ```bash
   cd SuperNotes
   gradlew.bat build
   ```

3. **Exécution :**
- Sous (macOS/Linux)
   ```bash
   ./gradlew run
   ```
- Sous Windows
   ```bash
   gradlew.bat run
   ```

---
## Version v0.2

La version v0.2 de SuperNote propose les fonctionnalités suivantes :

### Création et Géstion des Notes

- **Créer une Note de type texte avec un Tag :** Permet de créer une nouvelle note avec un contenu spécifié et l'associe à un tag.
  ```bash
  sn add "Contenu de la note" --tag mon_tag

- **Créer une Note de type image avec un Tag :** Permet de créer une nouvelle note avec un contenu spécifié et l'associe à un tag.
  ```bash
  sn add "chemin/vers/image.png" --tag "mon_tag"

- **AEnregistrer les Notes dans une Base de Données SQLite :**  Stocke toutes les notes et leurs tags associés dans une base de données SQLite.

- **Supprimer les notes par Tag :** Permet de supprimer toutes les notes associées à un tag spécifique
  ```bash
  sn delete --tag "mon_tag_a_supprimer"
  
- **Supprimer une note par Id :** Permet de supprimer une note par Id.
   ```bash
   sn delete --tag "mon_tag_a_supprimer"
   
- **Afficher les notes :**
   ```bash
   sn show notes

### Générer un Rapport de Notes

- **Générer un Rapport de Toutes les Notes :** Crée un rapport sous format PDF contenant toutes les notes existantes.
  ```bash
  sn export --all "chemin/vers/fichier.pdf"

- **Filtrer par Tag pour Générer le Rapport :** Génère un rapport PDF basé sur les notes associées à un tag spécifique.
  ```bash
  sn export --tag "mon_tag" "chemin/vers/fichier.pdf"

- **Filtrer par Mot dans le Contenu pour Générer le Rapport :** Crée un rapport PDF basé sur les notes contenant un mot spécifique.
  ```bash
  sn export --word "mot_a_chercher" "chemin/vers/fichier.pdf"

**Fermer l'Application :**
    ```bash
    exit

- Cette version offre la possibilité de créer, gérer, filtrer et générer des rapports pour vos notes, en utilisant des tags et des mots-clés pour une organisation avancée et une gestion efficace.
---

## Version v0.3

La version v0.3 de SuperNote propose les fonctionnalités suivantes :

## Intégration de l'API de Notion
#### Prérequis
- **Clé API Notion :** Pour bénéficier des fonctionnalités intégrées de Notion, une clé API Notion est nécessaire. Vous pouvez obtenir votre clé API depuis votre compte Notion afin d'accéder à ces fonctionnalités. Pour créer votre clé API :

- Accédez à cette page : https://www.notion.so/my-integrations.
- Cliquez sur "New Integration".
- Nommez votre intégration "SuperNotes" et appuyez sur "Submit".
- Félicitations ! Vous avez désormais votre clé API. Conservez-la en lieu sûr, vous en aurez besoin pour configurer SuperNotes avec Notion.

De plus, pour configurer correctement votre page Notion avec SuperNotes :

- Connectez-vous à Notion via ce lien : https://www.notion.so.
- Créez une nouvelle page en cliquant sur "Add New Page" dans le menu de gauche.
- Sur cette nouvelle page, cliquez sur les trois points en haut à droite.
- Descendez jusqu'à trouver "Add connections", cliquez dessus.
- Recherchez "SuperNotes" (il doit avoir le même nom que celui utilisé pour la clé API).

Dans l'URL de la page Notion que vous venez de créer, copiez les 32 derniers caractères après le dernier "/" : par exemple, si l'URL est https://www.notion.so/ma-page-**8210366e115d4a8d8f20d151c7e95308**, l'ID de la page est 8210366e115d4a8d8f20d151c7e95308. Conservez cet ID avec votre clé API, vous en aurez besoin pour l'intégration.

Ces étapes garantissent la liaison efficace entre SuperNotes et votre espace Notion, offrant ainsi une utilisation optimale des fonctionnalités.

#### Utilisation

- **Créer une Note sur Notion:** Ajoute la fonctionnalité de création de notes directement sur Notion en utilisant la commande suivante.
  ```bash
  sn notion create "Contenu de la note"

- **Mettre à jour le Contenu d'une Note sur Notion :** Permet de mettre à jour le contenu d'une note existante sur Notion en utilisant le contenu actuel de la note et le nouveau contenu spécifié :
  ```bash
  sn notion update "Ancien contenu de la note" --note "Nouveau contenu de la note"

- **Enregister le Contenu d'une Page Notion dans Supernotes :** Permet de récupérer le contenu d'une page Notion spécifique :
  ```bash
  sn notion get --page "ID_de_la_page_Notion"

- **Exporter les notes créer sur notion :** Permet de créer un fichier avec toutes les notes créer sur notion avec supernotes
  ```bash
  sn export --tag "notion" "chemin/vers/fichier.pdf"

- **AEnregistrer les Notes dans une Base de Données SQLite :**  Stocke toutes les notes créer sur notion avec un id_page et parent_id_page.

## Synchroniser les notes avec Google Drive

#### Prérequis

- SuperNote est actuellement conçu pour une utilisation locale et n'est pas destiné à être accessible au grand public. Pour utiliser la synchronisation avec Google Drive dans cet environnement restreint, il faut suivre les étapes suivantes :

  * Créer un compte Google : Chaque utilisateur doit posséder un compte Google valide pour synchroniser ses notes avec Google Drive.

  * Configurer l'API Google Drive : Pour activer la synchronisation, l'administrateur de l'application devra ajouter les comptes Google Drive des utilisateurs à la liste des utilisateurs autorisés au niveau de Google Cloud Platforme .

  * Accès aux comptes Google dans SuperNote : Une fois que les utilisateurs ont ajouté leurs comptes Google autorisés .

  * Tests dans un environnement local : Lorsqu'un utilisateur teste l'application pour la première fois et recherche le fichier exporté dans son compte Google Drive, il recevra un lien au niveau de termial qui va lui permettre de connecter son compte Google pour cette première utilisation. Par la suite, une fois que le compte est synchronisé, l'utilisateur n'aura plus besoin de réaliser cette action et les notes sont enrigistrés automatiquement dans son compte Google Drive.


#### Utilisation

- **Créer une Note de type texte:** 
  ```bash
  sn add "Contenu de la note"

- **Ajouter une Note de type texte avec un Tag :** 
  ```bash
  sn add "Contenu de la note" --tag mon_tag

- **Créer une Note de type Image:** 
  ```bash
  sn add "chemin/vers/image.png"

- **Générer un Rapport de Toutes les Notes :** Crée un rapport sous format PDF contenant toutes les notes existantes.Spécifié et l'associe à un tag.

  ```bash
  sn export --all "chemin/vers/fichier.pdf"

- **Filtrer par Tag pour Générer le Rapport :** Génère un rapport PDF basé sur les notes associées à un tag spécifique.

  ```bash
  sn export --tag "mon_tag" "chemin/vers/fichier.pdf"

-** Une fois le fichier qui contient les toutes les notes ou seulement les notes avec un tag spécifique est génére ça nous a affiche :

```
Connexion à SQLite établie.
Exportation terminée : chemin/vers/fichier.pdf
Please open the following address in your browser:
  https://accounts.google.com/o/oauth2/auth?access_type=offline&client_id=350708336561-evh4reon8308b2hkvtgodv0204d2hj81.apps.googleusercontent.com&redirect_uri=http://localhost:8081/Callback&response_type=code&scope=https://www.googleapis.com/auth/drive.file
File ID: 1aQzC9lhs1jWMLfGi2bOBzvjvo0T9v8R0
Fichier téléchargé sur Google Drive
notes exporter avec succès !

```

- Il faut cliquer sur ce lien pour autoriser la synchronisation avec le compte Google de l'utilisateur. Une fois l'autorisation accordée, les fichiers exportés à l'aide de la commande `sn export --tag "mon_tag" "chemin/vers/fichier.pdf"`, par exemple, seront automatiquement disponibles dans son espace Google Drive.


## Commande Help

- **Afficher l'Aide :**  La commande help peut être utilisée pour afficher les commandes disponibles et leurs descriptions.
  ```bash
  sn --help

## Version v0.4

La version v0.4 de SuperNote propose les fonctionnalités suivantes :

## Intégration de l'API Google Calendar

#### Prérequis

- SuperNote est actuellement conçu pour une utilisation locale et n'est pas destiné à être accessible au grand public. Pour utiliser la synchronisation avec Google Calendar, il faut suivre les étapes suivantes :

  * Créer un compte Google : Chaque utilisateur doit posséder un compte Google valide.

  * Configurer l'API Google Calendar : Pour activer la synchronisation, l'administrateur de l'application devra ajouter les comptes Google Drive des utilisateurs à la liste des utilisateurs autorisés au niveau de Google Cloud Platforme .

  * Tests dans un environnement local : Lorsqu'un utilisateur teste l'application pour la première fois, il recevra un lien au niveau de termial qui va lui permettre de connecter son compte Google pour cette première utilisation. Par la suite, une fois que le compte est synchronisé, l'utilisateur n'aura plus besoin de réaliser cette action.


#### Utilisation

- **Ajouter une Note avec Rappel :** Ajoutez des rappels pour des notes spécifiques avec une date et une heure précises.
  ```bash
  sn add "Contenu de la note" --tag "tag" --reminder "YYYY-MM-DD HH:mm"

- **Suppression des rappels pour une Note par Tag :** Supprimez tous les rappels associés à une note spécifique par son tag.
  ```bash
  sn delete --reminder --tag "tag"

- **Afficher des rappels pour une Note par Tag :** Afficher tous les rappels associés à une note spécifique par son tag.
  ```bash
  sn get --reminder --tag "tag"

## Exportation des notes en texte brut

- **Exporter les notes en texte brut :** Permettre aux utilisateurs d'exporter leurs notes dans un fichier texte (.txt) pour une compatibilité maximale avec d'autres applications et systèmes.
  ```bash
  sn export --text "chemin/vers/fichier.txt"


## Utilisation de l'extension SuperNote dans Visual Studio Code

#### Prérequis

Cette fonctionnalité vise à offrir aux utilisateurs la possibilité de gérer leurs notes directement depuis le terminal de Visual Studio Code en utilisant les commandes spécifiques de SuperNote. L'objectif principal de cette intégration est d'améliorer la gestion et l'organisation des informations pendant les sessions de programmation.

#### Utilisation

Pour exécuter l'extension Visual Studio Code :

- **Téléchargez le dossier compressé nommé "Jarunner" qui se trouve dans le repertoire SuperNote, ainsi que le fichier `app-all.jar` qui se trouve dans `SuperNotes/app/build/libs`.**

- **Décompressez le dossier compressé "Jarunner" et placez le dans le dossier des extensions de votre Visual Studio Code sur votre ordinateur.**

- **Pour utubuntu :**

- Utilisez ubutun utilisez la commande : "sudo cp -r jarunner /shemin ou le fichier extension se trouve " dans mon cas c'était `sudo cp -r jarunner /usr/share/code/resources/app/extensions/`

- **Ouvrez Visual Studio Code et ajoutez le dossier (jarunner) à votre espace de travail.**

- **Dans le dossier src, il y a un fichier nommé "extension.ts". Ouvrez ce fichier.**

- **Dans le fichier "extension.ts", il y a une ligne comme suit : const command = `java -jar "${'/Users/Downloads/app-all.jar'}"`**

- **Vous devez remplacer le chemin `/Users/engr/Downloads/app-all.jar` par le chemin du fichier `app-all.jar` sur votre ordinateur.**

- Pour Mac, ladresse utilisera un seul '/' et pour Windows, elle utilisera deux '//'.

- Par exemple, adresse Mac '/Users/engr/Downloads/app-all.jar' et  Windows 'C://Users//engr//Downloads//app-all.jar'

- **Après avoir modifié cette adresse, compilez-le en utilisant ctrl+shift+B (Windows & ubuntu) et cmd+shift+B (Mac).**

- **Après une compilation réussie, redémarrez Visual Studio Code et votre extension sexécutera en utilisant la commande suivante :**
        - ctrl+alt+J (Windows & Ubuntu)
        - cmd+alt+J (Mac)

## Lier des Notes (Commande sn link)

- **Lier des notes :** permet de lier une note existante à un ou plusieurs tags spécifiques, tout en lui attribuant un nom distinct pour une référence facile. Cette fonctionnalité est utile pour organiser et catégoriser vos notes de manière significative.
  ```bash
  sn link --id "ID_de_la_note" --tag "tag1" [and/or] "tag2" [...] --name "Nom_de_lien" [--at/--before/--after "Date"]

- **Afficher les Liens :** permet d'afficher les liens précédemment établis entre des notes et des tags, en utilisant le nom de lien spécifié lors de la création.
  ```bash
  sn show --link "Nom_de_lien"

## Documentation

[Lien vers la documentation](https://github.com/dounyaa/SuperNotes/blob/main/app/src/docs/asciidoc/main.adoc)

## Refactoring

### Statistiques SonarQube

Avant le refactoring :

![Statistiques de refactoring](https://github.com/dounyaa/SuperNotes/blob/main/app/src/main/resources/avant_refactoring.png)

Après le refactoring :

![Statistiques de refactoring](https://github.com/dounyaa/SuperNotes/blob/main/app/src/main/resources/apres_refactoring.png)

## Version v1.1

La version v1.1 de SuperNote propose les fonctionnalités suivantes :

## Synchronisation avec GitHub (Commandes sn github)
- **Authentification GitHub :** permet d'authentifier SuperNotes avec votre compte GitHub en utilisant un jeton d'accès personnel.
  ```bash
   sn github auth "<votre_token_d'accès_GitHub>"

- **Pousser les Notes vers GitHub :** permet de télécharger vos notes vers un dépôt GitHub spécifié au format AsciiDoc.
  ```bash
   sn github push "<nom_fichier_AsciiDoc>"

- **Partager les Notes via GitHub :** permet de créer un nouveau dépôt GitHub et d'ajouter des collaborateurs pour partager vos notes.
  ```bash
   sn github share "<nom_dépôt>" "<nom_fichier>" "<collaborateurs>" [--filtre "<type_filtre> <valeur_filtre>"]

- **Tirer les Notes depuis GitHub :** permet de récupérer les notes d'un dépôt GitHub spécifié et de les importer dans SuperNotes.
  ```bash
   sn github pull "<nom_dépôt>" "<nom_fichier>"
  ## Intégration de l'API Ollama

# Intégration de l'API Ollama

## Description

SuperNote intègre désormais l'API Ollama pour permettre aux utilisateurs de rechercher des informations dynamiques directement depuis l'interface en ligne de commande. Cette fonctionnalité offre une méthode rapide et pratique pour obtenir des informations pertinentes sans quitter l'application, même en mode hors ligne.

## Utilisation

### Effectuer une Recherche

Pour effectuer une recherche en utilisant l'API Ollama, utilisez la commande suivante :

```bash
sn --ollama search "terme de recherche"
```

Les résultats de la recherche seront affichés dans un format lisible dans la console.
Ajouter des Résultats de Recherche à une Note

- Après avoir effectué une recherche, vous pouvez ajouter directement un résumé des résultats à une note en utilisant l'ID de la recherche :

```bash
sn add --from-search "ID_de_la_recherche"
```

- Si la recherche ne dispose pas d'un ID, vous pouvez copier/coller les informations trouvées et les ajouter manuellement à une note :

```bash
sn add "Copier/COller le contenu selectionné"
```

Gestion des Erreurs

En cas de problèmes avec la recherche (par exemple, si aucun résultat n'est trouvé ou si la commande est mal formulée), des messages d'erreur clairs seront affichés pour vous guider.
Fonctionnement Hors Ligne

Cette fonctionnalité a été conçue pour fonctionner même sans connexion Internet, garantissant une disponibilité constante des recherches d'informations.


## Conteneurisation et Virtualisation
Pour faciliter le déploiement et les environnements de développement, SuperNote prend en charge la conteneurisation et la virtualisation à l'aide de Docker et Vagrant.

### Docker
Le Dockerfile fourni dans le dépôt du projet vous permet de construire une image Docker pour SuperNote. Pour construire l'image Docker, suivez ces étapes :

- Naviguez jusqu'au répertoire racine du projet.
- Exécutez la commande suivante pour construire l'image Docker :
  ```bash
  docker build -t supernotes . 

- Une fois l'image construite, vous pouvez exécuter un conteneur Docker en utilisant la commande suivante :
  ```bash
   docker run -it -p 8080:8080 supernotes

### Vagrant
Le fichier Vagrantfile inclus dans le dépôt du projet vous permet de créer une machine virtuelle Vagrant pour SuperNote. Pour configurer la machine virtuelle, suivez ces étapes :

- Naviguez jusqu'au répertoire racine du projet.
- Exécutez la commande suivante pour démarrer la machine virtuelle Vagrant :
  ```bash
   vagrant up
  
- Une fois la machine virtuelle démarrée, vous pouvez vous connecter en SSH à la machine en utilisant la commande suivante :
  ```bash
   vagrant ssh
  
Ces options de conteneurisation et de virtualisation offrent un moyen pratique de gérer SuperNote sur différents environnements et plates-formes.

## Groupe

- Dounya Alaoui
- Aya LAKEHAL
