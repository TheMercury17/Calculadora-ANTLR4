@echo off
rem ==============================================================================
rem Script de compilacion y ejecucion para Windows (PowerShell / CMD)
rem ==============================================================================
setlocal enabledelayedexpansion

set ANTLR_JAR=antlr-4.13.2-complete.jar

if not exist "%ANTLR_JAR%" (
    echo No se encontro %ANTLR_JAR% en el directorio actual. Descargando...
    powershell -Command "Invoke-WebRequest -Uri 'https://www.antlr.org/download/antlr-4.13.2-complete.jar' -OutFile 'antlr-4.13.2-complete.jar'"
)

echo [1/3] Generando analizador con ANTLR 4 (Visitor)...
java -jar "%ANTLR_JAR%" -no-listener -visitor ScientificCalc.g4
if %ERRORLEVEL% neq 0 (
    echo Error al generar el analizador con ANTLR.
    exit /b %ERRORLEVEL%
)

echo [2/3] Compilando archivos Java...
javac -cp ".;%ANTLR_JAR%" *.java
if %ERRORLEVEL% neq 0 (
    echo Error al compilar los archivos Java.
    exit /b %ERRORLEVEL%
)

echo [3/3] Compilacion completada con exito.

if "%~1"=="" (
    echo Para ejecutar pruebas:
    echo   java -cp ".;%ANTLR_JAR%" Main ejemplos.txt
    echo   build.bat ejemplos.txt
) else (
    echo Ejecutando con entrada: %~1
    java -cp ".;%ANTLR_JAR%" Main "%~1"
)
