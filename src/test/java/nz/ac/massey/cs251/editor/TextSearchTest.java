package nz.ac.massey.cs251.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the search functionality.
 */
public class TextSearchTest {

    @Test
    public void testFindAllSimple() {
        List<Integer> positions = FileHelper.findAll(
                "the cat and the dog", "the");

        assertEquals(2, positions.size());
        assertEquals(0, positions.get(0).intValue());
        assertEquals(12, positions.get(1).intValue());
    }

    @Test
    public void testFindAllIsNotCaseSensitive() {
        List<Integer> positions = FileHelper.findAll(
                "Hello hello HELLO", "hello");

        assertEquals(3, positions.size());
    }

    @Test
    public void testFindAllNoMatch() {
        List<Integer> positions = FileHelper.findAll(
                "nothing here", "zebra");

        assertTrue(positions.isEmpty());
    }

    @Test
    public void testFindAllEmptyWord() {
        List<Integer> positions = FileHelper.findAll(
                "some text", "");

        assertTrue(positions.isEmpty());
    }

    @Test
    public void testFindAllOverlappingWords() {
        List<Integer> positions = FileHelper.findAll(
                "aaa", "aa");

        // "aaa" contains "aa" once, the search moves past each match
        assertEquals(1, positions.size());
    }
}
