# Issue Report: Commit 4b24a4211bbe7cdeefe3eb9208e5f9e14cb726d2

## Quick Summary
**Repository:** Unity-Technologies/EditorXR  
**Commit:** 4b24a4211bbe7cdeefe3eb9208e5f9e14cb726d2  
**Issue Type:** False Positive - Line Number Shift Misinterpreted as Modifier Changes  

---

## Detection Accuracy

| Total Detections | True Positives | False Positives | Precision |
|------------------|----------------|-----------------|-----------|
| 14               | 12             | 2               | **86%**   |

---

## False Positives Identified

### ❌ Contradictory Static Modifier Detections in MeshData.cs

**Detection #1:** "Add Attribute Modifier static" to Dictionary field at line 40→38  
**Detection #2:** "Remove Attribute Modifier static" from Dictionary field at line 39→39  

**Reality:** Both Dictionary fields remained **completely unchanged**
- `s_MeshDataDictionary` (line 38): Always had `private static readonly`
- `m_TriBuckets` (line 39): Always had `private readonly` (never had `static`)

**What Actually Happened:**
- Only change to MeshData.cs: Removed `using UnityEngine.VR.Modules;` import at line 11
- This caused all subsequent lines to shift up by 1
- Tool mismatched fields based on line number proximity
- Incorrectly inferred modifier changes from line shifts

---

## How to Identify This Error

**Red Flag:** Contradictory operations (both "Add" and "Remove" static for same class)

**Verification:**
```bash
git show 4b24a4211bbe7cdeefe3eb9208e5f9e14cb726d2 -- Scripts/Data/MeshData.cs
# Shows only import removal, no field modifier changes
```

---

## Root Cause

### Line-Based Field Matching

RefactoringMiner matched fields based on **line number proximity** rather than **semantic identity** (name + type):

**Incorrect Matching:**
```
Line 40 (before): s_MeshDataDictionary (static)  →  Line 39 (after): m_TriBuckets (non-static)
Line 39 (before): m_TriBuckets (non-static)      →  Line 38 (after): s_MeshDataDictionary (static)
```

**Correct Matching (by name):**
```
s_MeshDataDictionary (line 40 before)  →  s_MeshDataDictionary (line 38 after)  [No changes]
m_TriBuckets (line 39 before)          →  m_TriBuckets (line 39 after)          [No changes]
```

### Contributing Factors

1. **Multiple similar fields:** Both are `Dictionary<K, V> : readonly` types
2. **Generic type ambiguity:** Tool may not fully distinguish `Dictionary<Mesh, MeshData>` from `Dictionary<IntVector3, List<IntVector3>>`
3. **Benign triggers:** Import/comment removal causing line shifts
4. **No validation:** No check for contradictory refactorings

---

## Impact Classification

### Severity: **Low to Medium**
- Easy to detect (contradictory operations)
- Unlikely to cause serious misinterpretation
- But undermines trust in detection accuracy

### Pattern: **Line-Shift False Positives**
- Triggered by any code that shifts line numbers:
  - Import/namespace cleanup
  - Comment removal
  - Code formatting changes
- Common in "cleanup" commits
- High reproducibility risk

### Affected Scenarios
- ✅ Automated refactoring studies using line-based analysis
- ✅ Code history archaeology
- ✅ Precision benchmarking for refactoring detection

---

## Correct Detections

**12 out of 14 detections were correct:**
- ✅ Access modifier changes (package → private) across multiple classes
- ✅ Method access change (internal → private)
- ✅ **Rename Method:** `OnIntersection` → `OnIntersectionEnter` (proper detection)

**Note:** This demonstrates RefactoringMiner performs well on clear refactorings when unaffected by line shifts.

---

## Proposed Fix

**Primary Solution:** Match fields by **semantic identity**, not line proximity

```java
// Use name + full type signature as primary key
FieldDeclaration findMatchingField(FieldDeclaration oldField, List<FieldDeclaration> newFields) {
    // First: Exact match by name and type
    for (FieldDeclaration newField : newFields) {
        if (oldField.getName().equals(newField.getName()) &&
            oldField.getTypeSignature().equals(newField.getTypeSignature())) {
            return newField;
        }
    }
    
    // Only use line proximity for actual renames (name differs)
    // ... (with much higher confidence threshold)
}
```

**Secondary Solution:** Detect contradictory refactorings

```java
validateRefactoringSet(refactorings) {
    if (hasContradiction(refactorings)) {
        logWarning("Contradictory modifier changes detected - likely false positive");
        markAsUncertain();
    }
}
```

---

## Test Case

### Scenario: Import Removal Causing Line Shift

**Before:**
```csharp
using System.Collections.Generic;
using UnityEngine.VR.Modules;  // Will be removed

public class MeshData
{
    private static readonly Dictionary<Mesh, MeshData> s_Dict = new Dictionary<Mesh, MeshData>();  // Line 10
    private readonly Dictionary<int, int> m_LocalDict = new Dictionary<int, int>();                 // Line 11
}
```

**After:**
```csharp
using System.Collections.Generic;
// Import removed

public class MeshData
{
    private static readonly Dictionary<Mesh, MeshData> s_Dict = new Dictionary<Mesh, MeshData>();  // Line 9 (shifted)
    private readonly Dictionary<int, int> m_LocalDict = new Dictionary<int, int>();                 // Line 10 (shifted)
}
```

**Expected Behavior:**
```
✓ SHOULD NOT DETECT: Any modifier changes
✓ SHOULD DETECT: Nothing (no semantic changes)
```

**Current Behavior:**
```
✗ DETECTS: "Add static" to one field
✗ DETECTS: "Remove static" from another field
❌ FALSE POSITIVES
```

---

## Related Issues

This is the **third pattern** identified in the dataset:

| Pattern | Commit | Cause |
|---------|--------|-------|
| Nested type scope | [9aaea6e6](./MISCLASSIFICATION_NESTED_TYPE_RENAME.md) | Missing operation equality check |
| Interface context | [150f711c](./ANALYSIS_150f711c_PROJECTION_REFACTORING.md) | Missing interface validation |
| **Line-shift matching** | **4b24a421** | **Weak field identity checking** |

**Common Theme:** All three reveal **insufficient validation** before declaring refactorings detected.

---

## Related Documentation
**Full Analysis:** [ANALYSIS_4b24a421_LINE_NUMBER_FALSE_POSITIVES.md](./ANALYSIS_4b24a421_LINE_NUMBER_FALSE_POSITIVES.md)

---

## Quick Verification Command

```bash
# Show only changed lines (no field modifiers changed)
git show 4b24a4211bbe7cdeefe3eb9208e5f9e14cb726d2 -- Scripts/Data/MeshData.cs | grep "^[+-]"
# Output: Only shows import line removal, no field changes
```
