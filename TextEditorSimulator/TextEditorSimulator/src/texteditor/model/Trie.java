package texteditor.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * DATA STRUCTURE: Trie (Prefix Tree)
 *
 * Used for word autocomplete and recently-typed-word suggestions.
 *
 * Why Trie?
 *  - Prefix search is O(L) where L = length of prefix
 *  - Much faster than scanning a list of all words
 *  - Naturally organises words by their shared prefixes
 *
 * Operations:
 *  - insert(word)          → O(L)
 *  - getSuggestions(prefix) → O(L + N) where N = number of matching words
 */
public class Trie {

    private final TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    // ─────────────────────────────────────────────
    //  INSERT
    // ─────────────────────────────────────────────

    /**
     * Insert a word into the Trie, incrementing its frequency count.
     */
    public void insert(String word) {
        if (word == null || word.isBlank()) return;
        word = word.toLowerCase().trim();

        TrieNode current = root;
        for (char c : word.toCharArray()) {
            current.children.putIfAbsent(c, new TrieNode());
            current = current.children.get(c);
        }
        current.isEndOfWord = true;
        current.frequency++;
    }

    // ─────────────────────────────────────────────
    //  SEARCH / SUGGESTIONS
    // ─────────────────────────────────────────────

    /**
     * Return all words in the Trie that start with the given prefix.
     * Results are sorted by frequency (most-typed first).
     *
     * @param prefix  the current partial word being typed
     * @param limit   maximum number of suggestions to return
     */
    public List<String> getSuggestions(String prefix, int limit) {
        List<String> results = new ArrayList<>();
        if (prefix == null || prefix.isBlank()) return results;

        prefix = prefix.toLowerCase().trim();

        // Navigate to the end of the prefix in the Trie
        TrieNode current = root;
        for (char c : prefix.toCharArray()) {
            if (!current.children.containsKey(c)) {
                return results; // Prefix not found → no suggestions
            }
            current = current.children.get(c);
        }

        // DFS from this node to collect all complete words
        collectWords(current, new StringBuilder(prefix), results);

        // Sort by frequency descending
        results.sort(Comparator.comparingInt(w -> -getFrequency(w)));

        return results.subList(0, Math.min(limit, results.size()));
    }

    /** DFS helper: walks all paths and adds complete words to results. */
    private void collectWords(TrieNode node, StringBuilder current, List<String> results) {
        if (node.isEndOfWord) {
            results.add(current.toString());
        }
        for (char c : node.children.keySet()) {
            current.append(c);
            collectWords(node.children.get(c), current, results);
            current.deleteCharAt(current.length() - 1);
        }
    }

    /** Returns the frequency count of a word (0 if not in Trie). */
    public int getFrequency(String word) {
        if (word == null) return 0;
        word = word.toLowerCase().trim();
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            if (!current.children.containsKey(c)) return 0;
            current = current.children.get(c);
        }
        return current.isEndOfWord ? current.frequency : 0;
    }

    /** Check if a complete word exists in the Trie. */
    public boolean contains(String word) {
        return getFrequency(word) > 0;
    }

    /** Check if any word in the Trie starts with the given prefix. */
    public boolean startsWith(String prefix) {
        if (prefix == null) return false;
        prefix = prefix.toLowerCase().trim();
        TrieNode current = root;
        for (char c : prefix.toCharArray()) {
            if (!current.children.containsKey(c)) return false;
            current = current.children.get(c);
        }
        return true;
    }
}
