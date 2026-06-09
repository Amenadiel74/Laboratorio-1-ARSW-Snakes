@echo off
echo ==========================================
echo Compiling and Running Laboratorio 1: Snakes
echo ==========================================
call mvn clean verify
call mvn -q -DskipTests exec:java -Dsnakes=4
pause
