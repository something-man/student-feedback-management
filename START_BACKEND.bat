@echo off
title College Feedback Backend - Spring Boot (Port 8081)
echo ============================================================
echo   College Feedback Management System - Backend Server
echo   Starting on http://localhost:8081
echo   Press Ctrl+C to stop
echo ============================================================
echo.

set JAVA_HOME=C:\Program Files\Java\jdk-21.0.10
set PATH=%JAVA_HOME%\bin;%PATH%

cd /d "%~dp0backend"
call mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local

pause
