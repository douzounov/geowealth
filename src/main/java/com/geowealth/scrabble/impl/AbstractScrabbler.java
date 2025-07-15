package com.geowealth.scrabble.impl;

import com.geowealth.scrabble.cli.Args;
import com.geowealth.scrabble.exceptions.ScrabbleLoadException;
import com.geowealth.scrabble.exceptions.ScrabbleMatchException;
import org.apache.tika.parser.txt.CharsetDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An abstract {@link Scrabbler} class which loads a dictionary from a specified
 * {@link java.net.URL URL} (e.g. on the local file system or on the web).
 * <p>
 * An attempt is made to automatically determine the encoding of the provided dictionary.
 * The probability of correct identification improves as the size of the dictionary
 * increases. Note that a hint about the encoding can be supplied when creating instances.
 * <p>
 * The dictionary must contain exactly one word per line. Leading and trailing whitespace
 * is removed from words (but note that whitespace in the middle of a word is left untouched).
 * Duplicate words are treated as a single word. Lines containing any character outside the
 * basic multilingual plane are ignored.
 */
public abstract class AbstractScrabbler implements Scrabbler {

    private static final Logger logger = LoggerFactory.getLogger(AbstractScrabbler.class);
    private final Profiler profiler;

    // size of the buffer (in bytes) used to read the dictionary and detect its character encoding
    private static final int BUFFER_SIZE_B = 1024 * 1024;

    private final Set<String> dictionaryWords;
    private final Set<String> candidateWords;

    /**
     * Creates a new instance by loading a dictionary from the specified {@link java.net.URL URL}
     * and applying several word matching settings.
     *
     * @param args {@link Args} instance that contains configuration options. Typically created
     *             by parsing command-line arguments.
     */
    public AbstractScrabbler(Args args) throws ScrabbleLoadException {

        profiler = new Profiler("Scrabbler");
        profiler.setLogger(logger);

        profiler.start("load");

        dictionaryWords = new HashSet<>();
        dictionaryWords.addAll(args.getOneCharWords());
        candidateWords = new HashSet<>();

        // used to automatically detect character encoding
        CharsetDetector detector = new CharsetDetector(BUFFER_SIZE_B);

        // wrap the original stream in a buffered stream as the original may not support mark and reset
        try (InputStream is = new URI(args.getDictionaryUrl()).toURL().openStream();
             BufferedReader br = new BufferedReader(
                     detector.getReader(new BufferedInputStream(is, BUFFER_SIZE_B), args.getDictionaryCharset()))) {

            for (String line = br.readLine(); line != null; line = br.readLine()) {

                String trimmed = line.trim();

                if (allCharsInBMP(trimmed) && !trimmed.isEmpty()) {
                    dictionaryWords.add(trimmed);
                    if (trimmed.length() == args.getWordLength()) {
                        candidateWords.add(trimmed);
                    }
                }
            }

            logger.trace("number of candidate words: {}", candidateWords.size());

            // remove all candidate words that do not contain the 1-char words present
            // in the full list and are thus not reducible to these 1-char words
            var oneCharWords = dictionaryWords.stream().filter(w -> w.length() == 1).collect(Collectors.toSet());
            candidateWords.removeIf(w -> oneCharWords.stream().noneMatch(w::contains));

            logger.trace("number of candidate words (after trimming): {}", candidateWords.size());

        } catch (Exception ex) {
            throw new ScrabbleLoadException(ex);
        }
    }

    /**
     * Checks whether all characters in the supplied string are in the Basic Multilingual Plane.
     *
     * @param str string to check
     * @return {@code true} if all characters are in the Basic Multilingual Plane; {@code false}
     * otherwise
     */
    private static boolean allCharsInBMP(String str) {

        for (int c = 0; c < str.length(); c++) {
            if (Character.isSurrogate(str.charAt(c))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Uses a <b>recursive sequential algorithm</b> to check whether the specified word
     * can be reduced to a single-character word by repeatedly removing one character
     * at a time, verifying after each removal that the resulting string remains a valid
     * word in the dictionary.
     * <p>
     * Word matching is <b>case-sensitive</b>.
     *
     * @param word word to check
     * @return {@code true} if a successful match; {@code false} otherwise
     */
    protected boolean isWordMatch(String word) {

        if (word.isEmpty()) {
            getLogger().trace("complete match!");
            return true;
        } else if (dictionaryWords.contains(word)) {

            getLogger().trace("match: {}, length={}", word, word.length());

            boolean found = false;
            for (int c = 0; c < word.length(); c++) {

                String candidate = word.substring(0, c) + word.substring(c + 1);
                getLogger().trace("candidate: {}, length={}", candidate, candidate.length());

                found = isWordMatch(candidate);
                if (found) {
                    break;
                }
            }
            if (!found) {
                getLogger().trace("backtracking...");
            }

            return found;

        } else {
            return false;
        }
    }

    protected static Logger getLogger() {

        return logger;
    }

    protected Profiler getProfiler() {

        return profiler;
    }

    @Override
    public Set<String> getDictionaryWords() {

        return Collections.unmodifiableSet(dictionaryWords);
    }

    @Override
    public Set<String> getCandidateWords() {

        return Collections.unmodifiableSet(candidateWords);
    }

    @Override
    public abstract Set<String> findMatchingWords() throws ScrabbleMatchException;
}
