package dz.exercise1_1.exception;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GestionErreurs {

    @ExceptionHandler(dz.exercise1_1.exception.ServiceIndisponibleException.class)
    public ResponseEntity<Map<String, String>> indisponible(ServiceIndisponibleException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("erreur", "Aucun modèle n'est disponible pour le moment."));
    }

}