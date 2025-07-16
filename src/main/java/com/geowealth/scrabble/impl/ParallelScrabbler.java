package com.geowealth.scrabble.impl;

import com.geowealth.scrabble.cli.Args;
import com.geowealth.scrabble.exceptions.ScrabbleLoadException;
import com.geowealth.scrabble.exceptions.ScrabbleMatchException;
import org.apache.commons.collections4.ListUtils;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
    public Set<String> findMatchingWords() throws ScrabbleMatchException {

        getProfiler().start("parallel match");

        // create a candidate words list which will further be divided into
        // partitions, with each partition processed by a separate thread
        ArrayList<String> candidateWords = new ArrayList<>(getCandidateWords());

        // TODO a decent heuristic, but can also be specified on the command line
        int numThreads = Runtime.getRuntime().availableProcessors();

        // calculate a partition size that maximizes the number of partitions
        // without the partition count exceeding the number of threads
        int partitionSize = !candidateWords.isEmpty() && (candidateWords.size() % numThreads == 0) ?
                candidateWords.size() / numThreads : (candidateWords.size() / numThreads) + 1;

        ExecutorService exec = Executors.newFixedThreadPool(numThreads);
        Set<String> matchingWords = new ConcurrentSkipListSet<>();

        // submit each partition as a new task to be executed by the thread pool
        for (var partition : ListUtils.partition(candidateWords, partitionSize)) {
            exec.submit(() -> {
                for (String word : partition) {
                    if (isWordMatch(word)) {
                        matchingWords.add(word);
                    }
                }
            });
        }

        exec.shutdown();
        try {
            while (!exec.awaitTermination(1, TimeUnit.SECONDS)) {
                getLogger().trace("waiting for tasks to complete");
            }
        } catch (InterruptedException ie) {
            throw new ScrabbleMatchException(ie);
        }

        getProfiler().stop();
        getProfiler().log();

        return matchingWords;
    }

}
