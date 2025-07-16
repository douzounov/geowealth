package com.geowealth.scrabble;

import com.geowealth.scrabble.cli.Args;
import com.geowealth.scrabble.impl.ParallelScrabbler;
import com.geowealth.scrabble.impl.Scrabbler;
import com.geowealth.scrabble.impl.SequentialScrabbler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.geowealth.scrabble.ArgsUtils.allArgCombinations;
import static com.geowealth.scrabble.ArgsUtils.args;
import static org.junit.jupiter.api.Assertions.*;

public class ScrabblerTest {

    private static Stream<Class<? extends Scrabbler>> allScrabblerClasses() {
        return Stream.of(SequentialScrabbler.class, ParallelScrabbler.class);
    }

    private static Scrabbler scrabbler(Class<? extends Scrabbler> cl, Args args) throws Exception {
        return cl.getConstructor(Args.class).newInstance(args);
    }

    @ParameterizedTest
    @MethodSource("allScrabblerClasses")
    public void test_whenDictionaryIsWindows1251_thenSuccess(Class<? extends Scrabbler> cl) throws Exception {

        File tmp = File.createTempFile("scrabbler", null);

        try (InputStream is = ScrabblerTest.class.getResourceAsStream("/bg-utf8.txt")) {

            assertNotNull(is);

            List<String> lines = IOUtils.readLines(is, StandardCharsets.UTF_8);
            FileUtils.writeLines(tmp, Charset.forName("windows-1251").name(), lines);

            Scrabbler scrabbler = scrabbler(cl, args("-du", tmp.toPath().toUri().toString(), "-seq", "-wl", "4"));
            assertEquals(200, scrabbler.getDictionaryWords().size());
            assertEquals(new HashSet<>(lines), scrabbler.getDictionaryWords());

            assertEquals(14, scrabbler.getCandidateWords().size());

            assertEquals(Set.of("вяръ", "земя"), scrabbler.findMatchingWords());

        } finally {
            FileUtils.deleteQuietly(tmp);
        }
    }

    @ParameterizedTest
    @MethodSource("allScrabblerClasses")
    public void test_whenDictionaryEmpty_thenSuccess(Class<? extends Scrabbler> cl) throws Exception {
        URL url = ScrabblerTest.class.getResource("/zero-bytes.txt");
        assertNotNull(url);

        Scrabbler scrabbler = scrabbler(cl, args("-du", url.toString(), "-seq"));
        assertEquals(0, scrabbler.getDictionaryWords().size());
        assertEquals(0, scrabbler.getCandidateWords().size());
        assertEquals(0, scrabbler.findMatchingWords().size());
    }

    @ParameterizedTest
    @MethodSource("allScrabblerClasses")
    public void test_whenDictionaryOnlyWhitespace_thenSuccess(Class<? extends Scrabbler> cl) throws Exception {
        URL url = ScrabblerTest.class.getResource("/whitespace-only.txt");
        assertNotNull(url);

        Scrabbler scrabbler = scrabbler(cl, args("-du", url.toString(), "-seq"));
        assertEquals(0, scrabbler.getDictionaryWords().size());
        assertEquals(0, scrabbler.getCandidateWords().size());
        assertEquals(0, scrabbler.findMatchingWords().size());
    }

    @ParameterizedTest
    @MethodSource("allScrabblerClasses")
    public void test_whenLinesWithLeadingOrTrailingWhitespace_thenSuccess(Class<? extends Scrabbler> cl) throws Exception {
        URL url = ScrabblerTest.class.getResource("/leading-trailing-whitespace.txt");
        assertNotNull(url);

        Scrabbler scrabbler = scrabbler(cl, args("-du", url.toString(), "-seq"));
        assertEquals(50, scrabbler.getDictionaryWords().size());
    }

    @ParameterizedTest
    @MethodSource("allScrabblerClasses")
    public void test_whenAnyCharOutsideBMP_thenLineRemoved(Class<? extends Scrabbler> cl) throws Exception {
        URL url = ScrabblerTest.class.getResource("/chars-outside-bmp.txt");
        assertNotNull(url);

        Scrabbler scrabbler = scrabbler(cl, args("-du", url.toString(), "-seq"));
        assertEquals(45, scrabbler.getDictionaryWords().size());
    }

    @ParameterizedTest
    @MethodSource("allScrabblerClasses")
    public void test_whenAttemptToModifyDictionaryWords_thenFailure(Class<? extends Scrabbler> cl) throws Exception {
        URL url = ScrabblerTest.class.getResource("/en-all-lines-valid.txt");
        assertNotNull(url);

        Scrabbler scrabbler = scrabbler(cl, args("-du", url.toString(), "-seq"));
        assertThrows(UnsupportedOperationException.class, () -> scrabbler.getDictionaryWords().add("word"));
    }

    @ParameterizedTest
    @MethodSource("allScrabblerClasses")
    public void test_whenAttemptToModifyCandidateWords_thenFailure(Class<? extends Scrabbler> cl) throws Exception {
        URL url = ScrabblerTest.class.getResource("/en-all-lines-valid.txt");
        assertNotNull(url);

        Scrabbler scrabbler = scrabbler(cl, args("-du", url.toString(), "-seq"));
        assertThrows(UnsupportedOperationException.class, () -> scrabbler.getCandidateWords().add("word"));
    }

    @ParameterizedTest
    @MethodSource("allScrabblerClasses")
    public void test_whenMatchesWithLength9Present_thenAlMatchesFound(Class<? extends Scrabbler> cl) throws Exception {
        URL url = ScrabblerTest.class.getResource("/en-all-lines-valid.txt");
        assertNotNull(url);

        Scrabbler scrabbler = scrabbler(cl, args("-du", url.toString(), "-seq", "-wl", "9"));
        assertEquals(Set.of("abcdefghi", "monastery"), scrabbler.getCandidateWords());
        assertEquals(Set.of("abcdefghi"), scrabbler.findMatchingWords());
    }

    @ParameterizedTest
    @MethodSource("allScrabblerClasses")
    public void test_whenMatchesWithLength2Present_thenAllMatchesFound(Class<? extends Scrabbler> cl) throws Exception {
        URL url = ScrabblerTest.class.getResource("/en-all-lines-valid.txt");
        assertNotNull(url);

        Scrabbler scrabbler = scrabbler(cl, args("-du", url.toString(), "-seq", "-wl", "2"));
        assertEquals(Set.of("gi", "am"), scrabbler.getCandidateWords());
        assertEquals(Set.of("gi", "am"), scrabbler.findMatchingWords());
    }

    @ParameterizedTest
    @MethodSource("allScrabblerClasses")
    public void test_whenScrabblerFindMatchingWordsCalledRepeatedly_thenSuccess(Class<? extends Scrabbler> cl) throws Exception {
        URL url = ScrabblerTest.class.getResource("/en-all-lines-valid.txt");
        assertNotNull(url);

        Scrabbler scrabbler = scrabbler(cl, args("-du", url.toString(), "-seq", "-wl", "9"));
        assertEquals(scrabbler.findMatchingWords(), scrabbler.findMatchingWords());
    }

    @ParameterizedTest
    @MethodSource("allScrabblerClasses")
    public void test_whenNoWordsWithLength1Present_thenNoMatchesFound(Class<? extends Scrabbler> cl) throws Exception {
        URL url = ScrabblerTest.class.getResource("/no-one-char-words.txt");
        assertNotNull(url);

        Scrabbler scrabbler = scrabbler(cl, args("-du", url.toString(), "-seq", "-wl", "9"));
        assertEquals(0, scrabbler.findMatchingWords().size());
    }

    @ParameterizedTest
    @MethodSource("allScrabblerClasses")
    public void test_whenNoWordsWithLength1PresentButSpecifiedAsArgs_thenAllMatchesFound(Class<? extends Scrabbler> cl) throws Exception {

        URL url = ScrabblerTest.class.getResource("/no-one-char-words.txt");
        assertNotNull(url);

        Scrabbler scrabbler = scrabbler(cl, args("-du", url.toString(), "-seq", "-wl", "9", "-ocw", "i"));
        assertEquals(Set.of("abcdefghi"), scrabbler.findMatchingWords());
    }

    @ParameterizedTest
    @MethodSource("allScrabblerClasses")
    public void testArgs_whenCreateScrabblerWithAnyArgCombination_thenSuccess(Class<? extends Scrabbler> cl) {

        URL resourceUrl = ScrabblerTest.class.getResource("/en-all-lines-valid.txt");
        assertNotNull(resourceUrl);

        var all = allArgCombinations(resourceUrl.toString(), StandardCharsets.UTF_8.toString(),
                "9", "A,B", true, false, true);

        for (Args combination : all) {
            assertDoesNotThrow(() -> scrabbler(cl, combination));
        }
    }

}
