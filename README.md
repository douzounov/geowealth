Scrabbler
----------

### Overview

#### Introduction

A scrabbler loads a dictionary of words, identifies candidate words of a specified length, and finds the candidates that can be reduced to a single-character word by repeatedly removing one character at a time, verifying after each removal that the resulting string remains a valid word in the dictionary.

This project includes two scrabbler implementations:

* *SequentialScrabbler*
    * Uses a recursive sequential algorithm to find matching words.
* *ParallelScrabbler*
    * Divides the list of candidate words into a number of partitions and processes each partition in a separate thread using the recursive sequential algorithm.

#### Loading dictionaries

Both implementations use the same logic for loading dictionaries:

* A dictionary can include words from any language that employs a standard alphabet (e.g., English, French, or Bulgarian).
    * Languages such as Japanese, which utilize a different writing system, are likely incompatible with the provided word matching algorithm.
* Words in a dictionary are not converted to lowercase or uppercase, as such a conversion could corrupt some words if the default *Locale* is used. Using a non-default *Locale* requires language detection, which is not currently supported.
    * As a result, the dictionary and the word matching algorithm are case-sensitive.
* A dictionary is fetched from a URL, which may reference either a local or a remote resource.
    * No limit is imposed on the size of the dictionary.
    * No verification is performed to determine whether the file is binary or text. If a binary file is loaded as a dictionary, the process will likely succeed, but the resulting dictionary will contain nonsensical data.
* Automatic detection of the dictionary character encoding is attempted, as one cannot assume that it is always UTF-8.
    * Detection is performed using Apache Tika, which employs character set detection capabilities similar to those in ICU4J. However, Tika allows users to specify the amount of content to be used during detection. This can lead to improved accuracy if used wisely. Furthermore, users can specify an encoding hint to support Tika's automatic detection process.
* Each line in the dictionary file undergoes a sanitization process before being accepted as valid:
    * Leading and trailing whitespace is stripped.
    * A line containing only whitespace is skipped.
    * A line containing any characters outside the Basic Multilingual Plane is skipped.
    * Duplicate words are considered as a single word.

#### Logging

The default Logback configuration file (`main/resources/logback.xml`) directs logs to both the console and a file (`logs/app.log`). The logger is configured to log profiling messages at the DEBUG level, a setting that is not suitable for production use. The default configuration can be easily overridden on the command line as follows:

```
-Dlogback.configurationFile=/path/to/logback.xml
```

To trace the operation of the word matching algorithm, set the logging level of the `com.geowealth.scrabble.impl` logger to TRACE.

#### Parallelism

###### Overview
The sequential algorithm operates efficiently on modern processors when handling real-world dictionaries. Nonetheless, a parallelized version was also developed to enable comparison with the sequential approach. This comparison was conducted using both small real-world dictionaries of up to 500,000 words and large, randomly generated dictionaries ranging from 10 million to 50 million words.

###### Small dictionaries
The sequential algorithm has roughly the same performance or is slightly faster than a parallelized version when working with small dictionaries. In this case the benefits of parallelization are completely offset by the multi-threading overhead.

###### Large dictionaries
Given the word matching task at hand, parallel processing is only beneficial when dealing with large dictionaries. A basic strategy for parallelizing the task is to create a fixed thread pool and submit the matching of each word as a separate task. Due to the multi-threading overhead related to the large number of tasks, this method offers only a slight performance gain in comparison to the sequential algorithm.

A better approach (and what was implemented) is to divide the candidate words into several partitions (attempting to maximize the number of partitions but keeping it lower than or equal to the number of threads) and submit each partition as a separate task in the pool. This method achieves a 3.5 speedup (when running on 8 cores) in comparison to the sequential algorithm. By removing some logging, slightly better performance is to be expected, and there are likely other areas where optimization is possible.

### Performance

The word matching algorithm creates multiple *String* objects whenever it removes a character from a word. This results in numerous objects being created and discarded rapidly, prompting the garbage collector to run more frequently (depending on garbage collection settings and the size of the data set). This seems difficult to avoid in Java. An alternative approach might involve using *char* arrays instead of *String* objects to represent words, but this would make verifying whether a candidate word is a match more complicated.

Before running the matching algorithm, any candidate words that do not contain the specified 1-character word(s) are removed, which likely provides only a minor boost in performance.

Profiling the application reveals that its slowest component is the data loading process, and more specifically, inserting *String* objects into a *Set*. Setting the initial capacity of the set will reduce rehashing and improve performance, but estimating the number of lines in a dictionary in advance is difficult. It is also possible to develop a custom *Set* implementation to further enhance performance.

### Command line execution

You can use the functionality of this project as a library or via the provided command line tool.

To use the sequential algorithm with an English Scrabble dictionary on the command line:
```
./gradlew run --args="-du https://raw.githubusercontent.com/nikiiv/JavaCodingTestOne/master/scrabble-words.txt -seq -ocw I,A"
```

The above uses the default word length of 9 and adds the words "I" and "A" to the dictionary, as they are not already present.

To use the parallel algorithm with a French Scrabble dictionary on the command line:
```
./gradlew run --args="-du https://raw.githubusercontent.com/Thecoolsim/French-Scrabble-ODS8/refs/heads/main/French%20ODS%20dictionary.txt -wl 14 -ocw A -par -lm"
```

The above sets the desired word length to 14, adds the word "A" to the dictionary, and logs all matching words.

To see the available command line options:

```
./gradlew run
```
