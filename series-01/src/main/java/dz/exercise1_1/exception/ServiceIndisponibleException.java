package dz.exercise1_1.exception;

public class ServiceIndisponibleException extends RuntimeException {

    public ServiceIndisponibleException(String message, Throwable cause) {
        super(message, cause);
    }
}