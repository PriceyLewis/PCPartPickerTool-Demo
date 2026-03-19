# PC Part Picker

Desktop Java application for browsing PC components, comparing options across stores, and building a basket from a local product database.

## Overview

This project was cleaned up as a portfolio-ready demo build. It now runs fully locally against the bundled Access database and does not depend on any external API service.

The original API-backed recommendation feature was removed for security reasons before publication. Recommendations are now generated from local database data only.

## Features

- Create or log into a demo user account
- Browse parts with brand and price sorting
- Search parts with keyword, brand, and price filters
- Get local recommendation suggestions from the in-app assistant
- Add parts to a basket and view a running total
- Leave and read store reviews
- View user search history

## Tech Stack

- Java
- Swing
- UCanAccess / Jackcess
- Microsoft Access `.accdb` database

## Project Structure

- `PCPartPicker/src/Main.java` contains the main UI flows
- `PCPartPicker/src/DatabaseAccess.java` contains database access logic
- `PCPartPicker/src/PCPartAI.java` contains the local recommendation logic
- `PCPartPicker/Database for App.accdb` is the sanitized demo database

## Setup

1. Open the project in your Java IDE.
2. Add these jars from `lib/` to the project libraries:
   - `commons-lang3-3.8.1.jar`
   - `commons-logging-1.2.jar`
   - `hsqldb-2.5.0.jar`
   - `jackcess-3.0.1.jar`
   - `ucanaccess-5.0.1.jar`
3. Run `PCPartPicker/src/Main.java`.

## Login Information

- Create a new demo user from the Sign Up screen
- Or log in with an existing User ID already stored in the sanitized demo database
- Example demo User IDs: `1`, `2`, `3`
- The employer-facing demo build does not expose the admin login screen

## Notes

- The database content has been sanitized for public release.
- The employer-facing demo build does not expose the admin screen or any hardcoded admin password.
- If your IDE reports missing JDBC classes, re-check the library setup above.

## Publishing

The repo is prepared for GitHub upload. Optional polish before publishing:

- add screenshots to the README
- add a license
