# Cahier des charges — LeafTracker Complete

## Décisions
- Application Android complète, hors ligne, sans IA.
- LeafGraph automatique et manuel.
- Espaces privé, professionnel et global.
- Sécurité prioritaire : PIN obligatoire, chiffrement local, verrouillage et sauvegardes chiffrées.
- LeafCalendar multi-calendriers inspiré des usages de TimeTree, sans dépendance ni synchronisation TimeTree.

## Modules cibles
LeafIdentity (profils et objectifs), LeafSocial (famille, amis, collègues, relations et groupes), LeafGraph (éditeur graphique et vues automatiques), LeafNotes (notes et recherche), LeafCalendar (calendriers, événements, récurrences, participants et rappels), LeafSecurity, LeafBackup, LeafExport, LeafArchive, LeafTimeline, LeafTasks, LeafMedia et LeafStatistics.

## Version 0.1.0
PIN obligatoire, coffre AES-GCM, verrouillage manuel et en arrière-plan, fiches d'identité CRUD, relations, filtre privé/professionnel, vue réseau textuelle, événements locaux CRUD et notes avec recherche simple.

## Limites de la 0.1.0
Pas encore d'éditeur graphique libre, biométrie, vues calendaires avancées, récurrences, rappels, export/import chiffré ni autorisations fines par champ. La récupération du coffre doit être développée avant d'y stocker des données irremplaçables.