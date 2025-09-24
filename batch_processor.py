#!/usr/bin/env python3
"""
C# RefactoringMiner Batch Processor
Processes all commits from Excel file and generates JSON outputs
"""

import pandas as pd
import os
import sys
import subprocess
import json
import re
import time
from datetime import datetime
from urllib.parse import urlparse
import logging

class CSharpRefactoringBatchProcessor:
    def __init__(self, excel_file, jar_path, output_base_dir):
        self.excel_file = excel_file
        self.jar_path = jar_path
        self.output_base_dir = output_base_dir
        self.results_summary = []
        self.setup_logging()
        self.setup_directories()
        
    def setup_logging(self):
        """Setup comprehensive logging"""
        log_dir = os.path.join(self.output_base_dir, "logs")
        os.makedirs(log_dir, exist_ok=True)
        
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        log_file = os.path.join(log_dir, f"batch_processing_{timestamp}.log")
        
        logging.basicConfig(
            level=logging.INFO,
            format='%(asctime)s - %(levelname)s - %(message)s',
            handlers=[
                logging.FileHandler(log_file),
                logging.StreamHandler(sys.stdout)
            ]
        )
        self.logger = logging.getLogger(__name__)
        
    def setup_directories(self):
        """Create organized directory structure"""
        directories = [
            "successful_analyses",
            "failed_analyses", 
            "progress_tracking",
            "logs",
            "summary_reports"
        ]
        
        for dir_name in directories:
            dir_path = os.path.join(self.output_base_dir, dir_name)
            os.makedirs(dir_path, exist_ok=True)
            
        self.logger.info(f"📁 Created directory structure in: {self.output_base_dir}")
    
    def parse_commit_url(self, commit_url):
        """Extract repository URL and commit hash from GitHub commit URL"""
        try:
            # Example: https://github.com/ExtendRealityLtd/Zinnia.Unity/commit/35cb3631904fec77ab2c68058ba4dd7b6aa75095
            pattern = r'https://github\.com/([^/]+)/([^/]+)/commit/([a-f0-9]+)'
            match = re.match(pattern, commit_url)
            
            if match:
                owner, repo, commit_hash = match.groups()
                repo_url = f"https://github.com/{owner}/{repo}.git"
                project_name = f"{owner}_{repo}"
                return {
                    'repo_url': repo_url,
                    'commit_hash': commit_hash,
                    'project_name': project_name,
                    'owner': owner,
                    'repo': repo
                }
            else:
                self.logger.error(f"❌ Invalid commit URL format: {commit_url}")
                return None
                
        except Exception as e:
            self.logger.error(f"❌ Error parsing commit URL {commit_url}: {e}")
            return None
    
    def generate_output_filename(self, project_name, commit_hash, row_index):
        """Generate a descriptive filename for the JSON output"""
        timestamp = datetime.now().strftime("%Y%m%d")
        short_hash = commit_hash[:8] if commit_hash else "unknown"
        return f"{row_index:03d}_{project_name}_{short_hash}_{timestamp}.json"
    
    def run_refactoring_miner(self, repo_info, output_file, row_index, total_rows):
        """Run C# RefactoringMiner on a single commit"""
        try:
            self.logger.info(f"🔄 [{row_index}/{total_rows}] Processing: {repo_info['project_name']} - {repo_info['commit_hash'][:8]}")
            
            # Prepare command
            cmd = [
                "java", "-cp", self.jar_path,
                "org.refactoringminer.csharp.CSharpRefactoringMiner",
                "-gc", repo_info['repo_url'], repo_info['commit_hash'], "300",  # 5 minute timeout
                "-json", output_file
            ]
            
            self.logger.info(f"   Command: {' '.join(cmd[-8:])}")  # Log last part of command
            
            # Run the command
            start_time = time.time()
            result = subprocess.run(cmd, capture_output=True, text=True, timeout=600)  # 10 minute max timeout
            end_time = time.time()
            
            duration = end_time - start_time
            
            if result.returncode == 0:
                # Check if output file was created and has content
                if os.path.exists(output_file) and os.path.getsize(output_file) > 10:
                    # Validate JSON format
                    try:
                        with open(output_file, 'r') as f:
                            json_data = json.load(f)
                        
                        # Count refactorings
                        refactoring_count = 0
                        if 'commits' in json_data:
                            for commit in json_data['commits']:
                                if 'refactorings' in commit:
                                    refactoring_count += len(commit['refactorings'])
                        
                        self.logger.info(f"✅ [{row_index}/{total_rows}] SUCCESS: {repo_info['project_name']} - Found {refactoring_count} refactorings ({duration:.1f}s)")
                        
                        return {
                            'status': 'success',
                            'refactoring_count': refactoring_count,
                            'duration': duration,
                            'output_file': output_file,
                            'stdout': result.stdout[-500:] if result.stdout else "",  # Last 500 chars
                            'stderr': result.stderr[-500:] if result.stderr else ""
                        }
                        
                    except json.JSONDecodeError as e:
                        self.logger.error(f"❌ [{row_index}/{total_rows}] JSON parsing error: {e}")
                        return {
                            'status': 'json_error',
                            'error': str(e),
                            'duration': duration,
                            'output_file': output_file,
                            'stdout': result.stdout[-500:] if result.stdout else "",
                            'stderr': result.stderr[-500:] if result.stderr else ""
                        }
                else:
                    self.logger.error(f"❌ [{row_index}/{total_rows}] No output file created or empty")
                    return {
                        'status': 'no_output',
                        'duration': duration,
                        'stdout': result.stdout[-500:] if result.stdout else "",
                        'stderr': result.stderr[-500:] if result.stderr else ""
                    }
            else:
                self.logger.error(f"❌ [{row_index}/{total_rows}] Command failed with exit code {result.returncode}")
                return {
                    'status': 'command_failed',
                    'exit_code': result.returncode,
                    'duration': duration,
                    'stdout': result.stdout[-500:] if result.stdout else "",
                    'stderr': result.stderr[-500:] if result.stderr else ""
                }
                
        except subprocess.TimeoutExpired:
            self.logger.error(f"❌ [{row_index}/{total_rows}] TIMEOUT after 10 minutes")
            return {
                'status': 'timeout',
                'duration': 600,
                'error': 'Process timeout after 10 minutes'
            }
            
        except Exception as e:
            self.logger.error(f"❌ [{row_index}/{total_rows}] Unexpected error: {e}")
            return {
                'status': 'error',
                'error': str(e),
                'duration': 0
            }
    
    def save_progress(self, current_index, total_count):
        """Save processing progress"""
        progress_file = os.path.join(self.output_base_dir, "progress_tracking", "progress.json")
        progress_data = {
            'current_index': current_index,
            'total_count': total_count,
            'completed_percentage': round((current_index / total_count) * 100, 2),
            'timestamp': datetime.now().isoformat(),
            'results_summary': self.results_summary
        }
        
        with open(progress_file, 'w') as f:
            json.dump(progress_data, f, indent=2)
    
    def generate_summary_report(self):
        """Generate comprehensive summary report"""
        if not self.results_summary:
            return
            
        # Calculate statistics
        successful = len([r for r in self.results_summary if r['result']['status'] == 'success'])
        failed = len(self.results_summary) - successful
        
        total_refactorings = sum([r['result'].get('refactoring_count', 0) for r in self.results_summary])
        total_duration = sum([r['result'].get('duration', 0) for r in self.results_summary])
        
        # Group by status
        status_counts = {}
        for result in self.results_summary:
            status = result['result']['status']
            status_counts[status] = status_counts.get(status, 0) + 1
        
        # Create summary
        summary = {
            'processing_summary': {
                'total_commits': len(self.results_summary),
                'successful_analyses': successful,
                'failed_analyses': failed,
                'success_rate': round((successful / len(self.results_summary)) * 100, 2) if self.results_summary else 0,
                'total_refactorings_found': total_refactorings,
                'average_refactorings_per_commit': round(total_refactorings / successful, 2) if successful > 0 else 0,
                'total_processing_time_hours': round(total_duration / 3600, 2),
                'average_time_per_commit_seconds': round(total_duration / len(self.results_summary), 2) if self.results_summary else 0
            },
            'status_breakdown': status_counts,
            'detailed_results': self.results_summary,
            'generated_at': datetime.now().isoformat()
        }
        
        # Save summary report
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        summary_file = os.path.join(self.output_base_dir, "summary_reports", f"processing_summary_{timestamp}.json")
        
        with open(summary_file, 'w') as f:
            json.dump(summary, f, indent=2)
            
        self.logger.info(f"📊 Summary report saved: {summary_file}")
        
        # Print summary to console
        print(f"\n" + "="*60)
        print(f"📊 PROCESSING SUMMARY")
        print(f"="*60)
        print(f"Total commits processed: {summary['processing_summary']['total_commits']}")
        print(f"Successful analyses: {summary['processing_summary']['successful_analyses']}")
        print(f"Failed analyses: {summary['processing_summary']['failed_analyses']}")
        print(f"Success rate: {summary['processing_summary']['success_rate']}%")
        print(f"Total refactorings found: {summary['processing_summary']['total_refactorings_found']}")
        print(f"Average refactorings per commit: {summary['processing_summary']['average_refactorings_per_commit']}")
        print(f"Total processing time: {summary['processing_summary']['total_processing_time_hours']:.1f} hours")
        print(f"Average time per commit: {summary['processing_summary']['average_time_per_commit_seconds']:.1f} seconds")
        print(f"\nStatus breakdown:")
        for status, count in status_counts.items():
            print(f"  {status}: {count}")
        print(f"="*60)
        
        return summary_file
    
    def process_all_commits(self, start_index=0, max_commits=None):
        """Process all commits from the Excel file"""
        try:
            # Read Excel file
            self.logger.info(f"📊 Reading Excel file: {self.excel_file}")
            df = pd.read_excel(self.excel_file)
            
            # Filter out rows without commit URLs
            df = df.dropna(subset=['Commit URL'])
            
            if max_commits:
                df = df.head(max_commits)
                
            total_commits = len(df)
            self.logger.info(f"📋 Found {total_commits} commits to process (starting from index {start_index})")
            
            # Process each commit
            for index, row in df.iterrows():
                if index < start_index:
                    continue
                    
                row_number = index + 1
                commit_url = row['Commit URL']
                commit_hash = row.get('Commit Hash', '')
                author = row.get('Author', 'Unknown')
                
                # Parse commit URL
                repo_info = self.parse_commit_url(commit_url)
                if not repo_info:
                    self.logger.error(f"❌ [{row_number}/{total_commits}] Skipping invalid URL: {commit_url}")
                    continue
                
                # Generate output filename
                output_filename = self.generate_output_filename(
                    repo_info['project_name'], 
                    repo_info['commit_hash'], 
                    row_number
                )
                
                # Determine output directory based on expected success
                temp_output_file = os.path.join(self.output_base_dir, "temp", output_filename)
                os.makedirs(os.path.dirname(temp_output_file), exist_ok=True)
                
                # Run RefactoringMiner
                result = self.run_refactoring_miner(repo_info, temp_output_file, row_number, total_commits)
                
                # Move output file to appropriate directory
                if result['status'] == 'success':
                    final_output_file = os.path.join(self.output_base_dir, "successful_analyses", output_filename)
                    if os.path.exists(temp_output_file):
                        os.rename(temp_output_file, final_output_file)
                        result['final_output_file'] = final_output_file
                else:
                    final_output_file = os.path.join(self.output_base_dir, "failed_analyses", output_filename + ".failed")
                    # Create a failure report
                    with open(final_output_file, 'w') as f:
                        json.dump({
                            'commit_url': commit_url,
                            'repo_info': repo_info,
                            'error_details': result,
                            'row_data': row.to_dict()
                        }, f, indent=2)
                
                # Add to results summary
                self.results_summary.append({
                    'row_index': row_number,
                    'commit_url': commit_url,
                    'repo_info': repo_info,
                    'author': author,
                    'result': result
                })
                
                # Save progress every 10 commits
                if row_number % 10 == 0:
                    self.save_progress(row_number, total_commits)
                    
                # Brief pause to avoid overwhelming GitHub API
                time.sleep(1)
            
            # Final progress save and summary
            self.save_progress(total_commits, total_commits)
            summary_file = self.generate_summary_report()
            
            self.logger.info(f"🎉 Batch processing completed!")
            return summary_file
            
        except Exception as e:
            self.logger.error(f"❌ Critical error during batch processing: {e}")
            raise

def main():
    """Main execution function"""
    
    # Configuration
    excel_file = "Final_Commit_Analysis _Iteration 3.xlsx"
    jar_path = "build/libs/RM-fat.jar"
    output_base_dir = "batch_processing_results"
    
    # Check prerequisites
    if not os.path.exists(excel_file):
        print(f"❌ Excel file not found: {excel_file}")
        sys.exit(1)
        
    if not os.path.exists(jar_path):
        print(f"❌ RefactoringMiner JAR not found: {jar_path}")
        sys.exit(1)
    
    # Create processor and run
    processor = CSharpRefactoringBatchProcessor(excel_file, jar_path, output_base_dir)
    
    print(f"🚀 Starting batch processing of C# commits...")
    print(f"📁 Results will be saved to: {output_base_dir}")
    print(f"📊 Excel file: {excel_file}")
    print(f"🔧 JAR file: {jar_path}")
    print(f"⏰ Started at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    try:
        # You can modify these parameters:
        # start_index: Resume from a specific row (0-based)
        # max_commits: Limit number of commits for testing
        summary_file = processor.process_all_commits(start_index=0, max_commits=None)
        
        print(f"\n✅ Batch processing completed successfully!")
        print(f"📊 Summary report: {summary_file}")
        
    except KeyboardInterrupt:
        print(f"\n⏹️  Processing interrupted by user")
        processor.generate_summary_report()
        
    except Exception as e:
        print(f"\n❌ Processing failed: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()