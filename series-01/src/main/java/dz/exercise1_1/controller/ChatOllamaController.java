/*package dz.exercise1_1.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatOllamaController {


    private final ChatClient chatClient;

    public ChatOllamaController(ChatClient.Builder builder) {   // inchangé
        this.chatClient = builder
                .defaultSystem("Tu es un assistant technique, réponds en français.")
                .build();
    }

    @GetMapping("/ask")
    public String ask(@RequestParam(name = "question") String q) {           // inchangé
        return chatClient.prompt().user(q).call().content();
    }
}


 */