package nz.ac.massey.cs251.editor;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Simple syntax highlighting for source code files.
 * It colours keywords, comments, strings and numbers based on the
 * file extension. A plain token scan is used, which is good enough
 * for the sizes of files an editor like this is normally used with.
 */
public class SyntaxHighlighter {

    private static final int MAX_LINES = 3000;

    private static final Color KEYWORD_COLOR = new Color(0, 0, 180);
    private static final Color COMMENT_COLOR = new Color(120, 120, 120);
    private static final Color STRING_COLOR = new Color(0, 128, 0);
    private static final Color NUMBER_COLOR = new Color(180, 90, 0);

    private static final Map<String, Set<String>> KEYWORDS = new HashMap<>();

    static {
        KEYWORDS.put("java", new HashSet<>(Arrays.asList(
                "abstract", "assert", "boolean", "break", "byte", "case", "catch",
                "char", "class", "const", "continue", "default", "do", "double",
                "else", "enum", "extends", "final", "finally", "float", "for",
                "goto", "if", "implements", "import", "instanceof", "int",
                "interface", "long", "native", "new", "package", "private",
                "protected", "public", "return", "short", "static", "strictfp",
                "super", "switch", "synchronized", "this", "throw", "throws",
                "transient", "try", "void", "volatile", "while")));

        KEYWORDS.put("py", new HashSet<>(Arrays.asList(
                "and", "as", "assert", "async", "await", "break", "class",
                "continue", "def", "del", "elif", "else", "except", "False",
                "finally", "for", "from", "global", "if", "import", "in", "is",
                "lambda", "None", "nonlocal", "not", "or", "pass", "raise",
                "return", "True", "try", "while", "with", "yield")));

        KEYWORDS.put("js", new HashSet<>(Arrays.asList(
                "break", "case", "catch", "class", "const", "continue",
                "debugger", "default", "delete", "do", "else", "export",
                "extends", "false", "finally", "for", "function", "if",
                "import", "in", "instanceof", "let", "new", "null", "return",
                "super", "switch", "this", "throw", "true", "try", "typeof",
                "var", "void", "while", "with", "yield")));

        KEYWORDS.put("c", new HashSet<>(Arrays.asList(
                "auto", "break", "case", "char", "const", "continue", "default",
                "do", "double", "else", "enum", "extern", "float", "for", "goto",
                "if", "inline", "int", "long", "register", "restrict", "return",
                "short", "signed", "sizeof", "static", "struct", "switch",
                "typedef", "union", "unsigned", "void", "volatile", "while")));
    }

    private SyntaxHighlighter() {
        // static helpers only
    }

    /**
     * Returns a listener that recolours the document whenever the text
     * changes. Returns null when the extension is not supported.
     */
    public static DocumentListener createListener(final JTextPane pane, final String ext) {
        final Set<String> keywords = keywordSetFor(ext);
        if (keywords == null) {
            return null;
        }
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                scheduleColour(pane, keywords);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                scheduleColour(pane, keywords);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // attribute changes do not need a recolour
            }
        };
    }

    /**
     * Colours the whole document immediately.
     */
    public static void colour(JTextPane pane, String ext) {
        Set<String> keywords = keywordSetFor(ext);
        if (keywords != null) {
            highlight(pane, keywords);
        }
    }

    /**
     * Restores the normal foreground for the whole document.
     */
    public static void clear(JTextPane pane) {
        StyledDocument doc = pane.getStyledDocument();
        int len = doc.getLength();
        if (len > 0) {
            SimpleAttributeSet normal = makeStyle(pane.getForeground(), pane.getFont());
            doc.setCharacterAttributes(0, len, normal, true);
        }
    }

    private static Set<String> keywordSetFor(String ext) {
        String lang = ext;
        if ("cpp".equals(ext) || "h".equals(ext)) {
            lang = "c";
        }
        return KEYWORDS.get(lang);
    }

    /**
     * Runs the colour pass a little later so typing stays responsive.
     */
    private static void scheduleColour(final JTextPane pane, final Set<String> keywords) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                highlight(pane, keywords);
            }
        });
    }

    private static void highlight(JTextPane pane, Set<String> keywords) {
        StyledDocument doc = pane.getStyledDocument();
        if (doc.getDefaultRootElement().getElementCount() > MAX_LINES) {
            return; // too big, keep it fast
        }
        String text = getText(pane);
        int len = text.length();
        if (len == 0) {
            return;
        }

        SimpleAttributeSet normal = makeStyle(pane.getForeground(), pane.getFont());
        doc.setCharacterAttributes(0, len, normal, true);

        SimpleAttributeSet keyword = makeStyle(KEYWORD_COLOR, pane.getFont());
        SimpleAttributeSet comment = makeStyle(COMMENT_COLOR, pane.getFont());
        SimpleAttributeSet string = makeStyle(STRING_COLOR, pane.getFont());
        SimpleAttributeSet number = makeStyle(NUMBER_COLOR, pane.getFont());

        int i = 0;
        while (i < len) {
            char c = text.charAt(i);

            // line comment: // ... or # ...
            if (c == '/' && i + 1 < len && text.charAt(i + 1) == '/') {
                int end = text.indexOf('\n', i);
                if (end < 0) {
                    end = len;
                }
                doc.setCharacterAttributes(i, end - i, comment, false);
                i = end;
                continue;
            }
            if (c == '#') {
                int end = text.indexOf('\n', i);
                if (end < 0) {
                    end = len;
                }
                doc.setCharacterAttributes(i, end - i, comment, false);
                i = end;
                continue;
            }

            // block comment: /* ... */
            if (c == '/' && i + 1 < len && text.charAt(i + 1) == '*') {
                int end = text.indexOf("*/", i + 2);
                if (end < 0) {
                    end = len - 1;
                } else {
                    end = end + 1;
                }
                doc.setCharacterAttributes(i, end - i + 1, comment, false);
                i = end + 1;
                continue;
            }

            // string: "..." or '...' (skips escaped quotes)
            if (c == '"' || c == '\'') {
                char quote = c;
                int end = i + 1;
                while (end < len) {
                    if (text.charAt(end) == '\\') {
                        end += 2;
                        continue;
                    }
                    if (text.charAt(end) == quote) {
                        break;
                    }
                    end++;
                }
                if (end >= len) {
                    end = len - 1;
                }
                doc.setCharacterAttributes(i, end - i + 1, string, false);
                i = end + 1;
                continue;
            }

            // word: check if it is a keyword
            if (Character.isLetter(c) || c == '_') {
                int end = i;
                while (end < len && (Character.isLetterOrDigit(text.charAt(end)) || text.charAt(end) == '_')) {
                    end++;
                }
                String word = text.substring(i, end);
                if (keywords.contains(word)) {
                    doc.setCharacterAttributes(i, end - i, keyword, false);
                }
                i = end;
                continue;
            }

            // number
            if (Character.isDigit(c)) {
                int end = i;
                while (end < len && (Character.isDigit(text.charAt(end)) || text.charAt(end) == '.')) {
                    end++;
                }
                doc.setCharacterAttributes(i, end - i, number, false);
                i = end;
                continue;
            }

            i++;
        }
    }

    private static SimpleAttributeSet makeStyle(Color color, java.awt.Font font) {
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, color);
        StyleConstants.setFontFamily(style, font.getFamily());
        StyleConstants.setFontSize(style, font.getSize());
        return style;
    }

    private static String getText(JTextPane pane) {
        try {
            return pane.getDocument().getText(0, pane.getDocument().getLength());
        } catch (Exception e) {
            return "";
        }
    }
}
