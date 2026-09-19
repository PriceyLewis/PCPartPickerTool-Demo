@echo off
setlocal
where mvn >nul 2>nul
if errorlevel 1 (
  echo Maven is required. Install Maven 3.9+ and ensure mvn is on PATH.
  exit /b 1
)

cd /d "%~dp0"
echo Building PC Part Picker...
call mvn -q -DskipTests package
if errorlevel 1 exit /b 1

echo Starting demo...
java -jar "target\pc-part-picker-demo.jar"
