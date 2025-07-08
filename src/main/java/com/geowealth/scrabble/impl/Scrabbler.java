package com.geowealth.scrabble.impl;

import com.geowealth.scrabble.exceptions.ScrabbleMatchException;

import java.util.Set;

/**
 * An object that loads a dictionary of words, identifies candidate words of a specified length,
 * and finds those candidates that can be reduced to a single-character word by repeatedly removing
 * one character at a time, verifying after each removal that the resulting string remains a valid
 * word in the dictionary.
 */
public interface Scrabbler {

    Set<String> getDictionaryWords();

    Set<String> getCandidateWords();

    Set<String> findMatchingWords() throws ScrabbleMatchException;
}
