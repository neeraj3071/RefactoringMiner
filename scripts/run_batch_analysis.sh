#!/bin/bash
# Quick start script for batch analysis

echo "=================================================="
echo "  C# RefactoringMiner Batch Analysis"
echo "=================================================="
echo ""

# Check prerequisites
echo "Checking prerequisites..."

if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 17+"
    exit 1
fi

if ! command -v python3 &> /dev/null; then
    echo "❌ Python3 not found. Please install Python 3.7+"
    exit 1
fi

if [ ! -f "build/libs/RM-fat.jar" ]; then
    echo "❌ RM-fat.jar not found. Please run: ./gradlew shadowJar"
    exit 1
fi

if [ ! -f "Dataset_Commits.xlsx" ]; then
    echo "❌ Dataset_Commits.xlsx not found"
    exit 1
fi

echo "✓ All prerequisites met"
echo ""

# Install Python dependencies
echo "Installing Python dependencies..."
pip3 install pandas openpyxl -q

echo ""
echo "Starting batch analysis..."
echo "This will process ~1800 commits from Dataset_Commits.xlsx"
echo ""
echo "Output will be saved to: batch_results/"
echo "Progress log: batch_results/analysis_log.txt"
echo ""
read -p "Press Enter to start, or Ctrl+C to cancel..."

# Run the analysis
python3 batch_analyze_dataset.py

echo ""
echo "=================================================="
echo "  Analysis Complete!"
echo "=================================================="
echo ""
echo "Results: batch_results/"
echo "Failed: batch_results/failed/"
echo "Log: batch_results/analysis_log.txt"
echo ""
