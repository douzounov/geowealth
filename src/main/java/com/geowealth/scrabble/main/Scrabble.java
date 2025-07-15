package com.geowealth.scrabble.main;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.geowealth.scrabble.cli.Args;
import com.geowealth.scrabble.exceptions.ScrabbleLoadException;
import com.geowealth.scrabble.exceptions.ScrabbleMatchException;
import com.geowealth.scrabble.impl.ParallelScrabbler;
import com.geowealth.scrabble.impl.SequentialScrabbler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class Scrabble {

    private static final Logger logger = LoggerFactory.getLogger(Scrabble.class);

    public static void main(String[] argv) {

        Args args = new Args();
        var builder = JCommander.newBuilder().addObject(args).build();
        builder.setProgramName("COMMAND");

        try {
            builder.parse(argv);
        } catch (ParameterException pe) {
            System.out.println(pe.getMessage());
            builder.usage();
            System.exit(1);
        }

        try {
            matchWords(args);
        } catch (Exception ex) {
            logger.error("error while matching words", ex);
        }
    }

    private static void matchWords(Args args) throws ScrabbleLoadException, ScrabbleMatchException {

        Set<String> matchingWords;

        if (args.getSequential()) {

            matchingWords = new SequentialScrabbler(args).findMatchingWords();

        } else if (args.getParallel()) {

            matchingWords = new ParallelScrabbler(args).findMatchingWords();

        } else {

            // this code should not be reachable under normal circumstances
            throw new IllegalStateException();
        }

        logger.info("number of matches: {}", matchingWords.size());
        if (args.getLogMatching()) {
            matchingWords.forEach(word -> logger.info("match: {}", word));
        }
    }

}