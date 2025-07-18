package com.geowealth.scrabble.impl;

import com.geowealth.scrabble.cli.Args;
import com.geowealth.scrabble.exceptions.ScrabbleLoadException;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/**
 * A concrete {@link AbstractScrabbler} class which parallelizes the recursive
 * algorithm defined in the base class to find matching words. The list of candidate
 * words is divided into a number of partitions, with each partition then processed
 * by a separate thread.
 */
public class ParallelScrabbler extends AbstractScrabbler {

    public ParallelScrabbler(Args args) throws ScrabbleLoadException {
        super(args);
    }

    /**
     * Finds matching words using a <b>recursive parallel algorithm</b>.
     * <p>
     * Word matching is <b>case-sensitive</b>.
     * <p>
     * The method does not have any side effects and can be called repeatedly on the same instance.
     *
     * @return set of matching words
     */
    @Override
    public Set<String> findMatchingWords() {

        getProfiler().start("parallel match");

        // The underlying parallel streams implementation utilizes various heuristics such as data set size,
        // the cost of processing each element, and the overhead of task creation to determine how and if to
        // partition the original stream. As a result, it delivers good performance with both small and large
        // data sets, all while requiring less code compared to manually partitioning the data set, using an
        // executor service, and submitting tasks to it.
        var matchingWords = getCandidateWords().parallelStream().filter(this::isWordMatch)
                .collect(Collectors.toCollection(ConcurrentSkipListSet::new));

        getProfiler().stop().log();

        return matchingWords;
    }

}
