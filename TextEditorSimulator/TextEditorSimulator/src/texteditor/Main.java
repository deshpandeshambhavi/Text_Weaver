package texteditor;

import texteditor.ui.EditorUI;

import javax.swing.*;

/**
 * Entry point for the Text Editor Simulator.
 *
 * Run this class in Eclipse:
 *   Right-click Main.java → Run As → Java Application
 */
public class Main {

    public static void main(String[] args) {
        // Launch on the Swing Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            EditorUI editor = new EditorUI();
            editor.setVisible(true);
        });
    }
}
