package com.geowealth.scrabble.exceptions;

import com.geowealth.scrabble.impl.Scrabbler;

/**
 * Checked exception thrown by {@link Scrabbler} instances when there is a problem loading a dictionary.
 */
public class ScrabbleLoadException extends Exception {

    public ScrabbleLoadException(String message) {
        super(message);
    }

    public ScrabbleLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ScrabbleLoadException(Throwable cause) {
        super(cause);
    }

}
