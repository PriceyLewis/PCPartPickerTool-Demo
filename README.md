# PC Part Picker

`PC Part Picker` is a Java Swing desktop application for browsing PC components, comparing store listings, using a local rule-based assistant, and building a basket from a bundled Microsoft Access database.

This repository is a self-contained portfolio demo. It does **not** require a cloud AI service, external API, or hosted backend.

## What This Project Demonstrates

- Java / object-oriented desktop development
- Swing UI flows across multiple screens
- JDBC-style database access through UCanAccess / Jackcess
- Search, filtering, basket and account state
- A deterministic local assistant for structured natural-language-style commands
- Safer mutating actions that require user confirmation
- Team software development and integration

## Features

- Sign up for a demo account or log in with an existing user ID
- Browse in-stock parts across stores
- Filter parts by keyword, brand, and price range
- Ask the built-in assistant to find, add, remove or clear parts
- Add parts to a basket and view a running total
- Leave and read store reviews
- View recent user search history

## My Contribution

This began as team coursework. My main contribution included Java Swing application code, database-backed flows, and the assistant integration. The public repository is presented as a demo rather than implying that every original team feature was solely authored by me.

## Tech Stack

- Java 17+
- Swing
- UCanAccess / Jackcess
- Microsoft Access (`.accdb`)

## Project Layout

- `PCPartPicker/src/Main.java` — main application flow and screens
- `PCPartPicker/src/DatabaseAccess.java` — database queries and updates
- `PCPartPicker/src/PCPartAI.java` — local assistant / recommendation logic
- `PCPartPicker/src/GUI.java` — shared Swing helpers
- `PCPartPicker/Database for App.accdb` — bundled demo database
- `lib/` — JDBC and Access-related dependencies required by the demo

## Requirements

- JDK 17 or later
- Windows is the safest target environment for the full UI/database demo because the project was developed around Microsoft Access through UCanAccess

## Quick Start

### Windows

From the repository root:

```powershell
.\run-demo.bat
```

### Linux / macOS

```bash
chmod +x run-demo.sh
./run-demo.sh
```

The shell launcher is useful for compilation checks; the Windows build remains the primary demo target.

### IDE

1. Open the repository in your Java IDE.
2. Mark `PCPartPicker/src` as a source folder if needed.
3. Add every JAR in `lib/` to the project classpath.
4. Run `Main.java`.

## Demo Login

- Create a new account from the Sign Up screen, or
- Use an existing user ID in the bundled demo database (for example `1`, `2`, or `3`).

## Verification

GitHub Actions compiles all Java source files against the bundled dependencies on every push and pull request. This catches missing classes, syntax errors and broken compile-time integrations before changes reach the demo branch.

## Notes / Limitations

- The app searches recursively for `Database for App.accdb`, so keep that file in the repository when moving the project.
- The app also searches recursively for `logo.png`, stored under `PCPartPicker/src/Assets/`.
- Assistant responses are generated locally; there is no external AI dependency in this demo.
- An admin dashboard exists in the codebase but is not part of the standard normal-user launch flow.
- Email verification, payment processing and real order fulfilment are outside the scope of this coursework demo.
- If UCanAccess classes are missing at runtime, confirm that all JARs in `lib/` are on the classpath.
