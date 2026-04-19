package texteditor.model;

/**
 * DATA STRUCTURE: Doubly Linked List
 *
 * Stores every character in the document as a linked chain of CharNodes.
 * The 'cursor' pointer marks the node BEFORE the insertion point (like a gap buffer).
 *
 * Why DLL?
 *  - O(1) insert and delete at the cursor position
 *  - Moving the cursor left/right is just pointer traversal
 *  - No shifting needed unlike arrays
 */
public class CharBuffer {

    private CharNode head;      // First character node
    private CharNode tail;      // Last character node
    private CharNode cursor;    // Node just before the insertion point (null = before head)
    private int size;           // Total number of characters
    private int cursorIndex;    // Numeric position of cursor (0-based)

    public CharBuffer() {
        head = null;
        tail = null;
        cursor = null;
        size = 0;
        cursorIndex = 0;
    }

    // ─────────────────────────────────────────────
    //  INSERT
    // ─────────────────────────────────────────────

    /**
     * Insert a character at the current cursor position.
     * The new node is placed AFTER 'cursor'.
     */
    public void insertAtCursor(char c) {
        CharNode newNode = new CharNode(c);

        if (cursor == null) {
            // Insert at the very beginning
            newNode.next = head;
            if (head != null) head.prev = newNode;
            head = newNode;
            if (tail == null) tail = newNode;
        } else {
            // Insert after cursor
            newNode.prev = cursor;
            newNode.next = cursor.next;
            if (cursor.next != null) {
                cursor.next.prev = newNode;
            } else {
                tail = newNode;
            }
            cursor.next = newNode;
        }

        cursor = newNode;
        size++;
        cursorIndex++;
    }

    // ─────────────────────────────────────────────
    //  DELETE (Backspace)
    // ─────────────────────────────────────────────

    /**
     * Delete the character at the cursor (backspace behaviour).
     * Returns the deleted character, or '\0' if nothing to delete.
     */
    public char deleteAtCursor() {
        if (cursor == null) return '\0'; // Nothing before cursor

        CharNode toDelete = cursor;

        // Reconnect neighbours
        if (toDelete.prev != null) {
            toDelete.prev.next = toDelete.next;
        } else {
            head = toDelete.next;
        }
        if (toDelete.next != null) {
            toDelete.next.prev = toDelete.prev;
        } else {
            tail = toDelete.prev;
        }

        cursor = toDelete.prev;
        size--;
        cursorIndex--;
        return toDelete.data;
    }

    // ─────────────────────────────────────────────
    //  CURSOR MOVEMENT
    // ─────────────────────────────────────────────

    /** Move cursor one step left (toward beginning). */
    public boolean moveCursorLeft() {
        if (cursor == null) return false; // Already at start
        cursor = cursor.prev;
        cursorIndex--;
        return true;
    }

    /** Move cursor one step right (toward end). */
    public boolean moveCursorRight() {
        CharNode nextNode = (cursor == null) ? head : cursor.next;
        if (nextNode == null) return false; // Already at end
        cursor = nextNode;
        cursorIndex++;
        return true;
    }

    /** Jump cursor to absolute index position. */
    public void setCursorToIndex(int index) {
        if (index < 0) index = 0;
        if (index > size) index = size;

        // Walk from head
        cursor = null;
        cursorIndex = 0;
        CharNode current = head;
        for (int i = 0; i < index; i++) {
            cursor = current;
            current = (current != null) ? current.next : null;
            cursorIndex++;
        }
        cursorIndex = index;
    }

    /** Move cursor to very beginning. */
    public void moveCursorToStart() {
        cursor = null;
        cursorIndex = 0;
    }

    /** Move cursor to very end. */
    public void moveCursorToEnd() {
        cursor = tail;
        cursorIndex = size;
    }

    // ─────────────────────────────────────────────
    //  ACCESSORS
    // ─────────────────────────────────────────────

    public int getCursorIndex() { return cursorIndex; }
    public int getSize()        { return size; }
    public boolean isEmpty()    { return size == 0; }

    /**
     * Build the full text from the DLL by traversing from head to tail.
     */
    public String getText() {
        StringBuilder sb = new StringBuilder(size);
        CharNode current = head;
        while (current != null) {
            sb.append(current.data);
            current = current.next;
        }
        return sb.toString();
    }

    /**
     * Replace all content with a new string (used after replaceAll).
     */
    public void setTextAndMoveCursorToEnd(String text) {
        head = null;
        tail = null;
        cursor = null;
        size = 0;
        cursorIndex = 0;
        for (char c : text.toCharArray()) {
            insertAtCursor(c);
        }
    }

    /** Peek at the character immediately before cursor (the one that would be deleted). */
    public char peekBeforeCursor() {
        return (cursor != null) ? cursor.data : '\0';
    }
}
