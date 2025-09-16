# CPatMinerV2: Graph-based Mining of Semantic Code Change Patterns in Csharp Projects

This is a fork of [CPatMiner](https://github.com/nguyenhoan/CPatMiner). We extended it to support Csharp.

This can be further adapted to other ASTs transformed to SrcML format.

## Publications
* [Graph-based Mining of In-the-Wild, Fine-grained, Semantic Code Change Patterns](https://2019.icse-conferences.org/event/icse-2019-technical-papers-graph-based-mining-of-in-the-wild-fine-grained-semantic-code-change-patterns) (The 41st ACM/IEEE International Conference on Software Engineering
(ICSE 2019) - Technical Track)

## Packages
**AtomicASTChangeMining:** extracts change graphs from commits.

**SemanticChangeGraphMiner:** mines change templates from change graphs.

## Requirements
- **Java** 11 or above.
- **srcML** v1.0.0, you can download it from [here](https://www.srcml.org/#download).

## Extracting change graphs from commits

main class: ``AtomicASTChangeMining/src/main/MainChangeAnalyzer.java``

arguments:
```
-i input_repos_root_path: each subfolder is a git repo, with this structure repo_user/repo_name

-o output_path: where the graphs are stored
```
**Note** file repos.csv under reposPath: a text file containing the names of repos to be processed, one repo name on each line. ls reposPath > repos.csv to create this file if you want to process all repos (repo_user/repo_name)

## Mining change templates from change graphs

main class: ``SemanticChangeGraphMiner/src/main/MineChangePatterns.java``
```
reposPath = input_repos_root_path: a subfolder is a git repo (Same one as earlier)

changesPath = change_graph_path: output_path of the extraction step

file repos.csv under reposPath: a text file containing the names of repos to be processed, one repo name on each line. ls reposPath > repos.csv to create this file if you want to process all repos

output: in a directory SemanticChangeGraphMiner/output/patterns/input_repos_root_path-hybrid under the working directory.
```
**Note:** The directory SemanticChangeGraphMiner/src/resources has to be in a directory named src under the working directory.

## License
All software provided in this repository is subject to the [Apache License Version 2.0](LICENSE).
