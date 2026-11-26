-- PodWatch — schéma initial (Phase 1, PW-103)
-- Les données Kubernetes ne sont PAS stockées en base : uniquement utilisateurs, rôles et audit.

-- Utilisateurs applicatifs.
-- password_hash est NULL pour un utilisateur authentifié via OIDC (choix d'authentification en Phase 2).
CREATE TABLE app_user (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    display_name  VARCHAR(200),
    password_hash VARCHAR(255),
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- Rôles applicatifs (référentiel fixe).
CREATE TABLE role (
    id   SMALLINT    PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE
);

INSERT INTO role (id, code) VALUES
    (1, 'ADMIN'),
    (2, 'DEVOPS'),
    (3, 'READONLY');

-- Un rôle PAR environnement : un même utilisateur peut être DEVOPS en PREPROD et READONLY en PROD.
-- Absence de ligne pour un environnement = READONLY (comportement par défaut, voir SECURITY dans ARCHITECTURE.md).
CREATE TABLE user_role (
    user_id     BIGINT      NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    role_id     SMALLINT    NOT NULL REFERENCES role (id),
    environment VARCHAR(20) NOT NULL CHECK (environment IN ('PREPROD', 'PROD')),
    PRIMARY KEY (user_id, environment)
);

-- Journal d'audit : en ajout seul (aucune mise à jour ni suppression exposée par l'application).
CREATE TABLE audit_event (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    occurred_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
    username    VARCHAR(100) NOT NULL,
    environment VARCHAR(20)  CHECK (environment IN ('PREPROD', 'PROD')),
    cluster_id  VARCHAR(100),
    action      VARCHAR(100) NOT NULL,
    target      VARCHAR(500),
    outcome     VARCHAR(20)  NOT NULL CHECK (outcome IN ('SUCCESS', 'DENIED', 'FAILURE')),
    details     JSONB
);

CREATE INDEX idx_audit_event_occurred_at ON audit_event (occurred_at DESC);
CREATE INDEX idx_audit_event_username ON audit_event (username);
