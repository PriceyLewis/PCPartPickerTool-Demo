# PC Part Picker

[![Java CI](https://github.com/PriceyLewis/PCPartPickerTool-Demo/actions/workflows/compile.yml/badge.svg)](https://github.com/PriceyLewis/PCPartPickerTool-Demo/actions/workflows/compile.yml)

[Launch the browser demo](https://priceylewis.github.io/PCPartPickerTool-Demo/) · [View the recruiter case study](https://priceylewis.github.io/projects/pc-part-picker.html)

A recruiter-friendly Java Swing demo for browsing PC components, comparing store inventory, building a basket and using a deterministic local recommendation assistant backed by Microsoft Access.

![PC Part Picker portfolio preview](https://priceylewis.github.io/assets/pc-part-picker.svg)

> This was originally team coursework. The repository deliberately distinguishes my contribution and does not imply sole authorship of the original team project. Recruiters can run the Java Swing portfolio build directly in the browser with no Java or Access setup.

## What this project demonstrates

- Java 17 and object-oriented desktop development
- multi-screen Swing UI flows
- JDBC-style database access through UCanAccess
- parameterised database mutations and search/filter flows
- basket, account, review and search-history state
- a deterministic natural-language-style recommendation assistant
- explicit confirmation before mutating recommendation actions
- Maven dependency management and automated CI
- team integration and maintenance of an existing codebase

## My contribution

My work included Java Swing application code, database-backed flows and the assistant integration. I later converted the public portfolio version away from its original cloud-AI dependency so reviewers can run the important journeys locally without API keys or paid services.

## One-click browser demo

The browser build runs the Java Swing application through CheerpJ and uses a disposable in-memory data adapter instead of requiring Microsoft Access inside the browser sandbox.

- [Launch browser demo](https://priceylewis.github.io/PCPartPickerTool-Demo/)
- No Java installation, API key or database setup is required.
- Browser mode opens directly with a demo user and seeded parts, stores, inventory, reviews and search history.
- Browsing, filters, recommendations, basket interactions, reviews and history remain interactive.
- The normal desktop build still uses the bundled Microsoft Access database through UCanAccess.

## Feature walkthrough

1. Create a demo account or sign in using an existing demo user ID.
2. Browse and filter in-stock components.
3. Ask the local assistant for a recommendation or budget-based option.
4. Confirm a recommendation before adding it to the basket.
5. Review the running basket total.
6. Leave/read store reviews and inspect recent search history.

## Tech stack

- Java 17+
- Swing
- UCanAccess / Jackcess
- Microsoft Access (`.accdb`)
- Maven
- GitHub Actions

## Quick start

Requirements:

- JDK 17+
- Maven 3.9+

### Windows

```powershell
.\run-demo.bat
```

### Linux / macOS

```bash
chmod +x run-demo.sh
./run-demo.sh
```

Or:

```bash
mvn verify
java -jar target/pc-part-picker-demo.jar
```

The application searches for the bundled `PCPartPicker/Database for App.accdb` and the local logo asset at runtime.

## Architecture

```text
Swing UI
  |
  +--> Browse / search / basket / reviews
  |
  +--> Local recommendation assistant
  |          |
  |          +--> ranked in-stock inventory
  |          +--> budget + preference matching
  |
  +--> DatabaseAccess
              |
              +--> UCanAccess
                      |
                      +--> bundled Access demo database
```

## Project layout

- `PCPartPicker/src/Main.java` — main application journeys and screens
- `PCPartPicker/src/DatabaseAccess.java` — database reads and writes
- `PCPartPicker/src/PCPartAI.java` — deterministic assistant/recommendation logic
- `PCPartPicker/src/GUI.java` — shared Swing helpers
- `PCPartPicker/Database for App.accdb` — bundled demo data
- `pom.xml` — maintained dependency and packaging configuration

Dependency JARs are no longer committed to the repository; Maven resolves them during the build.

## Verification

GitHub Actions runs `mvn verify` on every push and pull request and packages a runnable demo JAR on successful pushes. A separate browser workflow launches the packaged application in Chromium through CheerpJ, waits for the Swing home-screen readiness signal and publishes the verified build to GitHub Pages.

## Scope

This is a portfolio/coursework demo rather than an e-commerce system. It intentionally has no payment processing, order fulfilment or cloud AI dependency. The assistant ranks local demo data rather than claiming to be a generative-AI service.
