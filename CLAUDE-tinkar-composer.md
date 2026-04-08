# tinkar-composer — Project Notes

<!-- Migrated from CLAUDE.md by ws:init.
     This file is for hand-authored, project-specific information.
     Commit this file to git. -->

# Tinkar Composer

API for creating, updating, and retiring Tinkar data components.

## Build Standards

Files in `.claude/standards/` are build artifacts unpacked from `ike-build-standards`. DO NOT edit or commit them. See the workspace root CLAUDE.md for details.

## Build

```bash
mvn clean verify -DskipTests -T4
```

## Key Facts

- GroupId: `dev.ikm.tinkar`
- ArtifactId: `composer`
- Single jar artifact (no submodules)
- Uses `--enable-preview` (Java 25) — set via `maven.compiler.enablePreview`
