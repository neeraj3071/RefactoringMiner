#!/usr/bin/env python3
"""
Match tool-detected refactorings with manually labeled refactorings for General SE commits only.
"""

import pandas as pd
import json
import os
from pathlib import Path
from collections import defaultdict
import re

# ===== MAPPING DEFINITIONS =====

# Map manual subcategories to tool refactoring types
SUBCATEGORY_TO_REFACTORING_TYPES = {
    # Extract operations
    "Extract Method": ["Extract Method", "Extract And Move Method"],
    "Extract Class": ["Extract Class", "Extract Superclass", "Extract Interface", "Extract Subclass"],
    "Extract Variable": ["Extract Variable", "Extract Attribute"],
    
    # Move operations
    "Move Method": ["Move Method", "Move And Rename Method", "Move And Inline Method"],
    "Move Class": ["Move Class", "Move And Rename Class", "Move Source Folder"],
    "Move Attribute": ["Move Attribute", "Move And Rename Attribute"],
    
    # Rename operations
    "Rename Method": ["Rename Method", "Rename Method"],
    "Rename Class": ["Rename Class"],
    "Rename Variable": ["Rename Variable", "Rename Parameter", "Rename Attribute"],
    "Rename Package": ["Move Source Folder", "Move And Rename Class"],
    
    # Inline operations
    "Inline Method": ["Inline Method"],
    "Inline Variable": ["Inline Variable"],
    
    # Change operations
    "Change Method Signature": ["Change Parameter Type", "Add Parameter", "Remove Parameter", 
                                "Reorder Parameters", "Change Return Type"],
    "Change Variable Type": ["Change Variable Type", "Change Attribute Type", "Change Parameter Type"],
    "Change Attribute Access Modifier": ["Change Attribute Access Modifier"],
    "Change Method Access Modifier": ["Change Method Access Modifier"],
    
    # Pull/Push operations
    "Pull Up Method": ["Pull Up Method"],
    "Pull Up Attribute": ["Pull Up Attribute"],
    "Push Down Method": ["Push Down Method"],
    "Push Down Attribute": ["Push Down Attribute"],
    
    # Replace operations
    "Replace Method": ["Replace Method", "Replace Attribute"],
    "Replace Magic Number": ["Extract Variable", "Extract Attribute"],  # Often implemented via extract
    
    # Data structure refactorings
    "Data Structure Refactoring": ["Change Attribute Type", "Change Variable Type", "Change Return Type"],
    
    # Encapsulation
    "Encapsulation": ["Change Attribute Access Modifier", "Extract Method", "Encapsulate Attribute"],
    
    # Code organization
    "Removing Redundancy": ["Inline Method", "Inline Variable", "Remove Parameter"],
    "Simplify Conditional": ["Extract Method", "Inline Method"],
    "Replace Conditional with Polymorphism": ["Extract Interface", "Extract Superclass"],
    
    # General refactorings
    "General Refactoring": [],  # Match any refactoring
}

# Normalize refactoring type names
def normalize_refactoring_type(ref_type):
    """Normalize refactoring type for comparison"""
    return ref_type.lower().strip().replace("_", " ").replace("-", " ")

# ===== LOAD DATA =====

def load_excel_data(excel_path):
    """Load and filter General SE commits from Excel"""
    df = pd.read_excel(excel_path, sheet_name='Sheet1')
    
    # Filter for General SE only
    df = df[df['Refactoring Category'] == 'General Software Engineering Refactoring'].copy()
    
    # Extract commit info
    df['owner'] = df['Commit URL'].str.extract(r'github\.com/([^/]+)/')[0]
    df['repo'] = df['Commit URL'].str.extract(r'github\.com/[^/]+/([^/]+)/')[0]
    df['commit_hash'] = df['Commit Hash']
    
    print(f"Loaded {len(df)} General SE commits from Excel")
    print(f"Sub-categories: {df['Sub Category'].nunique()} unique")
    
    return df

def load_detected_refactorings(results_dir):
    """Load all detected refactorings from JSON files"""
    refactorings_by_commit = {}
    
    if not os.path.exists(results_dir):
        print(f"Results directory not found: {results_dir}")
        return refactorings_by_commit
    
    json_files = list(Path(results_dir).glob('*.json'))
    print(f"Found {len(json_files)} JSON result files")
    
    for json_file in json_files:
        try:
            with open(json_file, 'r') as f:
                data = json.load(f)
                
            for commit_data in data.get('commits', []):
                commit_hash = commit_data.get('sha1', '')
                refactorings = commit_data.get('refactorings', [])
                
                if commit_hash:
                    refactorings_by_commit[commit_hash] = refactorings
                    
        except Exception as e:
            print(f"Error loading {json_file}: {e}")
    
    print(f"Loaded refactorings for {len(refactorings_by_commit)} commits")
    return refactorings_by_commit

# ===== MATCHING LOGIC =====

def match_refactoring(manual_subcategory, detected_refactorings):
    """
    Match manual subcategory with detected refactorings
    
    Returns: (matched, all_detected_types, match_details)
    """
    if not detected_refactorings:
        return False, [], "No refactorings detected"
    
    # Get expected refactoring types for this subcategory
    expected_types = SUBCATEGORY_TO_REFACTORING_TYPES.get(manual_subcategory, [])
    
    # Extract all detected types
    detected_types = [r.get('type', '') for r in detected_refactorings]
    detected_types_normalized = [normalize_refactoring_type(t) for t in detected_types]
    
    # Special case: "General Refactoring" matches any detection
    if manual_subcategory == "General Refactoring" and detected_types:
        return True, detected_types, f"General match: {len(detected_types)} refactorings detected"
    
    # Check if any expected type matches detected types
    for expected in expected_types:
        expected_norm = normalize_refactoring_type(expected)
        if expected_norm in detected_types_normalized:
            matched_type = detected_types[detected_types_normalized.index(expected_norm)]
            return True, detected_types, f"Matched '{expected}' as '{matched_type}'"
    
    # No match
    if expected_types:
        return False, detected_types, f"Expected {expected_types} but got {detected_types}"
    else:
        return False, detected_types, f"No mapping for subcategory '{manual_subcategory}'"

# ===== ANALYSIS =====

def analyze_matches(df, refactorings_by_commit):
    """Analyze matching between manual labels and detected refactorings"""
    
    results = []
    
    for idx, row in df.iterrows():
        commit_hash = row['commit_hash']
        subcategory = row['Sub Category']
        repo_name = f"{row['owner']}/{row['repo']}"
        
        # Get detected refactorings
        detected = refactorings_by_commit.get(commit_hash, [])
        
        # Match
        matched, detected_types, details = match_refactoring(subcategory, detected)
        
        result = {
            'commit_hash': commit_hash[:12],
            'full_hash': commit_hash,
            'repo': repo_name,
            'manual_subcategory': subcategory,
            'detected_count': len(detected),
            'detected_types': ', '.join(detected_types) if detected_types else 'None',
            'matched': matched,
            'match_details': details
        }
        results.append(result)
    
    return pd.DataFrame(results)

def generate_summary(results_df):
    """Generate summary statistics"""
    total = len(results_df)
    matched = results_df['matched'].sum()
    unmatched = total - matched
    
    detected_any = (results_df['detected_count'] > 0).sum()
    detected_none = total - detected_any
    
    print("\n" + "="*80)
    print("MATCHING SUMMARY - GENERAL SE REFACTORINGS")
    print("="*80)
    
    print(f"\nTotal General SE commits: {total}")
    print(f"Commits with refactorings detected: {detected_any} ({detected_any/total*100:.1f}%)")
    print(f"Commits with zero detections: {detected_none} ({detected_none/total*100:.1f}%)")
    
    print(f"\n{'='*80}")
    print(f"MATCHED commits (detected type matches manual label): {matched} ({matched/total*100:.1f}%)")
    print(f"UNMATCHED commits (no matching type found): {unmatched} ({unmatched/total*100:.1f}%)")
    
    # Breakdown by subcategory
    print(f"\n{'='*80}")
    print("MATCHING BY SUB-CATEGORY:")
    print("="*80)
    
    subcategory_stats = results_df.groupby('manual_subcategory').agg({
        'matched': ['sum', 'count'],
        'detected_count': 'sum'
    }).round(2)
    subcategory_stats.columns = ['Matched', 'Total', 'Total Detections']
    subcategory_stats['Match Rate %'] = (subcategory_stats['Matched'] / subcategory_stats['Total'] * 100).round(1)
    subcategory_stats = subcategory_stats.sort_values('Total', ascending=False)
    
    print(subcategory_stats.to_string())
    
    # Best and worst performing subcategories
    print(f"\n{'='*80}")
    print("TOP 5 BEST MATCHED SUB-CATEGORIES:")
    top_5 = subcategory_stats[subcategory_stats['Total'] >= 3].sort_values('Match Rate %', ascending=False).head(5)
    for subcat, row in top_5.iterrows():
        print(f"  {subcat}: {row['Match Rate %']:.1f}% ({int(row['Matched'])}/{int(row['Total'])})")
    
    print(f"\nTOP 5 WORST MATCHED SUB-CATEGORIES:")
    bottom_5 = subcategory_stats[subcategory_stats['Total'] >= 3].sort_values('Match Rate %', ascending=True).head(5)
    for subcat, row in bottom_5.iterrows():
        print(f"  {subcat}: {row['Match Rate %']:.1f}% ({int(row['Matched'])}/{int(row['Total'])})")
    
    return subcategory_stats

# ===== MAIN =====

def main():
    # Paths
    excel_path = 'Updated_Final_Commit_Analysis.xlsx'
    results_dir = 'batch_processing_results/successful_analyses'
    output_file = 'general_se_matching_results.csv'
    
    # Load data
    print("Loading data...")
    df = load_excel_data(excel_path)
    refactorings = load_detected_refactorings(results_dir)
    
    # Analyze matches
    print("\nAnalyzing matches...")
    results_df = analyze_matches(df, refactorings)
    
    # Generate summary
    summary_stats = generate_summary(results_df)
    
    # Save results
    results_df.to_csv(output_file, index=False)
    print(f"\n{'='*80}")
    print(f"Detailed results saved to: {output_file}")
    
    # Show examples
    print(f"\n{'='*80}")
    print("SAMPLE MATCHED COMMITS:")
    print("="*80)
    matched_samples = results_df[results_df['matched']].head(5)
    for _, row in matched_samples.iterrows():
        print(f"\n{row['commit_hash']} - {row['repo']}")
        print(f"  Manual: {row['manual_subcategory']}")
        print(f"  Detected: {row['detected_types']}")
        print(f"  {row['match_details']}")
    
    print(f"\n{'='*80}")
    print("SAMPLE UNMATCHED COMMITS:")
    print("="*80)
    unmatched_samples = results_df[~results_df['matched'] & (results_df['detected_count'] > 0)].head(5)
    for _, row in unmatched_samples.iterrows():
        print(f"\n{row['commit_hash']} - {row['repo']}")
        print(f"  Manual: {row['manual_subcategory']}")
        print(f"  Detected: {row['detected_types']}")
        print(f"  {row['match_details']}")

if __name__ == '__main__':
    main()
