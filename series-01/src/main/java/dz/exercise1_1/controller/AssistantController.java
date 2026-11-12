package dz.exercise1_1.controller;

import dz.exercise1_1.dto.ReponseAvecRepli;
import dz.exercise1_1.service.AssistantAvecRepliService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AssistantController {

    private final AssistantAvecRepliService service;

    public AssistantController(AssistantAvecRepliService service) {
        this.service = service;
    }

    @GetMapping("/assistant")
    public ReponseAvecRepli repondre(@RequestParam(name = "message") String message) {
        return service.repondre(message);
    }
}