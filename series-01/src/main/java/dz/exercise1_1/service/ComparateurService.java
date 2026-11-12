package dz.exercise1_1.service;


import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import dz.exercise1_1.dto.Comparaison;
import dz.exercise1_1.dto.ResultatModele;
import jakarta.annotation.PreDestroy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ComparateurService {

    private static final long DELAI_MAX_SECONDES = 60;

    private final ChatClient geminiChatClient;
    private final ChatClient dockerChatClient;
    private final String nomGemini;
    private final String nomDocker;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public ComparateurService(
            @Qualifier("geminiChatClient") ChatClient geminiChatClient,
            @Qualifier("dockerChatClient") ChatClient dockerChatClient,
            @Value("${spring.ai.google.genai.chat.options.model:gemini}") String modeleGemini,
            @Value("${spring.ai.openai.chat.options.model:docker}") String modeleDocker) {
        this.geminiChatClient = geminiChatClient;
        this.dockerChatClient = dockerChatClient;
        this.nomGemini = modeleGemini;
        this.nomDocker = modeleDocker;
    }

    public Comparaison comparer(String message) {
        long debut = System.nanoTime();

        // Les deux appels démarrent immédiatement, sans s'attendre
        CompletableFuture<ResultatModele> gemini = interroger(nomGemini, geminiChatClient, message);
        CompletableFuture<ResultatModele> docker = interroger(nomDocker, dockerChatClient, message);

        // join() attend la fin de chaque appel
        List<ResultatModele> resultats = Stream.of(gemini, docker)
                .map(CompletableFuture::join)
                .toList();

        return new Comparaison(message, resultats, enMillisecondes(debut));
    }

    private CompletableFuture<ResultatModele> interroger(String nomModele, ChatClient client, String message) {
        return CompletableFuture.supplyAsync(() -> {
                    long debut = System.nanoTime();
                    try {
                        String reponse = client.prompt().user(message).call().content();
                        return new ResultatModele(nomModele, reponse, enMillisecondes(debut), null);
                    } catch (Exception e) {
                        return new ResultatModele(nomModele, null, enMillisecondes(debut),
                                e.getClass().getSimpleName());
                    }
                }, executor)
                .orTimeout(DELAI_MAX_SECONDES, TimeUnit.SECONDS)
                .exceptionally(e -> new ResultatModele(nomModele, null,
                        TimeUnit.SECONDS.toMillis(DELAI_MAX_SECONDES), "Délai dépassé"));
    }

    private static long enMillisecondes(long debutNanos) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - debutNanos);
    }

    @PreDestroy
    void arreter() {
        executor.shutdown();
    }
}