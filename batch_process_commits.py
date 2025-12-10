#!/usr/bin/env python3
"""
Batch processing script for RefactoringMiner
Processes all commits from Updated_Final_Commit_Analysis.xlsx
"""

import pandas as pd
import subprocess
import os
import shutil
import time
from datetime import datetime
import sys

def main():
    # Ensure we're in the RefactoringMiner directory
    script_dir = os.path.dirname(os.path.abspath(__file__))
    os.chdir(script_dir)
    
    print("=" * 100)
    print("REFACTORINGMINER BATCH PROCESSING")
    print("=" * 100)
    print(f"Working directory: {os.getcwd()}")
    print()

    # Configuration
    excel_file = "New Batch/Updated_Final_Commit_Analysis.xlsx"
    output_dir = "New Batch/batch_processing_results"
    success_dir = f"{output_dir}/successful_analyses"
    failed_dir = f"{output_dir}/failed_analyses"
    log_file = f"{output_dir}/processing_log.txt"
    jar_path = "build/libs/RM-fat.jar"
    
    # Verify JAR exists
    if not os.path.exists(jar_path):
        print(f"❌ ERROR: RefactoringMiner JAR not found: {jar_path}")
        print("Please build the project first: ./gradlew jar")
        sys.exit(1)

    # Load Excel
    print(f"Loading commits from: {excel_file}")
    try:
        df = pd.read_excel(excel_file)
        total_commits = len(df)
        print(f"✅ Loaded {total_commits} commits")
    except Exception as e:
        print(f"❌ Failed to load Excel: {e}")
        sys.exit(1)

    print()

    # Statistics
    successful = 0
    failed = 0
    start_time = time.time()

    # Initialize log
    with open(log_file, 'w') as f:
        f.write(f"RefactoringMiner Batch Processing Log\n")
        f.write(f"Started: {datetime.now()}\n")
        f.write(f"Total commits: {total_commits}\n")
        f.write("=" * 100 + "\n\n")

    print("=" * 100)
    print("PROCESSING COMMITS")
    print("=" * 100)
    print()

    # Process each commit
    for idx, row in df.iterrows():
        commit_num = idx + 1
        
        # Extract data
        commit_url = row['Commit URL']
        commit_hash = row['Commit Hash']
        
        # Parse repo info from URL
        try:
            parts = str(commit_url).split('/')
            owner = parts[3]
            repo_name = parts[4]
            repo_url = f"https://github.com/{owner}/{repo_name}.git"
        except Exception as e:
            error_msg = f"Failed to parse URL: {commit_url}"
            print(f"❌ [{commit_num}/{total_commits}] {error_msg}")
            
            with open(log_file, 'a') as f:
                f.write(f"[{commit_num}/{total_commits}] PARSE ERROR - {error_msg}\n")
            
            failed += 1
            continue
        
        # File paths
        output_filename = f"{repo_name}_{commit_hash}.json"
        output_path = f"{success_dir}/{output_filename}"
        error_log_path = f"{failed_dir}/{repo_name}_{commit_hash}_error.txt"
        
        # Skip if already processed successfully
        if os.path.exists(output_path):
            print(f"[{commit_num}/{total_commits}] {repo_name} @ {commit_hash[:8]} - SKIPPED (already processed)")
            successful += 1
            with open(log_file, 'a') as f:
                f.write(f"[{commit_num}/{total_commits}] ⊙ SKIPPED (already exists) - {repo_name}_{commit_hash}\n")
            continue
        
        print(f"[{commit_num}/{total_commits}] {repo_name} @ {commit_hash[:8]}")
        print(f"  Repo: {owner}/{repo_name}")
        
        try:
            # Clone repository locally first
            print(f"  → Cloning...")
            temp_repo_dir = f"temp_repos/{repo_name}"
            
            # Clean existing clone
            if os.path.exists(temp_repo_dir):
                shutil.rmtree(temp_repo_dir)
            
            clone_result = subprocess.run(
                ["git", "clone", "--quiet", repo_url, temp_repo_dir],
                capture_output=True,
                text=True,
                timeout=300
            )
            
            if clone_result.returncode != 0:
                raise Exception(f"Clone failed: {clone_result.stderr[:200]}")
            
            print(f"  ✓ Cloned")
            
            # Run C# RefactoringMiner with local clone
            print(f"  → Running C# RefactoringMiner...")
            
            # Use C# RefactoringMiner for Unity/C# projects
            # Format: java -cp <jar> org.refactoringminer.csharp.CSharpRefactoringMiner -c <repo-path> <commit-hash> -json <output-path>
            rm_result = subprocess.run(
                ["java", "-cp", jar_path,
                 "org.refactoringminer.csharp.CSharpRefactoringMiner",
                 "-c", temp_repo_dir, commit_hash, "-json", output_path],
                capture_output=True,
                text=True,
                timeout=600  # 10 minute max
            )
            
            # Check if output file was created
            if rm_result.returncode != 0 or not os.path.exists(output_path):
                error_output = rm_result.stderr if rm_result.stderr else rm_result.stdout
                raise Exception(f"RefactoringMiner failed:\n{error_output[:500]}")
            
            print(f"  ✓ Analysis complete")
            print(f"  ✓ Saved: {output_filename}")
            
            successful += 1
            
            with open(log_file, 'a') as f:
                f.write(f"[{commit_num}/{total_commits}] ✓ SUCCESS - {repo_name}_{commit_hash}\n")
        
        except Exception as e:
            error_msg = str(e)
            print(f"  ✗ FAILED: {error_msg[:150]}")
            
            # Save detailed error log
            with open(error_log_path, 'w') as f:
                f.write(f"Commit: {commit_hash}\n")
                f.write(f"Repository: {repo_url}\n")
                f.write(f"Commit URL: {commit_url}\n")
                f.write(f"Timestamp: {datetime.now()}\n")
                f.write(f"\nError:\n{error_msg}\n")
            
            with open(log_file, 'a') as f:
                f.write(f"[{commit_num}/{total_commits}] ✗ FAILED - {repo_name}_{commit_hash}: {error_msg[:200]}\n")
            
            failed += 1
        
        finally:
            # Always cleanup temp repo
            temp_repo_dir = f"temp_repos/{repo_name}"
            if os.path.exists(temp_repo_dir):
                try:
                    shutil.rmtree(temp_repo_dir)
                    print(f"  ✓ Cleaned up")
                except Exception as e:
                    print(f"  ⚠ Cleanup warning: {e}")
        
        print()
        
        # Progress checkpoint every 10 commits
        if commit_num % 10 == 0:
            elapsed = time.time() - start_time
            rate = commit_num / elapsed if elapsed > 0 else 0
            estimated_remaining = (total_commits - commit_num) / rate if rate > 0 else 0
            
            print("=" * 100)
            print(f"CHECKPOINT: {commit_num}/{total_commits} processed")
            print(f"  ✅ Success: {successful} ({successful/commit_num*100:.1f}%)")
            print(f"  ❌ Failed: {failed} ({failed/commit_num*100:.1f}%)")
            print(f"  ⏱ Elapsed: {elapsed/60:.1f} min")
            print(f"  📊 Rate: {rate*60:.1f} commits/hour")
            print(f"  ⏳ ETA: {estimated_remaining/60:.1f} min")
            print("=" * 100)
            print()

    # Final summary
    elapsed_total = time.time() - start_time
    
    print("=" * 100)
    print("BATCH PROCESSING COMPLETE")
    print("=" * 100)
    print()
    print(f"📊 FINAL RESULTS:")
    print(f"  Total commits: {total_commits}")
    print(f"  ✅ Successful: {successful} ({successful/total_commits*100:.1f}%)")
    print(f"  ❌ Failed: {failed} ({failed/total_commits*100:.1f}%)")
    print(f"  ⏱ Total time: {elapsed_total/60:.1f} minutes ({elapsed_total/3600:.1f} hours)")
    print(f"  📈 Average: {elapsed_total/total_commits:.1f} seconds/commit")
    print()
    print(f"📁 OUTPUT LOCATIONS:")
    print(f"  Successful: {success_dir}/")
    print(f"  Failed: {failed_dir}/")
    print(f"  Log: {log_file}")
    print()

    # Write final summary to log
    with open(log_file, 'a') as f:
        f.write("\n" + "=" * 100 + "\n")
        f.write("FINAL SUMMARY\n")
        f.write("=" * 100 + "\n")
        f.write(f"Completed: {datetime.now()}\n")
        f.write(f"Total commits: {total_commits}\n")
        f.write(f"Successful: {successful} ({successful/total_commits*100:.1f}%)\n")
        f.write(f"Failed: {failed} ({failed/total_commits*100:.1f}%)\n")
        f.write(f"Total time: {elapsed_total/60:.1f} minutes\n")
        f.write(f"Average: {elapsed_total/total_commits:.1f} seconds/commit\n")

    # Verify output count
    actual_files = len([f for f in os.listdir(success_dir) if f.endswith('.json')])
    print(f"✅ Verification: {actual_files} JSON files in successful_analyses/ (expected: {successful})")
    
    if actual_files != successful:
        print(f"⚠️  WARNING: File count mismatch!")
    
    print()
    print("✅ All done!")

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n❌ Processing interrupted by user (Ctrl+C)")
        print("You can resume by running the script again - it will skip existing JSON files")
        sys.exit(1)
    except Exception as e:
        print(f"\n\n❌ Fatal error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
