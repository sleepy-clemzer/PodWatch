/**
 * Client HTTP minimal vers le backend PodWatch.
 * Toutes les URL sont relatives (même origine, ADR-008) : aucun credential ni URL d'infrastructure côté navigateur.
 */

/** Erreur au format RFC 9457 renvoyée par le backend. */
export interface ProblemDetail {
  type?: string;
  title?: string;
  status: number;
  detail?: string;
  instance?: string;
}

export class ApiError extends Error {
  readonly problem: ProblemDetail;

  constructor(problem: ProblemDetail) {
    super(problem.title ?? `Erreur HTTP ${problem.status}`);
    this.name = 'ApiError';
    this.problem = problem;
  }
}

export async function getJson<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(path, {
    ...init,
    headers: { Accept: 'application/json', ...init?.headers },
    credentials: 'same-origin',
  });

  if (!response.ok) {
    let problem: ProblemDetail = { status: response.status };
    try {
      problem = { ...problem, ...((await response.json()) as Partial<ProblemDetail>) };
    } catch {
      // Corps absent ou non JSON : on garde le statut seul.
    }
    throw new ApiError(problem);
  }

  return (await response.json()) as T;
}
