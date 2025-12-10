# C# RefactoringMiner Support

![C# Support](https://img.shields.io/badge/language-C%23-239120?style=flat&logo=csharp)
![Status](https://img.shields.io/badge/status-experimental-orange)
![RefactoringMiner](https://img.shields.io/badge/RefactoringMiner-3.0.11-blue)

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Features](#features)
4. [Installation](#installation)
5. [Usage](#usage)
6. [Supported C# Features](#supported-csharp-features)
7. [Supported Refactoring Types](#supported-refactoring-types)
8. [Limitations](#limitations)
9. [Performance](#performance)
10. [Examples](#examples)
11. [Troubleshooting](#troubleshooting)
12. [Contributing](#contributing)

---

## Overview

RefactoringMiner C# Support extends the original [RefactoringMiner](https://github.com/tsantalis/RefactoringMiner) tool to detect refactorings in C# projects. This implementation uses **srcML** for parsing C# code and converting it to Java AST representations that RefactoringMiner can analyze.

### Key Highlights

- **Direct srcML Integration** - Uses srcML for reliable C# parsing
- **Enhanced C# Features** - Supports async/await, LINQ, properties, events, and more
- **Compatible with RefactoringMiner API** - Uses the same command-line interface
- **Batch Processing Support** - Analyze multiple commits efficiently

---

## Architecture

### Processing Pipeline

```
C# Source Code
      ↓
   srcML Parser (XML AST)
      ↓
Enhanced C# Processor
      ↓
Java AST (CompilationUnit)
      ↓
RefactoringMiner Core
      ↓
Detected Refactorings (JSON)
```

### Key Components

1. **`SrcMLBasedCSharpProcessor`** - Core processor that handles C# to Java AST conversion
2. **`CSharpGitHistoryRefactoringMiner`** - Git integration for C# repositories
3. **`CSharpRefactoringMiner`** - CLI entry point compatible with RefactoringMiner
4. **`CSharpFileProcessor`** - Handles .cs file detection and processing

---

## Features

### 🎯 Enhanced C# Language Support

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

The fat JAR will be created at: `build/libs/RM-fat.jar`

### Verify Installation

```bash
# Verify srcML is installed
srcml --version

# Verify RefactoringMiner build
java -jar build/libs/RM-fat.jar -h
```

---

## Usage

### Command Line Interface

The C# RefactoringMiner uses the **same command-line interface** as the original RefactoringMiner:

#### 1. Analyze a Single Commit

```bash
java -cp build/libs/RM-fat.jar \
  org.refactoringminer.csharp.CSharpRefactoringMiner \
  -c /path/to/repo <commit-sha> -json output.json
```

**Example:**
```bash
java -cp build/libs/RM-fat.jar \
  org.refactoringminer.csharp.CSharpRefactoringMiner \
  -c ~/projects/MyUnityGame 35cb3631 -json zinnia_refactorings.json
```

#### 2. Analyze All Commits on a Branch

```bash
java -cp build/libs/RM-fat.jar \
  org.refactoringminer.csharp.CSharpRefactoringMiner \
  -a /path/to/repo main -json all_refactorings.json
```

#### 3. Analyze Commits Between Two Tags

```bash
java -cp build/libs/RM-fat.jar \
  org.refactoringminer.csharp.CSharpRefactoringMiner \
  -bt /path/to/repo v1.0.0 v2.0.0 10 -json refactorings.json
```

#### 4. GitHub Direct Analysis (with OAuth token)

```bash
# Set up github-oauth.properties file first
java -cp build/libs/RM-fat.jar \
  org.refactoringminer.csharp.CSharpRefactoringMiner \
  -gc https://github.com/user/repo.git <commit-sha> 10 -json output.json
```

### Using the Shell Script

A convenient shell script is provided for easier execution:

```bash
# Make it executable
chmod +x csharp-refactoring-miner.sh

# Run analysis
./csharp-refactoring-miner.sh -c /path/to/repo <commit-sha> -json output.json
```

### Batch Processing Multiple Commits

For analyzing multiple commits from an Excel file:

```bash
python3 batch_process_commits.py
```

**Requirements:**
- Excel file with commit URLs in "Commit URL" column
- Configure paths in the script

---

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

## Performance

### Benchmark Results

Tested on VR/AR C# projects (January 2024):

| Metric | Value |
|--------|-------|
| **Average Processing Time** | ~2-5 seconds per commit |
| **srcML Parsing** | ~0.5-1 second per file |
| **AST Conversion** | ~0.5-1 second per file |
| **Refactoring Detection** | ~1-3 seconds per commit |
| **Large Commits (20+ files)** | ~10-30 seconds |

### Optimization Tips

1. **Use Fat JAR** - Single JAR reduces classpath overhead
2. **Batch Processing** - Process multiple commits in one session
3. **Filter Commits** - Pre-filter commits with C# changes only
4. **Parallel Processing** - Use multiple processes for independent commits

### Memory Usage

- **Base Memory:** ~512 MB
- **Per Commit:** ~50-200 MB (depending on size)
- **Recommended JVM:** `-Xmx4096M -Xms1024M`

---

## Examples

### Example 1: Extract Method Detection

**C# Commit:**
```csharp
// Before
public void ProcessPlayer() {
    int score = CalculateScore();
    UpdateUI();
    SaveToDatabase();
}

// After
public void ProcessPlayer() {
    int score = CalculateScore();
    FinishProcessing();
}

private void FinishProcessing() {
    UpdateUI();
    SaveToDatabase();
}
```

**Detected Refactoring:**
```json
{
  "type": "Extract Method",
  "description": "Extract Method private FinishProcessing() extracted from public ProcessPlayer() in class Player"
}
```

### Example 2: Change Access Modifier

**C# Commit:**
```csharp
// Before
public int health = 100;

// After
[SerializeField]
private int health = 100;
```

**Detected Refactoring:**
```json
{
  "type": "Change Attribute Access Modifier",
  "description": "Changed visibility of attribute health from public to private in class Player"
}
```

### Example 3: Rename Class

**C# Commit:**
```csharp
// Before: PlayerController.cs
public class PlayerController { }

// After: PlayerManager.cs
public class PlayerManager { }
```

**Detected Refactoring:**
```json
{
  "type": "Rename Class",
  "description": "Rename Class PlayerController renamed to PlayerManager"
}
```

### Example 4: Move Method

**C# Commit:**
```csharp
// Before: Player.cs
public class Player {
    public void UpdateScore() { }
}

// After: ScoreManager.cs
public class ScoreManager {
    public void UpdateScore() { }
}
```

**Detected Refactoring:**
```json
{
  "type": "Move Method",
  "description": "Move Method public UpdateScore() from class Player to ScoreManager"
}
```

### Real-World Example: Zinnia.Unity Commit

**Commit:** `35cb3631904fec77ab2c68058ba4dd7b6aa75095`

**Detected:** 12 refactorings
- 10× Change Attribute Access Modifier
- 1× Extract Method
- 1× Rename Attribute

**Processing Time:** 3.2 seconds

---

## Troubleshooting

### Issue: "srcML command not found"

**Solution:**
```bash
# Install srcML
brew install srcml  # macOS
sudo apt install srcml  # Linux

# Verify installation
srcml --version
```

### Issue: "No .cs files found in repository"

**Possible Causes:**
- Repository path incorrect
- No C# files in specified commit
- Files in subdirectories not scanned

**Solution:**
```bash
# Verify repository structure
ls -R /path/to/repo | grep ".cs$"

# Check specific commit
git show <commit-sha> --name-only | grep ".cs$"
```

### Issue: "OutOfMemoryError during analysis"

**Solution:**
```bash
# Increase heap size
java -Xmx4096M -Xms1024M -jar build/libs/RM-fat.jar -c ...
```

### Issue: "Empty JSON output / Zero refactorings detected"

**Possible Causes:**
- Commit contains only non-detectable changes
- Semantic refactorings (not AST-based)
- srcML parsing failures

**Debug Steps:**
1. Check srcML can parse the file:
   ```bash
   srcml file.cs -o output.xml
   ```
2. Enable verbose logging:
   ```bash
   java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug -jar ...
   ```
3. Verify file encoding (should be UTF-8)

### Issue: "GitHub API rate limit exceeded"

**Solution:**
Create `github-oauth.properties`:
```properties
username=your-github-username
token=ghp_your_personal_access_token
```

Place it next to the JAR file when using `-gc` or `-gp` options.

### Issue: "Compilation errors in converted Java code"

This is expected for some C# constructs. The processor handles this internally. If you need to debug:

```bash
# Check generated Java code (temporary files)
ls -la /tmp/csharp_*
```

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
├── CSharpRefactoringMiner.java          # Main CLI entry point
├── CSharpGitHistoryRefactoringMiner.java # Git integration
├── SrcMLBasedCSharpProcessor.java        # Core C# processor
├── CSharpFileProcessor.java              # File handling
├── CSharpUMLModelASTReader.java          # AST reader
└── cli/                                  # CLI utilities
    └── CSharpRefactoringMinerCLI.java    # Command-line interface
```

### Adding New C# Features

To add support for a new C# language feature:

1. **Update `SrcMLBasedCSharpProcessor.java`:**
   ```java
   private static String processNewFeature(Element element) {
       // Parse srcML XML element
       // Convert to Java equivalent
       // Return Java code
   }
   ```

2. **Add to `convertSrcMLXMLToJava()`:**
   ```java
   case "new_feature":
       result.append(processNewFeature(child));
       break;
   ```

3. **Test with sample C# code:**
   ```bash
   # Create test file
   echo "your C# code" > test.cs
   
   # Test conversion
   java -cp build/libs/RM-fat.jar \
     org.refactoringminer.csharp.CSharpRefactoringMiner \
     -c /path/to/test/repo <commit> -json test.json
   ```


## Related Tools

### CPatMinerV2

This repository includes **CPatMinerV2**, a C# code change pattern mining tool.

**Location:** `CPatMinerV2/`

**Features:**
- Semantic change pattern extraction
- Graph-based mining
- Supports C# via srcML

**See:** [CPatMinerV2/README.md](CPatMinerV2/README.md)

### Integration with RefactoringMiner

The C# support is fully integrated with RefactoringMiner's API:

```java
import org.refactoringminer.csharp.CSharpGitHistoryRefactoringMiner;

CSharpGitHistoryRefactoringMiner miner = 
    new CSharpGitHistoryRefactoringMiner();
    
miner.detectAtCommit(repository, commitId, new RefactoringHandler() {
    @Override
    public void handle(String commitId, List<Refactoring> refactorings) {
        // Process detected refactorings
    }
});
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

### CPatMiner
- **Original:** [nguyenhoan/CPatMiner](https://github.com/nguyenhoan/CPatMiner)
- **C# Extension:** CPatMinerV2 (included)

---