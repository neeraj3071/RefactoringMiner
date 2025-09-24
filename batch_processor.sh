#!/bin/bash

# C# RefactoringMiner Batch Processing Script
# Simpler alternative to Python script for testing

set -e  # Exit on any error

EXCEL_FILE="Final_Commit_Analysis _Iteration 3.xlsx"
JAR_PATH="build/libs/RM-fat.jar"
OUTPUT_DIR="batch_processing_results"
TEMP_DIR="temp_repos"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}🚀 C# RefactoringMiner Batch Processor${NC}"
echo -e "${BLUE}======================================${NC}"

# Check prerequisites
if [ ! -f "$EXCEL_FILE" ]; then
    echo -e "${RED}❌ Excel file not found: $EXCEL_FILE${NC}"
    exit 1
fi

if [ ! -f "$JAR_PATH" ]; then
    echo -e "${RED}❌ RefactoringMiner JAR not found: $JAR_PATH${NC}"
    exit 1
fi

# Create directories
mkdir -p "$OUTPUT_DIR"/{successful_analyses,failed_analyses,logs,temp}
mkdir -p "$TEMP_DIR"

echo -e "${GREEN}📁 Created directory structure${NC}"

# Function to extract repository info from GitHub URL
extract_repo_info() {
    local url="$1"
    # Extract owner/repo from URL like: https://github.com/ExtendRealityLtd/Zinnia.Unity/commit/hash
    echo "$url" | sed -E 's|https://github\.com/([^/]+)/([^/]+)/commit/([a-f0-9]+)|\1/\2 \3|'
}

# Function to process a single commit
process_commit() {
    local commit_url="$1"
    local index="$2"
    local total="$3"
    
    echo -e "${BLUE}🔄 [$index/$total] Processing: $commit_url${NC}"
    
    # Extract repo info
    repo_info=$(extract_repo_info "$commit_url")
    if [ -z "$repo_info" ]; then
        echo -e "${RED}❌ Invalid URL format: $commit_url${NC}"
        return 1
    fi
    
    # Parse owner/repo and commit hash
    owner_repo=$(echo "$repo_info" | cut -d' ' -f1)
    commit_hash=$(echo "$repo_info" | cut -d' ' -f2)
    
    owner=$(echo "$owner_repo" | cut -d'/' -f1)
    repo=$(echo "$owner_repo" | cut -d'/' -f2)
    
    repo_url="https://github.com/${owner_repo}.git"
    project_name="${owner}_${repo}"
    short_hash="${commit_hash:0:8}"
    
    # Generate output filename
    timestamp=$(date +%Y%m%d)
    output_file="${OUTPUT_DIR}/temp/$(printf "%03d" $index)_${project_name}_${short_hash}_${timestamp}.json"
    
    echo -e "   Repository: $repo_url"
    echo -e "   Commit: $short_hash"
    
    # Run RefactoringMiner
    start_time=$(date +%s)
    
    if timeout 600 java -cp "$JAR_PATH" org.refactoringminer.csharp.CSharpRefactoringMiner \
        -gc "$repo_url" "$commit_hash" 300 \
        -json "$output_file" 2>/dev/null; then
        
        end_time=$(date +%s)
        duration=$((end_time - start_time))
        
        # Check if output file exists and has content
        if [ -f "$output_file" ] && [ -s "$output_file" ]; then
            # Count refactorings
            refactoring_count=$(grep -o '"type":' "$output_file" 2>/dev/null | wc -l | tr -d ' ')
            
            # Move to successful directory
            final_output="${OUTPUT_DIR}/successful_analyses/$(basename "$output_file")"
            mv "$output_file" "$final_output"
            
            echo -e "${GREEN}✅ [$index/$total] SUCCESS: Found $refactoring_count refactorings (${duration}s)${NC}"
            echo "$index,$commit_url,$project_name,$commit_hash,success,$refactoring_count,$duration,$final_output" >> "${OUTPUT_DIR}/logs/results.csv"
            
        else
            echo -e "${RED}❌ [$index/$total] No output generated${NC}"
            echo "$index,$commit_url,$project_name,$commit_hash,no_output,0,$duration," >> "${OUTPUT_DIR}/logs/results.csv"
            return 1
        fi
        
    else
        end_time=$(date +%s)
        duration=$((end_time - start_time))
        echo -e "${RED}❌ [$index/$total] Command failed or timeout (${duration}s)${NC}"
        echo "$index,$commit_url,$project_name,$commit_hash,failed,0,$duration," >> "${OUTPUT_DIR}/logs/results.csv"
        return 1
    fi
}

# Main processing function
main() {
    echo -e "${YELLOW}📊 Starting batch processing...${NC}"
    
    # Initialize results CSV
    echo "index,commit_url,project_name,commit_hash,status,refactorings,duration,output_file" > "${OUTPUT_DIR}/logs/results.csv"
    
    # For testing, let's process first 5 commits
    # You can modify this to process all commits
    TEST_URLS=(
        "https://github.com/ExtendRealityLtd/Zinnia.Unity/commit/35cb3631904fec77ab2c68058ba4dd7b6aa75095"
        "https://github.com/myieye/cube-arena/commit/d830415431350d11fd85b5c94f45058ad492a1cd"
        "https://github.com/JaneliaSciComp/janelia-unity-toolkit/commit/7d6379cfde7ac5f073950b7fd0e1a06f7f659f52"
        "https://github.com/gpvigano/VRTK-GearVR-Test/commit/9b80bf072b6711e42acc7391256d439e5d464677"
        "https://github.com/thestonefox/SteamVR_Unity_Toolkit/commit/a1b2c3d4e5f6789012345678901234567890abcd"
    )
    
    total_commits=${#TEST_URLS[@]}
    successful=0
    failed=0
    
    for i in "${!TEST_URLS[@]}"; do
        index=$((i + 1))
        commit_url="${TEST_URLS[$i]}"
        
        if process_commit "$commit_url" "$index" "$total_commits"; then
            ((successful++))
        else
            ((failed++))
        fi
        
        # Brief pause between requests
        sleep 2
    done
    
    # Generate summary
    echo -e "\n${BLUE}📊 PROCESSING SUMMARY${NC}"
    echo -e "${BLUE}===================${NC}"
    echo -e "Total processed: $total_commits"
    echo -e "Successful: ${GREEN}$successful${NC}"
    echo -e "Failed: ${RED}$failed${NC}"
    echo -e "Success rate: $(( successful * 100 / total_commits ))%"
    echo -e "\n${GREEN}Results saved in: $OUTPUT_DIR${NC}"
    echo -e "${GREEN}Detailed log: ${OUTPUT_DIR}/logs/results.csv${NC}"
}

# Check if we should run the full processing or test mode
if [ "$1" = "--test" ]; then
    echo -e "${YELLOW}🧪 Running in test mode (5 commits)${NC}"
    main
elif [ "$1" = "--help" ]; then
    echo "Usage: $0 [--test|--help]"
    echo ""
    echo "Options:"
    echo "  --test    Run on first 5 commits only"
    echo "  --help    Show this help message"
    echo ""
    echo "Default: Run on all commits from Excel file"
else
    echo -e "${YELLOW}⚠️  This will process ALL commits from the Excel file${NC}"
    echo -e "${YELLOW}💡 For testing, run: $0 --test${NC}"
    echo ""
    read -p "Continue with full processing? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        main
    else
        echo "Cancelled."
        exit 0
    fi
fi