package pk.wc.pasir_wiktor_czerniak.exception;

import org.jspecify.annotations.NonNull;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(@NonNull final String message) {
        super(message);
    }
}