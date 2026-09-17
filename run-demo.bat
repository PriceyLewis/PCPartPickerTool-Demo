@echo off
setlocal
cd /d "%~dp0"

where javac >nul 2>nul || (
  echo Java JDK not found. Install a JDK and ensure javac is on PATH.
  exit /b 1
)

if not exist build mkdir build

echo Compiling PC Part Picker Tool...
javac -cp "lib/*" -d build PCPartPicker\src\*.java
if errorlevel 1 exit /b 1

echo Starting demo...
java -cp "build;lib/*" Main
