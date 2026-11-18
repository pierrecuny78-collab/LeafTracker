# 🌿 LeafTracker 0.5.0

Application Android personnelle et professionnelle, hors ligne, sans compte ni IA, avec coffre local chiffré AES-GCM et verrouillage par PIN.

## Nouveautés 0.5.0
- **LeafGraph** : gestion des cartes, renommage, changement de couleur, duplication, suppression propre des connexions orphelines et modèles prêts à l'emploi (carte mentale, projet, arbre familial, équipe, objectifs, brainstorming).
- **LeafSocial** : relations modifiables, typologie du lien, séparation privé/professionnel, date du dernier contact et historique des interactions (appel, message, rencontre, e-mail).
- **LeafNotes** : carnets, étiquettes, favoris, modification, tri des favoris et recherche globale dans les données.
- **LeafCalendar** : vues mois/semaine/jour/liste, événements, mémos, participants, lieux, récurrences descriptives, sélection des événements visibles sur widget et consultation des anniversaires.
- **LeafTasks** : échéances triées et statut terminé / à faire.
- **LeafGoals** : affichage de la progression.
- **Paramètres** : changement du PIN depuis l'écran principal.
- Correction de l'affichage des noms des modules dans le menu Leaf.
- Données toujours stockées dans le coffre local chiffré ; aucune connexion réseau ajoutée.

## Modules
LeafIdentity · LeafSocial · LeafGraph · LeafCalendar · LeafNotes · LeafGoals · LeafMoments · LeafPlaces · LeafTasks · LeafArchive · LeafTimeline · LeafDashboard.

## Inspirations
- Obsidian Canvas : cartes, connexions nommées, couleurs, organisation visuelle et modèles de toile.
- Canva Whiteboard : modèles de brainstorming et schémas.
- Milanote : carnets visuels, notes, tableaux et organisation d'idées.
- FamilySearch : anniversaires, souvenirs et chronologie familiale.
- Notion : organisation par propriétés, catégories et vues.
- TimeTree : organisation de calendriers et événements partagés (LeafCalendar reste local dans cette version).

## Sécurité et précautions
- PIN de 4 à 8 chiffres, dérivation PBKDF2 et verrouillage après échecs.
- Coffre AES-256-GCM via Android Keystore.
- Espaces Tous / Privé / Professionnel.
- Le widget n'affiche que les événements explicitement marqués comme visibles.
- La désinstallation ou l'effacement des données Android peut rendre le coffre irrécupérable. Faire une sauvegarde avant toute migration importante.
- L'export/import chiffré du coffre et la synchronisation réelle TimeTree ne sont pas encore inclus.

## Compilation
JDK 17, Android SDK 35, Gradle 8.9.

```bash
gradle --no-daemon assembleDebug
```

APK : `app/build/outputs/apk/debug/app-debug.apk`
- Application ID : `com.leafvibe.leaftracker`
- Version : `0.5.0` (versionCode 5)
- Android minimum : API 26

**Taille de l'APK :** la taille du fichier ne mesure pas le nombre de fonctionnalités. Le projet privilégie le code et les données utiles plutôt que l'ajout artificiel de fichiers volumineux.
