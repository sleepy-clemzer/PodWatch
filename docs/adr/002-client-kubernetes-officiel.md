# ADR-002 — Client Kubernetes : client Java officiel

- **Statut** : Accepté
- **Date** : 26/11/2026
- **Décideur** : owner du projet

## Contexte
PodWatch doit lire l'état de plusieurs clusters (pods, nodes, events, logs…), suivre leurs changements en temps réel et, plus tard, exécuter quelques actions contrôlées. Deux clients Java matures existent : le client officiel `io.kubernetes:client-java` (projet kubernetes-client) et Fabric8.

## Décision
Utiliser **`io.kubernetes:client-java`**, avec `client-java-extended` si nécessaire. Version de départ : **28.0.0**, à aligner avec la version des clusters cibles (compatibilité n-2).

Les lectures fréquentes passent par des **informers** (`SharedInformerFactory`) : un cache local par cluster, alimenté par des watch.

## Conséquences
- ➕ Client de référence du projet Kubernetes, conforme à la stack imposée.
- ➕ Les modèles (`V1Pod`, …) sont générés à partir de l'OpenAPI K8s et suivent donc fidèlement l'API.
- ➖ L'API est plus verbeuse que le DSL Fabric8. On l'encapsule dans des adaptateurs par domaine, de sorte que le reste du code ne dépende que de nos DTO.
- ➖ Chaque version majeure peut casser les modèles générés. Les montées de version se font par PR dédiée, validées par les tests d'intégration k3s.
