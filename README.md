# K-Y-part-1

Эмулятор консоли UNIX-подобной операционной системы на Java (Swing) с виртуальной
файловой системой, параметрами командной строки и стартовыми скриптами.

## Возможности

- Графический интерфейс на Swing, имитирующий консоль.
- Заголовок окна вида `Эмулятор - username@hostname`.
- Парсер аргументов командной строки с поддержкой двойных кавычек.
- Команды: `ls`, `cd`, `pwd`, `clear`, `echo`, `conf-dump`, `vfs-save`, `exit`.
- Виртуальная файловая система (VFS) в памяти.
- Загрузка VFS из CSV-файла.
- Поддержка текстовых и бинарных данных (base64).
- Сохранение состояния VFS обратно в CSV.
- Параметры командной строки `--vfs` и `--script`.
- Отладочный вывод конфигурации при запуске.
- Стартовые скрипты с остановкой при первой ошибке.
- Обработка ошибок команд без аварийного завершения приложения.

## Требования

- JDK 21 или выше.

## Структура репозитория

```text
.
├── README.md
├── .gitignore
├── run.sh
├── run-windows.bat
├── src/
│   ├── Main.java
│   ├── ConsoleApp.java
│   ├── config/
│   │   └── EmulatorConfig.java
│   └── vfs/
│       ├── Vfs.java
│       ├── VfsNode.java
│       ├── VfsCsvLoader.java
│       └── VfsException.java
├── tests/
│   ├── ConsoleAppTest.java
│   └── VfsCsvLoaderTest.java
├── vfs/
│   ├── minimal.csv
│   ├── multiple.csv
│   ├── deep.csv
│   └── saved.csv
└── scripts/
    ├── test-minimal.txt
    ├── test-multiple.txt
    ├── test-all.txt
    ├── test-error.txt
    └── test-commands.txt