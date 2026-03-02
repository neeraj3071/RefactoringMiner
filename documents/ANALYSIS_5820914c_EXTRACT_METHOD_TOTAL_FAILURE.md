# Analysis: Commit 5820914c27c2816a4c0c8913d2af5d115fccc2fd
## CognitiveVR/cvr-sdk-unity - Extract Method Refactoring

### Commit Information
- **Repository:** CognitiveVR/cvr-sdk-unity
- **Commit SHA:** 5820914c27c2816a4c0c8913d2af5d115fccc2fd
- **Commit Message:** "added export specific dynamic object" (with details about refactoring)
- **Date:** Wed Jan 8 17:16:04 2020 -0800
- **Author:** calderarchinuk
- **Files Changed:** 6 files (159 insertions, 118 deletions)

###Summary
This commit represents a **major Extract Method refactoring** where the export logic for dynamic objects was extracted from `ExportSelectedObjectsPrefab` (renamed to `ExportAllSelectedDynamicObjects`) into a new `ExportDynamicObject` method. RefactoringMiner detected **8 refactorings**, but **ALL 8 are false positives** with contradictory or nonsensical interpretations.

---

## RefactoringMiner Detection Results

RefactoringMiner detected **8 refactorings** in this commit:

| # | Type | Description | Status |
|---|------|-------------|--------|
| 1-2 | Rename Variable | `Selection` → `subdirectories` (2 detections) | ❌ **False Positive** |
| 3-4 | Rename Variable | `subdirectories` → `Selection` (2 detections) | ❌ **False Positive** |
| 5-6 | Change Variable Type | `var` → `AggregationManifest` (2 detections) | ❌ **False Positive** |
| 7-8 | Change Variable Type | `AggregationManifest` → `var` (2 detections) | ❌ **False Positive** |

### Detection Accuracy
- **True Positives:** 0/8 (0%)
- **False Positives:** 8/8 (100%)
- **Overall Precision:** **0%** ⚠️ **Complete failure**

---

## Detailed Analysis

### ❌ **False Positive #1-4: "Rename Variable" (Selection ↔ subdirectories)**

**Contradictory Detections:**
- 2 detections say: `Selection` renamed to `subdirectories`
- 2 detections say: `subdirectories` renamed to `Selection`

**RefactoringMiner's Claim:**
```json
{
  "type": "Rename Variable",
  "description": "Rename Variable Selection : in to subdirectories : in in method package foreach(v var) from class ExportUtility"
}
```

**Why This Is COMPLETELY WRONG:**

**Before (line 1070 in `UploadSelectedDynamicObjectMeshes`):**
```csharp
foreach (var v in Selection.transforms)
{
    var dyn = v.GetComponent<DynamicObject>();
    if (dyn == null) { continue; }
    dynamicMeshNames.Add(dyn.MeshName);
}
```

**After (line 1094 in MODIFIED `UploadSelectedDynamicObjectMeshes`):**
```csharp
var subdirectories = Directory.GetDirectories(path);
foreach (var v in subdirectories)
{
    var split = v.Split(Path.DirectorySeparatorChar);
    dynamicMeshNames.Add(split[split.Length - 1]);
}
```

**Reality:**
1. **`Selection` is NOT a variable** - it's a class (`UnityEditor.Selection`)
2. **`Selection.transforms` is a property access**, not a variable
3. **`subdirectories` is a NEW local variable**, not a renamed variable
4. The **loop variable is `v` in BOTH cases** (unchanged)
5. The **entire logic changed** - was iterating over selected GameObjects, now iterates over file system directories

**What Actually Happened:**
This is a **complete algorithmic change**:
- **Old approach:** Iterate over user's GUI selection to find DynamicObjects
- **New approach:** Scan file system directories to find exported meshes

**This is NOT a rename—it's a fundamental logic replacement.**

---

### ❌ **False Positive #5-8: "Change Variable Type" (var ↔ AggregationManifest)**

**Contradictory Detections:**
- 2 detections say: `var` changed to `AggregationManifest`
- 2 detections say: `AggregationManifest` changed to `var`

**RefactoringMiner's Claim:**
```json
{
  "type": "Change Variable Type",
  "description": "Change Variable Type manifest : var to manifest : AggregationManifest in method package delegate() from class ManageDynamicObjects"
}
```

**Why This Is COMPLETELY WRONG:**

**Before (line 173):**
```csharp
if (ExportUtility.ExportSelectedObjectsPrefab())
{
    EditorCore.RefreshSceneVersion(delegate ()
    {
        var manifest = new AggregationManifest();  // ← Type is AggregationManifest
        AddOrReplaceDynamic(manifest, GetDynamicObjectsInScene());
        ManageDynamicObjects.UploadManifest(...);
    });
}
```

**After (line 381):**
```csharp
EditorCore.RefreshSceneVersion(delegate ()
{
    var manifest = new AggregationManifest();  // ← Type is STILL AggregationManifest
    AddOrReplaceDynamic(manifest, GetDynamicObjectsInScene());
    ManageDynamicObjects.UploadManifest(...);
});
```

**Reality:**
1. **Both use `var`** keyword (C# type inference)
2. **Both have the SAME type:** `AggregationManifest` (inferred from `new AggregationManifest()`)
3. **No type change occurred whatsoever**
4. The code was  **restructured** (moved delegate outside the `if` block)
5. The variable declaration moved from one scope to another, but **type never changed**

**C# Type Inference Fundamentals:**
```csharp
var manifest = new AggregationManifest();  // Type is AggregationManifest
AggregationManifest manifest = new AggregationManifest();  // Identical, just explicit
```

These are **semantically identical**. RefactoringMiner is treating `var` as a different type than `AggregationManifest`, which demonstrates a **fundamental misunderstanding of C# syntax**.

---

## Root Cause Analysis

### 1. **Misidentification of Variables vs Properties**

**Issue:** RefactoringMiner cannot distinguish between:
- Variables: `var subdirectories = ...`
- Property accesses: `Selection.transforms`
- Class names: `Selection` (static class)

**Evidence:**
- `Selection` is **not a variable** in the user's code
- It's a property of the `UnityEditor.Selection` static class
- Tool extracted `Selection` from `Selection.transforms` and treated it as a variable name

**How It Should Work:**
```csharp
foreach (var v in Selection.transforms)  // Variable: v | Property: Selection.transforms
foreach (var v in subdirectories)        // Variable: v | Variable: subdirectories
```

**Correct Analysis:**
- Loop variable `v` unchanged (iterates over different collections)
- `subdirectories` is a new variable (not a rename of anything)

---

### 2. **C# Type Inference (`var`) Not Understood**

**Issue:** Tool treats `var` as an actual type, not as type inference

**C# Semantics:**
```csharp
var x = new AggregationManifest();  // Type: AggregationManifest (inferred)
AggregationManifest x = new AggregationManifest();  // Type: AggregationManifest (explicit)
```

These are **100% semantically identical**. No type change occurs when `var` is used.

**RefactoringMiner's Error:**
- Treats `var` as distinct type from `AggregationManifest`
- Detects "type change" when code moves between scopes
- Creates contradictory detections when multiple code blocks are restructured

**Why This Matters:**
- `var` is pervasive in modern C# (introduced in C# 3.0, year 2007)
- This is not an edge case—it's mainstream C# syntax
- Misunderstanding `var` undermines any C# analysis

---

### 3. **Code Movement Misinterpreted as Semantic Changes**

**Issue:** Structural refactoring (moving code between scopes) triggers false semantic change detections

**What Happened:**
```csharp
// Before: Code inside if block
if (ExportUtility.ExportSelectedObjectsPrefab()) {
    delegate() {
        var manifest = new AggregationManifest();  // ← Declaration #1
        // ...
    }
}

// After: Code moved outside if block (refactored)
EditorCore.RefreshSceneVersion(delegate ()
{
    var manifest = new AggregationManifest();  // ← Declaration #2
    // ...
});
```

**Tool's Interpretation:**
- "Declaration #1 no longer exists"
- "Declaration #2 is new"
- "They have the same variable name but different types (var vs AggregationManifest)"
- **Conclusion:** "Type changed"

**Reality:**
- This is code movement (Extract Method + scope restructuring)
- Variable semantics unchanged (name, type, purpose all identical)
- Simply relocated within call graph

---

### 4. **Contradictory Detections Not Validated**

**Issue:** Multiple contradictory refactorings reported without validation

**Evidence:**
- **Rename Variable:** Both `Selection → subdirectories` AND `subdirectories → Selection`
  - Impossible for a variable to swap names with another variable simultaneously
- **Change Variable Type:** Both `var → AggregationManifest` AND `AggregationManifest → var`
  - Impossible for two instances to change types in opposite directions

**Why Contradictions Occur:**
- Tool matches variables/expressions across multiple code blocks
- Each pair of (before, after) blocks generates a detection
- No global consistency check
- Likely caused by duplicate code patterns analyzed independently

---

## Impact Assessment

### Precision Impact
- **Detection Rate:** 8 detections
- **True Positives:** 0/8 (none)
- **False Positives:** 8/8 (all)
- **Precision:** **0%** ⚠️

**This is a complete failure** for this commit.

### Severity: **CRITICAL**
- **100% false positive rate** for detected refactorings
- Demonstrates fundamental gaps in C# language understanding
- Contradictory detections undermine tool credibility
- Would completely mislead code historians or automated tools

### Pattern: **Extract Method Mishandling**
- Extract Method is one of the most common refactorings
- Tool failed to detect the actual Extract Method refactoring
- Instead generated nonsensical variable/type change detections
- High reproducibility risk for any Extract Method scenario

### Affected Scenarios
- ✅ Extract Method refactorings (very common)
- ✅ Code with C# `var` keyword (ubiquitous in modern C#)
- ✅ Code accessing Unity API properties (`Selection.transforms`, etc.)
- ✅ Delegate-based callbacks (common in Unity/C# event systems)

---

## What RefactoringMiner SHOULD Have Detected

**Expected Detection:**
```
✓ Extract Method: ExportSelectedObjectsPrefab → ExportDynamicObject
  - Extracted lines: Export logic (105 lines)
  - Parameters: DynamicObject dynamicObject, bool displayPopup
  - Return type: bool

✓ Rename Method: ExportSelectedObjectsPrefab → ExportAllSelectedDynamicObjects
  - Name change indicates semantic shift (now a wrapper)

✗ SHOULD NOT DETECT: Any variable renames or type changes
```

**What It Actually Detected:**
```
✗ 4 bogus variable renames (contradictory)
✗ 4 bogus type changes (contradictory, based on misunderstanding `var`)
✓ 0 correct refactorings
```

---

## Comparison with Previous Issues

### Pattern Evolution

| Commit | Repository | Issue Type | Root Cause | Precision |
|--------|-----------|------------|------------|-----------|
| [9aaea6e6](./MISCLASSIFICATION_NESTED_TYPE_RENAME.md) | vrm-c/UniVRM | Variable rename across nested types | Missing operation equality | 50% |
| [150f711c](./ANALYSIS_150f711c_PROJECTION_REFACTORING.md) | BIVROST/360PlayerWindows | Method replacement as rename | Missing interface context | 60% |
| [4b24a421](./ANALYSIS_4b24a421_LINE_NUMBER_FALSE_POSITIVES.md) | Unity-Technologies/EditorXR | Line-shift false positives | Weak field matching | 86% |
| **5820914c** | **CognitiveVR/cvr-sdk-unity** | **Complete analysis failure** | **Multiple fundamental errors** | **0%** ⚠️ |

### Critical Escalation

This commit reveals the **worst performance** yet:
- **Previous commits:** Partial failures (50-86% precision)
- **This commit:** Total failure (0% precision)
- **Cause:** Combination of multiple foundational issues:
  1. Cannot distinguish variables from properties
  2. Does not understand C# `var` keyword
 3. Misinterprets code movement as semantic changes
  4. No contradiction detection

---

## Recommendations

### 1. **Implement C# Type Inference Support (CRITICAL)**

**Problem:** Tool treats `var` as a type distinct from the inferred type

**Solution:**
```java
// When analyzing C# variable declarations
if (declaration.usesVarKeyword()) {
    TypeReference actualType = declaration.getInferredType();
    declaration.setSemanticType(actualType);  // Use inferred type, not "var"
}

// When comparing types
boolean typesMatch(TypeReference type1, TypeReference type2) {
    // Resolve var to actual type before comparing
    TypeReference resolved1 = resolveVarToActualType(type1);
    TypeReference resolved2 = resolveVarToActualType(type2);
    return resolved1.equals(resolved2);
}
```

**Priority:** CRITICAL - Affects all modern C# code

---

### 2. **Distinguish Variables from Property Accesses (HIGH PRIORITY)**

**Problem:** `Selection` extracted from `Selection.transforms` and treated as a variable

**Solution:**
```java
// When identifying variables in expressions
Expression expr = parseExpression("Selection.transforms");
if (expr instanceof PropertyAccess) {
    PropertyAccess pa = (PropertyAccess) expr;
    // pa.receiver = "Selection" (class)
    // pa.property = "transforms"
    // DO NOT treat "Selection" as a variable!
}

// Variable extraction should only consider:
// - Local variable declarations
// - Parameter declarations
// - Field declarations
// NOT property receivers or class names
```

---

### 3. **Detect Extract Method Refactoring (HIGH PRIORITY)**

**Problem:** Obvious Extract Method refactoring not detected

**Solution:**
```java
// Detect blocks of code moved from one method to a new method
if (methodBodySubset(oldMethod.body, newMethod.body) &&
    newMethod.isNewlyAdded() &&
    oldMethod.hasCallToNewMethod(newMethod)) {
    
    return new ExtractMethodRefactoring(
        oldMethod,
        newMethod,
        extractedLines
    );
}
```

---

### 4. **Validate Refactoring Consistency (MEDIUM PRIORITY)**

**Problem:** Contradictory refactorings reported (A→B and B→A simultaneously)

**Solution:**
```java
void validateRefactorings(List<Refactoring> refactorings) {
    for (Refactoring r1 : refactorings) {
        for (Refactoring r2 : refactorings) {
            if (r1.contradicts(r2)) {
                // Remove both or mark as uncertain
                logError("Contradictory refactorings detected:");
                logError("  " + r1.getDescription());
                logError("  " + r2.getDescription());
                markAsInvalid(r1, r2);
            }
        }
    }
}

// For variable renames
boolean RenameVariable.contradicts(Refactoring other) {
    if (!(other instanceof RenameVariable)) return false;
    RenameVariable otherRename = (RenameVariable) other;
    
    // A→B contradicts B→A
    return this.oldName.equals(otherRename.newName) &&
           this.newName.equals(otherRename.oldName);
}
```

---

### 5. **Improve C# Property/Field/Variable Classification**

**Unity-specific issue:** `Selection` is a Unity Editor API class

**Solution:**
```java
// Build knowledge base of common API classes
Set<String> commonAPIClasses = Set.of(
    "UnityEditor.Selection",
    "UnityEngine.GameObject",
    "UnityEngine.Debug",
    // ... etc
);

// When analyzing member access
if (commonAPIClasses.contains(expr.getFullyQualifiedType())) {
    // This is an API class, not a user variable
    return ClassReference, not VariableReference;
}
```

---

## Test Case for Validation

### Scenario: Extract Method with `var` and Property Access

**Before:**
```csharp
using UnityEditor;

public class ExportTool
{
    public static bool ExportAll()
    {
        List<string> names = new List<string>();
        foreach (var item in Selection.transforms)  // Unity API property
        {
            var component = item.GetComponent<MyComponent>();  // var with type inference
            if (component != null)
            {
                names.Add(component.name);
            }
        }
        return Upload(names);
    }
}
```

**After:**
```csharp
using UnityEditor;
using System.IO;

public class ExportTool
{
    // NEW METHOD (extracted)
    public static bool ExportSingle(MyComponent component)
    {
        var name = component.name;  // var still used
        return Upload(new List<string> { name });
    }
    
    public static bool ExportAll()
    {
        // Changed to iterate over file system instead of Selection
        var subdirectories = Directory.GetDirectories("/path");
        foreach (var dir in subdirectories)
        {
            // Use extracted method
            ExportSingle(LoadComponent(dir));
        }
        return true;
    }
}
```

### Expected Detection

```
✓ Extract Method: ExportAll → ExportSingle (extracted component export logic)
✓ Rename Parameter: item → component (if deemed significant)
✗ SHOULD NOT DETECT: "Rename Variable Selection → subdirectories" (different scopes, Selection is not a variable)
✗ SHOULD NOT DETECT: "Change Variable Type var → MyComponent" (var infers to MyComponent, no change)
✗ SHOULD NOT DETECT: "Change Variable Type MyComponent → var" (contradictory, nonsensical)
```

### Current Behavior (WRONG)

```
✗ DETECTS: Rename Variable Selection → subdirectories
✗ DETECTS: Rename Variable subdirectories → Selection
✗ DETECTS: Change Variable Type var → MyComponent
✗ DETECTS: Change Variable Type MyComponent → var
✓ MISSES: Extract Method refactoring
```

---

## Conclusion

This commit represents a **catastrophic failure** for RefactoringMiner on C# code. With **0% precision** (8/8 false positives, 0/8 true positives), the tool completely misinterpreted a straightforward Extract Method refactoring.

### Root Causes (Multiple Compounding Failures)

1. **C# Language Gap:** Does not understand `var` keyword (type inference)
2. **Syntax Parsing Error:** Cannot distinguish variables from property accesses
3. **Missing Refactoring Type:** No Extract Method detection
4. **No Validation:** Contradictory detections reported without checks

### Critical Findings

**For C# projects:**
- Tool cannot reliably analyze modern C# (post-2007 syntax)
- `var` keyword is ubiquitous—this is not an edge case
- Unity API patterns (property access) cause systematic errors

**For Refactoring Studies:**
- Using RefactoringMiner on C# Extract Method commits will yield **misleading** results
- Contradictory detections should be automatically flagged
- Manual validation is **essential** for C# codebases

### Severity Assessment

**Previous commits:** Manageable errors (50-86% precision)  
**This commit:** **Unusable** results (0% precision)  

**This is a critical regression** that must be addressed before RefactoringMiner can be considered reliable for C# codebases using modern language features.

---

## Historical Context

This is the **fourth documented misclassification**, and the **worst**:

| # | Commit | Repository | Pattern | Precision | Severity |
|---|--------|-----------|---------|-----------|----------|
| 1 | [9aaea6e6](./MISCLASSIFICATION_NESTED_TYPE_RENAME.md) | vrm-c/UniVRM | Nested type scope | 50% | High |
| 2 | [150f711c](./ANALYSIS_150f711c_PROJECTION_REFACTORING.md) | BIVROST/360PlayerWindows | Interface replacement | 60% | High |
| 3 | [4b24a421](./ANALYSIS_4b24a421_LINE_NUMBER_FALSE_POSITIVES.md) | Unity-Technologies/EditorXR | Line-shift matching | 86% | Low-Med |
| 4 | **5820914c** | **CognitiveVR/cvr-sdk-unity** | **Extract Method + var + API properties** | **0%** | **CRITICAL** ⚠️ |

**Trend:** Precision has been variable (50% → 60% → 86%), but this commit shows a **complete breakdown** when multiple C#-specific issues compound.
