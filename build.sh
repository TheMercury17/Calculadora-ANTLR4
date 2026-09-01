#!/bin/bash
# ==============================================================================
# Script de compilacion y ejecucion para Linux (Kali / Debian / Ubuntu)
# ==============================================================================
set -e

ANTLR_JAR="./antlr-4.13.2-complete.jar"

if [ ! -f "$ANTLR_JAR" ]; then
    if [ -f "/usr/local/lib/antlr-4.13.2-complete.jar" ]; then
        ANTLR_JAR="/usr/local/lib/antlr-4.13.2-complete.jar"
    elif [ -f "$HOME/.local/lib/antlr-4.13.2-complete.jar" ]; then
        ANTLR_JAR="$HOME/.local/lib/antlr-4.13.2-complete.jar"
    elif [ -f "/usr/share/java/antlr4.jar" ]; then
        ANTLR_JAR="/usr/share/java/antlr4.jar"
    else
        echo "No se encontro antlr-4.13.2-complete.jar localmente. Descargando..."
        wget -q --show-progress https://www.antlr.org/download/antlr-4.13.2-complete.jar -O ./antlr-4.13.2-complete.jar
        ANTLR_JAR="./antlr-4.13.2-complete.jar"
    fi
fi

echo "[1/3] Generando analizador con ANTLR 4 (Visitor)..."
java -jar "$ANTLR_JAR" -no-listener -visitor ScientificCalc.g4

echo "[2/3] Compilando archivos Java..."
javac -cp ".:$ANTLR_JAR" *.java

echo "[3/3] Compilacion completada con exito."

if [ -n "$1" ]; then
    echo "Ejecutando con entrada: $1"
    java -cp ".:$ANTLR_JAR" Main "$1"
else
    echo "Uso para ejecutar:"
    echo "  java -cp .:$ANTLR_JAR Main ejemplos.txt"
    echo "  ./build.sh ejemplos.txt"
fi
