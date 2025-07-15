package com.geowealth.scrabble.impl;

import com.geowealth.scrabble.cli.Args;
import com.geowealth.scrabble.exceptions.ScrabbleLoadException;

import java.util.Set;

public class ParallelScrabbler extends AbstractScrabbler {

    public ParallelScrabbler(Args args) throws ScrabbleLoadException {
        super(args);
    }

    @Override
    public Set<String> findMatchingWords() {

        throw new UnsupportedOperationException("Parallel processing not supported.");
    }

}
