## User story

<!-- Ex. : PW-206 — API nodes / namespaces / pods (lien vers docs/BACKLOG.md) -->

## Changements

-

## Tests réalisés

<!-- Ce qui a été exécuté et où (local, CI, cluster kind/k3s…). Un test non exécuté est noté « NON EXÉCUTÉ ». -->

- [ ] Tests unitaires backend
- [ ] Tests d'intégration (Testcontainers)
- [ ] Tests frontend
- [ ] Vérification manuelle (préciser l'environnement)

## Checklist sécurité

- [ ] Aucun secret, token ou credential dans le code, les logs ou les réponses API
- [ ] Toute nouvelle action d'écriture est autorisée (rôle × environnement) **et** auditée
- [ ] Rien ne contourne le mode lecture seule par défaut
- [ ] Aucun impact sur PROD sans approbation explicite

## Documentation

- [ ] README / docs mis à jour si nécessaire
- [ ] ADR rédigé si décision d'architecture
- [ ] CHANGELOG mis à jour
