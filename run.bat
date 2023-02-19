@ECHO OFF
IF "%1"=="start" (
    ECHO Application start...
    START "AnimalShelter-1.0-SNAPSHOT" java -jar -Dspring.profiles.active=prod target\AnimalShelter-1.0-SNAPSHOT.jar
    ECHO Browser will be launched in 7 seconds
    TIMEOUT /T 7 /NOBREAK 
    EXPLORER http://localhost:8888/AnimalShelter/
    ECHO The application is running, this window can be closed
) ELSE IF "%1"=="stop" (
    ECHO Application stop...
    TASKKILL /FI "WINDOWTITLE eq AnimalShelter-1.0-SNAPSHOT"
    ECHO This window can be closed
) ELSE (
    ECHO Use start.bat and stop.bat !!!
)
pause