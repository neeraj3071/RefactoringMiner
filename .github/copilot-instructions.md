# AI coding agent guide for RefactoringMiner

This repo hosts RefactoringMiner, a Java 17+ Gradle project that detects code refactorings and provides an AST diff visualizer; it also integrates with CPatMinerV2 to preprocess C#.

## Project shape and key modules
- Core Java sources: `src/main/java/**` with tests under `src/test/java/**` (JUnit 5, parallel-friendly).
- Distribution and CLI: built via Gradle; runnable scripts generated in `build/distributions/**` and `build/scripts/**` (`RefactoringMiner` launcher).
- AST diff web UI: launched by CLI `diff` commands; runs a Jetty server at `http://127.0.0.1:6789`.
- Docker images: `docker/Dockerfile` and `docker/native/Dockerfile-native`; see `docker/README.md`.
- C# support: bridge via `CPatMinerV2` JAR placed in `libs/`; build CPatMinerV2 `AtomicASTChangeMining` with Maven, then `./gradlew build`.

## Build, test, run (local)
- Requirements: Java 17+ and Gradle 7.4+.
- Build jars: `./gradlew jar` (or `./gradlew distZip` for full distribution).
- Run tests: `./gradlew test` (JUnit 5; no external repo clones needed; runs in parallel).
- Launch CLI directly (example):
  - Detect refactorings for a local repo/commit:
    - `./gradlew run --args="-c /path/to/repo <commit-sha> -json output.json"`
  - End-to-end using distribution:
    - `./gradlew distZip && unzip build/distributions/RefactoringMiner-*.zip -d build && build/RefactoringMiner-*/bin/RefactoringMiner -h`

## CLI usage patterns (examples)
- Analyze all commits on a branch of a local clone:
  - `RefactoringMiner -a /path/to/repo main -json results.json`
- Single commit (local):
  - `RefactoringMiner -c /path/to/repo <sha> -json results.json`
- GitHub direct (requires OAuth token in `bin/github-oauth.properties`):
  - `RefactoringMiner -gc https://github.com/user/repo.git <sha> 10 -json results.json`
- AST diff visualizer:
  - `RefactoringMiner diff --url https://github.com/JabRef/jabref/pull/11180`
  - `RefactoringMiner diff --src /left/dir --dst /right/dir --export` (opens `http://127.0.0.1:6789`)

## Docker workflows
- Pull image: `docker pull tsantalis/refactoringminer`.
- Local diff via container (maps two folders and port 6789):
  - `docker run -v /my/left:/diff/left -v /my/right:/diff/right -p 6789:6789 tsantalis/refactoringminer diff --src left/ --dst right/`
- Git integration difftool snippets in `docker/README.md`.

## C# integration specifics
- Build `CPatMinerV2/AtomicASTChangeMining` with Maven:
  - `cd CPatMinerV2/AtomicASTChangeMining && mvn clean package`
- Copy the produced JAR (e.g., `AtomicASTChangeMining-0.0.1-SNAPSHOT.jar`) to `libs/`.
- Then run RefactoringMiner normally; C# files are auto-converted by the bridge; failures are logged and skipped.

## Conventions and gotchas
- Java target is 17+; ensure toolchains or JAVA_HOME match to avoid Gradle/toolchain mismatches.
- OAuth token placement: when using `-gc`/`-gp`/`diff --url`, put `github-oauth.properties` next to the executing `bin/RefactoringMiner`.
- AST diff output export: use `--export` to persist mappings/actions (defaults to `bin/`).
- To exclude comment-only diffs, add `--ignore-formatting` to `diff` commands.
- Tests rely on internal oracles under `src/test/resources/oracle/**`; do not remove these when pruning test data.

## Pointers to source/examples
- Top-level README: `README.md` (precision/recall, commands, API usage).
- Docker usage: `docker/README.md`.
- Benchmarks and oracle data: `src/test/resources/oracle/**`.
- Example classes: `TestJavaClass.java`, `TestJavaClass.xml`, and C# samples under repo root.

## When adding features
- Keep CLI compatibility: extend argument parsing without breaking documented flags (`-a`, `-c`, `-gc`, `-gp`, `diff --*`).
- Maintain JSON output schema as in README examples; changes should be additive and tested in `TestCommandLine`.
- Ensure AST diff server remains at port `6789` (config used by Docker/Git integrations).
