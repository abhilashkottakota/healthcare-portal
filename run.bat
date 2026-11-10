@echo off
cd /d "%~dp0"
echo ==================================================================
echo   Starting CarePulse Healthcare Portal & Appointment Booking System
echo ==================================================================

if not exist bin mkdir bin

echo Compiling Java Server and APIs...
javac -d bin src\com\healthcare\model\*.java src\com\healthcare\repository\*.java src\com\healthcare\util\*.java src\com\healthcare\api\*.java src\com\healthcare\server\*.java src\com\healthcare\Main.java test\com\healthcare\HealthcarePortalTest.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed!
    pause
    exit /b %ERRORLEVEL%
)

echo.
echo Launching Healthcare Portal at http://localhost:8080 ...
echo Press Ctrl+C in this terminal to stop the server anytime.
echo.

start "" "http://localhost:8080"
java -cp bin com.healthcare.Main --no-browser
pause
