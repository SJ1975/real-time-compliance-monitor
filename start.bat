@echo off
echo.
echo  ============================================
echo   Real-Time Compliance Monitor
echo   Starting all services...
echo  ============================================
echo.

REM Check if Docker is running
docker info > nul 2>&1
if errorlevel 1 (
    echo  ERROR: Docker is not running!
    echo  Please start Docker Desktop and try again.
    pause
    exit /b 1
)

echo  [1/3] Stopping any existing containers...
docker-compose down

echo  [2/3] Building and starting all services...
docker-compose up --build -d

echo  [3/3] Waiting for services to be ready...
timeout /t 30 /nobreak > nul

echo.
echo  ============================================
echo   SUCCESS! System is running!
echo  ============================================
echo.
echo   API:           http://localhost:8080
echo   Health Check:  http://localhost:8080/actuator/health
echo   Dashboard:     http://localhost:8080/api/v1/dashboard/summary
echo   Kibana:        http://localhost:5601
echo   ElasticSearch: http://localhost:9200
echo.
echo   Press any key to open the API in browser...
pause > nul
start http://localhost:8080/api/v1/dashboard/summary