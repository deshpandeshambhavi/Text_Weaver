package texteditor.model;

/**
 * DATA STRUCTURE: Stack element
 *
 * Represents a single reversible operation pushed onto the undo/redo stacks.
 * Each Action stores enough information to undo OR redo the operation.
 */
public class Action {

    public enum Type {
        INSERT,     // A character was inserted
        DELETE      // A character was deleted
    }

    public final Type type;
    public final char character;   // The character involved
    public final int cursorIndex;  // Cursor position AFTER the action

    public Action(Type type, char character, int cursorIndex) {
        this.type        = type;
        this.character   = character;
        this.cursorIndex = cursorIndex;
    }

    @Override
    public String toString() {
        return "Action(" + type + ", '" + character + "', pos=" + cursorIndex + ")";
    }
}
