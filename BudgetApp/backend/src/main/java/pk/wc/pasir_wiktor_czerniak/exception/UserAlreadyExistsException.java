package pk.wc.pasir_wiktor_czerniak.exception;

public class UserAlreadyExistsException
        extends RuntimeException {

    public UserAlreadyExistsException(
            String message) {

        super(message);
    }
}