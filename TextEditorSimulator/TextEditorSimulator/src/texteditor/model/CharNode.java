package texteditor.model;

/**
 * A single node in the Doubly Linked List representing one character.
 * DATA STRUCTURE: Doubly Linked List Node
 */
public class CharNode {
    public char data;
    public CharNode prev;
    public CharNode next;

    public CharNode(char data) {
        this.data = data;
        this.prev = null;
        this.next = null;
    }

    @Override
    public String toString() {
        return "CharNode('" + data + "')";
    }
}
