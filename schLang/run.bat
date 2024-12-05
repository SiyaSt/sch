@echo off
rem Проверка, что передан файл
if "%~1"=="" (
    echo Usage: run.bat ^<filename.mylang^>
    exit /b 1
)

rem Получение имени исходного файла
set INPUT_FILE=%~1

rem Проверка существования файла
if not exist "%INPUT_FILE%" (
    echo Error: File "%INPUT_FILE%" not found!
    exit /b 1
)

rem Компиляция исходного файла
echo Compiling...
javac -d out src\main\java\itmo\anastasiya\*.java

rem Проверка успешной компиляции
if %errorlevel% neq 0 (
    echo Compilation failed!
    exit /b 1
)

rem Запуск основной программы Main
echo Running program...
java -cp out itmo.anastasiya.Main "%INPUT_FILE%"

echo Execution complete!
rem Завершение работы скрипта
pause
