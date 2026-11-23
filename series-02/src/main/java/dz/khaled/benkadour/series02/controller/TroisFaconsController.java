package dz.khaled.benkadour.series02.controller;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class TroisFaconsController {

    private static final String SYSTEM = "Tu es un assistant RH interne";

    private final ChatClient chatClient;

    public TroisFaconsController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    // Façon 1 : API fluide
    @GetMapping("/fluide")
    public String fluide(@RequestParam(name = "message") String message) {
        return chatClient.prompt()
                .system(SYSTEM)
                .user(message)
                .call()
                .content();
    }

    // Façon 2 : objet Prompt avec SystemMessage et UserMessage
    @GetMapping("/prompt")
    public String parPrompt(@RequestParam(name = "message") String message) {
        Prompt prompt = new Prompt(List.of(
                new SystemMessage(SYSTEM),
                new UserMessage(message)));
        return chatClient.prompt(prompt)
                .call()
                .content();
    }

    // Façon 3 : liste de messages
    @GetMapping("/liste")
    public String parListe(@RequestParam(name = "message") String message) {
        List<Message> messages = List.of(
                new SystemMessage(SYSTEM),
                new UserMessage(message));
        return chatClient.prompt()
                .messages(messages)
                .call()
                .content();
    }

    // Pour répondre à la question : une liste avec un historique de conversation
    @GetMapping("/historique")
    public String avecHistorique(@RequestParam(name = "message") String message) {
        List<Message> historique = List.of(
                new SystemMessage(SYSTEM),
                new UserMessage("Je m'appelle Karim."),
                new AssistantMessage("Bonjour Karim, comment puis-je vous aider ?"),
                new UserMessage(message));
        return chatClient.prompt()
                .messages(historique)
                .call()
                .content();
    }
}