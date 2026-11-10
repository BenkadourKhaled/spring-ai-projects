package dz.exercise1_1.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MultiModelChatController {

    private final ChatClient geminiChatClient;
    private final ChatClient dockerChatClient;

    public MultiModelChatController(
            @Qualifier("geminiChatClient") ChatClient geminiChatClient,
            @Qualifier("dockerChatClient") ChatClient dockerChatClient) {
        this.geminiChatClient = geminiChatClient;
        this.dockerChatClient = dockerChatClient;
    }

    @GetMapping("/gemini/chat")
    public String geminiChat(@RequestParam(name = "message") String message) {
        return geminiChatClient.prompt(message).call().content();
    }

    @GetMapping("/docker/chat")
    public String dockerChat(@RequestParam(name = "message") String message) {
        return dockerChatClient.prompt(message).call().content();
    }
}