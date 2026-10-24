# LPELIA Accueil pour Android

Cette application Android ouvre le service officiel LPELIA Accueil dans une WebView sécurisée : https://lpelia-accueil.onrender.com.

## Récupérer l'APK

1. Ouvrir l'onglet **Actions** du dépôt GitHub.
2. Sélectionner **Build LPELIA Accueil APK** et vérifier que le workflow se termine avec succès.
3. Télécharger `LPELIA-Accueil.apk` dans **Releases** ou dans les artefacts de l'exécution.

Le workflow se lance automatiquement lorsque les fichiers Android changent sur `main`. Il peut aussi être lancé depuis Actions avec **Run workflow**.

## Installation

Télécharger l'APK sur Android, ouvrir le fichier puis autoriser l'installation depuis cette source si le téléphone le demande.

## À savoir

- Une connexion Internet est nécessaire : l'IA et le site restent hébergés en ligne.
- Le micro peut être autorisé pour les fonctions vocales.
- L'APK créé est une version debug destinée aux tests, pas une version signée pour Google Play.
