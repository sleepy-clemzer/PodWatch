package io.podwatch.common.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Format d'erreur unique de l'API : RFC 9457 « Problem Details » ({@code application/problem+json}).
 *
 * <p>{@link ResponseEntityExceptionHandler} traduit déjà les exceptions standard de Spring MVC
 * (400, 404, 405…). On ajoute ici :
 * <ul>
 *   <li>un filet de sécurité pour toute exception non prévue : réponse 500 <strong>générique</strong>,
 *       sans message ni stack trace (pas de fuite d'information interne), détail complet dans les logs ;</li>
 *   <li>les refus d'accès levés dans les contrôleurs (sécurité par méthode, Phase 6) → 403.</li>
 * </ul>
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setTitle("Accès refusé");
        return problem;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Erreur inattendue", ex);
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Erreur interne");
        problem.setDetail("Une erreur inattendue est survenue. Consultez les logs du serveur.");
        return problem;
    }
}
