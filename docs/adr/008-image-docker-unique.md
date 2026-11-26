# ADR-008 — Packaging : une seule image Docker (backend + frontend)

- **Statut** : Accepté
- **Date** : 26/11/2026
- **Décideur** : owner du projet

## Contexte
Le frontend React est une SPA statique. On peut la servir soit dans une image Nginx séparée, soit depuis Spring Boot.

## Décision
Produire **une seule image** `podwatch`, avec un Dockerfile multi-stage :
1. Build du frontend (`npm ci && npm run build`).
2. Build du backend Maven ; les fichiers du frontend sont copiés dans `classpath:/static`.
3. Image finale : JRE 25 minimale, utilisateur non-root, `java -jar`.

Spring Boot sert la SPA. Les routes inconnues hors `/api/**`, `/ws/**` et `/actuator/**` renvoient `index.html`, pour que React Router fonctionne.

`frontend/` et `backend/` restent des projets distincts dans le repo, développés et testés séparément. En dev local, le serveur Vite fait proxy vers le backend.

## Conséquences
- ➕ Un seul déploiement, un seul chart Helm, une seule version à promouvoir de PREPROD à PROD.
- ➕ Même origine : pas de CORS, cookie de session HttpOnly simple, CSP stricte facile à poser.
- ➖ Une modification du frontend seul reconstruit toute l'image. C'est acceptable vu la taille du projet.
- ➖ Le frontend ne peut pas être scalé séparément du backend. Inutile pour une console interne.
