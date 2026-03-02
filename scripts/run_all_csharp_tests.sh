#!/bin/bash

################################################################################
# Script to run ASTFlowDebugger on all C# test files
# Processes all .cs files in tests/csharp-features/ and saves output
################################################################################

# Set working directory to project root
cd "/Users/neerajsaini/Documents/VR Research/RefactoringMiner"

# Create output directory if it doesn't exist
OUTPUT_DIR="output/csharp-features-test"
mkdir -p "$OUTPUT_DIR"

# Clear old outputs
echo "Cleaning old outputs from $OUTPUT_DIR..."
rm -f "$OUTPUT_DIR"/*.txt

# Find all C# test files (in tests/ root and subdirectories)
TEST_DIR="tests"
CS_FILES=$(find "$TEST_DIR" -name "*.cs" -type f | sort)

# Count total files
TOTAL=$(echo "$CS_FILES" | wc -l)
CURRENT=0

echo "=========================================="
echo "Running ASTFlowDebugger on $TOTAL C# test files"
echo "=========================================="
echo ""

# Process each file
for CS_FILE in $CS_FILES; do
    CURRENT=$((CURRENT + 1))
    
    # Extract filename without path and extension, preserve subdirectory structure
    # tests/csharp-features/04_auto_properties.cs -> csharp-features_04_auto_properties
    # tests/test_lambda.cs -> test_lambda
    RELATIVE_PATH="${CS_FILE#tests/}"  # Remove tests/ prefix
    FILENAME=$(echo "$RELATIVE_PATH" | sed 's/\.cs$//' | sed 's/\//_/g')
    OUTPUT_FILE="$OUTPUT_DIR/${FILENAME}_output.txt"
    
    echo "[$CURRENT/$TOTAL] Processing: $CS_FILE"
    echo "          Output: $OUTPUT_FILE"
    
    # Run ASTFlowDebugger and capture output
    java -cp "build/libs/RM-fat.jar" org.refactoringminer.csharp.debug.ASTFlowDebugger "$CS_FILE" > "$OUTPUT_FILE" 2>&1
    
    # Check if output was created
    if [ -f "$OUTPUT_FILE" ]; then
        LINES=$(wc -l < "$OUTPUT_FILE")
        echo "          Status: ✓ Complete ($LINES lines)"
    else
        echo "          Status: ✗ FAILED - No output generated"
    fi
    
    echo ""
done

echo "=========================================="
echo "Processing complete!"
echo "=========================================="
echo "Output directory: $OUTPUT_DIR"
echo "Total files processed: $TOTAL"
echo ""
echo "Summary of outputs:"
ls -lh "$OUTPUT_DIR" | tail -n +2 | awk '{print "  " $9 " - " $5}'
echo ""
echo "To view a specific output:"
echo "  cat $OUTPUT_DIR/<filename>_output.txt"
echo "=========================================="
