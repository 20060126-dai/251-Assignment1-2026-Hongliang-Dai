# 159.251 Text Editor - Assignment 1

A simple text editor written in Java with Swing, built for Massey University
159.251 Software Design and Construction (Assignment 1).

## Team Members

| Name | ID |
|------|----|
| Hongliang Dai | 25014387 |

## How to Run

The project uses Maven and needs Java 17 or newer.

```bash
# build the project and run the unit tests
mvn clean package

# start the editor
mvn exec:java
```

Or run the jar that was built by Maven:

```bash
java -jar target/text-editor.jar
```

### Docker

A Dockerfile is included. To build and run the container:

```bash
docker build -t 251-text-editor .
docker run --rm 251-text-editor
```

The default container command compiles the project and runs the unit tests,
which verifies that the Maven project works inside the container without any
extra setup. The editor itself is a GUI application, so to actually open the
window run the jar on a machine with a display (see "How to Run" above).

## Project Structure

```
├── pom.xml                  Maven build file with all dependencies
├── editor.yaml              default editor settings (font, colours, ...)
├── pmd-ruleset.xml          PMD rules used for the code quality check
├── Dockerfile               container definition
├── .github/workflows/ci.yml GitHub Actions CI pipeline
├── src/main/java/...        editor source code
├── src/test/java/...        JUnit tests (open, save, search)
└── reports/
    ├── metrics/metrics.csv  size and complexity metrics per class
    ├── spotbugs/            SpotBugs bug analysis report
    └── pmd/                 PMD code quality report
```

## Version Control

- Private GitHub repository:
  https://github.com/20060126-dai/251-Assignment1-2026-Hongliang-Dai

### Significant Commits

- Hongliang Dai (25014387):
  - `<commit-id-1>` - <short description>
  - `<commit-id-2>` - <short description>

*(The commit IDs above will be filled in from the actual git history of the
repository.)*

## Features

- File menu: New, Open, Save, Save as PDF, Print, Exit
- Edit menu: Select All, Copy, Cut, Paste
- Search menu: Find (highlights all matches of a word)
- View menu: Insert Date and Time
- Help menu: About
- Opens plain text (.txt), source code files (.java, .py, .js, .cpp, ...)
  with syntax highlighting, Rich Text Format (.rtf) and OpenDocument
  Text (.odt) files
- Exports plain text to PDF using Apache PDFBox
- Settings loaded from editor.yaml at startup

## Code Quality

The following reports are generated with Maven:

```bash
mvn spotbugs:spotbugs   # bug analysis -> target/spotbugsXml.xml
mvn pmd:pmd             # code quality rules -> target/pmd.xml
powershell -ExecutionPolicy Bypass -File reports/metrics/metrics.ps1
                        # size and complexity metrics -> reports/metrics/metrics.csv
```

The reports are also saved under `reports/`.
