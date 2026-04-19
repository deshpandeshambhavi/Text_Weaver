# ✨ Text Weaver

**Text Weaver** is a high-performance text editor built using fundamental data structures like Doubly Linked Lists, Stacks, and Tries.  
It demonstrates how core computer science concepts can power efficient, real-time text editing systems.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Installation & Setup](#installation--setup)
- [Project Structure](#project-structure)
- [Usage](#usage)
- [Core Data Structures](#core-data-structures)
- [Key Highlights](#key-highlights)
- [Future Improvements](#future-improvements)
- [Author](#author)

---

## 🎯 Overview

Text Weaver is a custom-built text editor designed to simulate real-world editing systems using optimized data structures.  
Instead of relying on traditional string manipulation, it uses a **Doubly Linked List for text storage**, ensuring efficient insertions and deletions.

The project integrates multiple data structures to deliver features like undo/redo, autocomplete, and fast text processing.

**Tagline:** *“Where structure meets speed.”*

---

## ✨ Features

- ✏️ Efficient text editing using Doubly Linked List
- ↩️ Undo / Redo functionality using Stack
- ⚡ Real-time Autocomplete using Trie
- 🔍 Search and Replace functionality
- 📍 Line number tracking
- 🎯 Cursor position tracking (line & column)
- 🎨 Clean dark-themed UI using Java Swing
- 📊 Word and character count statistics

---

## 🛠 Tech Stack

### Core
- **Java** – Core programming language
- **Java Swing** – UI framework

### Data Structures
- **Doubly Linked List** – Text storage
- **Stack** – Undo / Redo operations
- **Trie** – Autocomplete suggestions
- **HashMap** – Word frequency tracking
- **ArrayList** – Line indexing

---

## 🚀 Installation & Setup

### Prerequisites
- Java JDK (8 or above)
- Eclipse / IntelliJ IDEA

1. **Clone the repository**
```bash
git clone https://github.com/your-username/text-weaver.git

2. **Open in IDE**
Import as a Java Project in Eclipse / IntelliJ
3. **Run the application**
Locate Main.java
Right-click → **Run as Java Application**

📁 Project Structure
text-weaver/
│
├── src/
│   └── com/minieditor/
│       ├── core/        # Core logic (EditorCore, data structures)
│       ├── ui/          # UI components (EditorPanel, Popup)
│       └── Main.java    # Entry point
│
├── README.md
└── .gitignore

💡 Usage
Basic Editing
Type text normally
Use Backspace/Delete for removal
Press Enter for new line
Shortcuts
Ctrl + Z → Undo
Ctrl + Y → Redo
Ctrl + F / H → Search & Replace
Autocomplete
Start typing a word → suggestions appear automatically
Use ↑ / ↓ to navigate
Press Enter / Tab to accept

🧠 Core Data Structures
| Feature        | Data Structure Used |
| -------------- | ------------------- |
| Text Storage   | Doubly Linked List  |
| Undo / Redo    | Stack               |
| Autocomplete   | Trie                |
| Word Frequency | HashMap             |
| Line Indexing  | ArrayList           |


🚀 Key Highlights
Efficient editing operations using DLL instead of strings
Separation of UI and core logic
Real-time autocomplete system
Optimized undo/redo using stacks
Demonstrates practical use of multiple data structures

🔮 Future Improvements
Syntax highlighting
File save/open support
Multi-tab editing
Plugin system
Performance optimization for large files
