@echo off

echo Starting Docker containers...
docker compose up -d


echo Waiting for database to start...


echo Running JavaFX application with Maven...
mvn javafx:run

