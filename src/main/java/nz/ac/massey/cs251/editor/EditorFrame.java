package nz.ac.massey.cs251.editor;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

/**
 * The main editor window. Each window is its own EditorFrame, so the
 * "New" menu simply opens another instance. The window has a menu bar
 * with File, Edit, Search, View and Help menus.
 */
public class EditorFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextPane textPane;
    private JLabel statusLabel;
    private File currentFile;
    private DocumentListener highlighterListener;

    public EditorFrame() {
        super("Text Editor");
        buildUI();
        applySettings();
        packWindow();
    }

    private void buildUI() {
        textPane = new JTextPane();
        textPane.setFont(new Font("Consolas", Font.PLAIN, 14));
        add(new JScrollPane(textPane), BorderLayout.CENTER);

        statusLabel = new JLabel("Ready");
        add(statusLabel, BorderLayout.SOUTH);

        setJMenuBar(buildMenuBar());
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        // ---------------- File menu ----------------
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        fileMenu.add(makeItem("New", KeyEvent.VK_N, "control N", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new EditorFrame().setVisible(true);
            }
        }));
        fileMenu.add(makeItem("Open...", KeyEvent.VK_O, "control O", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileHelper.openFile(EditorFrame.this);
            }
        }));
        fileMenu.add(makeItem("Save", KeyEvent.VK_S, "control S", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileHelper.saveFile(EditorFrame.this);
            }
        }));
        fileMenu.addSeparator();
        fileMenu.add(makeItem("Save as PDF...", 0, "control shift P", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileHelper.saveAsPdf(EditorFrame.this);
            }
        }));
        fileMenu.add(makeItem("Print...", 0, "control P", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileHelper.print(EditorFrame.this);
            }
        }));
        fileMenu.addSeparator();
        fileMenu.add(makeItem("Exit", KeyEvent.VK_X, "alt F4", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                closeAllWindows();
            }
        }));
        bar.add(fileMenu);

        // ---------------- Edit menu (select, copy, cut, paste) ----------------
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);

        editMenu.add(makeItem("Select All", KeyEvent.VK_A, "control A", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textPane.selectAll();
            }
        }));
        editMenu.add(makeItem("Copy", KeyEvent.VK_C, "control C", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textPane.copy();
            }
        }));
        editMenu.add(makeItem("Cut", KeyEvent.VK_T, "control X", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textPane.cut();
            }
        }));
        editMenu.add(makeItem("Paste", KeyEvent.VK_P, "control V", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textPane.paste();
            }
        }));
        bar.add(editMenu);

        // ---------------- Search menu ----------------
        JMenu searchMenu = new JMenu("Search");
        searchMenu.setMnemonic(KeyEvent.VK_S);

        searchMenu.add(makeItem("Find...", KeyEvent.VK_F, "control F", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SearchDialog(EditorFrame.this).setVisible(true);
            }
        }));
        bar.add(searchMenu);

        // ---------------- View menu ----------------
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);

        viewMenu.add(makeItem("Insert Date and Time", 0, "F5", new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                insertDateTime();
            }
        }));
        bar.add(viewMenu);

        // ---------------- Help menu ----------------
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        helpMenu.add(makeItem("About", 0, null, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AboutDialog.show(EditorFrame.this);
            }
        }));
        bar.add(helpMenu);

        return bar;
    }

    /**
     * Helper that creates one menu item with an optional shortcut.
     */
    private JMenuItem makeItem(String title, int mnemonic, String shortcut,
                               ActionListener action) {
        JMenuItem item = new JMenuItem(title);
        if (mnemonic != 0) {
            item.setMnemonic(mnemonic);
        }
        if (shortcut != null) {
            item.setAccelerator(KeyStroke.getKeyStroke(shortcut.replace(' ', '+')));
        }
        item.addActionListener(action);
        return item;
    }

    private void packWindow() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 650);
        setLocationByPlatform(true);
    }

    /**
     * Reads editor.yaml and applies the font and colours to this window.
     */
    private void applySettings() {
        Settings s = Settings.load();
        textPane.setFont(new Font(s.getFont(), Font.PLAIN, s.getFontSize()));
        textPane.setForeground(s.getFontColor());
        textPane.setCaretColor(s.getFontColor());
        textPane.setBackground(s.getBackgroundColor());
    }

    /**
     * Inserts the current time and date at the very top of the document.
     */
    private void insertDateTime() {
        SimpleDateFormat format = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String stamp = format.format(new Date());
        try {
            textPane.getDocument().insertString(0, stamp + "\n", null);
        } catch (BadLocationException e) {
            // should never happen with position 0
        }
    }

    /**
     * Closes every editor window and stops the program.
     */
    private void closeAllWindows() {
        for (Window w : getWindows()) {
            w.dispose();
        }
        System.exit(0);
    }

    // ---------- methods used by FileHelper and SearchDialog ----------

    public String getText() {
        return textPane.getText();
    }

    public StyledDocument getStyledDocument() {
        return textPane.getStyledDocument();
    }

    public void setEditorText(String text) {
        removeHighlighter();
        textPane.setText(text);
        textPane.setCaretPosition(0);
        statusLabel.setText(text.length() + " characters");
    }

    public void setCurrentFile(File file) {
        this.currentFile = file;
        setTitle("Text Editor - " + file.getName());
    }

    public File getCurrentFile() {
        return currentFile;
    }

    /**
     * Turns on syntax highlighting based on the opened file name.
     */
    public void applySyntaxHighlighting(String fileName) {
        String ext = extensionOf(fileName);
        removeHighlighter();
        highlighterListener = SyntaxHighlighter.createListener(textPane, ext);
        if (highlighterListener != null) {
            textPane.getDocument().addDocumentListener(highlighterListener);
        }
        SyntaxHighlighter.colour(textPane, ext);
    }

    /**
     * Removes highlighting after opening a plain text file.
     */
    public void clearSyntaxHighlighting() {
        removeHighlighter();
        SyntaxHighlighter.clear(textPane);
    }

    private void removeHighlighter() {
        if (highlighterListener != null) {
            textPane.getDocument().removeDocumentListener(highlighterListener);
            highlighterListener = null;
        }
    }

    private String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    public void moveCaretTo(int position) {
        int pos = Math.min(position, textPane.getDocument().getLength());
        textPane.setCaretPosition(pos);
        try {
            textPane.scrollRectToVisible(textPane.modelToView(pos));
        } catch (BadLocationException e) {
            // the position was already clamped, nothing to do
        }
    }

    public void printText() throws Exception {
        textPane.print();
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Text Editor",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
