#!/bin/bash

# C# RefactoringMiner Convenience Script
# Usage: ./run_csharp_refactoring_miner.sh <repo-path> <commit-id> [output-filename]

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RESULTS_DIR="$SCRIPT_DIR/results"

# Create results directory if it doesn't exist
mkdir -p "$RESULTS_DIR"

if [ $# -lt 2 ]; then
    echo "Usage: $0 <repo-path> <commit-id> [output-filename]"
    echo ""
    echo "Examples:"
    echo "  $0 /path/to/repo abc123"
    echo "  $0 /path/to/repo abc123 my_analysis.json"
    echo ""
    echo "Results will be saved to: $RESULTS_DIR/"
    exit 1
fi

REPO_PATH="$1"
COMMIT_ID="$2"
OUTPUT_FILE="${3:-analysis_$(date +%Y%m%d_%H%M%S).json}"

# Ensure output file has .json extension
if [[ "$OUTPUT_FILE" != *.json ]]; then
    OUTPUT_FILE="${OUTPUT_FILE}.json"
fi

OUTPUT_PATH="$RESULTS_DIR/$OUTPUT_FILE"

echo "=== C# RefactoringMiner ==="
echo "Repository: $REPO_PATH"
echo "Commit: $COMMIT_ID"
echo "Output: $OUTPUT_PATH"
echo ""

# Run the C# RefactoringMiner
java -cp "$SCRIPT_DIR/build/libs/RM-fat.jar" \
     org.refactoringminer.csharp.CSharpRefactoringMiner \
     -c "$REPO_PATH" "$COMMIT_ID" \
     -json "$OUTPUT_PATH"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Analysis complete! Results saved to:"
    echo "   $OUTPUT_PATH"
    echo ""
    echo "📊 Quick stats:"
    wc -l "$OUTPUT_PATH" | awk '{print "   Lines: " $1}'
    du -h "$OUTPUT_PATH" | awk '{print "   Size: " $1}'
else
    echo ""
    echo "❌ Analysis failed!"
    exit 1
fi