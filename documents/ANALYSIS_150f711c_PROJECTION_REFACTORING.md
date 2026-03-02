# Analysis: Commit 150f711cf4070b56696920c3117b41d7b9828f76
## BIVROST/360PlayerWindows - Projection Refactoring

### Commit Information
- **Repository:** BIVROST/360PlayerWindows
- **Commit SHA:** 150f711cf4070b56696920c3117b41d7b9828f76
- **Commit Message:** "Refactored the way that projection is being changed in the runtime"
- **Date:** Wed May 9 12:00:14 2018 +0200
- **Author:** Krzysztof Bociurko
- **Files Changed:** 9 files (98 insertions, 94 deletions)

### Summary
This commit represents a **major architectural refactoring** that restructures how projection mode changes propagate through the VR player system. The refactoring moves from a **push model** (where callers directly invoke `UpdateSceneSettings`) to an **observer/callback pattern** (where `MediaDecoder` notifies content receivers via `SetProjection`).

---

## RefactoringMiner Detection Results

RefactoringMiner detected **10 refactorings** in this commit:

| # | Type | Location | Description | Status |
|---|------|----------|-------------|--------|
| 1 | Add Parameter | `ShellViewModel.SetProjection()` | Changed from `ProjectionMode?` to `ProjectionMode` | ✅ Correct |
| 2 | Inline Method | `Headset.UpdateSceneSettings → SetDefaultScene` | UpdateSceneSettings calls inlined | ⚠️ Partially Correct |
| 3 | Add static Modifier | `MediaDecoder.Action` | Event made static | ✅ Correct |
| 4 | **Rename Method** | `Scene.UpdateSceneSettings → SetProjection` | Method renamed | ❌ **INCORRECT** |
| 5 | Change Access Modifier | `Scene.projectionMode` | private → package | ✅ Correct |
| 6 | Remove Parameter | `Scene` constructor | Removed `ProjectionMode projection` | ✅ Correct |
| 7 | Remove Parameter | `Scene.UpdateSceneSettings` | Removed parameter(s) | ⚠️ Context-dependent |
| 8 | Rename Parameter | `Scene.SetProjection` | `projectionMode` → `projection` | ⚠️ Misleading |
| 9 | Change Method Access | Various methods | public → package | ✅ Correct |
| 10 | Remove static Modifier | `MediaDecoder.Action` | Event no longer static | ❌ Duplicate/Contradicts #3 |

---

## Detailed Analysis

### ❌ **Primary Misclassification: "Rename Method" (Scene.UpdateSceneSettings → SetProjection)**

**RefactoringMiner's Detection:**
```json
{
  "type": "Rename Method",
  "description": "Rename Method public UpdateSceneSettings(projectionMode ProjectionMode, stereoscopy VideoMode) : void renamed to public SetProjection(projection ProjectionMode) : void in class Bivrost.Bivrost360Player.Scene",
  "leftSideLocations": [{
    "filePath": "360Player/WPF/Scene.cs",
    "startLine": 104,
    "endLine": 118,
    "description": "original method declaration",
    "codeElement": "public UpdateSceneSettings(projectionMode ProjectionMode, stereoscopy VideoMode) : void",
    "codeElementType": "METHOD_DECLARATION"
  }],
  "rightSideLocations": [{
    "filePath": "360Player/WPF/Scene.cs",
    "startLine": 104,
    "endLine": 118,
    "description": "renamed method declaration",
    "codeElement": "public SetProjection(projection ProjectionMode) : void",
    "codeElementType": "METHOD_DECLARATION"
  }]
}
```

**Why This Is INCORRECT:**

This is **NOT** a simple rename. The refactoring involves:

1. **Interface migration:** The method moved from one interface to another
   - **Before:** `IUpdatableSceneSettings.UpdateSceneSettings(ProjectionMode, VideoMode)`
   - **After:** `IContentUpdatableFromMediaEngine.SetProjection(ProjectionMode)`

2. **Complete interface removal:** 
   - The `IUpdatableSceneSettings` interface was **completely deleted**
   - Scene no longer implements this interface

3. **Signature change:** Not just parameter rename, but **semantic change**
   - Removed `VideoMode stereoscopy` parameter (narrowed responsibility)
   - Different calling convention and purpose

4. **Implementation change:** The method bodies differ significantly
   - **Before (UpdateSceneSettings):** Used `ActionQueue` to enqueue geometry creation
   - **After (SetProjection):** Direct synchronous execution with `lock(localCritical)`

5. **Caller migration:** All call sites changed
   - **Before:** Various classes called `UpdateSceneSettings` directly
   - **After:** `MediaDecoder` invokes `SetProjection` via interface callback

**Actual Refactoring Pattern:**
This is closer to **"Replace Method with Interface Method"** or **"Pull Up Method"** combined with **"Change Method Signature"**. The old method was deleted and a new method with different semantics was created in a different interface.

---

### ⚠️ **Questionable Detection: "Inline Method" (Headset.UpdateSceneSettings)**

**RefactoringMiner's Detection:**
```json
{
  "type": "Inline Method",
  "description": "Inline Method public UpdateSceneSettings(projectionMode ProjectionMode, stereoscopy VideoMode) : void inlined to public SetDefaultScene() : void in class Bivrost.Bivrost360Player.Headset"
}
```

**Why This Is Partially Correct:**

**Evidence supporting "Inline":**
- The call to `UpdateSceneSettings(ProjectionMode.Sphere, VideoMode.Mono)` in `SetDefaultScene` was removed
- The logic (primitive geometry creation) was moved inline into `SetDefaultScene`

**Evidence against pure "Inline":**
- `UpdateSceneSettings` was not just inlined — it was **completely removed** from Headset class
- The inlined code is **different** — changed from ActionQueue pattern to direct execution
- A **new** method `SetProjection` was added to `IContentUpdatableFromMediaEngine` with similar (but not identical) logic

**Verdict:** This is more accurately described as **"Remove Method + Extract Implementation"** rather than pure inline. The method was deleted, and its core logic was reorganized across multiple locations.

---

### ⚠️ **Misleading Detection: "Rename Parameter" (projectionMode → projection)**

**RefactoringMiner's Detection:**
```json
{
  "type": "Rename Parameter",
  "description": "Rename Parameter projectionMode : ProjectionMode to projection : ProjectionMode in method public SetProjection(projection ProjectionMode) : void in class Bivrost.Bivrost360Player.Scene"
}
```

**Why This Is Misleading:**

This parameter is in the **new** `SetProjection` method, not a renamed version of the old method. Since `UpdateSceneSettings` was deleted (not renamed), this parameter didn't "rename" — it's a **new parameter** in a **new method**.

**Verdict:** False positive caused by the misclassification of the method as "renamed" (see #4 above).

---

### ❌ **Contradiction: Add static vs Remove static Modifier**

**Detections #3 and #10:**
- #3: "Add Attribute Modifier static" for `MediaDecoder.Action`
- #10: "Remove static Attribute Modifier" for `MediaDecoder.Action`

**Analysis:**
These two detections **contradict each other**. Looking at the diff:

```diff
+               public static event Action OnContentChanged;
```

The event was **added** as static (it didn't exist before, or was non-static and changed to static). One of these detections is incorrect, or they conflict.

**Verdict:** Tool detected conflicting refactorings for the same element. Requires investigation.

---

## Root Cause Analysis

### 1. **Semantic Ambiguity: Method Replacement vs Rename**

**Issue:** RefactoringMiner cannot distinguish between:
- **Simple Rename:** Method signature preserved, only name changes
- **Replace Method:** Old method deleted, new method with similar purpose created

**Why it happens:**
- Both operations result in "method A gone, method B present"
- AST-based similarity heuristics (parameter types, location, body similarity) can trigger false rename detection
- Threshold for "similar enough to be a rename" may be too permissive

**Evidence from this commit:**
- `UpdateSceneSettings(ProjectionMode, VideoMode)` signature has 50% parameter overlap with `SetProjection(ProjectionMode)`
- Both methods manipulate `primitive` geometry objects
- Line numbers overlap (before: 104-118, after: 104-118)
- Body contains similar code patterns (`primitive?.Dispose()`, `GraphicTools.CreateGeometry`)

**Proposed Solution:**
Add **interface/inheritance context** validation:
- Compare declaring types/interfaces
- If methods belong to different interfaces, weight similarity lower
- Check if old interface was deleted (strong signal of non-rename)

---

### 2. **Interface Deletion Not Detected as High-Level Refactoring**

**Issue:** RefactoringMiner didn't report removal of `IUpdatableSceneSettings` interface

**Impact:**
- Missing context that would invalidate "Rename Method" detection
- No signal that this is architectural change, not simple rename

**Proposed Enhancement:**
Detect and report interface-level refactorings:
- Remove Interface
- Merge Interfaces
- Split Interface

---

### 3. **Inline Method Detection Too Aggressive**

**Issue:** `UpdateSceneSettings` removal categorized as "inline" despite:
- Method completely deleted (not just call removed)
- New method with different signature added
- Logic reorganized, not literally inlined

**Proposed Solution:**
Strengthen inline detection criteria:
- Verify that **all** method body statements appear in caller
- Check that method is only called once (or few times)
- Penalize cases where method is replaced with an interface method

---

## Impact Assessment

### Precision Impact
- **True Positives:** 6/10 detections (Add Parameter, Constructor change, Access modifiers, Remove Parameter in constructor)
- **False Positives:** 2/10 (Rename Method, Rename Parameter)
- **Questionable:** 2/10 (Inline Method, static modifier contradictions)
- **Precision:** ~60% (severe degradation from method replacement misclassification)

### Severity
- **High:** "Rename Method" false positive could mislead developers attempting to understand architectural changes
- **Medium:** "Inline Method" provides partial information but misses broader restructuring
- **Low:** Parameter rename is technically wrong but low impact

### Reproducibility
- **Pattern:** Method replacement in different interface context
- **Likelihood:** Common in interface-driven refactorings and architectural changes
- **Risk:** High for C# projects with interface hierarchies

---

## Recommendations

### 1. Add Interface Context Validation
```java
// In MethodSignatureComparator or similar
boolean isSameDeclaringContext(MethodDeclaration before, MethodDeclaration after) {
    UMLOperation op1 = before.getOperation();
    UMLOperation op2 = after.getOperation();
    
    // Check if declaring types match
    if (!op1.getClassName().equals(op2.getClassName())) {
        return false;
    }
    
    // Check if both implement same interface method
    Set<String> interfaces1 = getImplementedInterfaces(op1);
    Set<String> interfaces2 = getImplementedInterfaces(op2);
    
    return !Collections.disjoint(interfaces1, interfaces2);
}
```

### 2. Detect Interface-Level Refactorings
Track interface additions/removals and use as context for method-level detections.

### 3. Improve Inline Method Detection
Require higher threshold for body similarity and stricter call-site analysis.

### 4. Detect "Replace Method" Pattern
Create new refactoring type for method replacement with signature change.

---

## Test Case for Validation

### Input
```csharp
// Before
public interface IUpdatableSceneSettings {
    void UpdateSceneSettings(ProjectionMode projectionMode, VideoMode stereoscopy);
}

public interface IContentUpdatable {
    void ReceiveTextures(Texture2D left, Texture2D right);
}

public class Scene : IUpdatableSceneSettings, IContentUpdatable {
    public void UpdateSceneSettings(ProjectionMode projectionMode, VideoMode stereoscopy) {
        actionQueue.Enqueue(() => {
            primitive?.Dispose();
            primitive = GraphicTools.CreateGeometry(projectionMode, device, false);
        });
    }
    
    public void ReceiveTextures(Texture2D left, Texture2D right) { /* ... */ }
}
```

```csharp
// After
public interface IContentUpdatable {
    void ReceiveTextures(Texture2D left, Texture2D right);
    void SetProjection(ProjectionMode projection);
}

public class Scene : IContentUpdatable {
    public void SetProjection(ProjectionMode projection) {
        lock(localCritical) {
            primitive?.Dispose();
            primitive = GraphicTools.CreateGeometry(projection, device, false);
        }
    }
    
    public void ReceiveTextures(Texture2D left, Texture2D right) { /* ... */ }
}
```

### Expected Detection
```
✓ Remove Interface: IUpdatableSceneSettings deleted
✓ Add Method: IContentUpdatable.SetProjection added
✓ Remove Method: Scene.UpdateSceneSettings removed
✓ Add Method: Scene.SetProjection added (implements IContentUpdatable.SetProjection)
✗ SHOULD NOT DETECT: Rename Method UpdateSceneSettings → SetProjection
```

---

## Conclusion

This commit demonstrates a **critical weakness** in RefactoringMiner's handling of **interface-driven architectural refactorings**. The tool struggles to distinguish between:
- Simple method renames (preserving semantics)
- Method replacements in different interface contexts (changing semantics)

The misclassification of `UpdateSceneSettings → SetProjection` as a "Rename Method" obscures the true nature of this refactoring: a **design pattern migration** from push-based state updates to an observer pattern with centralized projection management.

### Key Takeaway
**Method similarity heuristics must incorporate interface context** to avoid false positives in projects with rich interface hierarchies. This is especially critical for C# codebases where interface-based design is prevalent.
