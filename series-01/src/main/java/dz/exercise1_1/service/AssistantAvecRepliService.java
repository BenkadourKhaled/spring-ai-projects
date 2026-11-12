package dz.exercise1_1.service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dz.exercise1_1.dto.ReponseAvecRepli;
import dz.exercise1_1.exception.ServiceIndisponibleException;
import dz.exercise1_1.fournisseur.Fournisseur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AssistantAvecRepliService {

    private static final Logger log = LoggerFactory.getLogger(AssistantAvecRepliService.class);
    private static final Pattern CODE_HTTP = Pattern.compile("^(\\d{3})\\b");

    private final Fournisseur principal;
    private final Fournisseur secours;
    private final AtomicLong nombreBasculements = new AtomicLong();

    public AssistantAvecRepliService(
            @Qualifier("fournisseurPrincipal") Fournisseur principal,
            @Qualifier("fournisseurSecours") Fournisseur secours) {
        this.principal = principal;
        this.secours = secours;
    }

    public ReponseAvecRepli repondre(String message) {
        long debut = System.nanoTime();

        try {
            String reponse = principal.appeler(message);
            return new ReponseAvecRepli(reponse, principal.nom(), false, null, enMillisecondes(debut));
        } catch (Exception erreurPrincipal) {
            String raison = raison(erreurPrincipal);
            long numero = nombreBasculements.incrementAndGet();
            log.warn("Basculement n°{} : {} -> {} (raison : {})",
                    numero, principal.nom(), secours.nom(), raison);
            log.debug("Détail de l'erreur du modèle principal", erreurPrincipal);

            try {
                String reponse = secours.appeler(message);
                return new ReponseAvecRepli(reponse, secours.nom(), true, raison, enMillisecondes(debut));
            } catch (Exception erreurSecours) {
                log.error("Échec du modèle de secours {} (raison : {})",
                        secours.nom(), raison(erreurSecours));
                throw new ServiceIndisponibleException("Aucun modèle disponible", erreurSecours);
            }
        }
    }

    /** Résume une erreur sans exposer son message complet (il pourrait contenir des détails sensibles). */
    public static String raison(Throwable erreur) {
        Throwable racine = erreur;
        while (racine.getCause() != null && racine.getCause() != racine) {
            racine = racine.getCause();
        }
        String type = racine.getClass().getSimpleName();
        String message = racine.getMessage();
        if (message != null) {
            Matcher m = CODE_HTTP.matcher(message);
            if (m.find()) {
                return type + " (HTTP " + m.group(1) + ")";
            }
        }
        return type;
    }

    private static long enMillisecondes(long debutNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - debutNanos);
    }
}