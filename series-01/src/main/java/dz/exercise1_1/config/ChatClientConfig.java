package dz.exercise1_1.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    // Style 1 : création rapide
    @Bean
    public ChatClient geminiChatClient(GoogleGenAiChatModel chatModel) {
        return ChatClient.create(chatModel);
    }

    // Style 2 : avec le builder, pour plus de contrôle (advisors, system, etc.)
    @Bean
    public ChatClient dockerChatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }
}