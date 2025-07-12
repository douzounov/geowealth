package com.geowealth.scrabble;

import com.beust.jcommander.JCommander;
import com.geowealth.scrabble.cli.Args;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.util.Combinations;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for working with {@link Args}.
 */
public class ArgsUtils {

    /**
     * Creates a new {@link Args} instance from an array of command-line arguments.
     *
     * @param argv command-line arguments
     * @return {@link Args} instance
     */
    static Args args(String... argv) {

        Args args = new Args();
        var builder = JCommander.newBuilder().addObject(args).build();
        builder.parse(argv);

        return args;
    }

    /**
     * Creates a list of all valid command-line argument combinations based on the given options.
     * Each combination is represented by an {@link Args} instance within the list. The provided
     * options are validated using the validators specified in the {@link Args} class.
     *
     * @param dictionaryUrl     URL of a dictionary with one word per line
     * @param dictionaryCharset suggested dictionary charset
     * @param wordLength        length of words to find (>=2 and <=50)
     * @param oneCharWords      list of case-sensitive 1-character words to add to the dictionary
     * @param sequential        if {@code true}, use a sequential algorithm to find matching words in the dictionary
     * @param parallel          if {@code true}, use a parallel algorithm to find matching words in the dictionary
     * @param logMatching       if {@code true}, log matching words; do not log matching words otherwise
     * @return list of all possible argument combinations
     */
    static List<Args> allArgCombinations(String dictionaryUrl, String dictionaryCharset,
                                         String wordLength, String oneCharWords,
                                         boolean sequential, boolean parallel, boolean logMatching) {

        var allArgCombinations = new ArrayList<Args>();

        var requiredArgs = new ArrayList<Pair<String, String>>() {{
            add(Pair.of("-du", dictionaryUrl));
            if (sequential) {
                add(Pair.of("-seq", null));
            }
            if (parallel) {
                add(Pair.of("-par", null));
            }
        }};

        var optionalArgs = new ArrayList<Pair<String, String>>() {{
            add(Pair.of("-dc", dictionaryCharset));
            add(Pair.of("-wl", wordLength));
            add(Pair.of("-ocw", oneCharWords));
            if (logMatching) {
                add(Pair.of("-lm", null));
            }
        }};

        for (int combinationSize = 0; combinationSize <= optionalArgs.size(); combinationSize++) {

            for (int[] combination : new Combinations(optionalArgs.size(), combinationSize)) {

                allArgCombinations.add(args(argv(requiredArgs, optionalArgs, combination)));
            }
        }

        return allArgCombinations;
    }

    /**
     * Creates an array of command-line arguments based on a provided list of required
     * parameters, a list of optional parameters, and an array of indexes denoting a
     * specific combination of optional parameters.
     *
     * @param required    list of {@link Pair} objects with parameter name and parameter value
     *                    (a value not present is denoted by a {@code null} value in the pair)
     * @param optional    list of {@link Pair} objects with parameter name and parameter value
     *                    (a value not present is denoted by a {@code null} value in the pair)
     * @param combination array of indexes in the optional parameters list denoting a specific
     *                    combination of optional parameters
     * @return array of command-line arguments for the specific combination
     */
    private static String[] argv(List<Pair<String, String>> required,
                                 List<Pair<String, String>> optional,
                                 int[] combination) {

        var argList = new ArrayList<String>();

        for (Pair<String, String> pair : required) {
            argList.add(pair.getLeft());
            if (pair.getRight() != null) {
                argList.add(pair.getRight());
            }
        }

        for (int combIdx : combination) {
            var pair = optional.get(combIdx);
            argList.add(pair.getLeft());
            if (pair.getRight() != null) {
                argList.add(pair.getRight());
            }
        }

        return argList.toArray(String[]::new);
    }

}
