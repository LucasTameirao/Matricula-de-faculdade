@echo off
rem Compila e executa o Sistema de Matriculas no Windows.
cd /d "%~dp0"
if exist out rmdir /s /q out
javac --release 17 -encoding UTF-8 -d out --source-path src/main/java src/main/java/br/pucminas/matricula/Main.java
if errorlevel 1 (
    echo Erro na compilacao. Verifique se o JDK 17 ou superior esta instalado.
    pause
    exit /b 1
)
java -cp out br.pucminas.matricula.Main
pause
