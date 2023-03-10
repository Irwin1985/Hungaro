@echo off

set hungaroSourceFile=%1

REM if hungaroSourceFile is empty then we lauch java -jar %HUNGARO_HOME%\Hungaro.jar
if "%hungaroSourceFile%" == "" goto :runHungaroREPL

REM if hungaroSourceFile is not empty then we lauch java -jar %HUNGARO_HOME%\Hungaro.jar %hungaroSourceFile%
if NOT "%hungaroSourceFile%" == "" goto :runHungaroFile

REM create the label runHungaroREPL
:runHungaroREPL
echo "Running Hungaro REPL"
    java -jar %HUNGARO_HOME%\Hungaro.jar


REM create the label runHungaroFile
:runHungaroFile
    REM if hungaroSourceFile does not ends with .hgr then we add it
    if NOT "%hungaroSourceFile:~-4%" == ".hgr" set "hungaroSourceFile=%hungaroSourceFile%.hgr"

    REM check if the file exists
    if NOT EXIST %hungaroSourceFile% (
        echo "File %hungaroSourceFile% does not exist"
        goto :eof
    )

    REM remove the last character if it is a backslash
    set hungaroHome=%HUNGARO_HOME%
    if "%hungaroHome:~-1%" == "\" set "hungaroHome=%hungaroHome:~0,-1%"
    echo "Running file: %hungaroSourceFile%"
    java -jar %HUNGARO_HOME%\Hungaro.jar %hungaroSourceFile%

REM create the label endOfFile
:eof
    REM end of file