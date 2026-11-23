package dz.khaled.benkadour.series02.controller;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rh")
public class RolesController {

    private static final String SYSTEM = "Tu es un assistant RH interne";

    private final ChatClient chatClient;

    public RolesController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @GetMapping("/sans-system")
    public String sansSystem(@RequestParam(name = "message") String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }

    @GetMapping("/avec-system")
    public String avecSystem(@RequestParam(name = "message") String message) {
        return chatClient.prompt()
                .system(SYSTEM)
                .user(message)
                .call()
                .content();
    }
}