package nz.ac.massey.cs251.editor;

import java.awt.Frame;

import javax.swing.JOptionPane;

/**
 * Small popup that shows the author information of the program.
 */
public class AboutDialog {

    /**
     * Shows the about box as a modal dialog on top of the given frame.
     */
    public static void show(Frame owner) {
        String message = "159.251 Text Editor\n"
                + "Assignment 1 - Software Design and Construction\n"
                + "\n"
                + "Author: Hongliang Dai (25014387)\n"
                + "\n"
                + "A simple text editor written in Java with Swing.\n"
                + "Supports plain text, source code highlighting, RTF and ODT files,\n"
                + "and PDF export.";
        JOptionPane.showMessageDialog(owner, message, "About",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
