package com.geowealth.scrabble.cli;

import com.beust.jcommander.IParametersValidator;
import com.beust.jcommander.ParameterException;

import java.util.Map;

public class SequentialOrParallelValidator implements IParametersValidator {

    @Override
    public void validate(Map<String, Object> params) throws ParameterException {

        Boolean seq = params.get("--sequential") == null ? Boolean.FALSE : (Boolean) params.get("--sequential");
        Boolean par = params.get("--parallel") == null ? Boolean.FALSE : (Boolean) params.get("--parallel");

        if (seq.equals(par)) {
            throw new ParameterException("-seq and -par are mutually exclusive and exactly one of them must be specified");
        }
    }
}
