# AUDIT — PodWatch (Phase 0)

> Date de l'audit : 26/11/2026
> Repository : https://github.com/sleepy-clemzer/PodWatch (branche `main`)
> Statut : **en attente de validation**. Aucun code n'a été modifié.

---

## 1. État actuel du projet

| Élément | Constat |
|---|---|
| Commits | 1 seul : `366ef05 Initial commit` (26/11/2026, 18:33) |
| Branches | `main` uniquement |
| Fichiers | 1 seul : `README.md`, qui contient uniquement `# PodWatch` |
| Code backend | Aucun |
| Code frontend | Aucun |
| Build (Maven, npm) | Aucun `pom.xml`, aucun `package.json` |
| Docker / Helm / K8s | Aucun Dockerfile, chart ou manifeste |
| CI/CD | Aucun workflow `.github/workflows/` |
| Tests | Aucun |
| Documentation | Aucune (hors titre du README) |
| Licence | Aucune |
| `.gitignore` | Absent |

**Conclusion : c'est un projet greenfield.** Il n'y a ni code ni dépendance à préserver ou migrer. Seul le `README.md` existe : il sera enrichi, pas remplacé.

Conséquence positive : aucune dette technique, et on peut choisir librement les versions.
Conséquence à gérer : toutes les décisions d'architecture sont à prendre maintenant. C'est l'objet d'`ARCHITECTURE.md`.

---

## 2. Technologies identifiées

### 2.1 Dans le repository
Aucune : le repository ne contient que du Markdown.

### 2.2 Stack imposée (prompt maître) et versions proposées

Versions vérifiées le 26/11/2026. Elles seront re-vérifiées et figées au démarrage de la Phase 1.

| Technologie | Version proposée | Justification / source |
|---|---|---|
| Java | **25 (LTS)** | LTS en cours. Spring Boot 4.x supporte Java 17 à 26. Repli possible : Java 21 LTS. |
| Spring Boot | **4.1.x** (dernier stable : 4.1.1, 20/08/2026) | La 4.2.0 était en RC1 en octobre 2026, GA annoncée pour novembre. À réévaluer au démarrage de la Phase 1. La branche 3.5 n'a plus de support OSS depuis le 30/06/2026 : **ne pas partir sur Spring Boot 3**. |
| Kubernetes Java Client (`io.kubernetes:client-java`) | **28.0.0** (29/10/2026) | Client officiel. Sa version majeure suit une version mineure de Kubernetes ; la compatibilité est garantie à n-2. **À croiser avec la version de tes clusters** (inconnue à ce stade). |
| Maven | 3.9.x | Standard Spring Boot. Utilisation du Maven Wrapper (`mvnw`). |
| React | **19.3.x** | 19.3.0 sortie le 09/09/2026, patch 19.3.1 le 14/10/2026. |
| Outil de build frontend | **Vite 8.x** | Standard actuel pour React (Create React App est abandonné). |
| TypeScript | 5.x | Recommandé plutôt que JavaScript : typage des DTO de l'API, plus proche de ta culture Java. |
| PostgreSQL | dernière majeure stable | **Nécessaire** (utilisateurs, rôles, audit, configuration). Version à figer en Phase 1. |
| Prometheus / Loki / Alertmanager / Grafana | versions déjà déployées chez toi | PodWatch les **consomme** via leurs API HTTP ; il ne les installe pas. À confirmer : existent-ils déjà dans tes clusters ? |

### 2.3 Environnement d'audit
- JDK 21, Node 22 et Docker sont disponibles dans mon environnement. `kubectl` et `helm` ne le sont pas.
- **Je n'ai aucun accès à tes clusters PREPROD/PROD**, et c'est voulu. Toute validation réelle d'intégration Kubernetes, Prometheus ou Loki se fera chez toi, ou en CI sur un cluster éphémère (kind/k3s).
- Mon accès au repository est **en lecture seule** : il faudra m'ouvrir l'accès en écriture pour créer des branches et des Pull Requests.

---

## 3. Fonctionnalités existantes

**Aucune.**

---

## 4. Fonctionnalités manquantes

Tout le périmètre du prompt maître est à construire. Le regroupement en epics se trouve dans `BACKLOG.md`.

| Domaine | Manque | Priorité |
|---|---|---|
| Fondations | Structure du monorepo, build Maven et Vite, conventions, CI | MVP |
| Connexion K8s | Registre multi-cluster, credentials, health-check | MVP |
| Ressources K8s | Nodes, namespaces, pods, puis workloads, réseau, stockage, Jobs, HPA, NetworkPolicies | MVP pour nodes/ns/pods/deployments/events ; le reste ensuite |
| Dashboard | Vue globale et filtres | MVP |
| Logs | Logs à la demande, streaming WebSocket, `--previous`, téléchargement | MVP ; Loki ensuite |
| Monitoring | Prometheus, graphiques, historique | Post-MVP |
| Alerting | Détections internes et Alertmanager | Post-MVP |
| Run | Restart/delete contrôlés, exec, runbooks, comparaison PREPROD/PROD | Post-MVP |
| Sécurité | Authentification, RBAC, permissions par environnement, audit | **Authentification minimale dans le MVP** (voir risque R1) |
| Livraison | Docker, Helm, CD PREPROD/PROD | Docker en Phase 1, le reste en Phase 7 |

---

## 5. Produit de référence : OpenLens

La commande `brew install --cask openlens` installe **OpenLens**, le build open source de l'IDE Kubernetes Lens. Le cask Homebrew est désormais marqué *deprecated* (dernière version 6.5.2). Le relais communautaire actif s'appelle **Freelens**.

**Ce qui est à reprendre de ces outils** : navigation par type de ressource, vues liste et détail, logs, shell dans un pod, multi-cluster, métriques intégrées.

**Ce qui rendra PodWatch « mieux » : sa nature différente.** OpenLens et Freelens sont des applications **desktop mono-utilisateur** qui utilisent le kubeconfig personnel de chaque ingénieur. PodWatch sera une application **web, centralisée et multi-utilisateurs** :

| Axe | OpenLens / Freelens | PodWatch |
|---|---|---|
| Déploiement | Installé sur chaque poste | Serveur unique, accessible par navigateur |
| Credentials K8s | kubeconfig de chaque utilisateur sur son poste | Détenus **uniquement par le backend**, jamais transmis au navigateur |
| Droits | Ceux du kubeconfig | RBAC applicatif : rôle × environnement |
| Traçabilité | Aucune | Audit de chaque action |
| Protection PROD | Aucune spécifique | Lecture seule par défaut, confirmation et permission explicites |
| Observabilité | Métriques basiques | Prometheus, Loki et Alertmanager intégrés |
| Run | Outil générique | Comparaison PREPROD/PROD, runbooks, diagnostic guidé |

Ce positionnement guide les priorités : la **sécurité et l'audit sont le cœur de la valeur**, pas une finition.

---

## 6. Risques techniques

| # | Risque | Impact | Probabilité | Mitigation |
|---|---|---|---|---|
| **R1** | **Exposer une interface qui lit les clusters sans authentification** (le plan initial place la sécurité en Phase 6) | Critique | Élevée si l'on suit l'ordre initial | **Proposition : intégrer une authentification minimale et le rôle ReadOnly dès la Phase 2**, avant toute connexion à un vrai cluster. Le RBAC complet reste en Phase 6. |
| R2 | Compte de service K8s trop privilégié | Critique | Moyenne | Un ServiceAccount dédié par cluster, avec un `ClusterRole` en lecture seule **sans accès aux `secrets`**. Un SA séparé pour les actions, désactivé en PROD par défaut. |
| R3 | Fuite des credentials (tokens, CA) | Critique | Faible | Credentials stockés en Secrets K8s montés dans le pod backend, jamais en base, jamais dans les logs ni dans les réponses API. |
| R4 | Charge sur l'API server (polling de nombreux pods) | Élevé | Moyenne | **Informers** (watch et cache local) au lieu de `list` répétés. Pagination côté backend. |
| R5 | Streaming des logs : connexions WebSocket et threads qui s'accumulent | Élevé | Moyenne | Limite de flux par utilisateur, timeout d'inactivité, fermeture explicite du flux K8s à la déconnexion, tests de charge. |
| R6 | Exec dans un container = shell distant en PROD | Critique | — | Livré en dernier, désactivé par défaut, réservé à Admin et PREPROD tant que tu n'as pas décidé autrement, avec audit de la session. |
| R7 | Accès réseau du backend aux API servers PREPROD et PROD (firewall, endpoint privé) | Bloquant | **Inconnue** | À clarifier : où PodWatch sera-t-il déployé ? Comment joint-il chaque cluster ? |
| R8 | Hétérogénéité des stacks Prometheus/Loki (labels, versions) | Moyen | Moyenne | Requêtes PromQL/LogQL centralisées dans un catalogue côté backend et configurables. Aucune requête libre envoyée par le frontend. |
| R9 | Incompatibilité client Java ↔ version K8s | Moyen | Faible | Matrice de compatibilité documentée, tests d'intégration sur k3s/kind à la même version que tes clusters. |
| R10 | Les « trois agents IA » perçus comme autonomes alors qu'ils ne le sont pas | Moyen | — | Voir `PROJECT_PLAN.md §4` : rôles explicites et workflow PR, sans prétention d'orchestration automatique. |
| R11 | Spring Boot 4 est récent (Jakarta EE 11, Spring Framework 7) ; certaines bibliothèques tierces peuvent tarder à suivre | Faible | Faible | Limiter les dépendances tierces. Vérification en Phase 1 (build et tests d'un squelette). |
| R12 | Périmètre très large (17 types de ressources, 7 domaines) | Élevé | Élevée | MVP strict (`ROADMAP.md`) et composants génériques « liste + détail » réutilisables pour toutes les ressources. |

---

## 7. Architecture recommandée (résumé)

Détail dans `ARCHITECTURE.md`.

- **Monolithe modulaire Spring Boot** : un seul backend, découpé en packages par domaine. Pas de microservices.
- **SPA React + TypeScript (Vite)**, servie par Nginx ou embarquée dans le jar (à décider).
- **Backend = unique point d'accès** aux API Kubernetes, Prometheus, Loki et Alertmanager. Le navigateur ne voit jamais de credentials.
- **Un client K8s et un cache d'informers par cluster**, déclarés dans une configuration typée (`podwatch.clusters[]`) avec l'environnement `PREPROD` ou `PROD`.
- **REST** pour la lecture, **WebSocket** pour les logs et les événements temps réel.
- **PostgreSQL + Flyway** : utilisateurs, rôles, permissions, audit.
- **Lecture seule par défaut** : les actions passent par une couche d'autorisation (rôle × environnement), une confirmation et un audit.

---

## 8 à 10. Découpage, priorités, estimations, prochaines étapes

Voir `ROADMAP.md` (phases et critères de sortie) et `BACKLOG.md` (user stories, priorités, estimations).

### Décisions à valider avant la Phase 1
1. **Ordre des phases** : avancer l'authentification minimale en Phase 2 (risque R1).
2. **Client Kubernetes** : client officiel `io.kubernetes:client-java` (recommandé, conforme au prompt) ou Fabric8.
3. **Authentification** : comptes locaux en base, ou SSO OIDC existant (Keycloak, Entra ID, Google…).
4. **Infrastructure cible** : distribution K8s (EKS, GKE, AKS, on-prem), versions des clusters, emplacement de déploiement de PodWatch, stack Prometheus/Loki déjà en place.
5. **Bibliothèque UI et graphiques** (voir `ARCHITECTURE.md §8`).
6. **Accès en écriture au repository** pour travailler par Pull Requests.
