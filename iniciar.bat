@echo off
cd /d "%~dp0"

echo ========================================
echo   Iniciando Locadora de Video Games
echo ========================================
echo.

echo Verificando Java...
java -version
if %errorlevel% neq 0 (
    echo Java nao encontrado! Instale JDK 17
    pause
    exit /b 1
)

echo.
echo Verificando Maven...
call mvn --version
if %errorlevel% neq 0 (
    if exist "mvnw.cmd" (
        echo Usando Maven Wrapper...
        call mvnw.cmd --version
    ) else (
        echo Maven nao encontrado!
        pause
        exit /b 1
    )
)

echo.
echo Iniciando o projeto...
call mvn clean spring-boot:run

pause