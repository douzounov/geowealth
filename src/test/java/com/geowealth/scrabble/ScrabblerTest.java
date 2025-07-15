package com.geowealth.scrabble;

import com.geowealth.scrabble.cli.Args;
import com.geowealth.scrabble.impl.Scrabbler;
import com.geowealth.scrabble.impl.SequentialScrabbler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.geowealth.scrabble.ArgsUtils.allArgCombinations;
import static com.geowealth.scrabble.ArgsUtils.args;
import static org.junit.jupiter.api.Assertions.*;

public class ScrabblerTest {

    @Test
    public void test_whenDictionaryIsWindows1251_thenSuccess() throws Exception {

        File tmp = File.createTempFile("scrabbler", null);

        try (InputStream is = ScrabblerTest.class.getResourceAsStream("/bg-utf8.txt")) {

            assertNotNull(is);

            List<String> lines = IOUtils.readLines(is, StandardCharsets.UTF_8);
            FileUtils.writeLines(tmp, Charset.forName("windows-1251").name(), lines);

            Scrabbler scrabbler = new SequentialScrabbler(args("-du", tmp.toPath().toUri().toString(), "-seq", "-wl", "4"));

            assertEquals(200, scrabbler.getDictionaryWords().size());
            assertEquals(new HashSet<>(lines), scrabbler.getDictionaryWords());

            assertEquals(14, scrabbler.getCandidateWords().size());

            assertEquals(Set.of("вяръ", "земя"), scrabbler.findMatchingWords());

        } finally {
            FileUtils.deleteQuietly(tmp);
        }
    }

    @Test
    public void test_whenDictionaryEmpty_thenSuccess() throws Exception {
        URL url = ScrabblerTest.class.getResource("/zero-bytes.txt");
        assertNotNull(url);

        Scrabbler scrabbler = new SequentialScrabbler(args("-du", url.toString(), "-seq"));
        assertEquals(0, scrabbler.getDictionaryWords().size());
        assertEquals(0, scrabbler.getCandidateWords().size());
        assertEquals(0, scrabbler.findMatchingWords().size());
    }

    @Test
    public void test_whenDictionaryOnlyWhitespace_thenSuccess() throws Exception {
        URL url = ScrabblerTest.class.getResource("/whitespace-only.txt");
        assertNotNull(url);

        Scrabbler scrabbler = new SequentialScrabbler(args("-du", url.toString(), "-seq"));
        assertEquals(0, scrabbler.getDictionaryWords().size());
        assertEquals(0, scrabbler.getCandidateWords().size());
        assertEquals(0, scrabbler.findMatchingWords().size());
    }

    @Test
    public void test_whenLinesWithLeadingOrTrailingWhitespace_thenSuccess() throws Exception {
        URL url = ScrabblerTest.class.getResource("/leading-trailing-whitespace.txt");
        assertNotNull(url);

        Scrabbler scrabbler = new SequentialScrabbler(args("-du", url.toString(), "-seq"));
        assertEquals(50, scrabbler.getDictionaryWords().size());
    }

    @Test
    public void test_whenAnyCharOutsideBMP_thenLineRemoved() throws Exception {
        URL url = ScrabblerTest.class.getResource("/chars-outside-bmp.txt");
        assertNotNull(url);

        Scrabbler scrabbler = new SequentialScrabbler(args("-du", url.toString(), "-seq"));
        assertEquals(45, scrabbler.getDictionaryWords().size());
    }

    @Test
    public void test_whenAttemptToModifyDictionaryWords_thenFailure() throws Exception {
        URL url = ScrabblerTest.class.getResource("/en-all-lines-valid.txt");
        assertNotNull(url);

        Scrabbler scrabbler = new SequentialScrabbler(args("-du", url.toString(), "-seq"));
        assertThrows(UnsupportedOperationException.class, () -> scrabbler.getDictionaryWords().add("word"));
    }

    @Test
    public void test_whenAttemptToModifyCandidateWords_thenFailure() throws Exception {
        URL url = ScrabblerTest.class.getResource("/en-all-lines-valid.txt");
        assertNotNull(url);

        Scrabbler scrabbler = new SequentialScrabbler(args("-du", url.toString(), "-seq"));
        assertThrows(UnsupportedOperationException.class, () -> scrabbler.getCandidateWords().add("word"));
    }

    @Test
    public void test_whenMatchesWithLength9Present_thenAlMatchesFound() throws Exception {
        URL url = ScrabblerTest.class.getResource("/en-all-lines-valid.txt");
        assertNotNull(url);

        Scrabbler scrabbler = new SequentialScrabbler(args("-du", url.toString(), "-seq", "-wl", "9"));
        assertEquals(Set.of("abcdefghi", "monastery"), scrabbler.getCandidateWords());
        assertEquals(Set.of("abcdefghi"), scrabbler.findMatchingWords());
    }

    @Test
    public void test_whenMatchesWithLength2Present_thenAllMatchesFound() throws Exception {
        URL url = ScrabblerTest.class.getResource("/en-all-lines-valid.txt");
        assertNotNull(url);

        Scrabbler scrabbler = new SequentialScrabbler(args("-du", url.toString(), "-seq", "-wl", "2"));
        assertEquals(Set.of("gi", "am"), scrabbler.getCandidateWords());
        assertEquals(Set.of("gi", "am"), scrabbler.findMatchingWords());
    }

    @Test
    public void test_whenScrabblerFindMatchingWordsCalledRepeatedly_thenSuccess() throws Exception {
        URL url = ScrabblerTest.class.getResource("/en-all-lines-valid.txt");
        assertNotNull(url);

        Scrabbler scrabbler = new SequentialScrabbler(args("-du", url.toString(), "-seq", "-wl", "9"));
        assertEquals(scrabbler.findMatchingWords(), scrabbler.findMatchingWords());
    }

    @Test
    public void test_whenNoWordsWithLength1Present_thenNoMatchesFound() throws Exception {
        URL url = ScrabblerTest.class.getResource("/no-one-char-words.txt");
        assertNotNull(url);

        Scrabbler scrabbler = new SequentialScrabbler(args("-du", url.toString(), "-seq", "-wl", "9"));
        assertEquals(0, scrabbler.findMatchingWords().size());
    }

    @Test
    public void test_whenNoWordsWithLength1PresentButSpecifiedAsArgs_thenAllMatchesFound() throws Exception {

        URL url = ScrabblerTest.class.getResource("/no-one-char-words.txt");
        assertNotNull(url);

        Scrabbler scrabbler = new SequentialScrabbler(args("-du", url.toString(), "-seq", "-wl", "9", "-ocw", "i"));
        assertEquals(Set.of("abcdefghi"), scrabbler.findMatchingWords());
    }

    @Test
    public void testArgs_whenCreateScrabblerWithAnyArgCombination_thenSuccess() {

        URL resourceUrl = ScrabblerTest.class.getResource("/en-all-lines-valid.txt");
        assertNotNull(resourceUrl);

        var all = allArgCombinations(resourceUrl.toString(), StandardCharsets.UTF_8.toString(),
                "9", "A,B", true, false, true);

        for (Args combination : all) {
            assertDoesNotThrow(() -> new SequentialScrabbler(combination));
        }
    }

}
