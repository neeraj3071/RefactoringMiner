#!/bin/bash

# C# RefactoringMiner CLI Helper Script
# Usage: ./csharp-refactoring-miner.sh <command> [options]

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Project paths
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MAIN_CLASS="org.refactoringminer.csharp.cli.CSharpRefactoringMinerCLI"
CLASSPATH="$SCRIPT_DIR/build/classes/java/main"

echo -e "${BLUE}🚀 C# RefactoringMiner Integration Tool${NC}"
echo -e "${BLUE}=====================================${NC}"

# Check if project is compiled
if [ ! -d "$CLASSPATH" ]; then
    echo -e "${YELLOW}⚠️  Project not compiled. Building...${NC}"
    cd "$SCRIPT_DIR"
    ./gradlew compileJava -x test
    if [ $? -ne 0 ]; then
        echo -e "${RED}❌ Build failed${NC}"
        exit 1
    fi
    echo -e "${GREEN}✅ Build successful${NC}"
    echo
fi

# Run the CLI tool
echo -e "${BLUE}Executing command:${NC} $*"
echo

java -cp "$CLASSPATH" "$MAIN_CLASS" "$@"
exit_code=$?

echo
if [ $exit_code -eq 0 ]; then
    echo -e "${GREEN}✅ Command completed successfully${NC}"
else
    echo -e "${RED}❌ Command failed with exit code $exit_code${NC}"
fi

exit $exit_code