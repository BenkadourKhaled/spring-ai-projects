package dz.exercise1_1.services;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.function.Function;

import dz.exercise1_1.dto.ReponseAvecRepli;
import dz.exercise1_1.exception.ServiceIndisponibleException;
import dz.exercise1_1.fournisseur.Fournisseur;
import dz.exercise1_1.service.AssistantAvecRepliService;
import org.junit.jupiter.api.Test;

class AssistantAvecRepliServiceTest {

    record FournisseurSimule(String nom, Function<String, String> comportement) implements Fournisseur {
        @Override
        public String appeler(String message) {
            return comportement.apply(message);
        }
    }

    private static Fournisseur qui(String nom, String reponse) {
        return new FournisseurSimule(nom, m -> reponse);
    }

    private static Fournisseur enPanne(String nom, RuntimeException erreur) {
        return new FournisseurSimule(nom, m -> {
            throw erreur;
        });
    }

    @Test
    void sansPanneLePrincipalRepond() {
        var service = new AssistantAvecRepliService(qui("cloud", "A"), qui("local", "B"));

        ReponseAvecRepli r = service.repondre("Bonjour");

        assertThat(r.repli()).isFalse();
        assertThat(r.modeleUtilise()).isEqualTo("cloud");
        assertThat(r.reponse()).isEqualTo("A");
        assertThat(r.raison()).isNull();
    }

    @Test
    void siLePrincipalEchoueLeSecoursRepondEtLeSignale() {
        var service = new AssistantAvecRepliService(
                enPanne("cloud", new IllegalStateException("429 quota dépassé")),
                qui("local", "B"));

        ReponseAvecRepli r = service.repondre("Bonjour");

        assertThat(r.repli()).isTrue();
        assertThat(r.modeleUtilise()).isEqualTo("local");
        assertThat(r.raison()).contains("HTTP 429");
    }

    @Test
    void siLesDeuxEchouentOnLeveUneException() {
        var service = new AssistantAvecRepliService(
                enPanne("cloud", new IllegalStateException("panne 1")),
                enPanne("local", new IllegalStateException("panne 2")));

        assertThatThrownBy(() -> service.repondre("Bonjour"))
                .isInstanceOf(ServiceIndisponibleException.class);
    }

    @Test
    void laRaisonExtraitLeCodeHttpDeLaCauseRacine() {
        var erreur = new RuntimeException("Failed to generate content",
                new IllegalStateException("404 . modèle retiré"));

        assertThat(AssistantAvecRepliService.raison(erreur))
                .isEqualTo("IllegalStateException (HTTP 404)");
    }
}