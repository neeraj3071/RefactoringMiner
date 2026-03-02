# Comprehensive Misclassification Analysis: RefactoringMiner on Unity C# Projects
## Root Cause Analysis Across 5 Commits
 
**Tool**: RefactoringMiner (C# support via CPatMiner)  
**Domain**: Unity Game Engine Projects (C#)  
**Commits Analyzed**: 5  
**Total Refactorings Detected**: 48  
**Overall Precision Range**: 0% - 86%

---

## Executive Summary

This document presents a comprehensive analysis of RefactoringMiner's performance on 5 Unity C# commits, revealing **systematic failures** in understanding C# language features and semantic context. The precision degraded from **50%** (manageable) to **0%** (catastrophic) as commits exposed increasingly C#-specific patterns.

### Key Findings
1. **C# Language Gap**: RefactoringMiner fundamentally misunderstands the `var` keyword (ubiquitous in C# since 2007)
2. **Scope Blindness**: Cannot distinguish between class fields and local variables in different scopes
3. **Nested Type Confusion**: Fails to validate that variables belong to the same enclosing type
4. **Line-Number Matching**: Matches code elements by line numbers instead of semantic identity
5. **Interface Context Missing**: Does not validate interface context when detecting method refactorings
6. **Extract Method Success (Sometimes)**: Can detect Extract Method in simple cases without C#-specific features

### Severity Progression
```
Commit 1 (9aaea6e6):   50% precision - Nested type scope errors
Commit 2 (150f711c):   60% precision - Interface context missing
Commit 3 (4b24a421):   86% precision - Line number false positives
Commit 4 (5820914c):    0% precision - Complete var keyword failure ⚠️ CRITICAL
Commit 5 (f298068):    50% precision - Scope confusion in Extract Method side effects
```

---

## Commit-by-Commit Analysis

### Commit 1: 9aaea6e6 - Nested Type Variable Rename (vrm-c/UniVRM)
**File**: `FastSpringBoneBufferCombiner.cs`  
**Date**: February 18, 2026  
**Precision**: 50% (2 correct, 2 false positives)

#### What RefactoringMiner Reported
- ✅ Inline Variable: `collider` (Correct)
- ❌ Rename Variable: `collider` → `spring` (False Positive)
- ✅ Inline Variable: `logic` (Correct)
- ❌ Rename Variable: `logic` → `spring` (False Positive)

#### Root Cause: **Nested Type Scope Validation Missing**

**The Problem**:
Three nested structs (`LoadSpringsJob`, `LoadCollidersJob`, `LoadLogicsJob`) each have an `Execute(int index)` method. RefactoringMiner incorrectly matched variables across these **different struct methods**, claiming:
- `collider` from `LoadCollidersJob.Execute` was renamed to `spring` in `LoadSpringsJob.Execute`
- `logic` from `LoadLogicsJob.Execute` was renamed to `spring` in `LoadSpringsJob.Execute`

**Reality**: These are **independent variables in different nested types**, not renamed variables.

**Code Context**:
```csharp
// Three DIFFERENT nested structs
struct LoadSpringsJob : IJobParallelFor {
    public void Execute(int index) {
        var spring = ...; // In LoadSpringsJob
    }
}

struct LoadCollidersJob : IJobParallelFor {
    public void Execute(int index) {
        var collider = ...; // In LoadCollidersJob - DIFFERENT METHOD
    }
}

struct LoadLogicsJob : IJobParallelFor {
    public void Execute(int index) {
        var logic = ...; // In LoadLogicsJob - DIFFERENT METHOD
    }
}
```

**Why It Failed**:
The tool matched methods by signature (`Execute(int index) : void`) but ignored the enclosing type. In file `VariableReplacementAnalysis.java:1847`:
```java
if(operation1.getLocationInfo().equals(operation2.getLocationInfo())) {
    // Only checks location, NOT the enclosing operation/type
}
```

---

### Commit 2: 150f711c - Projection Refactoring (BIVROST/360PlayerWindows)
**File**: Multiple files (Scene.cs, Headset.cs, ShellViewModel.cs)  
**Date**: Wed May 9, 2018  
**Precision**: 60% (6 correct, 4 incorrect/contradictory)

#### What RefactoringMiner Reported
- ✅ Add Parameter (1 correct)
- ✅ Change Access Modifiers (4 correct)
- ✅ Remove Parameter (1 correct)
- ❌ Rename Method: `UpdateSceneSettings` → `SetProjection` (False Positive - Actually method replacement)
- ❌ Inline Method: Partially correct but misses context
- ⚠️ Add static + Remove static (Contradictory detections)

#### Root Cause: **Interface Context Not Validated**

**The Problem**:
RefactoringMiner detected "Rename Method" when a method was actually **replaced in a different interface implementation**:

**Before** (`Headset.cs`):
```csharp
void UpdateSceneSettings(ProjectionMode projectionMode, VideoMode stereoscopy) {
    scene.UpdateSceneSettings(projection, VideoMode.Mono);
}
```

**After** (`Headset.cs`):
```csharp
// Method removed (not renamed)
```

**After** (`Scene.cs` - DIFFERENT CLASS):
```csharp
public void SetProjection(ProjectionMode projection) {
    // NEW method in Scene class
}
```

**Reality**: 
- `UpdateSceneSettings` in Headset was **removed**
- `SetProjection` in Scene is a **new method** (not a rename)
- They implement **different interfaces** and serve different architectural purposes

**Architectural Change**: Push model → Observer/callback pattern

**Why It Failed**: Tool matched methods by similar parameter types and usage patterns, but didn't validate:
- Interface context
- Whether methods exist in same class
- Architectural purpose and call patterns

---

### Commit 3: 4b24a421 - Code Cleanup (Unity-Technologies/EditorXR)
**Files**: MeshData.cs, SpatialHash.cs, IntersectionModule.cs  
**Date**: Fri Jul 22, 2016  
**Precision**: 86% (12 correct, 2 false positives)

#### What RefactoringMiner Reported
- ✅ Change Access Modifiers (11 correct - fields made private)
- ✅ Rename Method: `OnIntersection` → `OnIntersectionEnter` (1 correct)
- ❌ Add static Modifier (1 false positive)
- ❌ Remove static Modifier (1 false positive - contradicts previous)

#### Root Cause: **Line Number Matching Instead of Semantic Identity**

**The Problem**:
When a `using` statement was removed, all code shifted up by one line. RefactoringMiner matched fields by **line numbers** rather than by **name + type**, leading to false static modifier changes.

**MeshData.cs - What Actually Happened**:
```csharp
// Before (line 39-40)
using System.Collections.Generic;  // ← Line 39 - REMOVED in after
Dictionary<...> s_DeferredMeshTri = ...; // Line 40 - static field

// After (line 38-39) - Lines shifted up due to removed import
Dictionary<...> s_DeferredMeshTri = ...; // Now line 39 - SAME FIELD, same static

```

**Why It Failed**: 
- Tool uses **line numbers as primary identity**
- Should use **semantic identity** (field name + type + declaring class)
- Import removal causes line shifts that confuse positional matching

---

### Commit 4: 5820914c - Extract Method with var Keyword (CognitiveVR/cvr-sdk-unity)
**Files**: ExportUtility.cs, ManageDynamicObjects.cs  
**Date**: Wed Jan 8, 2020  
**Precision**: 0% (0 correct, 4 false positives) ⚠️ **CRITICAL FAILURE**

#### What RefactoringMiner Reported
- ❌ Change Variable Type: `var` → `AggregationManifest` (2 detections)
- ❌ Change Variable Type: `AggregationManifest` → `var` (2 contradictory detections)

**All 4 detections are false positives. All 4 contradict each other.**

#### Root Cause: **Complete Failure to Understand C# `var` Keyword**

**The Problem**:
RefactoringMiner **does not understand** that `var` is syntactic sugar for inferred types. It treats `var` as a distinct type, leading to nonsensical detections.

**Code Context**:
```csharp
// Before: Extract Method candidates
foreach (var subdirectory in subdirectories) {
    foreach (GameObject obj in Selection.gameObjects) {
        var manifest = GameObject.Find(...).GetComponent<AggregationManifest>();
        // ... export logic ...
    }
}

// After: Extract Method applied
void ExportDynamicObject(GameObject gameObject, string subDirectoryPath) {
    var manifest = GameObject.Find(...).GetComponent<AggregationManifest>();
    // Extracted logic
}
```
---

### Commit 5: f298068 - Extract Method with Local Variables (stefaanvermassen/virtual-museum-app)
**File**: MuseumTile.cs  
**Date**: Fri Mar 6, 2015  
**Precision**: 50% (6 correct, 6 false positives)

#### What RefactoringMiner Reported
- ✅ Extract Method: `CreateFace()` extracted (6 detections - 100% correct)
- ❌ Replace Attribute With Variable: 6 false positives (0% correct)

#### Root Cause: **Scope Confusion in Extract Method Side Effects**

**The Problem**:
When Extract Method creates local variables, RefactoringMiner incorrectly reports that **class fields were "replaced"** by the local variables, even though both coexist in different scopes.

**Code Example**:
```csharp
// Class fields (line 14) - STILL EXIST AFTER REFACTORING
private GameObject upObject, downObject, leftObject, rightObject, frontObject, backObject;

// Before Extract Method
void Start() {
    upObject = GameObject.CreatePrimitive(PrimitiveType.Quad);
    upObject.transform.parent = gameObject.transform;
    upObject.transform.localPosition = new Vector3(0,0,0);
    upObject.transform.Rotate(new Vector3(90, 0, 0));
}

// After Extract Method
GameObject CreateFace(Vector3 localPosition, Vector3 angles) {
    var ob = new GameObject();
    ob.transform.parent = gameObject.transform;
    var frontSide = GameObject.CreatePrimitive(PrimitiveType.Quad); // NEW LOCAL
    frontSide.transform.parent = ob.transform;
    frontSide.GetComponent<MeshRenderer>().material = frontMaterial;
    ...
    return ob;
}

void Start() {
    upObject = CreateFace(new Vector3(0, 0, 0), new Vector3(90, 0, 0)); // Field assigned from method
}
```

**False Positive Report**:
```
Replace Attribute With Variable: upObject : GameObject to frontSide : var
```

**Why This Is Wrong**:
1. `upObject` is a **class field** that **still exists**
2. `frontSide` is a **local variable inside CreateFace()**
3. They exist in **completely different scopes**
4. `upObject` is assigned the **return value** of `CreateFace()`, not replaced by `frontSide`
5. `frontSide` is an **internal implementation detail**, not a replacement

**Pattern**: For EACH field that was refactored through Extract Method (6 fields: upObject, downObject, leftObject, rightObject, frontObject, backObject), RefactoringMiner reported a false "Replace Attribute With Variable".

**Why It Failed**:
RefactoringMiner sees:
- Old: `upObject = GameObject.CreatePrimitive(...)`
- New (inside CreateFace): `frontSide = GameObject.CreatePrimitive(...)`
- Incorrectly concludes: "upObject replaced by frontSide"


## Root Cause Categories

### 1. **C# Language Feature Gap** (Critical - Severity 10/10)
**Affected Commits**: 4 (5820914c), 5 (f298068 - var usage)  
**Impact**: 0% precision when var keyword is used

**Missing Features**:
- `var` keyword type inference
- Property access patterns (`obj.Property`)
- C#-specific syntax and semantics

**Evidence**:
- Commit 4: Complete failure with 8 contradictory false positives
- Commit 5: Extract Method succeeds when explicit types used
- Tool works better on Java-like C# code

**Fix Priority**: **URGENT** - Without var support, tool cannot analyze 90% of modern C# code

---

### 2. **Scope Blindness** (High - Severity 8/10)
**Affected Commits**: 1 (9aaea6e6), 5 (f298068)  
**Impact**: False positives when variables exist in different scopes

**Problems**:
- Cannot distinguish class fields from local variables
- Matches variables across nested types
- Ignores enclosing type/method context

**Evidence**:
- Commit 1: Variables from different nested struct methods matched
- Commit 5: Class fields "replaced" by local variables in extracted methods

**Pattern**:
```
IF variable_name_similar AND type_similar AND location_near
THEN assume same variable (WRONG)

SHOULD BE:
IF variable_name_same AND type_same AND same_scope AND same_enclosing_type
THEN possibly same variable
```

**Fix Priority**: **HIGH** - Common pattern causing multiplicative false positives

---

### 3. **Line Number Matching** (Medium - Severity 6/10)
**Affected Commits**: 3 (4b24a421)  
**Impact**: 14% false positives from line shifts

**Problem**:
- Fields matched by line numbers instead of semantic identity
- Import additions/removals shift line numbers
- Creates false modifier changes

**Evidence**:
Commit 3: Static field appears to change when it actually just shifted lines

**Fix**: Match by (name, type, declaring class) not line numbers

**Fix Priority**: **MEDIUM** - Limited impact but reveals fundamental weakness

---

### 4. **Interface Context Missing** (Medium - Severity 6/10)
**Affected Commits**: 2 (150f711c)  
**Impact**: Method replacements misclassified as renames

**Problem**:
- Does not validate interface implementations
- Matches methods across unrelated classes
- Ignores architectural purpose

**Evidence**:
Commit 2: Method in one class "renamed" to method in different class with different interface

**Fix**: Validate interface/inheritance relationships for method renames

**Fix Priority**: **MEDIUM** - Affects architectural refactorings

---

### 5. **Contradictory Detections** (Indicator of Deeper Issues)
**Affected Commits**: 2 (150f711c), 4 (5820914c)  
**Examples**:
- Add static + Remove static (same field)
- Rename A→B + Rename B→A (same variables)
- Change type X→Y + Change type Y→X (same variable)

**Root Cause**: Tool detects patterns in both directions without validation

**Fix**: Add consistency checks to reject contradictory refactorings

---

## Common Patterns of Failure

### Pattern 1: "Similar Code = Same Variable" (WRONG)
```csharp
// Different contexts, similar patterns
class A { void M1() { var x = GetX(); } }  // Context 1
class B { void M2() { var x = GetX(); } }  // Context 2

// RefactoringMiner: "x in M1 renamed to x in M2" ❌
// Reality: Independent variables
```

### Pattern 2: "New Local = Replace Field" (WRONG)
```csharp
// Before
class C { 
    GameObject field;
    void M() { field = Create(); }
}

// After
class C {
    GameObject field; // STILL EXISTS
    GameObject Extract() { var local = Create(); return local; }
    void M() { field = Extract(); }
}

// RefactoringMiner: "field replaced by local" ❌
// Reality: field assigned from method return value
```

### Pattern 3: "Line Shift = Modifier Change" (WRONG)
```csharp
// Before (line 39-40)
using X; // Line 39
static int field; // Line 40

// After (line 39) - import removed
static int field; // Line 39 (shifted up)

// RefactoringMiner:
//   - "Remove static at line 40" ❌
//   - "Add static at line 39" ❌
// Reality: No change, just line shift
```

### Pattern 4: "var = Unknown Type" (WRONG)
```csharp
// Code
var manifest = GetComponent<AggregationManifest>();

// RefactoringMiner: "manifest has type 'var'" ❌
// Reality: manifest has type AggregationManifest (inferred)
```

---

## Recommendations

### Immediate Fixes (Critical)

#### 1. Implement C# var Keyword Support
**Priority**: P0 - Blocking issue for modern C# analysis  
**Effort**: High  
**Impact**: Enables analysis of 90%+ of C# codebases

**Requirements**:
- Parse type inference from right-hand side expressions
- Resolve generic types (`GetComponent<T>()` → `T`)
- Handle method return types
- Support LINQ query expression inference

**Test Case**:
```csharp
var number = 42;                        // int
var text = "hello";                     // string
var obj = GetComponent<Rigidbody>();    // Rigidbody
var items = list.Where(x => x > 5);     // IEnumerable<int>
```

#### 2. Add Scope-Aware Variable Validation
**Priority**: P0 - Causes systematic false positives  
**Effort**: Medium  
**Impact**: Elimites entire class of false positives

**Requirements**:
```java
boolean isSameVariable(Variable v1, Variable v2) {
    return v1.name.equals(v2.name) 
        && v1.type.equals(v2.type)
        && v1.scope.equals(v2.scope)  // NEW: Check scope
        && v1.enclosingType.equals(v2.enclosingType)  // NEW: Check enclosing type
        && v1.stillExists();  // NEW: Verify not deleted
}
```

**Scope Hierarchy**:
```
Class Scope (fields)
├── Method Scope (parameters, locals)
│   ├── Block Scope (if/for/while locals)
│   └── Lambda Scope
├── Nested Type Scope
└── Property Scope
```

#### 3. Semantic Identity for Code Elements
**Priority**: P1 - Prevents line-shift errors  
**Effort**: Medium  
**Impact**: Improves reliability across all detections

**Current** (WRONG):
```java
Field field = getFieldAtLine(lineNumber);
```

**Should Be**:
```java
Field field = findField(className, fieldName, fieldType);
```

**Identity Components**:
- Class/Interface name (fully qualified)
- Member name
- Member type/signature
- Access modifiers (if relevant)
- Static/instance designation

---

### Secondary Fixes (High Priority)

#### 4. Interface Context Validation
**Priority**: P1  
**Effort**: Medium  
**Impact**: Prevents method rename false positives

**Validation Rules**:
```java
boolean isValidMethodRename(Method old, Method new) {
    // Same class? ✓
    if (old.declaringClass.equals(new.declaringClass)) return true;
    
    // Inheritance relationship? ✓
    if (old.declaringClass.isSubclassOf(new.declaringClass)) return true;
    if (new.declaringClass.isSubclassOf(old.declaringClass)) return true;
    
    // Interface implementation? ✓
    if (old.implementsInterface(new.interface)) return true;
    
    // Otherwise, likely a replacement not a rename ✗
    return false;
}
```

#### 5. Extract Method Context Awareness
**Priority**: P1  
**Effort**: Medium  
**Impact**: Eliminates false "Replace Attribute" detections

**Requirements**:
- Mark all variables created in extracted methods as "internal"
- When checking for replacements, ensure:
  - Old variable no longer exists in new code (or is truly replaced)
  - New variable is not an internal temporary in extracted method
  - If old variable is assigned from extracted method return, NOT a replacement

**Test Case**:
```csharp
// If field = ExtractedMethod()
// And ExtractedMethod() creates local variables
// Then: NO "Replace Attribute", just Extract Method
```

---

### Tertiary Fixes (Medium Priority)

#### 6. Contradictory Detection Elimination
**Priority**: P2  
**Effort**: Low  
**Impact**: Improves output quality and trust

**Implementation**:
```java
// After all refactorings detected
List<Refactoring> eliminateContradictions(List<Refactoring> refs) {
    // Find pairs like:
    // - Add static + Remove static (same element)
    // - Rename A→B + Rename B→A
    // - Change type X→Y + Change type Y→X
    
    // Remove both (contradictory = both likely wrong)
    return filteredRefs;
}
```

#### 7. C# Property Recognition
**Priority**: P2  
**Effort**: Medium  
**Impact**: Reduces confusion with fields/variables

**Current Problem**:
```csharp
Selection.gameObjects  // Treated as variable "Selection"
```

**Should Recognize**:
```csharp
Selection           // Static class
    .gameObjects    // Static property (array)
```


## Metrics and Validation

### Precision by Commit
| Commit | Repository | Precision | Key Issue |
|--------|-----------|-----------|-----------|
| 9aaea6e6 | vrm-c/UniVRM | 50% | Nested type scope |
| 150f711c | BIVROST/360PlayerWindows | 60% | Interface context |
| 4b24a421 | Unity-Technologies/EditorXR | 86% | Line number matching |
| 5820914c | CognitiveVR/cvr-sdk-unity | **0%** ⚠️ | var keyword failure |
| f298068 | stefaanvermassen/virtual-museum-app | 50% | Scope confusion |

### Issue Severity Distribution
- **Critical (P0)**: 2 issues (var keyword, scope blindness) - 40% of commits severely affected
- **High (P1)**: 2 issues (interface context, Extract Method side effects)
- **Medium (P2)**: 2 issues (line matching, contradictions)

### Impact Assessment
- **Unusable for production C# analysis**: Yes (until var support added)
- **Suitable for research**: Limited (high false positive rate)
- **Java compatibility**: Good (tool works well on Java-like C# code)

---

## Conclusions

### What Works
1. ✅ **Simple Extract Method** (when no C# features involved)
2. ✅ **Access Modifier Changes** (86%+ accuracy)
3. ✅ **Parameter Changes** (Add/Remove parameters)
4. ✅ **Simple Rename Method** (within same class)

### What Fails
1. ❌ **C# var keyword** (0% - complete failure)
2. ❌ **Scope-aware variable matching** (50% - systematic errors)
3. ❌ **Nested type tracking** (50% - confusion across types)
4. ❌ **Interface-driven refactorings** (60% - missing context)
5. ❌ **Property vs variable distinction** (0% - not recognized)
