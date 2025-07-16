package com.geowealth.scrabble.cli;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

import java.net.URI;
import java.net.URL;

public class DictionaryURLValidator implements IValueValidator<String> {

    @Override
    public void validate(String name, String value) throws ParameterException {

        try {
            // all URL constructors deprecated as of Java 21
            URL url = new URI(value).toURL();
        } catch (Exception ex) {
            throw new ParameterException(name + ": specified URL is not valid", ex);
        }
    }
}
