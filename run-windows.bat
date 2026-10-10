@echo off
set CP=out\production\KY_PART_1

echo [1/5] minimal VFS
java -cp %CP% Main --vfs .\vfs\minimal.csv --script .\scripts\test-minimal.txt

echo.
echo [2/5] multiple files
java -cp %CP% Main --vfs .\vfs\multiple.csv --script .\scripts\test-multiple.txt

echo.
echo [3/5] deep tree
java -cp %CP% Main --vfs .\vfs\deep.csv --script .\scripts\test-all.txt

echo.
echo [4/5] error handling
java -cp %CP% Main --vfs .\vfs\multiple.csv --script .\scripts\test-error.txt

echo.
echo [5/5] stage 4 commands
java -cp %CP% Main --vfs .\vfs\minimal.csv --script .\scripts\test-commands.txt