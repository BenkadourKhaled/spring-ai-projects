package dz.exercise1_1.controller;

import dz.exercise1_1.dto.Comparaison;
import dz.exercise1_1.service.ComparateurService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CompareController {

    private final ComparateurService comparateurService;

    public CompareController(ComparateurService comparateurService) {
        this.comparateurService = comparateurService;
    }

    @GetMapping("/compare")
    public Comparaison comparer(@RequestParam(name = "message") String message) {
        return comparateurService.comparer(message);
    }
}