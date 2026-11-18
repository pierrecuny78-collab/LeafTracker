# Architecture — LeafTracker 0.1.0

- UI : Activity Android native et navigation par onglets.
- Sécurité : PBKDF2-HMAC-SHA256, 210 000 itérations, sel aléatoire, temporisation progressive.
- Données : JSON local chiffré AES-256-GCM ; clé Android Keystore ; remplacement atomique.
- Modèles : Identity, Relationship, CalendarEvent, Note ; champs workspace Privé/Professionnel.
- Aucun droit Internet ni transfert automatique.

Évolutions : Room/SQLite et migrations, biométrie, export/import chiffré vérifié, audit local, permissions par champ, éditeur graphique, calendrier avancé et rappels.