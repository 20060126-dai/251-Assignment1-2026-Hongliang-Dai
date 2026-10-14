package nz.ac.massey.cs251.editor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;

import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.odftoolkit.odfdom.doc.OdfTextDocument;

/**
 * Unit tests for the open and save functionality of the editor.
 * These tests work directly on the file helper methods so no GUI
 * window is needed.
 */
public class FileHelperTest {

    @TempDir
    File tempDir;

    @Test
    public void testWriteAndReadTextFile() throws Exception {
        File file = new File(tempDir, "notes.txt");
        String content = "first line\nsecond line\nthird line";

        FileHelper.writeTextFile(file, content);

        assertTrue(file.exists());
        assertEquals(content, FileHelper.readTextFile(file));
    }

    @Test
    public void testReadRtfFile() throws Exception {
        File file = new File(tempDir, "letter.rtf");

        // build a small rtf file with the java rtf kit
        RTFEditorKit kit = new RTFEditorKit();
        Document doc = kit.createDefaultDocument();
        doc.insertString(0, "Hello from RTF", null);
        try (FileOutputStream out = new FileOutputStream(file)) {
            kit.write(out, doc, 0, doc.getLength());
        }

        String text = FileHelper.readRtf(file);
        assertTrue(text.contains("Hello from RTF"));
    }

    @Test
    public void testReadOdtFile() throws Exception {
        File file = new File(tempDir, "document.odt");

        // build a small odt file with the odf toolkit
        OdfTextDocument doc = OdfTextDocument.newTextDocument();
        doc.newParagraph("Hello from ODT");
        doc.save(file);
        doc.close();

        String text = FileHelper.readOdt(file);
        assertTrue(text.contains("Hello from ODT"));
    }
}
