# PC Part Picker

`PC Part Picker` is a Java Swing desktop app for browsing PC components, comparing store listings, getting simple local recommendations, and building a basket from a bundled Microsoft Access database.

This repository is set up as a local demo build. It does not require any external API or hosted backend.

## Features

- Sign up for a demo account or log in with an existing user ID
- Browse in-stock parts across stores
- Filter parts by keyword, brand, and price range
- Get local recommendation suggestions from the built-in assistant
- Add parts to a basket and view a running total
- Leave and read store reviews
- View recent user search history

## Tech Stack

- Java
- Swing
- UCanAccess / Jackcess
- Microsoft Access (`.accdb`)

## Project Layout

- `PCPartPicker/src/Main.java` contains the main application flow and screens
- `PCPartPicker/src/DatabaseAccess.java` handles database queries and updates
- `PCPartPicker/src/PCPartAI.java` contains the local recommendation logic
- `PCPartPicker/src/GUI.java` contains shared Swing helpers
- `PCPartPicker/Database for App.accdb` is the bundled demo database
- `lib/` contains the JDBC and Access-related dependencies required to run the app

## Requirements

- JDK 17 or later
- Windows is the safest target environment because the app uses a bundled Microsoft Access database through UCanAccess

## Running The App

### Option 1: Run from an IDE

1. Open the repository in your Java IDE.
2. Mark `PCPartPicker/src` as a source folder if needed.
3. Add every JAR in `lib/` to the project classpath.
4. Run `Main.java`.

### Option 2: Run from the command line on Windows

Compile:

```powershell
javac -cp "lib/*" -d out PCPartPicker/src/*.java
```

Run:

```powershell
java -cp "out;lib/*" Main
```

## Demo Login

- You can create a new account from the Sign Up screen
- You can also log in with an existing user ID already stored in the demo database
- Example user IDs listed in the current README flow: `1`, `2`, `3`

## Notes

- The app searches recursively for `Database for App.accdb`, so keep that file in the repository when moving the project
- The app also searches recursively for `logo.png`, which is stored under `PCPartPicker/src/Assets/`
- Recommendation responses are generated locally from the database contents
- An admin dashboard exists in the codebase, but it is not part of the standard launch flow exposed to normal users
- If JDBC or UCanAccess classes are missing at runtime, re-check that all JARs in `lib/` are on the classpath

## Repository Readiness

The repository already includes:

- source code
- bundled demo database
- bundled runtime dependencies
- a `.gitignore`

Good optional follow-ups before publishing:

- add screenshots or a short demo GIF
- add a license
- add a short section on known limitations
