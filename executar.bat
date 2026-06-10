@echo off
title Locadora de Video Games
color 0A

echo ========================================
echo    🎮 Locadora de Video Games 🎮
echo ========================================
echo.

REM Usar o diretorio do script como base
cd /d "%~dp0"

echo 📁 Pasta atual: %CD%
echo.

REM Verificar se o Maven wrapper existe
if exist "mvnw.cmd" (
    echo 🔧 Usando Maven Wrapper...
    echo.
    echo 🚀 Compilando e executando o projeto...
    echo.
    call mvnw.cmd clean spring-boot:run
) else (
    echo 🔧 Verificando Maven...
    where mvn >nul 2>nul
    if %errorlevel% neq 0 (
        echo ❌ Maven nao encontrado!
        echo.
        echo Para executar:
        echo 1. Instale o Maven
        echo 2. OU execute: mvnw.cmd clean spring-boot:run
        pause
        exit /b 1
    )
    echo.
    echo 🚀 Compilando e executando o projeto...
    echo.
    call mvn clean spring-boot:run
)

echo.
echo ⚠️ Se apareceu erro, verifique se o Java 17 esta instalado.
pause