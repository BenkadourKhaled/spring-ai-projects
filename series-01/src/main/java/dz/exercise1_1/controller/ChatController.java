package dz.exercise1_1.controller;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/api/chat")
    public String chat(@RequestParam(name = "message") String message) {
        ChatResponse response = chatClient.prompt()
                .user("Qu'est-ce que Spring AI ?")
                .call()
                .chatResponse();

        Usage usage = response.getMetadata().getUsage();
        System.out.println("Entrée  : " + usage.getPromptTokens());
        System.out.println("Sortie  : " + usage.getCompletionTokens());
        System.out.println("Total   : " + usage.getTotalTokens());
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}