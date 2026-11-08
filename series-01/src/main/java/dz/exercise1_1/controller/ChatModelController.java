package dz.exercise1_1.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class ChatModelController {

    private final ChatModel chatModel;   // bean googleGenAiChatModel, injecté par son interface

    ChatModelController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/ask-model")
    String ask(@RequestParam String q) {
        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage("Tu es un assistant technique, réponds en français."),
                        new UserMessage(q)),
                ChatOptions.builder().temperature(0.7).build());   // options portables

        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }
}