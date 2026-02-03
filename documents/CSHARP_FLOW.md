# C# Refactoring Detection Flow

Complete architecture and execution flow for RefactoringMiner's C# support.

---

## 8-Stage Execution Flow

### 1. CLI Entry Point

```
org.refactoringminer.RefactoringMiner (main class)
└─> org.refactoringminer.csharp.cli.CSharpRefactoringMinerCLI
    - Parses command-line arguments (-c, -a, -gc, -gp)
    - Routes to appropriate RefactoringMiner implementation
    - Outputs JSON results
```

**Files:**
- [src/main/java/org/refactoringminer/csharp/cli/CSharpRefactoringMinerCLI.java](src/main/java/org/refactoringminer/csharp/cli/CSharpRefactoringMinerCLI.java)

---

### 2. C# Refactoring Miner Selection

```
org.refactoringminer.csharp.CSharpRefactoringMiner
└─> Determines refactoring detection type:
    • Single commit analysis → CSharpGitHistoryRefactoringMiner
    • Branch/history analysis → CSharpGitHistoryRefactoringMiner
    • GitHub integration → CSharpGitHistoryRefactoringMiner
    
    Delegates to Git history processor
```

**Files:**
- [src/main/java/org/refactoringminer/csharp/CSharpRefactoringMiner.java](src/main/java/org/refactoringminer/csharp/CSharpRefactoringMiner.java)

---

### 3. Git History Processing (C# Specific)

```
org.refactoringminer.csharp.CSharpGitHistoryRefactoringMiner
extends org.refactoringminer.rm2.GitHistoryRefactoringMinerImpl

For each commit in range:
├─ Extract file changes (diff)
├─ Identify C# files (.cs extension)
├─ Get before/after versions of C# files
├─ Pass to CSharpUMLModelASTReader
└─ Collect refactorings from all commits
```

**Files:**
- [src/main/java/org/refactoringminer/csharp/CSharpGitHistoryRefactoringMiner.java](src/main/java/org/refactoringminer/csharp/CSharpGitHistoryRefactoringMiner.java)

---

### 4. UML Model AST Reader (C# Specialized)

```
org.refactoringminer.csharp.CSharpUMLModelASTReader
extends org.refactoringminer.rm2.UMLModelASTReader

Method: processFilesWithCPatMiner(beforeMap, afterMap)
├─ Create UMLModel for before version
├─ Create UMLModel for after version
├─ Pass C# files to CPatMinerExecutor for AST transformation
├─ Generate UML class diagrams from transformed AST
├─ Compare UML models
├─ Extract refactorings using GumTree diff analysis
└─ Returns: List<Refactoring>
```

**Files:**
- [src/main/java/org/refactoringminer/csharp/CSharpUMLModelASTReader.java](src/main/java/org/refactoringminer/csharp/CSharpUMLModelASTReader.java)

---

### 5. C# AST Transformation (Bridge to CPatMiner) ### IMPORTANT

```
org.refactoringminer.csharp.CPatMinerExecutor

Static Initialization:
└─ initializeCPatMiner()
   ├─ Load JAR: CPatMinerV2/AtomicASTChangeMining/target/
   │             AtomicASTChangeMining-0.0.1-SNAPSHOT-jar-with-dependencies.jar
   ├─ Create URLClassLoader with CPatMiner JAR
   ├─ Reflection setup: transformation.Transformation class
   └─ Cache Method: transform_csharp_to_java()

Processing Methods:
├─ processCSharpFiles(Map<filePath, content>)
│  └─ For each .cs file:
│     └─ Call transformCSharpToJavaAST()
│
└─ transformCSharpToJavaAST(content, filePath)
   ├─ Invoke via Reflection: transformation.Transformation.
   │                         transform_csharp_to_java(content)
   └─ Returns: CompilationUnit (Java AST equivalent)

Result: Map<filePath, CompilationUnit AST>
```

**Files:**
- [src/main/java/org/refactoringminer/csharp/CPatMinerExecutor.java](src/main/java/org/refactoringminer/csharp/CPatMinerExecutor.java)

**Key Methods:**
- `initializeCPatMiner()` - Loads CPatMiner JAR dynamically
- `processCSharpFiles(Map<String, String>)` - Batch C# file transformation
- `transformCSharpToJavaAST(String, String)` - Single file transformation

---

### 6. External C# Transformation (CPatMiner)

```
CPatMinerV2/AtomicASTChangeMining (Maven project)
└─ transformation.Transformation.transform_csharp_to_java(csharpCode)
   
   Step 1: Parse C# → XML (srcML parser)
   ├─ Input: C# source code string
   └─ Output: srcML XML tree
   
   Step 2: XML → Eclipse JDT AST
   ├─ org.refactoringminer.astparser.SrcMLTreeVisitor
   ├─ Input: srcML DOM nodes
   └─ Output: CompilationUnit (Java AST representation)
   
   Returns: CompilationUnit (or null if parsing fails)
```

**Key Component:** [SrcMLTreeVisitor.java](CPatMinerV2/AtomicASTChangeMining/src/main/astparser/SrcMLTreeVisitor.java)
- Maps srcML C# nodes to Eclipse JDT Java AST nodes
---

### 7. AST Diff & Refactoring Detection

```
Back in CSharpUMLModelASTReader:
├─ Now has CompilationUnit AST for before AND after C# code
├─ Create UML models from CompilationUnit ASTs
├─ Use GumTree for AST diffing
│  └─ com.github.gumtreediff.actions.ActionGenerator
│  └─ Detects node insertions, deletions, moves, updates
│
└─ Map AST changes to refactoring operations
   └─ Extract Method, Rename Variable, Add Parameter, etc.

Returns: List<Refactoring> with details
```

**Dependencies:**
- GumTree v4.0.0-beta6 (AST diff engine)
- Eclipse JDT (Java AST library)

---

### 8. Output Generation

```
CSharpRefactoringMinerCLI:
└─ Serialize results to JSON format
   ├─ Each commit
   ├─ Each refactoring with:
   │  ├─ Type (Extract Method, Rename Variable, etc.)
   │  ├─ Description
   │  ├─ Left/Right side locations
   │  ├─ Code elements involved
   │  └─ Line numbers & column ranges
   │
   └─ Write to output file or stdout
```

---

## File Organization

### Main Implementation Files

```
src/main/java/org/refactoringminer/
├─ RefactoringMiner.java
│  └─ Entry point for the tool
│
└─ csharp/
   ├─ CSharpRefactoringMiner.java
   │  └─ Main selection logic
   │
   ├─ CSharpGitHistoryRefactoringMiner.java
   │  └─ Git history processor
   │
   ├─ CSharpUMLModelASTReader.java
   │  └─ AST & refactoring detection
   │
   ├─ CPatMinerExecutor.java
   │  └─ C# → Java AST transformation bridge (CRITICAL)
   │
   └─ cli/
      └─ CSharpRefactoringMinerCLI.java
         └─ Command-line interface
```

### External C# Transformation

```
CPatMinerV2/AtomicASTChangeMining/ (Maven project)
└─ src/main/
   ├─ transformation/
   │  └─ Transformation.java
   │     └─ Main transform logic
   │
   ├─ astparser/
   │  └─ SrcMLTreeVisitor.java
   │     └─ srcML → JDT converter
   │
   └─ ... (supporting utilities)

Compiled JAR:
└─ CPatMinerV2/AtomicASTChangeMining/target/
   └─ AtomicASTChangeMining-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

### Dependencies

- **org.eclipse.jdt** - Java AST library
- **com.github.gumtreediff** - GumTree diff engine (v4.0.0-beta6)
- **srcML** - C# parser
- **org.eclipse.jgit** - Git operations

---

## Execution Flow Summary

```
1. CLI parses arguments
   ↓
2. CSharpRefactoringMiner routes to CSharpGitHistoryRefactoringMiner
   ↓
3. For each commit, extract C# file before/after versions
   ↓
4. CSharpUMLModelASTReader.processFilesWithCPatMiner()
   ↓
5. CPatMinerExecutor.processCSharpFiles()
   ↓
6. For each C# file:
   ├─ Load CPatMiner JAR (dynamic ClassLoader)
   ├─ Invoke transformation.Transformation.transform_csharp_to_java()
   └─ Receive CompilationUnit AST (Java equivalent of C# code)
   ↓
7. Create UML models from CompilationUnit ASTs
   ↓
8. Use GumTree to diff AST before/after versions
   ↓
9. Identify code changes (insertions, deletions, moves, updates)
   ↓
10. Map AST changes to refactoring types
    ↓
11. Serialize refactorings to JSON output
    ↓
12. Write to file or stdout
```

---

### CLI Invocation
```bash
./build/scripts/RefactoringMiner -c /path/to/repo <commit-sha> -json output.json
```

---
## Testing & Validation

### Tested Repositories

1. **VR Pacman** (Traditional C#)
   - Commit: `5ec895d3cdd80d8ab87b4b899038afd00f99d45d`
   - Result: 64 refactorings detected
   - MISSING tokens: 0

2. **TUX** (Unity C# project)
   - Commit: `db3679cc884cca261116c9d367c305320671e798`
   - Result: 38 refactorings detected
   - MISSING tokens: 0


## Architecture Diagrams

### High-Level Flow

```
┌──────────────────────┐
│  CLI Arguments       │
└──────────────────────┘
          ↓
┌──────────────────────┐
│  CSharpRefactoring   │
│      Miner           │
└──────────────────────┘
          ↓
┌──────────────────────┐
│  CSharpGitHistory    │
│    RefactoringMiner  │
└──────────────────────┘
          ↓
┌──────────────────────┐
│  CSharpUMLModel      │
│    ASTReader         │
└──────────────────────┘
          ↓
┌──────────────────────┐
│  CPatMinerExecutor   │ ⭐
│  (JAR Bridge)        │
└──────────────────────┘
          ↓
┌──────────────────────┐
│  CPatMiner JAR       │
│  (C# → Java AST)     │
└──────────────────────┘
          ↓
┌──────────────────────┐
│  GumTree Diffing     │
│  & Refactoring       │
│  Detection           │
└──────────────────────┘
          ↓
┌──────────────────────┐
│  JSON Output         │
└──────────────────────┘
```

### C# to AST Transformation Pipeline

```
C# Source Code
      ↓
    srcML
   (XML)
      ↓
SrcMLTreeVisitor
   (visitor)
      ↓
CompilationUnit
   (JDT AST)
      ↓
UML Model Generation
      ↓
GumTree Diff Analysis
      ↓
Refactoring Detection
      ↓
JSON Output
```
