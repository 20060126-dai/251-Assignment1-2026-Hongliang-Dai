package nz.ac.massey.cs251.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.odftoolkit.odfdom.doc.OdfTextDocument;

/**
 * All the file related work of the editor: opening, saving, converting
 * to PDF, printing, and the plain text search used by the find dialog.
 */
public class FileHelper {

    private static final String[] TEXT_EXTENSIONS = {
            "txt", "java", "py", "js", "cpp", "c", "h", "rtf", "odt"
    };

    /**
     * Shows the open dialog, reads the chosen file and puts the content
     * into the editor. Supports .txt, source code files, .rtf and .odt.
     * Plain text files are always read as UTF-8.
     */
    public static void openFile(EditorFrame frame) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Open File");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Text and Source Files", TEXT_EXTENSIONS));
        int result = chooser.showOpenDialog(frame);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try {
            String text;
            String name = file.getName().toLowerCase(Locale.ROOT);
            if (name.endsWith(".odt")) {
                text = readOdt(file);
            } else if (name.endsWith(".rtf")) {
                text = readRtf(file);
            } else {
                text = readTextFile(file);
            }
            frame.setEditorText(text);
            frame.setCurrentFile(file);
            if (isSourceFile(name)) {
                frame.applySyntaxHighlighting(name);
            } else {
                frame.clearSyntaxHighlighting();
            }
        } catch (Exception e) {
            frame.showError("Could not open the file:\n" + e.getMessage());
        }
    }

    /**
     * Shows the save dialog and writes the editor content as a .txt file.
     * Adds the .txt extension automatically if the user forgot to type it.
     */
    public static void saveFile(EditorFrame frame) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save File");
        chooser.setFileFilter(new FileNameExtensionFilter("Text File", "txt"));
        int result = chooser.showSaveDialog(frame);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".txt")) {
            file = new File(file.getAbsolutePath() + ".txt");
        }
        try {
            writeTextFile(file, frame.getText());
            frame.setCurrentFile(file);
        } catch (IOException e) {
            frame.showError("Could not save the file:\n" + e.getMessage());
        }
    }

    /**
     * Shows a save dialog and exports the editor content to a PDF file.
     */
    public static void saveAsPdf(EditorFrame frame) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export as PDF");
        chooser.setFileFilter(new FileNameExtensionFilter("PDF File", "pdf"));
        int result = chooser.showSaveDialog(frame);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            file = new File(file.getAbsolutePath() + ".pdf");
        }
        try {
            exportPdf(file, frame.getText());
            frame.showInfo("PDF saved to:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            frame.showError("Could not create the PDF:\n" + e.getMessage());
        }
    }

    /**
     * Sends the editor content to the local printer.
     */
    public static void print(EditorFrame frame) {
        try {
            frame.printText();
        } catch (Exception e) {
            frame.showError("Printing failed:\n" + e.getMessage());
        }
    }

    /**
     * Reads a normal text file as UTF-8.
     */
    public static String readTextFile(File file) throws IOException {
        String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        // remove a UTF-8 BOM if it is there
        if (content.startsWith("\uFEFF")) {
            content = content.substring(1);
        }
        return content;
    }

    /**
     * Writes a string to a text file as UTF-8.
     */
    public static void writeTextFile(File file, String text) throws IOException {
        Files.write(file.toPath(), text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Reads the text content of an OpenDocument (.odt) file.
     */
    public static String readOdt(File file) throws Exception {
        try (OdfTextDocument doc = OdfTextDocument.loadDocument(file)) {
            return doc.getContentRoot().getTextContent();
        }
    }

    /**
     * Reads the text content of a Rich Text Format (.rtf) file using the
     * RTF editor kit that comes with Java.
     */
    public static String readRtf(File file) throws Exception {
        RTFEditorKit kit = new RTFEditorKit();
        Document doc = kit.createDefaultDocument();
        try (FileInputStream in = new FileInputStream(file)) {
            kit.read(in, doc, 0);
        }
        return doc.getText(0, doc.getLength());
    }

    /**
     * Finds every position of a word inside a text. The search is not
     * case sensitive. Used by the find dialog and by the unit tests.
     */
    public static List<Integer> findAll(String text, String word) {
        List<Integer> positions = new ArrayList<>();
        if (text == null || word == null || word.isEmpty()) {
            return positions;
        }
        String lowerText = text.toLowerCase(Locale.ROOT);
        String lowerWord = word.toLowerCase(Locale.ROOT);
        int index = 0;
        while (index < lowerText.length()) {
            int pos = lowerText.indexOf(lowerWord, index);
            if (pos < 0) {
                break;
            }
            positions.add(pos);
            index = pos + lowerWord.length();
        }
        return positions;
    }

    private static boolean isSourceFile(String name) {
        return name.endsWith(".java") || name.endsWith(".py")
                || name.endsWith(".js") || name.endsWith(".cpp")
                || name.endsWith(".c") || name.endsWith(".h");
    }

    /**
     * Writes plain text into a PDF file, a simple page of text per page.
     */
    private static void exportPdf(File file, String text) throws IOException {
        try (PDDocument pdf = new PDDocument()) {
            String[] lines = text.split("\\r?\\n", -1);
            for (int start = 0; start < lines.length; start += 45) {
                int end = Math.min(start + 45, lines.length);
                writePdfPage(pdf, lines, start, end);
            }
            pdf.save(file);
        }
    }

    /**
     * Writes up to 45 lines onto one PDF page.
     */
    private static void writePdfPage(PDDocument pdf, String[] lines, int start, int end)
            throws IOException {
        PDPage page = new PDPage();
        pdf.addPage(page);
        try (PDPageContentStream stream = new PDPageContentStream(pdf, page)) {
            stream.beginText();
            stream.setFont(PDType1Font.HELVETICA, 12);
            stream.setLeading(16f);
            stream.newLineAtOffset(50, 750);
            for (int i = start; i < end; i++) {
                stream.showText(cleanPdfText(lines[i]));
                stream.newLine();
            }
            stream.endText();
        }
    }

    /**
     * Removes characters that the built-in PDF font cannot display.
     */
    private static String cleanPdfText(String line) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\t') {
                sb.append("    ");
            } else if (c >= 32 && c <= 255) {
                sb.append(c);
            } else if (c > 255) {
                sb.append('?');
            }
            // other control characters are simply skipped
        }
        return sb.toString();
    }
}
