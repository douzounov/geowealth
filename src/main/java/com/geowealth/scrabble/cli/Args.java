package com.geowealth.scrabble.cli;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.geowealth.scrabble.impl.Scrabbler;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents configuration options parsed from the command line using
 * {@link com.beust.jcommander.JCommander JCommander}. Used to create
 * {@link Scrabbler} instances.
 * <p>
 * If the user does not supply any configuration options through the command line or
 * if incorrect ones are specified, the parser will display a usage message to guide
 * the user in specifying correct options.
 */
@Parameters(parametersValidators = SequentialOrParallelValidator.class)
public class Args {

    @Parameter(names = {"-du", "--dictionary-url"}, required = true,
            description = "URL of a dictionary with one word per line. Dictionary contents are considered case-sensitive.",
            validateValueWith = DictionaryURLValidator.class)
    private String dictionaryUrl;

    @Parameter(names = {"-dc", "--dictionary-charset"},
            description = "Suggested dictionary charset",
            validateValueWith = CharsetValidator.class)
    private String dictionaryCharset;

    @Parameter(names = {"-wl", "--word-length"},
            description = "Length of words to find (>=2 and <=50)",
            validateValueWith = WordLengthValidator.class)
    private Integer wordLength;

    @Parameter(names = {"-ocw", "--one-char-words"},
            description = "List of case-sensitive 1-character words to add to the dictionary",
            validateValueWith = OneCharWordsValidator.class)
    private Set<String> oneCharWords;

    @Parameter(names = {"-seq", "--sequential"},
            description = "Use a sequential algorithm to find matching words in the dictionary")
    private Boolean sequential;

    @Parameter(names = {"-par", "--parallel"},
            description = "Use a parallel algorithm to find matching words in the dictionary")
    private Boolean parallel;

    @Parameter(names = {"-lm", "--log-matching"},
            description = "Log matching words")
    private Boolean logMatching;

    public Args() {
        dictionaryCharset = StandardCharsets.UTF_8.toString();
        wordLength = 9;
        oneCharWords = new HashSet<>();
        sequential = Boolean.FALSE;
        parallel = Boolean.FALSE;
        logMatching = Boolean.FALSE;
    }

    @Override
    public boolean equals(Object other) {

        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (getClass() != other.getClass()) {
            return false;
        }

        return Objects.equals(dictionaryUrl, ((Args) other).dictionaryUrl) &&
                Objects.equals(dictionaryCharset, ((Args) other).dictionaryCharset) &&
                Objects.equals(wordLength, ((Args) other).wordLength) &&
                Objects.equals(oneCharWords, ((Args) other).oneCharWords) &&
                Objects.equals(sequential, ((Args) other).sequential) &&
                Objects.equals(parallel, ((Args) other).parallel) &&
                Objects.equals(logMatching, ((Args) other).logMatching);
    }

    public String getDictionaryUrl() {
        return dictionaryUrl;
    }

    public String getDictionaryCharset() {
        return dictionaryCharset;
    }

    public Integer getWordLength() {
        return wordLength;
    }

    public Set<String> getOneCharWords() {
        return oneCharWords;
    }

    public Boolean getSequential() {
        return sequential;
    }

    public Boolean getParallel() {
        return parallel;
    }

    public Boolean getLogMatching() {
        return logMatching;
    }
}
