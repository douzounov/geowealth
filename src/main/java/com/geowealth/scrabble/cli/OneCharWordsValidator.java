package com.geowealth.scrabble.cli;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

import java.util.Collection;

public class OneCharWordsValidator implements IValueValidator<Collection<String>> {

    @Override
    public void validate(String name, Collection<String> values) throws ParameterException {

        values.forEach(val -> {
            if (val.length() != 1)
                throw new ParameterException(name + ": each specified word must have length=1");
        });
    }
}
