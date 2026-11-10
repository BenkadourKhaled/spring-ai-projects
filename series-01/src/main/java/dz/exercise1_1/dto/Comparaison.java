package dz.exercise1_1.dto;

import java.util.List;

public record Comparaison(String message, List<ResultatModele> resultats, long dureeTotaleMs) {
}