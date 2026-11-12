package dz.exercise1_1.config;


import dz.exercise1_1.fournisseur.FournisseurChatClient;
import dz.exercise1_1.fournisseur.Fournisseur;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FournisseursConfig {

    @Bean
    public Fournisseur fournisseurPrincipal(
            @Qualifier("geminiChatClient") ChatClient client,
            @Value("${spring.ai.google.genai.chat.options.model:gemini}") String nom) {
        return new FournisseurChatClient(nom, client);
    }

    @Bean
    public Fournisseur fournisseurSecours(
            @Qualifier("dockerChatClient") ChatClient client,
            @Value("${spring.ai.openai.chat.options.model:modele-local}") String nom) {
        return new FournisseurChatClient(nom, client);
    }
}