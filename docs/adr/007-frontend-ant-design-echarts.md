# ADR-007 — Frontend : React + TypeScript + Ant Design + ECharts

- **Statut** : Accepté
- **Date** : 26/11/2026
- **Décideur** : owner du projet

## Contexte
L'interface de PodWatch est une console d'exploitation : beaucoup de tableaux filtrables (pods, events, ressources), des fiches détaillées, des graphiques de séries temporelles issus de Prometheus, et des flux temps réel.

## Décision
- **React 19 + TypeScript**, build **Vite**.
- **Ant Design** comme bibliothèque UI.
- **Apache ECharts** (via `echarts-for-react`) pour les graphiques.
- **TanStack Query** pour les données serveur, **React Router** pour la navigation, **@stomp/stompjs** pour le WebSocket.
- Versions exactes figées dans `frontend/package.json` en Phase 1.

## Alternatives écartées
- Mantine : plus légère, mais ses tables sont trop basiques pour notre usage sans ajouter TanStack Table.
- MUI : sa DataGrid avancée est en partie payante.
- Recharts : plus simple, mais moins performant sur les longues séries Prometheus.

## Conséquences
- ➕ Composant `Table` riche (tri, filtres, pagination, colonnes fixes), adapté à une console.
- ➕ ECharts gère des milliers de points, le zoom et le brush nativement.
- ➖ Bundle plus lourd : imports ciblés et découpage par route (lazy loading).
- ➖ Style Ant marqué : le thème est personnalisable via `ConfigProvider` (tokens de design).
