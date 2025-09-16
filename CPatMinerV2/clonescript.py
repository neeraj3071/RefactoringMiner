import os
import subprocess

def clone_repositories(repo_links, base_directory):
    for repo_link in repo_links:
        owner, project_name = extract_owner_project(repo_link)
        repo_directory = os.path.join(base_directory, f"{owner}/{project_name}")
        os.makedirs(repo_directory, exist_ok=True)

        # Cloning the repository
        clone_command = f"git clone {repo_link} {repo_directory}"
        subprocess.run(clone_command, shell=True)

def extract_owner_project(repo_link):
    # Extracting owner and project name from the GitHub URL
    parts = repo_link.rstrip('/').split('/')[-2:]
    owner, project_name = parts
    project_name = project_name.rstrip('.git')
    return owner, project_name

if __name__ == "__main__":
    
    with open("E:/PhD1/research_project_2/CPatMinerV2/VR_Project_List.txt", "r") as file:
        repo_links = [line.strip() for line in file if line.strip()]

    base_directory = "E:/PhD1/research_project_2/CPatMinerV2/repositories"

    clone_repositories(repo_links, base_directory)