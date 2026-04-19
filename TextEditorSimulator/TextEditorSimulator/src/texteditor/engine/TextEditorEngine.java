package texteditor.engine;

import texteditor.model.Action;
import texteditor.model.CharBuffer;
import texteditor.model.Trie;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DATA STRUCTURES SUMMARY:
 *
 *  ┌─────────────────────────────────────────────────────────────┐
 *  │  CharBuffer (DLL)   → stores every character in the file    │
 *  │  undoStack (Stack)  → history of insert/delete operations   │
 *  │  redoStack (Stack)  → operations undone, available to redo  │
 *  │  Trie               → prefix tree for word autocomplete      │
 *  │  wordFrequency (Map)→ hash table: word → count              │
 *  │  lineIndex (List)   → array: line number → char offset      │
 *  └─────────────────────────────────────────────────────────────┘
 *
 * TextEditorEngine wires all five structures into a single API
 * that the UI layer can call.
 */
public class TextEditorEngine {

    // ── Data Structures ──────────────────────────────────────────

    /** Doubly Linked List — stores every character */
    private final CharBuffer buffer;

    /** Stack — undo history (Ctrl+Z) */
    private final Deque<Action> undoStack;

    /** Stack — redo history (Ctrl+Y) */
    private final Deque<Action> redoStack;

    /** Trie — word autocomplete */
    private final Trie trie;

    /**
     * Hash Table — word frequency counter.
     * Key: lowercase word, Value: number of times typed
     */
    private final Map<String, Integer> wordFrequency;

    /**
     * Array — line index.
     * lineIndex.get(n) = character offset where line n starts.
     * Rebuilt after every insert/delete of '\n'.
     */
    private final List<Integer> lineIndex;

    // ── Constructor ──────────────────────────────────────────────

    public TextEditorEngine() {
        buffer        = new CharBuffer();
        undoStack     = new ArrayDeque<>();
        redoStack     = new ArrayDeque<>();
        trie          = new Trie();
        wordFrequency = new HashMap<>();
        lineIndex     = new ArrayList<>();
        lineIndex.add(0); // Line 0 always starts at index 0
    }

    // ─────────────────────────────────────────────────────────────
    //  BASIC EDITING
    // ─────────────────────────────────────────────────────────────

    /**
     * Insert a character at the current cursor position.
     * Pushes an INSERT Action onto the undo stack.
     * Clears the redo stack (new edit invalidates redo history).
     */
    public void insert(char c) {
        buffer.insertAtCursor(c);
        undoStack.push(new Action(Action.Type.INSERT, c, buffer.getCursorIndex()));
        redoStack.clear();  // ← IMPORTANT: new edit wipes redo

        // If a word boundary was just typed, register the completed word
        if (isWordBoundary(c)) {
            String word = extractWordBefore(buffer.getCursorIndex() - 1);
            if (!word.isEmpty()) {
                registerWord(word);
            }
        }

        // Rebuild line index if newline was inserted
        if (c == '\n') updateLineIndex();
    }

    /**
     * Insert a full string (e.g. when pasting or loading a file).
     */
    public void insertString(String text) {
        for (char c : text.toCharArray()) {
            insert(c);
        }
    }

    /**
     * Delete the character before the cursor (Backspace).
     * Pushes a DELETE Action onto the undo stack.
     */
    public void delete() {
        char deleted = buffer.deleteAtCursor();
        if (deleted == '\0') return; // Nothing to delete

        undoStack.push(new Action(Action.Type.DELETE, deleted, buffer.getCursorIndex()));
        redoStack.clear();

        if (deleted == '\n') updateLineIndex();
    }

    /**
     * Insert a newline character (Enter key).
     */
    public void newLine() {
        insert('\n');
    }

    // ─────────────────────────────────────────────────────────────
    //  UNDO / REDO (Stack operations)
    // ─────────────────────────────────────────────────────────────

    /**
     * CTRL+Z — Undo the last action.
     * Pops from undoStack, reverses the operation, pushes to redoStack.
     */
    public boolean undo() {
        if (undoStack.isEmpty()) return false;

        Action action = undoStack.pop();

        // Move cursor to where the action happened
        buffer.setCursorToIndex(action.cursorIndex);

        // Reverse the action
        if (action.type == Action.Type.INSERT) {
            // An INSERT was undone → delete that character
            buffer.deleteAtCursor();
        } else {
            // A DELETE was undone → re-insert that character
            // Move back one so we insert at the right spot
            buffer.moveCursorLeft();
            buffer.insertAtCursor(action.character);
        }

        redoStack.push(action);
        updateLineIndex();
        return true;
    }

    /**
     * CTRL+Y — Redo the last undone action.
     * Pops from redoStack, replays the operation, pushes to undoStack.
     */
    public boolean redo() {
        if (redoStack.isEmpty()) return false;

        Action action = redoStack.pop();
        buffer.setCursorToIndex(action.cursorIndex);

        if (action.type == Action.Type.INSERT) {
            // Redo an insert
            buffer.moveCursorLeft();
            buffer.insertAtCursor(action.character);
        } else {
            // Redo a delete
            buffer.deleteAtCursor();
        }

        undoStack.push(action);
        updateLineIndex();
        return true;
    }

    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }

    // ─────────────────────────────────────────────────────────────
    //  CURSOR MOVEMENT
    // ─────────────────────────────────────────────────────────────

    public void moveCursorLeft()  { buffer.moveCursorLeft(); }
    public void moveCursorRight() { buffer.moveCursorRight(); }

    public void moveCursorUp() {
        int line = getCurrentLine();
        if (line == 0) return;
        int col = buffer.getCursorIndex() - lineIndex.get(line);
        int prevLineStart = lineIndex.get(line - 1);
        int prevLineEnd   = (line >= 1 ? lineIndex.get(line) - 1 : buffer.getSize());
        int targetIndex   = Math.min(prevLineStart + col, prevLineEnd);
        buffer.setCursorToIndex(targetIndex);
    }

    public void moveCursorDown() {
        int line = getCurrentLine();
        if (line >= lineIndex.size() - 1) return;
        int col = buffer.getCursorIndex() - lineIndex.get(line);
        int nextLineStart = lineIndex.get(line + 1);
        int nextLineEnd   = (line + 2 < lineIndex.size())
                            ? lineIndex.get(line + 2) - 1
                            : buffer.getSize();
        int targetIndex = Math.min(nextLineStart + col, nextLineEnd);
        buffer.setCursorToIndex(targetIndex);
    }

    public void moveCursorToStart() { buffer.moveCursorToStart(); }
    public void moveCursorToEnd()   { buffer.moveCursorToEnd(); }

    // ─────────────────────────────────────────────────────────────
    //  LINE INDEX (Array)
    // ─────────────────────────────────────────────────────────────

    /**
     * Rebuild the lineIndex array by scanning the full text.
     * lineIndex.get(n) = char offset of the first character on line n.
     *
     * Called after any insert/delete that involves '\n'.
     */
    public void updateLineIndex() {
        lineIndex.clear();
        lineIndex.add(0); // Line 0 starts at offset 0
        String text = buffer.getText();
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                lineIndex.add(i + 1);
            }
        }
    }

    public int getLineCount() {
        return lineIndex.size();
    }

    /** Returns the 0-based line number the cursor is currently on. */
    public int getCurrentLine() {
        int pos = buffer.getCursorIndex();
        for (int i = lineIndex.size() - 1; i >= 0; i--) {
            if (pos >= lineIndex.get(i)) return i;
        }
        return 0;
    }

    /** Returns the 0-based column of the cursor within its line. */
    public int getCurrentColumn() {
        int line = getCurrentLine();
        return buffer.getCursorIndex() - lineIndex.get(line);
    }

    // ─────────────────────────────────────────────────────────────
    //  SEARCH & REPLACE (Hash Table + string scan)
    // ─────────────────────────────────────────────────────────────

    /**
     * Find all start-indices of 'searchTerm' in the document (case-insensitive).
     * Returns an empty list if not found.
     */
    public List<Integer> findAll(String searchTerm) {
        List<Integer> positions = new ArrayList<>();
        if (searchTerm == null || searchTerm.isEmpty()) return positions;

        String text    = buffer.getText();
        String pattern = Pattern.quote(searchTerm);
        Matcher m      = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text);
        while (m.find()) {
            positions.add(m.start());
        }
        return positions;
    }

    /**
     * Replace every occurrence of 'target' with 'replacement' (case-insensitive).
     * Rebuilds the buffer from the modified text.
     */
    public int replaceAll(String target, String replacement) {
        if (target == null || target.isEmpty()) return 0;
        String text     = buffer.getText();
        String newText  = text.replaceAll("(?i)" + Pattern.quote(target),
                                          Matcher.quoteReplacement(replacement));
        int count = (text.length() - newText.length()) / (target.length() - replacement.length() == 0
                     ? 1 : Math.abs(target.length() - replacement.length()));
        // Simpler count:
        count = (int) ((text.length() - text.replaceAll("(?i)" + Pattern.quote(target), "").length())
                       / (double) target.length());

        buffer.setTextAndMoveCursorToEnd(newText);
        undoStack.clear();  // Replace-all is not undoable (simplification)
        redoStack.clear();
        updateLineIndex();

        // Update word frequency for the replacement
        registerWord(replacement);
        return count;
    }

    // ─────────────────────────────────────────────────────────────
    //  TRIE — AUTOCOMPLETE
    // ─────────────────────────────────────────────────────────────

    /**
     * Returns up to 'limit' word suggestions for the current partial word
     * being typed (the word immediately before the cursor).
     */
    public List<String> getAutoSuggestions(int limit) {
        String partial = getCurrentPartialWord();
        if (partial.isEmpty()) return Collections.emptyList();
        return trie.getSuggestions(partial, limit);
    }

    /**
     * Returns the partial word currently being typed (characters after
     * the last word boundary before the cursor).
     */
    public String getCurrentPartialWord() {
        String text = buffer.getText();
        int pos     = buffer.getCursorIndex();
        int start   = pos - 1;
        while (start >= 0 && isWordChar(text.charAt(start))) {
            start--;
        }
        return text.substring(start + 1, pos);
    }

    /**
     * Complete the current partial word with the given suggestion.
     * Replaces the typed prefix with the full word.
     */
    public void applyAutoComplete(String fullWord) {
        String partial = getCurrentPartialWord();
        // Delete the partial word
        for (int i = 0; i < partial.length(); i++) {
            buffer.deleteAtCursor();
        }
        // Insert the full word
        for (char c : fullWord.toCharArray()) {
            buffer.insertAtCursor(c);
        }
        undoStack.clear(); // Autocomplete replaces the partial sequence
    }

    // ─────────────────────────────────────────────────────────────
    //  HASH TABLE — WORD FREQUENCY
    // ─────────────────────────────────────────────────────────────

    /**
     * Register a completed word: increment its count in the Hash Table
     * and insert it into the Trie.
     */
    private void registerWord(String word) {
        if (word == null || word.isBlank()) return;
        word = word.toLowerCase().replaceAll("[^a-zA-Z0-9']", "");
        if (word.isEmpty()) return;
        wordFrequency.merge(word, 1, Integer::sum);
        trie.insert(word);
    }

    /** Count total number of words in the document. */
    public int getWordCount() {
        String text = buffer.getText().trim();
        if (text.isEmpty()) return 0;
        return text.split("\\s+").length;
    }

    /** Count total number of characters (excluding newlines). */
    public int getCharCount() {
        return (int) buffer.getText().chars().filter(c -> c != '\n').count();
    }

    /** Get the N most frequently typed words, sorted by frequency. */
    public List<Map.Entry<String, Integer>> getTopWords(int n) {
        return wordFrequency.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(n)
                .collect(java.util.stream.Collectors.toList());
    }

    /** Get frequency of a specific word. */
    public int getWordFrequency(String word) {
        return wordFrequency.getOrDefault(word.toLowerCase(), 0);
    }

    // ─────────────────────────────────────────────────────────────
    //  DOCUMENT ACCESS
    // ─────────────────────────────────────────────────────────────

    /** Full document text as a String. */
    public String getText() { return buffer.getText(); }

    /** Text of a specific line (0-based). */
    public String getLine(int lineNum) {
        if (lineNum < 0 || lineNum >= lineIndex.size()) return "";
        String text = buffer.getText();
        int start   = lineIndex.get(lineNum);
        int end     = (lineNum + 1 < lineIndex.size())
                      ? lineIndex.get(lineNum + 1) - 1
                      : text.length();
        return text.substring(start, Math.min(end, text.length()));
    }

    /** Returns all lines as an array. */
    public String[] getAllLines() {
        return buffer.getText().split("\n", -1);
    }

    public int getCursorIndex()  { return buffer.getCursorIndex(); }
    public int getTotalSize()    { return buffer.getSize(); }
    public List<Integer> getLineIndex() { return Collections.unmodifiableList(lineIndex); }

    // ─────────────────────────────────────────────────────────────
    //  STACK INSPECTION (for display)
    // ─────────────────────────────────────────────────────────────

    public int getUndoStackSize() { return undoStack.size(); }
    public int getRedoStackSize() { return redoStack.size(); }

    // ─────────────────────────────────────────────────────────────
    //  HELPERS
    // ─────────────────────────────────────────────────────────────

    private boolean isWordBoundary(char c) {
        return c == ' ' || c == '\n' || c == '\t'
            || c == ',' || c == '.' || c == '!' || c == '?'
            || c == ';' || c == ':';
    }

    private boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '\'';
    }

    private String extractWordBefore(int endIndex) {
        String text = buffer.getText();
        if (endIndex <= 0 || endIndex > text.length()) return "";
        int start = endIndex - 1;
        while (start >= 0 && isWordChar(text.charAt(start))) {
            start--;
        }
        return text.substring(start + 1, endIndex);
    }

    /**
     * Load a document from a string (e.g., opening a file).
     * Clears all history.
     */
    public void loadDocument(String content) {
        buffer.setTextAndMoveCursorToEnd("");
        undoStack.clear();
        redoStack.clear();
        wordFrequency.clear();
        lineIndex.clear();
        lineIndex.add(0);
        insertString(content);
    }
}
