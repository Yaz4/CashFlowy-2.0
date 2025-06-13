@echo off




echo Starting Docker containers...
docker compose up -d

REM Optional: Wait for PostgreSQL
echo Waiting for database to start...


echo Running JavaFX application with Maven...
mvn javafx:run

