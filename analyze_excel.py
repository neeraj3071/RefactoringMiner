#!/usr/bin/env python3
"""
Excel Analysis Script for C# Commit Processing
Analyzes the Excel file structure to understand the data format
"""

import pandas as pd
import sys
import os

def analyze_excel_structure(excel_file):
    """Analyze the structure of the Excel file"""
    try:
        print(f"📊 Analyzing Excel file: {excel_file}")
        
        # Read Excel file
        df = pd.read_excel(excel_file)
        
        print(f"\n📋 Basic Information:")
        print(f"   Total rows: {len(df)}")
        print(f"   Total columns: {len(df.columns)}")
        
        print(f"\n📝 Column Names:")
        for i, col in enumerate(df.columns, 1):
            print(f"   {i:2d}. {col}")
        
        print(f"\n🔍 First 5 rows preview:")
        print(df.head().to_string())
        
        print(f"\n📊 Data types:")
        print(df.dtypes.to_string())
        
        print(f"\n🔢 Non-null counts:")
        print(df.count().to_string())
        
        # Look for common column patterns
        potential_repo_cols = [col for col in df.columns if 'repo' in col.lower() or 'url' in col.lower() or 'project' in col.lower()]
        potential_commit_cols = [col for col in df.columns if 'commit' in col.lower() or 'sha' in col.lower() or 'hash' in col.lower()]
        
        if potential_repo_cols:
            print(f"\n🗂️ Potential repository columns: {potential_repo_cols}")
        if potential_commit_cols:
            print(f"🔗 Potential commit columns: {potential_commit_cols}")
        
        # Sample some data
        if potential_repo_cols and len(df) > 0:
            print(f"\n📌 Sample repository data:")
            sample_col = potential_repo_cols[0]
            print(f"   Column: {sample_col}")
            for i in range(min(3, len(df))):
                print(f"   Row {i+1}: {df[sample_col].iloc[i]}")
        
        if potential_commit_cols and len(df) > 0:
            print(f"\n📌 Sample commit data:")
            sample_col = potential_commit_cols[0]
            print(f"   Column: {sample_col}")
            for i in range(min(3, len(df))):
                print(f"   Row {i+1}: {df[sample_col].iloc[i]}")
        
        return df
        
    except Exception as e:
        print(f"❌ Error reading Excel file: {e}")
        return None

if __name__ == "__main__":
    excel_file = "Final_Commit_Analysis _Iteration 3.xlsx"
    
    if not os.path.exists(excel_file):
        print(f"❌ Excel file not found: {excel_file}")
        sys.exit(1)
    
    df = analyze_excel_structure(excel_file)
    
    if df is not None:
        print(f"\n✅ Analysis complete! Found {len(df)} commits to process.")
    else:
        print(f"❌ Failed to analyze Excel file.")
        sys.exit(1)