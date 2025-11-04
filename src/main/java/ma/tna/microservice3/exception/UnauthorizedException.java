package ma.tna.microservice3.exception;

/**
 * Exception levée lorsqu'un accès non autorisé est tenté
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}

