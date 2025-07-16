package com.geowealth.scrabble.impl;

import com.geowealth.scrabble.cli.Args;
import com.geowealth.scrabble.exceptions.ScrabbleLoadException;

import java.util.Set;
import java.util.TreeSet;

/**
 * A concrete {@link AbstractScrabbler} class which uses the recursive sequential
 * algorithm defined in the base class to find matching words.
 */
public class SequentialScrabbler extends AbstractScrabbler {

    public SequentialScrabbler(Args args) throws ScrabbleLoadException {
        super(args);
    }

    /**
     * Finds matching words using a <b>recursive sequential algorithm</b>.
     * <p>
     * Word matching is <b>case-sensitive</b>.
     * <p>
     * The method does not have any side effects and can be called repeatedly on the same instance.
     *
     * @return ordered set of matching words
     */
    @Override
    public Set<String> findMatchingWords() {

        getProfiler().start("sequential match");

        // sort matching words
        Set<String> matchingWords = new TreeSet<>();

        for (String word : getCandidateWords()) {

            if (isWordMatch(word)) {
                matchingWords.add(word);
                getLogger().trace("+{}", word);
            } else {
                getLogger().trace("-{}", word);
            }
        }

        getProfiler().stop();
        getProfiler().log();

        return matchingWords;
    }

}
