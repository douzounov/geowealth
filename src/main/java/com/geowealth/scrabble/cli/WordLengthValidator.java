package com.geowealth.scrabble.cli;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

public class WordLengthValidator implements IValueValidator<Integer> {

    @Override
    public void validate(String name, Integer value) throws ParameterException {

        if (value < 2 || value > 50) {
            throw new ParameterException(name + ": value must be >=2 and <=50 characters");
        }
    }
}
