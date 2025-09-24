#!/usr/bin/env python3
"""
Enhanced C# RefactoringMiner Batch Processor
Uses local git clones instead of GitHub API to avoid authentication issues
"""

import pandas as pd
import os
import sys
import subprocess
import json
import re
import time
import shutil
from datetime import datetime
from urllib.parse import urlparse
import logging

class LocalCloneBatchProcessor:
    def __init__(self, excel_file, jar_path, output_base_dir):
        self.excel_file = excel_file
        self.jar_path = jar_path
        self.output_base_dir = output_base_dir
        self.temp_repos_dir = os.path.join(output_base_dir, "temp_repos")
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
            "summary_reports",
            "temp_repos"
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
                    'repo': repo,
                    'local_repo_path': os.path.join(self.temp_repos_dir, project_name)
                }
            else:
                self.logger.error(f"❌ Invalid commit URL format: {commit_url}")
                return None
                
        except Exception as e:
            self.logger.error(f"❌ Error parsing commit URL {commit_url}: {e}")
            return None
    
    def clone_or_update_repo(self, repo_info):
        """Clone repository locally or update if already exists"""
        try:
            local_path = repo_info['local_repo_path']
            repo_url = repo_info['repo_url']
            
            if os.path.exists(local_path):
                # Repository already exists, update it
                self.logger.info(f"🔄 Updating existing repo: {repo_info['project_name']}")
                result = subprocess.run(['git', 'fetch', '--all'], 
                                      cwd=local_path, 
                                      capture_output=True, text=True, timeout=120)
                if result.returncode != 0:
                    self.logger.warning(f"⚠️ Git fetch failed, re-cloning: {result.stderr}")
                    shutil.rmtree(local_path)
                    return self.clone_or_update_repo(repo_info)
            else:
                # Clone the repository
                self.logger.info(f"📥 Cloning repo: {repo_info['project_name']}")
                result = subprocess.run(['git', 'clone', repo_url, local_path], 
                                      capture_output=True, text=True, timeout=300)
                if result.returncode != 0:
                    self.logger.error(f"❌ Git clone failed: {result.stderr}")
                    return False
            
            # Verify the commit exists
            result = subprocess.run(['git', 'cat-file', '-e', repo_info['commit_hash']], 
                                  cwd=local_path, capture_output=True)
            if result.returncode != 0:
                self.logger.error(f"❌ Commit {repo_info['commit_hash']} not found in repository")
                return False
                
            return True
            
        except subprocess.TimeoutExpired:
            self.logger.error(f"❌ Git operation timeout for: {repo_info['project_name']}")
            return False
        except Exception as e:
            self.logger.error(f"❌ Error cloning/updating repo {repo_info['project_name']}: {e}")
            return False
    
    def generate_output_filename(self, project_name, commit_hash, row_index):
        """Generate a descriptive filename for the JSON output"""
        timestamp = datetime.now().strftime("%Y%m%d")
        short_hash = commit_hash[:8] if commit_hash else "unknown"
        return f"{row_index:03d}_{project_name}_{short_hash}_{timestamp}.json"
    
    def run_refactoring_miner_local(self, repo_info, output_file, row_index, total_rows):
        """Run C# RefactoringMiner on a locally cloned repository"""
        try:
            self.logger.info(f"🔄 [{row_index}/{total_rows}] Processing: {repo_info['project_name']} - {repo_info['commit_hash'][:8]}")
            
            # First ensure we have the repository locally
            if not self.clone_or_update_repo(repo_info):
                return {
                    'status': 'clone_failed',
                    'error': 'Failed to clone or update repository',
                    'duration': 0
                }
            
            # Prepare command - using local repository path
            cmd = [
                "java", "-cp", self.jar_path,
                "org.refactoringminer.csharp.CSharpRefactoringMiner",
                "-c", repo_info['local_repo_path'], repo_info['commit_hash'],
                "-json", output_file
            ]
            
            self.logger.info(f"   Command: java -cp {os.path.basename(self.jar_path)} ... -c {repo_info['project_name']} {repo_info['commit_hash'][:8]}")
            
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
                # Log some stderr for debugging
                if result.stderr:
                    self.logger.error(f"   Error output: {result.stderr[-200:]}")
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
    
    def cleanup_temp_repos(self, keep_successful=False):
        """Clean up temporary repository clones"""
        if not keep_successful:
            if os.path.exists(self.temp_repos_dir):
                self.logger.info(f"🧹 Cleaning up temporary repositories...")
                shutil.rmtree(self.temp_repos_dir)
                os.makedirs(self.temp_repos_dir, exist_ok=True)
    
    def save_progress(self, current_index, total_count):
        """Save processing progress"""
        progress_file = os.path.join(self.output_base_dir, "progress_tracking", "progress.json")
        progress_data = {
            'current_index': current_index,
            'total_count': total_count,
            'completed_percentage': round((current_index / total_count) * 100, 2),
            'timestamp': datetime.now().isoformat(),
            'results_summary': self.results_summary[-10:]  # Last 10 results only
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
            'top_projects_by_refactorings': self.get_top_projects_by_refactorings(),
            'generated_at': datetime.now().isoformat()
        }
        
        # Save summary report
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        summary_file = os.path.join(self.output_base_dir, "summary_reports", f"processing_summary_{timestamp}.json")
        
        with open(summary_file, 'w') as f:
            json.dump(summary, f, indent=2)
            
        self.logger.info(f"📊 Summary report saved: {summary_file}")
        
        # Print summary to console
        print(f"\n" + "="*70)
        print(f"📊 PROCESSING SUMMARY")
        print(f"="*70)
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
        print(f"="*70)
        
        return summary_file
    
    def get_top_projects_by_refactorings(self):
        """Get top projects by refactoring count"""
        project_refactorings = {}
        for result in self.results_summary:
            if result['result']['status'] == 'success':
                project = result['repo_info']['project_name']
                count = result['result'].get('refactoring_count', 0)
                if project not in project_refactorings:
                    project_refactorings[project] = {'count': 0, 'commits': 0}
                project_refactorings[project]['count'] += count
                project_refactorings[project]['commits'] += 1
        
        # Sort by total refactorings
        sorted_projects = sorted(project_refactorings.items(), 
                               key=lambda x: x[1]['count'], reverse=True)
        return dict(sorted_projects[:10])  # Top 10
    
    def process_all_commits(self, start_index=0, max_commits=None):
        """Process all commits from the Excel file using local clones"""
        try:
            # Read Excel file
            self.logger.info(f"📊 Reading Excel file: {self.excel_file}")
            df = pd.read_excel(self.excel_file)
            
            # Filter out rows without commit URLs
            df = df.dropna(subset=['Commit URL'])
            
            if start_index > 0:
                df = df.iloc[start_index:]
            
            if max_commits:
                df = df.head(max_commits)
                
            total_commits = len(df)
            self.logger.info(f"📋 Found {total_commits} commits to process (starting from index {start_index})")
            
            # Process each commit
            for idx, (index, row) in enumerate(df.iterrows()):
                row_number = start_index + idx + 1
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
                
                # Run RefactoringMiner with local clone
                result = self.run_refactoring_miner_local(repo_info, temp_output_file, row_number, total_commits)
                
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
                            'row_data': {k: str(v) for k, v in row.to_dict().items()}  # Convert to strings
                        }, f, indent=2)
                
                # Add to results summary
                self.results_summary.append({
                    'row_index': row_number,
                    'commit_url': commit_url,
                    'repo_info': repo_info,
                    'author': author,
                    'result': result
                })
                
                # Save progress every 5 commits
                if row_number % 5 == 0:
                    self.save_progress(row_number, total_commits)
                    
                # Clean up old repos every 20 commits to save disk space
                if row_number % 20 == 0:
                    self.cleanup_temp_repos(keep_successful=False)
            
            # Final progress save and summary
            self.save_progress(total_commits, total_commits)
            summary_file = self.generate_summary_report()
            
            # Final cleanup
            self.cleanup_temp_repos(keep_successful=False)
            
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
    output_base_dir = "batch_processing_results_local"
    
    # Check prerequisites
    if not os.path.exists(excel_file):
        print(f"❌ Excel file not found: {excel_file}")
        sys.exit(1)
        
    if not os.path.exists(jar_path):
        print(f"❌ RefactoringMiner JAR not found: {jar_path}")
        sys.exit(1)
    
    # Create processor and run
    processor = LocalCloneBatchProcessor(excel_file, jar_path, output_base_dir)
    
    print(f"🚀 Starting LOCAL CLONE batch processing of C# commits...")
    print(f"📁 Results will be saved to: {output_base_dir}")
    print(f"📊 Excel file: {excel_file}")
    print(f"🔧 JAR file: {jar_path}")
    print(f"⏰ Started at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"🔑 Using LOCAL CLONES (no GitHub API authentication needed)")
    
    try:
        # You can modify these parameters:
        # start_index: Resume from a specific row (0-based)
        # max_commits: Limit number of commits for testing (None for all)
        
        # Process ALL commits from the Excel file
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