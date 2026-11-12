package dz.exercise1_1.fournisseur;


import org.springframework.ai.chat.client.ChatClient;

public class FournisseurChatClient implements Fournisseur {

    private final String nom;
    private final ChatClient client;

    public FournisseurChatClient(String nom, ChatClient client) {
        this.nom = nom;
        this.client = client;
    }

    @Override
    public String nom() {
        return nom;
    }

    @Override
    public String appeler(String message) {
        String reponse = client.prompt().user(message).call().content();
        if (reponse == null || reponse.isBlank()) {
            // Une réponse vide est traitée comme une panne : elle déclenche le repli
            throw new IllegalStateException("Réponse vide du modèle");
        }
        return reponse;
    }
}