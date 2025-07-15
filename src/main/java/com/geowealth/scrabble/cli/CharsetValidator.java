package com.geowealth.scrabble.cli;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

import java.nio.charset.Charset;

public class CharsetValidator implements IValueValidator<String> {

    @Override
    public void validate(String name, String value) throws ParameterException {

        try {
            Charset.forName(value);
        } catch (Exception ex) {
            throw new ParameterException(name + ": specified charset is not valid", ex);
        }
    }
}
