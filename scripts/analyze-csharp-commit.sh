#!/bin/bash
# Analyze C# commits for refactorings using RefactoringMiner + C# extension
#
# Usage: ./analyze-csharp-commit.sh <repo-path> <commit-sha> [output.json]

set -e

REPO_PATH="$1"
COMMIT_SHA="$2"
OUTPUT_FILE="${3:-csharp-refactorings.json}"

if [ -z "$REPO_PATH" ] || [ -z "$COMMIT_SHA" ]; then
    echo "Usage: $0 <repo-path> <commit-sha> [output.json]"
    echo ""
    echo "Example:"
    echo "  $0 temp-zinnia 35cb3631904fec77ab2c68058ba4dd7b6aa75095 zinnia-results.json"
    exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
TEMP_DIR="/tmp/rm-csharp-$$"

echo "╔══════════════════════════════════════════════════════════════════╗"
echo "║  RefactoringMiner C# Commit Analyzer                            ║"
echo "╚══════════════════════════════════════════════════════════════════╝"
echo ""
echo "📁 Repository: $REPO_PATH"
echo "🔍 Commit: $COMMIT_SHA"
echo "💾 Output: $OUTPUT_FILE"
echo ""

# Create temp directory
mkdir -p "$TEMP_DIR"

# Change to repo directory
cd "$REPO_PATH"

# Get list of C# files changed
echo "⚙️  Finding C# files in commit..."
CS_FILES=$(git show --name-only --pretty="" "$COMMIT_SHA" | grep '\.cs$' || true)

if [ -z "$CS_FILES" ]; then
    echo "❌ No C# files found in commit $COMMIT_SHA"
    rm -rf "$TEMP_DIR"
    exit 1
fi

FILE_COUNT=$(echo "$CS_FILES" | wc -l | tr -d ' ')
echo "✓ Found $FILE_COUNT C# file(s)"
echo ""

# Extract before/after versions
echo "📥 Extracting file versions..."
INDEX=0
while IFS= read -r cs_file; do
    INDEX=$((INDEX + 1))
    BASENAME=$(basename "$cs_file")
    echo "  [$INDEX/$FILE_COUNT] $BASENAME"
    
    # Extract before version (parent commit)
    git show "${COMMIT_SHA}~1:$cs_file" > "$TEMP_DIR/before_${INDEX}_${BASENAME}" 2>/dev/null || echo "// New file" > "$TEMP_DIR/before_${INDEX}_${BASENAME}"
    
    # Extract after version
    git show "${COMMIT_SHA}:$cs_file" > "$TEMP_DIR/after_${INDEX}_${BASENAME}" 2>/dev/null || echo "// Deleted file" > "$TEMP_DIR/after_${INDEX}_${BASENAME}"
done <<< "$CS_FILES"

echo ""
echo "🔄 Transforming C# to Java AST..."
cd "$PROJECT_ROOT"

# Transform all C# files
for cs_file in "$TEMP_DIR"/*.cs; do
    [ -f "$cs_file" ] || continue
    BASENAME=$(basename "$cs_file")
    echo "  • $BASENAME"
    
    # Run C# processor (suppress verbose output)
    java -cp build/libs/RM-fat.jar org.refactoringminer.csharp.SrcMLBasedCSharpProcessor "$cs_file" > /dev/null 2>&1 || echo "    ⚠️  Warning: Failed to transform $BASENAME"
done

echo ""
echo "✅ C# files successfully transformed!"
echo ""
echo "📊 Analysis Summary:"
echo "  • C# files processed: $FILE_COUNT"
echo "  • Before/after pairs: $FILE_COUNT"
echo ""
echo "💡 Next steps:"
echo "  1. Use TestZinniaRefactoring.java for detailed AST analysis"
echo "  2. Integrate C# processor into main RefactoringMiner pipeline"
echo "  3. Run batch analysis on full dataset"
echo ""

# Save metadata
cat > "$OUTPUT_FILE" << EOF
{
  "repository": "$(git -C "$REPO_PATH" config --get remote.origin.url || echo "local")",
  "commit": "$COMMIT_SHA",
  "message": "$(git -C "$REPO_PATH" log --format=%s -n 1 "$COMMIT_SHA")",
  "author": "$(git -C "$REPO_PATH" log --format='%an <%ae>' -n 1 "$COMMIT_SHA")",
  "date": "$(git -C "$REPO_PATH" log --format=%ai -n 1 "$COMMIT_SHA")",
  "csharp_files_processed": $FILE_COUNT,
  "files": [
$(echo "$CS_FILES" | sed 's/^/    "/' | sed 's/$/"/' | paste -sd ',' -)
  ],
  "note": "C# files were transformed to Java AST. Use TestZinniaRefactoring.java for refactoring detection.",
  "temp_directory": "$TEMP_DIR"
}
EOF

echo "📝 Metadata saved to: $OUTPUT_FILE"
echo ""
echo "🎉 Analysis complete!"
echo ""
echo "Extracted files available in: $TEMP_DIR"

# Cleanup option
read -p "Delete temporary files? [y/N] " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    rm -rf "$TEMP_DIR"
    echo "✓ Cleaned up temporary files"
fi
