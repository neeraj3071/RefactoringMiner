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
│ Stage 2: C# File Detection                                      │
│ Component: CSharpGitServiceImpl                                 │
│ Process: Extend GitServiceImpl to detect .cs files              │
│ Output: Set of file paths (javaFilesBefore, javaFilesCurrent)   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Stage 3: File Content Reading                                   │
│ Component: GitServiceImpl.populateFileContents() (JGit)         │
│ Process: Read file contents from git using TreeWalk             │
│ Output: Map<String, String> fileContents                        │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Stage 4: UML Model Creation Router                              │
│ Component: CSharpGitHistoryRefactoringMiner.createModel()       │
│ Logic: Count .cs files → route to C# or Java reader             │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Stage 5: C# AST Reader                                          │
│ Component: CSharpUMLModelASTReader                              │
│ Process: Orchestrate CPatMiner integration                      │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Stage 6: CPatMiner JAR Loading                                  │
│ Component: CPatMinerExecutor                                    │
│ Process: Load CPatMiner JAR via URLClassLoader, get transform   │
│         method via reflection                                   │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Stage 7: CPatMiner C# → Java AST (INSIDE CPATMINER JAR)        │
│ Component: Transformation.transform_csharp_to_java()            │
│ Substages:                                                      │
│   7a: srcML Parsing (C# → XML AST) - external srcML tool        │
│   7b: GumTree XML Parsing - parse XML into GumTree nodes        │
│   7c: SrcMLTreeVisitor - 75+ visitor methods transform tree     │
│ Output: CompilationUnit (Eclipse JDT Java AST)                  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Stage 8: AST to String Conversion                               │
│ Component: CPatMinerExecutor.astToString()                      │
│ Process: Convert CompilationUnit back to Java source strings    │
│ Output: Map<String, String> with Java code                      │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Stage 9: UML Model Building                                     │
│ Component: UMLModelASTReader (RefactoringMiner core)            │
│ Process: Parse Java strings into UMLModel                       │
│ Output: UMLModel (before and after commit)                      │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Stage 10: Refactoring Detection                                 │
│ Component: GitHistoryRefactoringMinerImpl.detectRefactorings()  │
│ Process: Compare before/after UML models, apply 60+ rules       │
│ Output: List<Refactoring>                                       │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│ Stage 11: JSON Serialization & Output                           │
│ Component: CSharpRefactoringMiner (handler callbacks)           │
│ Format: RefactoringMiner standard JSON schema                   │
│ Output: refactorings.json with detected changes                 │
└─────────────────────────────────────────────────────────────────┘
```

### AST Transformation Inside CPatMiner (Stage 7 Details)

Inside CPatMiner's `transform_csharp_to_java()` method, the transformation occurs in substages:

```
Stage 7a: srcML C# Parsing (External Process)
├─ Input: C# source code string
├─ Tool: srcML command-line utility
├─ Process: Parse C# syntax into XML representation
└─ Output: XML AST document

Stage 7b: GumTree XML Parsing
├─ Input: srcML XML document
├─ Library: GumTree 4.0.0-beta6
├─ Process: Parse XML into typed tree nodes (INode, TreeNode)
└─ Output: GumTree AST with C# node types

Stage 7c: SrcMLTreeVisitor Transformation (2000+ lines)
├─ Input: GumTree INode (from srcML XML)
├─ Process: 75+ visitor methods for node transformation
├─ Logic: Pattern match on node types (class, method, property, lambda, LINQ)
├─ C# Language Construct Mapping:
│  ├─ Properties → Getter/Setter pairs (Java patterns)
│  ├─ Events → Observer pattern delegates
│  ├─ Async/Await → Method markers + Task wrapping
│  ├─ LINQ → Stream chains or loop equivalents
│  ├─ Lambda → FunctionExpression (with body handling)
│  └─ Attributes → Java annotations
├─ Statement Handling: 20+ types (if, while, foreach, switch, try, etc.)
├─ Expression Handling: Method calls, field access, operators, literals
└─ Output: Eclipse JDT CompilationUnit (Java AST)

Stage 7d: Return to RefactoringMiner
├─ CPatMiner returns CompilationUnit to CPatMinerExecutor
├─ RefactoringMiner continues with Stage 8 (AST to String)
└─ Ready for: UML Model Building (Stage 9)
```

### Key Components

1. **`CSharpRefactoringMiner`** - CLI entry point, handles command parsing and git operations (Stage 1)
2. **`CSharpGitHistoryRefactoringMiner`** - Git integration, repository cloning and commit navigation (Stage 2)
3. **`CSharpGitServiceImpl`** - Extends GitServiceImpl to detect .cs files alongside .java files (Stage 3)
4. **`CSharpUMLModelASTReader`** - Orchestrates CPatMiner integration for C# AST generation (Stage 6)
5. **`CPatMinerExecutor`** - Dynamically loads and executes CPatMiner JAR via reflection (Stage 7)
6. **CPatMiner Internal Components** (inside JAR):
   - **`Transformation.transform_csharp_to_java()`** - Main entry point for C# parsing
   - **`SrcMLTreeVisitor`** - 2000+ line visitor with 75+ methods for C# to Java AST transformation
   - **srcML integration** - External tool for C# source to XML AST parsing
   - **GumTree** - XML parsing and tree construction
7. **RefactoringMiner Core** - Tree diffing, refactoring detection rules, and JSON serialization (Stage 10-11)

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
├── CSharpRefactoringMiner.java           # Main CLI entry point (Stage 1)
├── CSharpGitHistoryRefactoringMiner.java # Git integration, overrides createModel() (Stage 2, 4)
├── CSharpGitServiceImpl.java             # Extends GitServiceImpl for .cs files (Stage 3)
├── CSharpUMLModelASTReader.java          # Orchestrates CPatMiner integration (Stage 5)
└── CPatMinerExecutor.java                # Dynamic JAR loader & executor (Stage 6)

CPatMinerV2/AtomicASTChangeMining/src/transformation/
├── Transformation.java                   # Main entry: transform_csharp_to_java()
├── SrcMLTreeVisitor.java                 # 2000+ lines, 75+ visitor methods (Stage 7c)
├── nodes/                                # C# AST node types
│   ├── BlockNode.java
│   ├── ClassNode.java
│   ├── ExprNode.java
│   ├── MethodNode.java
│   └── ... (50+ node types)
└── utils/                                # Parsing utilities

External Dependencies:
├── srcML CLI                             # C# to XML parser (Stage 7a)
├── GumTree 4.0.0-beta6                   # XML to tree parser (Stage 7b)
├── Eclipse JDT                           # Java AST representation
└── RefactoringMiner Core                 # Refactoring detection (Stage 10)
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