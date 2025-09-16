# C# Integration Flow: RefactoringMiner + CPatMinerV2

This document explains the end-to-end flow used to analyze C# repositories with RefactoringMiner, leveraging CPatMinerV2 to convert C# code into Java AST-compatible representations.

---

## Overview
- RefactoringMiner is extended to detect `.cs` files.
- C# code is converted to a Java-like AST via CPatMinerV2 (`Transformation.transform_csharp_to_java`).
- The resulting Java AST is analyzed by RefactoringMiner just like Java code.

---

## Sequence of Operations

1. CLI Entry
   - File: `src/main/java/org/refactoringminer/RefactoringMiner.java`
   - Method: `main(String[] args)`
   - Responsibility: parse CLI args (e.g., `-a <repo> <branch> -json <file>`), start detection.

2. Repository Processing
   - File: `src/main/java/gr/uom/java/xmi/UMLModelASTReader.java`
   - Method: `processJavaFileContents(Map<String, String> javaFileContents, boolean astDiff)`
   - Responsibility: iterate all files; identify `.java` vs `.cs` files; preprocess `.cs` files.

3. C#→Java AST Conversion (Bridge)
   - File: `src/main/java/org/refactoringminer/csharp/CPatMinerCSharpConverter.java`
   - Method: `convertToJava(String csharpCode)`
   - Responsibility: call CPatMinerV2 transformation and return Java-like source string.
   - Core call:
     ```java
     CompilationUnit javaAst = Transformation.transform_csharp_to_java(csharpCode);
     return javaAst != null ? javaAst.toString() : null;
     ```

4. CPatMinerV2 Transformation (External lib)
   - Module: `CPatMinerV2/AtomicASTChangeMining`
   - File: `src/transformation/Transformation.java`
   - Method: `transform_csharp_to_java(String content)`
   - Internals:
     - `SrcmlCsTreeGenerator` parses C# into a srcML-based AST (TreeContext).
     - `TransformationUtils.transformTree(...)` maps C# constructs to Java-like constructs.
     - `SrcMLTreeVisitor` produces an `org.eclipse.jdt.core.dom.CompilationUnit`.

5. Back in RefactoringMiner
   - File: `src/main/java/gr/uom/java/xmi/UMLModelASTReader.java`
   - Methods:
     - `getCompilationUnit(...)` parses the Java-like code into a JDT `CompilationUnit`.
     - `processCompilationUnit(...)` performs model building and triggers refactoring detection.

6. Output
   - CLI args like `-json output.json` write results to JSON.
   - Example run:
     ```sh
     ./gradlew run --args="-a ../AutoMapper master -json output.json"
     ```

---

## Data Flow Summary
- Input: Git repo path + branch
- For each file:
  - `.java` → parse directly via JDT
  - `.cs` → `CPatMinerCSharpConverter.convertToJava` → Java-like source → JDT parse
- Aggregated ASTs → UML model → Refactoring detection → JSON

---

## Logging & Diagnostics
- Success: `[INFO] Converted C# file: <path> → Java AST-ready code`
- Failure: `[WARN] Skipping C# file: <path>` (conversion returned null)

To enable deeper debugging, add logs in `CPatMinerCSharpConverter` and confirm the number of `.cs` files processed.

---

## Build & Dependencies
- Build CPatMinerV2 module and add the JAR to RefactoringMiner `libs/`:
  ```sh
  cd CPatMinerV2/AtomicASTChangeMining && mvn clean package
  cp target/AtomicASTChangeMining-0.0.1-SNAPSHOT.jar ../../RefactoringMiner/libs/
  ```
- Add to `build.gradle`:
  ```groovy
  implementation files('libs/AtomicASTChangeMining-0.0.1-SNAPSHOT.jar')
  ```

---

## Known Limitations
- Detection quality depends on the fidelity of the C#→Java transformation.
- Not all C# constructs map cleanly to Java; some refactorings may not be detectable.
- Some repositories/commits may legitimately have no detectable refactorings.

---

## Quick Test
```sh
./gradlew clean build
./gradlew run --args="-a ../AutoMapper master -json output.json"
```

If `output.json` has empty refactorings, try a repository with known structural changes or create a small test repo with controlled refactorings.
