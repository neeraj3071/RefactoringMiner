import csv
import re

# Extract project owner and project name
data = []
# Read the URLs from the text file
with open('VR_Project_List.txt', 'r') as file:
    urls = file.readlines()

def extract_owner_project(url):
    match = re.match(r'https://github.com/([^/]+)/([^/]+)\.git', url)
    if match:
        return f"{match.group(1)}/{match.group(2)}"
    else:
        return None

# Extract owner and project name for each URL
data = [extract_owner_project(url.strip()) for url in urls]

with open('GitHubProjects.csv', 'w', newline='') as csvfile:
    csv_writer = csv.writer(csvfile)
    csv_writer.writerows([[project] for project in data])
