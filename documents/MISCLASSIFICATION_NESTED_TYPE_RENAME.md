# Misclassification: False Variable Rename Detection Across Nested Types

**Date**: February 18, 2026  
**Commit**: [vrm-c/UniVRM@9aaea6e6](https://github.com/vrm-c/UniVRM/commit/9aaea6e6a1fe270842c05854714b7b24b0284097)  
**File**: `Assets/VRM10/Runtime/FastSpringBone/System/FastSpringBoneBufferCombiner.cs`  
**Severity**: High - False Positive  
**Language**: C#

---

## Issue Summary

RefactoringMiner incorrectly detected two "Rename Variable" refactorings where variables from **different nested struct methods** were falsely matched to a variable in another nested struct method. Both `collider` and `logic` variables (which were actually inlined in their respective methods) were incorrectly reported as being renamed to `spring` (which exists in a completely different method).

---

## Detected Refactorings (INCORRECT)

RefactoringMiner reported **4 refactorings**:

### 1. ❌ Rename Variable: `collider` → `spring`
- **Description**: "Rename Variable collider : var to spring : var in method public Execute(index int) : void from class FastSpringBoneBufferCombiner"
- **Left Side**: Line 261 (LoadCollidersJob.Execute)
- **Right Side**: Line 245 (LoadSpringsJob.Execute)
- **Status**: **FALSE POSITIVE** - Variables are in different nested struct methods

### 2. ✓ Inline Variable: `collider`
- **Description**: "Inline Variable collider : var in method public Execute(index int) : void from class FastSpringBoneBufferCombiner"
- **Left Side**: Line 261 
- **Right Side**: Line 263
- **Status**: **CORRECT**

### 3. ❌ Rename Variable: `logic` → `spring`
- **Description**: "Rename Variable logic : var to spring : var in method public Execute(index int) : void from class FastSpringBoneBufferCombiner"
- **Left Side**: Line 279 (LoadLogicsJob.Execute)
- **Right Side**: Line 245 (LoadSpringsJob.Execute)
- **Status**: **FALSE POSITIVE** - Variables are in different nested struct methods

### 4. ✓ Inline Variable: `logic`
- **Description**: "Inline Variable logic : var in method public Execute(index int) : void from class FastSpringBoneBufferCombiner"
- **Left Side**: Line 279
- **Right Side**: Line 279
- **Status**: **CORRECT**

---

## Actual Refactorings

### LoadSpringsJob.Execute (Line ~245)
```csharp
// BEFORE & AFTER - Variable spring already exists
var spring = SrcSprings[index];
spring.colliderSpan.startIndex += CollidersOffset;
spring.logicSpan.startIndex += LogicsOffset;
spring.transformIndexOffset = TransformOffset;  // NEW LINE ADDED
DestSprings[index] = spring;
```
**Actual Change**: Add statement to existing method

### LoadCollidersJob.Execute (Line ~260)
```csharp
// BEFORE:
var collider = SrcColliders[index];
DestColliders[index] = collider;

// AFTER:
DestColliders[index] = SrcColliders[index];
```
**Actual Change**: ✓ Inline Variable `collider`

### LoadLogicsJob.Execute (Line ~277)
```csharp
// BEFORE:
var logic = SrcLogics[index];
logic.transformIndexOffset = TransformOffset;  // This line removed
DestLogics[index] = logic;
DestJoints[index] = SrcJoints[index];

// AFTER:
DestLogics[index] = SrcLogics[index];
DestJoints[index] = SrcJoints[index];
```
**Actual Change**: ✓ Inline Variable `logic` + Remove dead code

---

## Root Cause Analysis

### The Core Problem: **Lack of Nested Type Scope Differentiation**

The three `Execute` methods exist in **different nested struct types**:

```csharp
public class FastSpringBoneBufferCombiner 
{
    struct LoadSpringsJob : IJobParallelFor { 
        public void Execute(int index) { 
            var spring = ...;  // Line 245
        }
    }
    
    struct LoadCollidersJob : IJobParallelFor { 
        public void Execute(int index) { 
            var collider = ...;  // Line 261 (removed)
        }
    }
    
    struct LoadLogicsJob : IJobParallelFor { 
        public void Execute(int index) { 
            var logic = ...;  // Line 279 (removed)
        }
    }
}
```

### Why the Detection Failed

#### 1. **Method Name Collision Without Proper Qualification**
- All three nested structs have the **identical method signature**: `public void Execute(int index)`
- The tool's description incorrectly attributes all to: `"from class FastSpringBoneBufferCombiner"`
- **Missing**: The nested type qualification (LoadSpringsJob, LoadCollidersJob, LoadLogicsJob)

#### 2. **Insufficient Scope Validation**
Location: `VariableReplacementAnalysis.java:1842-1843`

```java
if((variableDeclaration1.getScope().subsumes(mapping.getFragment1().getLocationInfo()) || 
    mapping.getFragment1().getVariableDeclarations().contains(variableDeclaration1)) &&
   (variableDeclaration2.getScope().subsumes(mapping.getFragment2().getLocationInfo()) || 
    mapping.getFragment2().getVariableDeclarations().contains(variableDeclaration2))) {
    actualReferences.add(mapping);
}
```

**Problem**: The scope check validates that references are *within* variable scopes, but **does not validate**:
- ❌ Variables are in the **same method**
- ❌ Variables are in the **same declaring container** (nested type)
- ❌ The `operationBefore` equals `operationAfter`

#### 3. **C# Nested Structs → Java Transformation Loss**
- C# nested structs: `FastSpringBoneBufferCombiner.LoadSpringsJob`
- Converted to Java AST by CPatMiner
- **The precise nested type qualification appears lost** during variable scope tracking

#### 4. **Line Number Proximity Heuristic**
After refactoring:
- `collider` (line 261) and `logic` (line 279) are removed → lines shift up
- `spring` appears at line 245 (earlier location)
- Tool logic:
  1. "Variable `collider` disappeared from line 261"
  2. "Variable `spring` exists at line 245"
  3. "Similar usage patterns (array index access, assignment)"
  4. ❌ **Incorrect conclusion**: "Renamed `collider` → `spring`"

#### 5. **Missing Method Container Equality Check**
The `RenameVariableRefactoring` class stores:
```java
private VariableDeclarationContainer operationBefore;
private VariableDeclarationContainer operationAfter;
```

But the detection logic at line 1847 **does not enforce**:
```java
if (operation1.equals(operation2)) { // ← MISSING CHECK
    // Only then consider it a rename within same method
}
```

---

## Proposed Fix

### Location: `VariableReplacementAnalysis.java:1847`

**Current Code**:
```java
RenameVariableRefactoring ref = new RenameVariableRefactoring(variableDeclaration1, variableDeclaration2, operation1, operation2, actualReferences, insideExtractedOrInlinedMethod);
if(!existsConflictingExtractVariableRefactoring(ref) && 
   !existsConflictingMergeVariableRefactoring(ref) && 
   !existsConflictingSplitVariableRefactoring(ref) && 
   !existsConflictingParameter(ref) &&
   variableDeclaration1.isVarargsParameter() == variableDeclaration2.isVarargsParameter() && 
   matchedEnhancedForLoopFormalParameter(variableDeclaration1, variableDeclaration2)) {
    variableRenames.add(ref);
    // ...
}
```

**Proposed Fix**:
```java
RenameVariableRefactoring ref = new RenameVariableRefactoring(variableDeclaration1, variableDeclaration2, operation1, operation2, actualReferences, insideExtractedOrInlinedMethod);
if(!existsConflictingExtractVariableRefactoring(ref) && 
   !existsConflictingMergeVariableRefactoring(ref) && 
   !existsConflictingSplitVariableRefactoring(ref) && 
   !existsConflictingParameter(ref) &&
   operation1.equals(operation2) &&  // ← ADD THIS: Same method container check
   variableDeclaration1.isVarargsParameter() == variableDeclaration2.isVarargsParameter() && 
   matchedEnhancedForLoopFormalParameter(variableDeclaration1, variableDeclaration2)) {
    variableRenames.add(ref);
    // ...
}
```

### Rationale
A variable can only be **renamed** if both the original and renamed declarations exist in the **same method/container**. Variables in different methods (even with the same signature in different nested types) cannot be renamed to each other—they are entirely separate entities.

---

## Test Case

### Input
- **Repository**: https://github.com/vrm-c/UniVRM.git
- **Commit**: 9aaea6e6a1fe270842c05854714b7b24b0284097
- **File**: Assets/VRM10/Runtime/FastSpringBone/System/FastSpringBoneBufferCombiner.cs

### Expected Output
```json
{
  "refactorings": [
    {
      "type": "Inline Variable",
      "description": "Inline Variable collider : var in method public Execute(index int) : void from class LoadCollidersJob"
    },
    {
      "type": "Inline Variable",
      "description": "Inline Variable logic : var in method public Execute(index int) : void from class LoadLogicsJob"
    }
  ]
}
```

### Actual Output (Incorrect)
```json
{
  "refactorings": [
    {
      "type": "Rename Variable",
      "description": "Rename Variable collider : var to spring : var ..."
    },
    {
      "type": "Inline Variable",
      "description": "Inline Variable collider : var ..."
    },
    {
      "type": "Rename Variable", 
      "description": "Rename Variable logic : var to spring : var ..."
    },
    {
      "type": "Inline Variable",
      "description": "Inline Variable logic : var ..."
    }
  ]
}
```

---

## Impact Assessment

### Severity: **High**
- Creates **false positive** refactorings
- Can mislead refactoring analysis studies
- Affects precision metrics for C# projects with nested types

### Affected Scenarios
1. **C# nested structs/classes** with identically-named methods
2. **Java nested/inner classes** with method name collisions
3. Any code with **multiple scopes** containing variables with similar usage patterns

### Precision Impact
For this specific commit:
- **Detected**: 4 refactorings
- **Actual**: 2 refactorings
- **False Positives**: 2 (50% false positive rate)

---

## Related Issues

### Similar Potential Problems
1. **Cross-class variable matching** in inheritance hierarchies
2. **Anonymous class variable confusion** with outer scope variables
3. **Lambda parameter matching** with method parameters of same name

### Prevention Strategy
All variable refactoring detection should enforce:
```java
// Rule: Variables can only be renamed within the same declaration container
assert operation1.equals(operation2) : "Cannot rename variables across different methods/containers";
```

---

## References

- Source File: `src/main/java/gr/uom/java/xmi/decomposition/VariableReplacementAnalysis.java`
- Method: `findConsistentVariableRenames()` (Line 1756-1891)
- Validation Location: Line 1847
- Related Class: `gr.uom.java.xmi.diff.RenameVariableRefactoring`

---

## Status

- [x] Issue Identified
- [x] Root Cause Analyzed
- [x] Fix Proposed
- [ ] Fix Implemented
- [ ] Test Case Added
- [ ] Verified on Sample Dataset
