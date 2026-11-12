package dz.exercise1_1.dto;

public record ReponseAvecRepli(
        String reponse,
        String modeleUtilise,
        boolean repli,
        String raison,
        long dureeMs) {
}