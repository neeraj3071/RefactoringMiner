# C# RefactoringMiner Support

![C# Support](https://img.shields.io/badge/language-C%23-239120?style=flat&logo=csharp)
![RefactoringMiner](https://img.shields.io/badge/RefactoringMiner-3.0.11-blue)

## Overview

RefactoringMiner C# Support extends the original [RefactoringMiner](https://github.com/tsantalis/RefactoringMiner) tool to detect refactorings in C# projects. This implementation uses **CpatMinerV2** for parsing C# code and converting it to Java AST representations that RefactoringMiner can analyze.

### Key Highlights

- **Direct CpatminerV2 Integration** - Uses CpatminerV2 for reliable C# parsing
- **Compatible with RefactoringMiner API** - Uses the same command-line interface
- **Batch Processing Support** - Analyze multiple commits efficiently

---

## Architecture

### 8-Stage Refactoring Detection Pipeline

The C# RefactoringMiner implements a complete refactoring detection pipeline with distinct stages:

```
┌─────────────────────────────────────────────────────────────────┐
│ Stage 1: Git Repository Cloning/Checkout                        │
│ Entry Point: CSharpRefactoringMiner CLI                         │
│ Output: Clean git repository at specified commit                │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Stage 2: C# File Detection & Reading                            │
│ Component: CSharpFileProcessor                                  │
│ Process: Find all .cs files in repo or commit delta             │
│ Output: Set of C# source file contents (String)                 │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Stage 3: srcML Parsing (C# → XML AST)                           │
│ Tool: srcML CLI (external process)                              │
│ Process: Convert C# code to structured XML representation       │
│ Output: XML AST tree (DOM Document)                             │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Stage 4: GumTree XML Parsing & Tree Construction                │
│ Library: GumTree 4.0.0-beta6                                    │
│ Process: Parse XML into GumTree node types                      │
│ Output: Typed tree with INode, TreeNode implementations         │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Stage 5: SrcML AST → Java AST Transformation                    │
│ Component: SrcMLTreeVisitor (2028 lines, 75 visitor methods)    │
│ Process: Pattern matching to convert C# AST to Java AST nodes   │
│ Output: Eclipse JDT CompilationUnit                             │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Stage 6: Tree Diffing (Before vs After Commit)                  │
│ Library: GumTree tree diff algorithm                            │
│ Process: Compare parent-commit AST with child-commit AST        │
│ Output: Edit script (Insert, Delete, Move, Update nodes)        │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Stage 7: Refactoring Pattern Matching                           │
│ Component: RefactoringMiner Core Engine                         │
│ Process: Apply 60+ predefined refactoring detection rules       │
│ Output: List of detected Refactoring objects                    │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Stage 8: JSON Serialization & Output                            │
│ Format: RefactoringMiner standard JSON schema                   │
│ Output: refactorings.json with detected changes                 │
└─────────────────────────────────────────────────────────────────┘
```

### 5-Stage AST Transformation (Stage 5 Details)

Inside Stage 5, the SrcMLTreeVisitor performs detailed structural transformation:

```
Stage 5.1: srcML XML Node Processing
├─ Input: GumTree INode (from srcML XML)
├─ Logic: Identify node type (class, method, property, lambda, LINQ, etc.)
└─ Output: Routed to specific visitor method

Stage 5.2: C# Language Construct Mapping
├─ Properties → Getter/Setter pairs (Java patterns)
├─ Events → Observer pattern delegates
├─ Async/Await → Method markers + Task wrapping
├─ LINQ → Stream chains or loop equivalents
├─ Lambda → FunctionExpression (with body handling)
└─ Attributes → Java annotations

Stage 5.3: Method Body Transformation
├─ Input: BlockNode (C# method body)
├─ Process: Recursively visit all statement nodes
├─ Handle: 20+ statement types (if, while, foreach, switch, try, etc.)
└─ Output: Block with transformed statements

Stage 5.4: Expression Transformation
├─ Input: ExprNode (C# expressions)
├─ Process: Convert expressions preserving semantics
├─ Handle: Method calls, field access, operators, literals
└─ Output: Expression nodes in Java AST

Stage 5.5: AST Assembly
├─ Combine all transformed pieces
├─ Link parent-child relationships
├─ Return: Complete Eclipse JDT CompilationUnit
└─ Ready for: Stage 6 (Tree Diffing)
```

### Key Components

1. **`CSharpRefactoringMiner`** - CLI entry point, handles command parsing and git operations (Stage 1-2)
2. **`CSharpGitHistoryRefactoringMiner`** - Git integration, repository cloning and commit navigation
3. **`CSharpFileProcessor`** - C# file detection and reading from git commits (Stage 2)
4. **`SrcMLBasedCSharpProcessor`** - Orchestrates srcML parsing and tree transformation (Stage 3-5)
5. **`SrcMLTreeVisitor`** - Core pattern matching engine with 75 visitor methods for AST transformation (Stage 5)
6. **`CPatMinerExecutor`** - Dynamic bridge for CPatMiner integration (alternative analysis path)
7. **RefactoringMiner Core** - Tree diffing, refactoring detection rules, and JSON serialization (Stage 6-8)

---

## Features

###  Enhanced C# Language Support

The processor implements **14 enhanced C# features** for accurate refactoring detection:

| Feature | Description | 
|---------|-------------|
| **Properties** | Auto-properties, getter/setter conversion | 
| **Events** | Event declarations, handlers, delegates | 
| **Attributes** | C# attributes to Java annotations | 
| **Async/Await** | Async method detection and marking | 
| **Extension Methods** | Static extension method patterns | 
| **LINQ Queries** | Query expressions and method chains | 
| **String Interpolation** | `$"{var}"` to concatenation | 
| **Nullable Types** | `int?`, `string?` handling | 
| **Pattern Matching** | `is`, `switch` patterns | 
| **Lambda Expressions** | Arrow functions, delegates | 
| **Partial Classes** | Multi-file class declarations | 
| **Using Directives** | Namespace imports mapping  |
| **Var Keyword** | Type inference preservation | 
| **Namespaces** | Nested namespaces to packages | 

## Installation

### Prerequisites

1. **Java 17+** (Required for RefactoringMiner)
2. **srcML** (Required for C# parsing)
3. **Gradle 7.4+** (For building)
4. **Git** (For repository analysis)

### Installing srcML

#### macOS
```bash
brew install srcml
```

#### Ubuntu/Debian
```bash
sudo apt-get update
sudo apt-get install srcml
```

#### Windows
Download from [srcML official website](https://www.srcml.org/)

### Building RefactoringMiner with C# Support

```bash
# Clone the repository
git clone https://github.com/tsantalis/RefactoringMiner.git
cd RefactoringMiner

# Build the project
./gradlew build

# Create fat JAR with all dependencies
./gradlew shadowJar
```

---

## Quick Reference

### One-Liners for Common Tasks

```bash
# 1. Quick analysis of a single commit
java -cp build/libs/RM-fat.jar org.refactoringminer.csharp.CSharpRefactoringMiner -c /repo abc1234 -json out.json

# 2. Check if srcML and Java are installed
srcml --version && java -version

# 3. Build and create fat JAR in one step
./gradlew clean shadowJar && ls -lh build/libs/RM-fat.jar

# 4. Run analysis on specific branch
java -cp build/libs/RM-fat.jar org.refactoringminer.csharp.CSharpRefactoringMiner -a /repo main -json out.json

# 5. Analyze commits between two tags
java -cp build/libs/RM-fat.jar org.refactoringminer.csharp.CSharpRefactoringMiner -bt /repo v1.0 v2.0 4 -json out.json

# 6. Count refactorings in JSON output
cat out.json | jq '[.commits[].refactorings | length] | add'

# 7. Show refactoring types summary
cat out.json | jq -r '.commits[].refactorings[].type' | sort | uniq -c | sort -rn

# 8. Extract first 10 lines of JSON for preview
cat out.json | python3 -m json.tool | head -20
```

### Quick Start Guide

#### Step 1: Verify Prerequisites

Before starting, ensure all tools are installed:

```bash
# Check Java version (must be 17+)
java -version

# Check srcML installation
srcml --version

# Check Gradle installation
gradle --version

# Verify Git is available
git --version
```

#### Step 2: Build RefactoringMiner

```bash
# Navigate to RefactoringMiner directory
cd /path/to/RefactoringMiner

# Build the project with Gradle
./gradlew build

# Create the fat JAR (includes all dependencies)
./gradlew shadowJar

# Verify the JAR was created
ls -lh build/libs/RM-fat.jar
```

The fat JAR should be approximately 64-65 MB.

#### Step 3: Prepare Your C# Repository

```bash
# Clone the C# repository you want to analyze
git clone https://github.com/username/csharp-project.git
cd csharp-project

# Ensure the repository has a clean working directory
git status

# Optionally, checkout a specific branch
git checkout main
```

---

### Command Line Interface

The C# RefactoringMiner uses the **same command-line interface** as the original RefactoringMiner.

#### Basic Command Structure

```bash
java -cp build/libs/RM-fat.jar \
  org.refactoringminer.csharp.CSharpRefactoringMiner \
  [OPTIONS] [ARGUMENTS]
```

#### Available Options

| Option | Arguments | Description |
|--------|-----------|-------------|
| `-c` | `<repo-path> <commit-sha>` | Analyze a single commit |
| `-a` | `<repo-path> <branch>` | Analyze all commits on a branch |
| `-bt` | `<repo-path> <start-tag> <end-tag> <thread-count>` | Analyze commits between two tags |
| `-bc` | `<repo-path> <start-commit> <end-commit> <thread-count>` | Analyze commits between two commit SHAs |
| `-gc` | `<git-url> <commit-sha> <thread-count>` | Analyze GitHub commit directly |
| `-gp` | `<git-url> <pull-request-id> <thread-count>` | Analyze GitHub pull request |
| `-json` | `<output-file>` | Export results to JSON file |
| `-h` | - | Display help message |

---

### Usage Examples

#### Example : Analyze a Single Commit

Analyze refactorings in a specific commit:

```bash
java -cp build/libs/RM-fat.jar \
  org.refactoringminer.csharp.CSharpRefactoringMiner \
  -c /path/to/csharp-repo 35cb3631 -json refactorings.json
```

**Real-world example:**
```bash
# Analyzing a Unity VR project commit
java -cp build/libs/RM-fat.jar \
  org.refactoringminer.csharp.CSharpRefactoringMiner \
  -c ~/projects/Zinnia.Unity 35cb3631d6 -json zinnia_refactorings.json
```

**Output:** Creates `refactorings.json` with all detected refactorings in the commit.

### Understanding the Output

#### JSON Output Structure

The output JSON follows RefactoringMiner's standard format:

```json
{
  "commits": [
    {
      "repository": "https://github.com/user/repo.git",
      "sha1": "35cb3631d6a72633f85bd2c02bc7b8c0c0d82f26",
      "url": "https://github.com/user/repo/commit/35cb3631",
      "refactorings": [
        {
          "type": "Extract Method",
          "description": "Extract Method ProcessInput() from Start() in class GameController",
          "leftSideLocations": [...],
          "rightSideLocations": [...]
        }
      ]
    }
  ]
}
```


## Supported C# Features

### 1. **Properties**

C# properties are converted to Java getter/setter patterns:

**C# Code:**
```csharp
public class Player {
    public string Name { get; set; }
    public int Score { get; private set; }
}
```

**Detected Patterns:**
- Property addition/removal
- Property rename
- Access modifier changes
- Auto-property to full property refactoring

### 2. **Events**

C# events are mapped to Java observer patterns:

**C# Code:**
```csharp
public event EventHandler<GameEvent> OnGameStart;
```

**Detected Patterns:**
- Event declaration changes
- Event handler modifications
- Delegate pattern refactorings

### 3. **Async/Await**

Async methods are preserved with markers:

**C# Code:**
```csharp
public async Task<string> LoadDataAsync() {
    await Task.Delay(1000);
    return "Data loaded";
}
```

**Detected Patterns:**
- Async method extraction
- Async to sync conversions
- Await usage changes

### 4. **LINQ Queries**

LINQ expressions are converted to equivalent patterns:

**C# Code:**
```csharp
var results = players.Where(p => p.Score > 100)
                    .OrderBy(p => p.Name)
                    .Select(p => p.Name);
```

**Detected Patterns:**
- LINQ query extraction
- LINQ to loop conversions
- Method chain refactorings

### 5. **Attributes → Annotations**

C# attributes map to Java annotations:

**C# Code:**
```csharp
[SerializeField]
[Tooltip("Player health value")]
private int health = 100;
```

**Detected Patterns:**
- Attribute addition/removal
- Attribute parameter changes

### 6. **Extension Methods**

Extension methods are recognized and marked:

**C# Code:**
```csharp
public static class StringExtensions {
    public static bool IsNullOrEmpty(this string str) {
        return string.IsNullOrEmpty(str);
    }
}
```

**Detected Patterns:**
- Extension method extraction
- Extension to instance method conversion

### 7. **Nullable Types**

Nullable value types are handled:

**C# Code:**
```csharp
int? score = null;
string? name = GetName();
```

**Detected Patterns:**
- Nullable to non-nullable conversions
- Null-checking refactorings

### 8. **Pattern Matching**

Pattern matching expressions are converted:

**C# Code:**
```csharp
if (obj is Player player && player.Score > 100) {
    // Do something
}
```

**Detected Patterns:**
- Pattern matching introduction
- Type casting refactorings

---

## Supported Refactoring Types

RefactoringMiner C# detects **60+ refactoring types**. The most commonly detected in C# projects:

### Structural Refactorings

| Refactoring Type | Description | Detection Rate |
|------------------|-------------|----------------|
| **Extract Method** | Extract code into new method | High |
| **Extract Class** | Extract code into new class | High |
| **Move Method** | Move method between classes | High |
| **Move Attribute** | Move field between classes | High |
| **Move Class** | Move class to different namespace | High |
| **Inline Method** | Inline method body | High |
| **Inline Class** | Merge class into another | High |

### Access Modifier Refactorings

| Refactoring Type | Description | Detection Rate |
|------------------|-------------|----------------|
| **Change Attribute Access Modifier** | public ↔ private ↔ protected | Very High |
| **Change Method Access Modifier** | Visibility changes | Very High |
| **Add/Remove Method Modifier** | static, sealed, virtual | High |

### Naming Refactorings

| Refactoring Type | Description | Detection Rate |
|------------------|-------------|----------------|
| **Rename Method** | Method name change | High |
| **Rename Attribute** | Field name change | High |
| **Rename Class** | Class name change | High |
| **Rename Variable** | Local variable rename | High |
| **Rename Parameter** | Parameter rename | High |

### Type Refactorings

| Refactoring Type | Description | Detection Rate |
|------------------|-------------|----------------|
| **Change Attribute Type** | Field type change | High |
| **Change Return Type** | Method return type change | High |
| **Change Parameter Type** | Parameter type change | High |
| **Change Variable Type** | Local variable type | High |

### Other Refactorings

- Extract Variable
- Inline Variable
- Extract Interface
- Pull Up Method/Attribute
- Push Down Method/Attribute
- Add/Remove Parameter
- Reorder Parameters
- Extract Superclass
- And 40+ more...

**Full list:** See [RefactoringMiner Supported Types](https://github.com/tsantalis/RefactoringMiner#supported-refactorings)

---

## Contributing

### Development Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/tsantalis/RefactoringMiner.git
   cd RefactoringMiner
   ```

2. **Import into IDE:**
   - IntelliJ IDEA: File → Open → Select `build.gradle`
   - Eclipse: Import → Gradle Project

3. **Build and test:**
   ```bash
   ./gradlew clean build
   ./gradlew test
   ```

### Project Structure

```
src/main/java/org/refactoringminer/csharp/
├── CSharpRefactoringMiner.java          # Main CLI entry point (Stage 1)
├── CSharpGitHistoryRefactoringMiner.java # Git integration (Stage 1-2)
├── CSharpFileProcessor.java              # File detection (Stage 2)
├── SrcMLBasedCSharpProcessor.java        # srcML & GumTree orchestration (Stage 3-4)
├── SrcMLTreeVisitor.java                 # AST transformation - 75 visitor methods (Stage 5) 
├── CSharpUMLModelASTReader.java          # AST reader utility
├── CPatMinerExecutor.java                # CPatMiner dynamic bridge
└── cli/                                  # CLI utilities
    └── CSharpRefactoringMinerCLI.java    # Command-line interface
```

---

## License

This project is licensed under the **MIT License** - see [LICENSE](LICENSE) file.

---

## Acknowledgments

### Original RefactoringMiner
- **Author:** Nikolaos Tsantalis
- **Repository:** [tsantalis/RefactoringMiner](https://github.com/tsantalis/RefactoringMiner)

### srcML
- **Project:** [srcML](https://www.srcml.org/)
- **Purpose:** Multi-language source code analysis
- **Used in:** C# parsing (Stage 3)

### GumTree
- **Version:** 4.0.0-beta6
- **Purpose:** Tree diffing algorithm
- **Used in:** Tree comparison (Stage 6)

### Eclipse JDT
- **Project:** Eclipse Java Development Tools
- **Purpose:** Java AST representation
- **Used in:** Target AST format (Stage 5 output)

### CPatMiner
- **Original:** [nguyenhoan/CPatMiner](https://github.com/nguyenhoan/CPatMiner)
- **C# Extension:** CPatMinerV2 (included)
- **Purpose:** Semantic pattern mining (alternative to structural analysis)

---