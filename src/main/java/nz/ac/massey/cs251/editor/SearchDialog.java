package nz.ac.massey.cs251.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * A simple find dialog. It looks for a single word inside the editor,
 * highlights every match and shows how many were found.
 */
public class SearchDialog extends JDialog {

    private static final long serialVersionUID = 1L;

    private final EditorFrame owner;
    private JTextField wordField;
    private JLabel resultLabel;
    private List<Integer> lastMatches;
    private static final Color HIGHLIGHT = new Color(255, 255, 150);

    public SearchDialog(EditorFrame owner) {
        super(owner, "Find", true);
        this.owner = owner;
        buildUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void buildUI() {
        wordField = new JTextField(20);
        wordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                find();
            }
        });

        JButton findButton = new JButton("Find");
        findButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                find();
            }
        });

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        resultLabel = new JLabel(" ", SwingConstants.CENTER);

        JPanel top = new JPanel();
        top.add(new JLabel("Find:"));
        top.add(wordField);
        top.add(findButton);
        top.add(closeButton);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(resultLabel, BorderLayout.SOUTH);
    }

    /**
     * Clears the previous highlights, then searches the whole document
     * for the word typed by the user.
     */
    private void find() {
        clearHighlight();
        String word = wordField.getText();
        if (word == null || word.trim().isEmpty()) {
            resultLabel.setText("Please type a word.");
            return;
        }
        String text = owner.getText();
        lastMatches = FileHelper.findAll(text, word);
        if (lastMatches.isEmpty()) {
            resultLabel.setText("No matches found.");
            return;
        }
        highlightMatches(word.length());
        owner.moveCaretTo(lastMatches.get(0));
        resultLabel.setText(lastMatches.size() + " match(es) found.");
    }

    /**
     * Paints a yellow background over every matching word.
     */
    private void highlightMatches(int wordLength) {
        for (int pos : lastMatches) {
            SimpleAttributeSet style = new SimpleAttributeSet();
            StyleConstants.setBackground(style, HIGHLIGHT);
            owner.getStyledDocument().setCharacterAttributes(pos, wordLength, style, false);
        }
    }

    /**
     * Restores the normal white background before a new search.
     */
    private void clearHighlight() {
        if (lastMatches == null) {
            return;
        }
        SimpleAttributeSet normal = new SimpleAttributeSet();
        StyleConstants.setBackground(normal, Color.WHITE);
        owner.getStyledDocument().setCharacterAttributes(0, owner.getText().length(), normal, true);
        lastMatches = null;
    }
}
