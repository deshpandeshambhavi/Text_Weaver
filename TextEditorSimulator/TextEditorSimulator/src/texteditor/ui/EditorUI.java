package texteditor.ui;

import texteditor.engine.TextEditorEngine;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.Map;

/**
 * ╔══════════════════════════════════════════════════════╗
 *  Text Editor Simulator — Amber Walnut Morning Theme
 *  Palette:
 *    WALNUT_DARK   #4A413C  — base background
 *    AMBER         #BB6C43  — primary accent
 *    CREAM         #EBEFEE  — primary text / light surface
 *    TERRACOTTA    #C8906D  — hover / secondary accent
 *    SAND          #CCB499  — muted text, borders, chips
 * ╚══════════════════════════════════════════════════════╝
 */
public class EditorUI extends JFrame {

    // ── Palette ───────────────────────────────────────────────────
    private static final Color WALNUT_DARK   = new Color(0x4A413C);
    private static final Color WALNUT_DARKER = new Color(0x2E2925);
    private static final Color WALNUT_MID    = new Color(0x3A3330);
    private static final Color AMBER         = new Color(0xBB6C43);
    private static final Color AMBER_HOVER   = new Color(0xC8906D);
    private static final Color CREAM         = new Color(0xEBEFEE);
    private static final Color SAND          = new Color(0xCCB499);
    private static final Color SAND_DIM      = new Color(0x9E8B78);

    // ── Engine ────────────────────────────────────────────────────
    private final TextEditorEngine engine = new TextEditorEngine();

    // ── UI Components ─────────────────────────────────────────────
    private final JTextArea   textArea         = new JTextArea();
    private final JTextArea   lineNumbers      = new JTextArea();
    private final JLabel      statusLine       = new JLabel();
    private final JLabel      statusWords      = new JLabel();
    private final JLabel      statusUndo       = new JLabel();
    private final JPanel      suggestionsPanel = new JPanel();
    private final JTextField  searchField      = createStyledField("Search…");
    private final JTextField  replaceField     = createStyledField("Replace with…");

    private boolean updating = false;

    // ─────────────────────────────────────────────────────────────
    //  CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────

    public EditorUI() {
        super("✦ Text Editor Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(980, 700);
        setMinimumSize(new Dimension(720, 500));
        setLocationRelativeTo(null);

        getContentPane().setBackground(WALNUT_DARKER);

        buildUI();
        attachListeners();
        updateStatus();
        updateLineNumbers();

        engine.loadDocument(
            "Welcome to Text Editor Simulator\n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
            "Start typing to see everything in action.\n"
        );
        syncUIFromEngine();
    }

    // ─────────────────────────────────────────────────────────────
    //  BUILD UI
    // ─────────────────────────────────────────────────────────────

    private void buildUI() {

        // ── Text Area ─────────────────────────────────────────────
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        textArea.setBackground(WALNUT_DARKER);
        textArea.setForeground(CREAM);
        textArea.setCaretColor(AMBER);
        textArea.setSelectionColor(new Color(0xBB6C43));
        textArea.setSelectedTextColor(CREAM);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setMargin(new Insets(12, 14, 12, 14));
        textArea.setBorder(null);

        // ── Line Numbers ──────────────────────────────────────────
        lineNumbers.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 15));
        lineNumbers.setEditable(false);
        lineNumbers.setBackground(WALNUT_MID);
        lineNumbers.setForeground(SAND_DIM);
        lineNumbers.setMargin(new Insets(12, 10, 12, 10));
        lineNumbers.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2,
                new Color(0x5C4438)));

        // ── Scroll Pane ───────────────────────────────────────────
        JScrollPane editorScrollPane = new JScrollPane(textArea);
        editorScrollPane.setRowHeaderView(lineNumbers);
        editorScrollPane.setBorder(BorderFactory.createLineBorder(
                new Color(0x5C504A), 1));
        editorScrollPane.getViewport().setBackground(WALNUT_DARKER);
        styleScrollBar(editorScrollPane.getVerticalScrollBar());
        styleScrollBar(editorScrollPane.getHorizontalScrollBar());

        // ── Suggestions Panel ─────────────────────────────────────
        suggestionsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 8, 6));
        suggestionsPanel.setBackground(WALNUT_MID);
        suggestionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0x5C504A)),
                new EmptyBorder(2, 6, 2, 6)
        ));
        suggestionsPanel.setPreferredSize(new Dimension(980, 46));

        // ── Status Bar ────────────────────────────────────────────
        JPanel statusBar = buildStatusBar();

        // ── Toolbar + Title ───────────────────────────────────────
        JPanel topStack = new JPanel();
        topStack.setLayout(new BoxLayout(topStack, BoxLayout.Y_AXIS));
        topStack.setBackground(WALNUT_DARKER);
        topStack.add(buildTitleBar());
        topStack.add(buildToolbar());

        // ── Center ────────────────────────────────────────────────
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.setBackground(WALNUT_DARKER);
        editorPanel.add(editorScrollPane, BorderLayout.CENTER);
        editorPanel.add(suggestionsPanel, BorderLayout.SOUTH);

        // ── Assemble ──────────────────────────────────────────────
        getContentPane().setLayout(new BorderLayout());
        setJMenuBar(buildMenuBar());
        getContentPane().add(topStack,     BorderLayout.NORTH);
        getContentPane().add(editorPanel,  BorderLayout.CENTER);
        getContentPane().add(statusBar,    BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────────────────────
    //  TITLE BAR
    // ─────────────────────────────────────────────────────────────

    private JPanel buildTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(WALNUT_DARK);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x5C504A)),
                new EmptyBorder(8, 16, 8, 16)
        ));

        JLabel title = new JLabel("✦  Text Editor Simulator");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        title.setForeground(AMBER);



        bar.add(title, BorderLayout.WEST);
        return bar;
    }

    // ─────────────────────────────────────────────────────────────
    //  TOOLBAR
    // ─────────────────────────────────────────────────────────────

    private JPanel buildToolbar() {
        JPanel tb = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        tb.setBackground(WALNUT_DARK);
        tb.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x5C504A)));

        tb.add(makeToolButton("↩  Undo",       e -> performUndo()));
        tb.add(makeToolButton("↪  Redo",       e -> performRedo()));
        tb.add(makeSeparator());
        tb.add(searchField);
        tb.add(makeToolButton("Find",          e -> performFind()));
        tb.add(replaceField);
        tb.add(makeToolButton("Replace All",   e -> performReplaceAll()));
        tb.add(makeSeparator());
        tb.add(makeToolButton("◈  Stats",      e -> showStats()));

        return tb;
    }

    // ─────────────────────────────────────────────────────────────
    //  STATUS BAR
    // ─────────────────────────────────────────────────────────────

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bar.setBackground(WALNUT_DARK);
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(0x5C504A)));
        bar.setPreferredSize(new Dimension(980, 30));

        styleStatusChip(statusLine);
        styleStatusChip(statusWords);
        styleStatusChip(statusUndo);

        bar.add(statusLine);
        bar.add(makeStatusDot());
        bar.add(statusWords);
        bar.add(makeStatusDot());
        bar.add(statusUndo);

        return bar;
    }

    private void styleStatusChip(JLabel label) {
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        label.setForeground(SAND);
        label.setBorder(new EmptyBorder(0, 14, 0, 14));
    }

    private JLabel makeStatusDot() {
        JLabel dot = new JLabel("·");
        dot.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        dot.setForeground(SAND_DIM);
        return dot;
    }

    // ─────────────────────────────────────────────────────────────
    //  MENU BAR
    // ─────────────────────────────────────────────────────────────

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(WALNUT_DARK);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x3A3330)));

        JMenu fileMenu   = styledMenu("File");
        JMenu editMenu   = styledMenu("Edit");
        JMenu searchMenu = styledMenu("Search");
        JMenu viewMenu   = styledMenu("View");

        JMenuItem newItem  = styledMenuItem("New");
        JMenuItem exitItem = styledMenuItem("Exit");
        newItem.addActionListener(e -> { engine.loadDocument(""); syncUIFromEngine(); });
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(newItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenuItem undoItem = styledMenuItem("Undo   Ctrl+Z");
        JMenuItem redoItem = styledMenuItem("Redo   Ctrl+Y");
        JMenuItem selAll   = styledMenuItem("Select All   Ctrl+A");
        undoItem.addActionListener(e -> performUndo());
        redoItem.addActionListener(e -> performRedo());
        selAll.addActionListener(e -> textArea.selectAll());
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        editMenu.add(selAll);

        JMenuItem findItem    = styledMenuItem("Find");
        JMenuItem replaceItem = styledMenuItem("Replace All");
        findItem.addActionListener(e -> performFind());
        replaceItem.addActionListener(e -> performReplaceAll());
        searchMenu.add(findItem);
        searchMenu.add(replaceItem);

        JMenuItem statsItem = styledMenuItem("Word Statistics");
        statsItem.addActionListener(e -> showStats());
        viewMenu.add(statsItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(searchMenu);
        menuBar.add(viewMenu);
        return menuBar;
    }

    // ─────────────────────────────────────────────────────────────
    //  LISTENERS
    // ─────────────────────────────────────────────────────────────

    private void attachListeners() {
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z) {
                    e.consume(); performUndo();
                } else if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Y) {
                    e.consume(); performRedo();
                }
            }
        });

        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { if (!updating) syncEngineFromUI(e, true);  }
            @Override public void removeUpdate(DocumentEvent e)  { if (!updating) syncEngineFromUI(e, false); }
            @Override public void changedUpdate(DocumentEvent e) {}
        });

        textArea.addCaretListener(e -> { if (!updating) updateStatus(); });
    }

    // ─────────────────────────────────────────────────────────────
    //  SYNC
    // ─────────────────────────────────────────────────────────────

    private void syncEngineFromUI(DocumentEvent e, boolean isInsert) {
        try {
            if (isInsert) {
                String inserted = e.getDocument().getText(e.getOffset(), e.getLength());
                for (char c : inserted.toCharArray()) engine.insert(c);
            } else {
                for (int i = 0; i < e.getLength(); i++) engine.delete();
            }
            updateAutoSuggestions();
            updateLineNumbers();
            updateStatus();
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    private void syncUIFromEngine() {
        updating = true;
        try {
            String text = engine.getText();
            int pos = Math.min(engine.getCursorIndex(), text.length());
            textArea.setText(text);
            textArea.setCaretPosition(pos);
        } finally {
            updating = false;
        }
        updateAutoSuggestions();
        updateLineNumbers();
        updateStatus();
    }

    // ─────────────────────────────────────────────────────────────
    //  OPERATIONS
    // ─────────────────────────────────────────────────────────────

    private void performUndo() {
        if (engine.undo()) syncUIFromEngine();
        else flashStatus("Nothing to undo");
    }

    private void performRedo() {
        if (engine.redo()) syncUIFromEngine();
        else flashStatus("Nothing to redo");
    }

    private void performFind() {
        String term = searchField.getText().trim();
        if (term.isEmpty()) return;

        List<Integer> positions = engine.findAll(term);
        if (positions.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "\"" + term + "\" not found in document.",
                    "Find", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Highlighter h = textArea.getHighlighter();
        h.removeAllHighlights();
        Highlighter.HighlightPainter painter =
                new DefaultHighlighter.DefaultHighlightPainter(AMBER);
        for (int pos : positions) {
            try { h.addHighlight(pos, pos + term.length(), painter); }
            catch (BadLocationException ex) { ex.printStackTrace(); }
        }
        textArea.setCaretPosition(positions.get(0));
        flashStatus("Found " + positions.size() + " occurrence(s) of \"" + term + "\"");
    }

    private void performReplaceAll() {
        String target  = searchField.getText().trim();
        String replace = replaceField.getText();
        if (target.isEmpty()) return;
        int count = engine.replaceAll(target, replace);
        syncUIFromEngine();
        flashStatus("Replaced " + count + " occurrence(s) of \"" + target + "\"");
    }

    private void flashStatus(String message) {
        Color original = statusLine.getForeground();
        statusLine.setForeground(AMBER);
        statusLine.setText("  " + message);
        Timer timer = new Timer(2500, e -> {
            statusLine.setForeground(original);
            updateStatus();
        });
        timer.setRepeats(false);
        timer.start();
    }

    // ─────────────────────────────────────────────────────────────
    //  AUTOCOMPLETE CHIPS (Trie)
    // ─────────────────────────────────────────────────────────────

    private void updateAutoSuggestions() {
        List<String> suggestions = engine.getAutoSuggestions(7);
        suggestionsPanel.removeAll();

        JLabel lbl = new JLabel("  ◆ ");
        lbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        lbl.setForeground(SAND_DIM);
        suggestionsPanel.add(lbl);

        if (suggestions.isEmpty()) {
            JLabel empty = new JLabel("Type a word for autocomplete suggestions…");
            empty.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
            empty.setForeground(SAND_DIM);
            suggestionsPanel.add(empty);
        } else {
            for (String word : suggestions) {
                suggestionsPanel.add(makeSuggestionChip(word));
            }
        }

        suggestionsPanel.revalidate();
        suggestionsPanel.repaint();
    }

    private JButton makeSuggestionChip(String word) {
        JButton chip = new JButton(word) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? AMBER : new Color(0x5C4F47);
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        chip.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        chip.setForeground(CREAM);
        chip.setContentAreaFilled(false);
        chip.setBorderPainted(false);
        chip.setFocusPainted(false);
        chip.setOpaque(false);
        chip.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        chip.setBorder(new EmptyBorder(3, 12, 3, 12));
        chip.setToolTipText("Autocomplete: " + word);
        chip.addActionListener(e -> {
            engine.applyAutoComplete(word);
            syncUIFromEngine();
        });
        return chip;
    }

    // ─────────────────────────────────────────────────────────────
    //  LINE NUMBERS
    // ─────────────────────────────────────────────────────────────

    private void updateLineNumbers() {
        int lines = textArea.getLineCount();
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= lines; i++) {
            sb.append(String.format("%3d  %n", i));
        }
        lineNumbers.setText(sb.toString());
    }

    // ─────────────────────────────────────────────────────────────
    //  STATUS UPDATE
    // ─────────────────────────────────────────────────────────────

    private void updateStatus() {
        int line  = engine.getCurrentLine() + 1;
        int col   = engine.getCurrentColumn() + 1;
        int words = engine.getWordCount();
        int chars = engine.getCharCount();
        int undoD = engine.getUndoStackSize();
        int redoD = engine.getRedoStackSize();

        statusLine.setText("  Ln " + line + "  Col " + col);
        statusWords.setText("  " + words + " words  ·  " + chars + " chars");
        statusUndo.setText("  Undo " + undoD + "  ·  Redo " + redoD);
    }

    // ─────────────────────────────────────────────────────────────
    //  STATS DIALOG
    // ─────────────────────────────────────────────────────────────

    private void showStats() {
        List<Map.Entry<String, Integer>> topWords = engine.getTopWords(10);

        StringBuilder sb = new StringBuilder();
        sb.append("  Document Statistics\n");
        sb.append("  ──────────────────────────────\n\n");
        sb.append(String.format("  %-16s %d%n", "Words",       engine.getWordCount()));
        sb.append(String.format("  %-16s %d%n", "Characters",  engine.getCharCount()));
        sb.append(String.format("  %-16s %d%n", "Lines",       engine.getLineCount()));
        sb.append(String.format("  %-16s %d%n", "Undo depth",  engine.getUndoStackSize()));
        sb.append(String.format("  %-16s %d%n", "Redo depth",  engine.getRedoStackSize()));
        sb.append("\n  Top Words (Hash Table)\n");
        sb.append("  ──────────────────────────────\n\n");

        if (topWords.isEmpty()) {
            sb.append("  (no words recorded yet)\n");
        } else {
            for (Map.Entry<String, Integer> entry : topWords) {
                sb.append(String.format("  %-20s ×%d%n", entry.getKey(), entry.getValue()));
            }
        }

        JTextArea area = new JTextArea(sb.toString());
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setEditable(false);
        area.setBackground(WALNUT_DARK);
        area.setForeground(CREAM);
        area.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(380, 340));
        sp.setBorder(BorderFactory.createLineBorder(SAND_DIM));
        sp.getViewport().setBackground(WALNUT_DARK);

        JOptionPane.showMessageDialog(this, sp, "◈  Statistics", JOptionPane.PLAIN_MESSAGE);
    }

    // ─────────────────────────────────────────────────────────────
    //  COMPONENT FACTORIES
    // ─────────────────────────────────────────────────────────────

    private JButton makeToolButton(String label, ActionListener action) {
        JButton btn = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover()
                        ? AMBER_HOVER
                        : getModel().isPressed()
                        ? AMBER.darker()
                        : AMBER;
                g2.setColor(bg);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btn.setForeground(CREAM);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(5, 14, 5, 14));
        btn.addActionListener(action);
        return btn;
    }

    private JComponent makeSeparator() {
        JPanel sep = new JPanel();
        sep.setPreferredSize(new Dimension(1, 24));
        sep.setBackground(new Color(0x6A5D55));
        sep.setOpaque(true);
        return sep;
    }

    private JTextField createStyledField(String placeholder) {
        JTextField field = new JTextField(13) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WALNUT_DARKER);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.dispose();
                super.paintComponent(g);

                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D pg = (Graphics2D) g.create();
                    pg.setColor(SAND_DIM);
                    pg.setFont(getFont().deriveFont(Font.ITALIC));
                    FontMetrics fm = pg.getFontMetrics();
                    pg.drawString(placeholder, getInsets().left + 4,
                            (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    pg.dispose();
                }
            }
        };
        field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        field.setForeground(CREAM);
        field.setBackground(WALNUT_DARKER);
        field.setCaretColor(AMBER);
        field.setOpaque(false);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x6A5D55), 1, true),
                new EmptyBorder(4, 8, 4, 8)
        ));
        return field;
    }

    private JMenu styledMenu(String text) {
        JMenu menu = new JMenu(text);
        menu.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        menu.setForeground(SAND);
        menu.getPopupMenu().setBackground(WALNUT_DARK);
        menu.getPopupMenu().setBorder(
                BorderFactory.createLineBorder(new Color(0x5C504A), 1));
        return menu;
    }

    private JMenuItem styledMenuItem(String text) {
        JMenuItem item = new JMenuItem(text);
        item.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        item.setForeground(CREAM);
        item.setBackground(WALNUT_DARK);
        item.setOpaque(true);
        item.setBorder(new EmptyBorder(6, 14, 6, 14));
        item.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                item.setBackground(new Color(0x5C4F47));
            }
            @Override public void mouseExited(MouseEvent e) {
                item.setBackground(WALNUT_DARK);
            }
        });
        return item;
    }

    private void styleScrollBar(JScrollBar bar) {
        bar.setBackground(WALNUT_MID);
        bar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                thumbColor          = new Color(0x6A5D55);
                trackColor          = WALNUT_MID;
                thumbHighlightColor = AMBER;
            }
            @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
            private JButton zeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isDragging ? AMBER : new Color(0x7A6D65));
                g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
                g2.dispose();
            }
            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
                g.setColor(WALNUT_MID);
                g.fillRect(r.x, r.y, r.width, r.height);
            }
        });
    }
}
