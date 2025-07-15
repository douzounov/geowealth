package com.geowealth.scrabble.exceptions;

import com.geowealth.scrabble.impl.Scrabbler;

/**
 * Checked exception thrown by {@link Scrabbler} instances when there is a problem finding word matches.
 */
public class ScrabbleMatchException extends Exception {

    public ScrabbleMatchException(String message) {
        super(message);
    }

    public ScrabbleMatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScrabbleMatchException(Throwable cause) {
        super(cause);
    }

}
