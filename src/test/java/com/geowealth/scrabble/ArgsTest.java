package com.geowealth.scrabble;

import com.beust.jcommander.ParameterException;
import com.geowealth.scrabble.cli.Args;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static com.geowealth.scrabble.ArgsUtils.args;
import static org.junit.jupiter.api.Assertions.*;

public class ArgsTest {

    private static final String VALID_URL_WEB = "http://java.sun.com/FAQ.html";
    private static final String VALID_URL_FS = "file://path/to/file";
    private static final String INVALID_URL = "INVALID_URL";
    private static final String INVALID_CHARSET = "INVALID_CHARSET";

    @Test
    public void testArgs_whenInvalidDictionaryUrl_thenThrow() {
        assertThrowsExactly(ParameterException.class, () -> args("-du", INVALID_URL, "-seq"));
    }

    @Test
    public void testArgs_whenValidDictionaryUrlWeb_thenSuccess() {
        assertDoesNotThrow(() -> {
            Args args = args("-du", VALID_URL_WEB, "-seq");
            assertEquals(VALID_URL_WEB, args.getDictionaryUrl());
        });
    }

    @Test
    public void testArgs_whenValidDictionaryUrlFileSystem_thenSuccess() {
        assertDoesNotThrow(() -> {
            Args args = args("-du", VALID_URL_FS, "-seq");
            assertEquals(VALID_URL_FS, args.getDictionaryUrl());
        });
    }

    @Test
    public void testArgs_whenNoDictionaryCharsetSpecified_thenDefault() {
        Args args = args("-du", VALID_URL_WEB, "-seq");
        assertEquals(StandardCharsets.UTF_8.toString(), args.getDictionaryCharset());
    }

    @Test
    public void testArgs_whenInvalidDictionaryCharset_thenThrow() {
        assertThrowsExactly(ParameterException.class, () -> args("-du", VALID_URL_WEB, "-seq", "-dc", INVALID_CHARSET));
    }

    @Test
    public void testArgs_whenValidDictionaryCharset_thenSuccess() {
        assertDoesNotThrow(() -> Charset.availableCharsets().forEach((s, charset) -> {
            Args args = args("-du", VALID_URL_WEB, "-seq", "-dc", charset.name());
            assertEquals(charset.name(), args.getDictionaryCharset());
        }));
    }

    @Test
    public void testArgs_whenWordLengthNotSpecified_thenDefault() {
        Args args = args("-du", VALID_URL_WEB, "-seq");
        assertEquals(9, args.getWordLength());
    }

    @Test
    public void testArgs_whenInvalidWordLength_thenThrow() {
        assertThrowsExactly(ParameterException.class, () -> args("-du", VALID_URL_WEB, "-seq", "-wl", "1"));
        assertThrowsExactly(ParameterException.class, () -> args("-du", VALID_URL_WEB, "-seq", "-wl", "51"));
    }

    @Test
    public void testArgs_whenValidWordLength_thenSuccess() {
        assertDoesNotThrow(() -> args("-du", VALID_URL_WEB, "-seq", "-wl", "9"));
    }

    @Test
    public void testArgs_whenOneCharWordsNotSpecified_thenDefault() {
        Args args = args("-du", VALID_URL_WEB, "-seq");
        assertEquals(Set.of(), args.getOneCharWords());
    }

    @Test
    public void testArgs_whenInvalidOneCharWordsSpecifiedListSize1_thenThrow() {
        assertThrowsExactly(ParameterException.class, () -> args("-du", VALID_URL_WEB, "-seq", "-ocw", "AB"));
    }

    @Test
    public void testArgs_whenInvalidOneCharWordsSpecifiedListSize2_thenThrow() {
        assertThrowsExactly(ParameterException.class, () -> args("-du", VALID_URL_WEB, "-seq", "-ocw", "AB,C"));
    }

    @Test
    public void testArgs_whenValidOneCharWordsSpecifiedListSize1_thenSuccess() {
        Args args = args("-du", VALID_URL_WEB, "-seq", "-ocw", "A");
        assertEquals(Set.of("A"), args.getOneCharWords());
    }

    @Test
    public void testArgs_whenValidOneCharWordsSpecifiedListSize2_thenSuccess() {
        Args args = args("-du", VALID_URL_WEB, "-seq", "-ocw", "A,B");
        assertEquals(Set.of("A", "B"), args.getOneCharWords());
    }

    @Test
    public void testArgs_whenSeqAndParSpecified_thenThrow() {
        assertThrowsExactly(ParameterException.class, () -> args("-du", VALID_URL_WEB, "-seq", "-par"));
    }

    @Test
    public void testArgs_whenSeqAndParNotSpecified_thenThrow() {
        assertThrowsExactly(ParameterException.class, () -> args("-du", VALID_URL_WEB));
    }

    @Test
    public void testArgs_whenOnlySeqSpecified_thenSuccess() {
        assertDoesNotThrow(() -> {
            Args args = args("-du", VALID_URL_WEB, "-seq");
            assertEquals(Boolean.TRUE, args.getSequential());
            assertEquals(Boolean.FALSE, args.getParallel());
        });
    }

    @Test
    public void testArgs_whenOnlyParSpecified_thenSuccess() {
        assertDoesNotThrow(() -> {
            Args args = args("-du", VALID_URL_WEB, "-par");
            assertEquals(Boolean.TRUE, args.getParallel());
            assertEquals(Boolean.FALSE, args.getSequential());
        });
    }

    @Test
    public void testArgs_whenLogMatchingNotSpecified_thenDefault() {
        Args args = args("-du", VALID_URL_WEB, "-seq");
        assertEquals(false, args.getLogMatching());
    }

    @Test
    public void testArgs_whenLogMatchingSpecified_thenSuccess() {
        Args args = args("-du", VALID_URL_WEB, "-seq", "-lm");
        assertEquals(true, args.getLogMatching());
    }

    @Test
    public void testArgs_whenIncorrectOptionSpecified_thenThrow() {
        assertThrowsExactly(ParameterException.class, () -> args("-du", VALID_URL_WEB, "-seq", "--NO-SUCH-OPTION"));
    }

    @Test
    public void testArgs_whenSameDefaultShortAndLongParamsTestEqual_thenSuccess() {
        Args shortArgs = args("-du", VALID_URL_WEB, "-seq");
        Args longArgs = args("--dictionary-url", VALID_URL_WEB, "--sequential");
        assertEquals(shortArgs, longArgs);
    }

    @Test
    public void testArgs_whenSameShortAndLongParamsTestEqual_thenSuccess() {
        Args shortArgs = args("-du", VALID_URL_WEB, "-dc", "UTF-8", "-wl", "9", "-ocw", "A", "-seq", "-lm");
        Args longArgs = args("--dictionary-url", VALID_URL_WEB, "--dictionary-charset", "UTF-8",
                "--word-length", "9", "--one-char-words", "A", "--sequential", "--log-matching");
        assertEquals(shortArgs, longArgs);
    }

    @Test
    public void testArgs_validateArgsEqualsMethod() {
        Args args1 = args("-du", VALID_URL_WEB, "-seq");
        Args args2 = args("-du", VALID_URL_WEB, "-dc", "UTF-8", "-wl", "2", "-ocw", "A,B", "-par", "-lm");
        Args args3 = args("-du", VALID_URL_WEB, "-dc", "UTF-8", "-wl", "2", "-ocw", "A,B", "-par", "-lm");

        assertEquals(args1, args1);
        assertEquals(args2, args3);

        assertNotEquals(null, args1);
        assertNotEquals(new Object(), args1);
        assertNotEquals(args1, args2);
    }

}
