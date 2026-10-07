@echo off
echo [1/2] Only --vfs
java -cp out\production\KY_PART_1 Main --vfs .\vfs

echo.
echo [2/2] --vfs and --script
java -cp out\production\KY_PART_1 Main --vfs .\vfs --script .\scripts\test-all.txt