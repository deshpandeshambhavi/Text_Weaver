package texteditor.model;

import java.util.HashMap;
import java.util.Map;

/**
 * DATA STRUCTURE: Trie Node
 *
 * Each node represents one character in a path from root to a complete word.
 * Children are stored in a HashMap for O(1) child lookup.
 */
public class TrieNode {
    public Map<Character, TrieNode> children;
    public boolean isEndOfWord;
    public int frequency;   // How many times this word has been typed

    public TrieNode() {
        children    = new HashMap<>();
        isEndOfWord = false;
        frequency   = 0;
    }
}
