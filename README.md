# Text Editor Simulator — Mini Notepad
## Java Data Structures Project

---

## How to Import into Eclipse

1. Open Eclipse
2. Go to **File → Import → General → Existing Projects into Workspace**
3. Click **Browse** and select this folder (`TextEditorSimulator`)
4. Click **Finish**
5. In the Package Explorer, open `src/texteditor/Main.java`
6. Right-click → **Run As → Java Application**

---

## Data Structures Used

| Structure          | Class                  | Purpose                          |
|--------------------|------------------------|----------------------------------|
| Doubly Linked List | `CharBuffer.java`      | Store every character in the doc |
| Stack (x2)         | `TextEditorEngine`     | Undo (Ctrl+Z) and Redo (Ctrl+Y)  |
| Trie               | `Trie.java`            | Word autocomplete suggestions    |
| Hash Table (Map)   | `TextEditorEngine`     | Word frequency counter           |
| Array (List)       | `TextEditorEngine`     | Line number → char offset index  |

---

## Project Structure

```
src/
 └── texteditor/
      ├── Main.java                  ← Entry point (run this)
      ├── model/
      │    ├── CharNode.java         ← DLL node (one character)
      │    ├── CharBuffer.java       ← Doubly Linked List
      │    ├── Action.java           ← Undo/redo record
      │    ├── TrieNode.java         ← Trie node
      │    └── Trie.java             ← Prefix tree for autocomplete
      ├── engine/
      │    └── TextEditorEngine.java ← Core logic tying all DS together
      └── ui/
           └── EditorUI.java         ← Swing GUI
```

---

## Features

- **Insert / Delete** characters using the Doubly Linked List
- **Ctrl+Z** Undo, **Ctrl+Y** Redo via two Stacks
- **Line numbers** on the left via Array-indexed line offsets
- **Find** (highlights all occurrences in yellow)
- **Replace All** — replaces every occurrence in the document
- **Trie Autocomplete** — buttons appear as you type, click to complete
- **Word Statistics** — word count, top words from the Hash Table
- **Status bar** — live line/col, word count, undo/redo depth

---

## Requirements

- Java 11 or higher
- Eclipse IDE (any recent version)
- No external libraries needed — pure Java + Swing
