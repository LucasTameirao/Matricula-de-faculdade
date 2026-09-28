#!/usr/bin/env bash
# Compila e executa o Sistema de Matrículas.
set -e
cd "$(dirname "$0")"
rm -rf out
javac --release 17 -encoding UTF-8 -d out --source-path src/main/java src/main/java/br/pucminas/matricula/Main.java
java -cp out br.pucminas.matricula.Main
