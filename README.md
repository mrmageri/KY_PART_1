# K-Y-part-1

Эмулятор консоли UNIX-подобной операционной системы на Java (Swing)
с виртуальной файловой системой, параметрами командной строки,
стартовыми скриптами и поддержкой прав доступа.

## Возможности

- Графический интерфейс на Swing, имитирующий консоль.
- Заголовок окна вида `Эмулятор - username@hostname`.
- Парсер аргументов командной строки с поддержкой двойных кавычек.
- Команды: `ls`, `cd`, `pwd`, `clear`, `echo`, `chmod`, `touch`,
  `conf-dump`, `vfs-save`, `exit`.
- Виртуальная файловая система (VFS) в памяти.
- Загрузка VFS из CSV-файла и сохранение обратно.
- Поддержка текстовых и бинарных данных (base64).
- Права доступа с восьмеричными режимами (по умолчанию `755` для
  каталогов и `644` для файлов).
- Параметры командной строки `--vfs` и `--script`.
- Отладочный вывод конфигурации при запуске.
- Стартовые скрипты с остановкой при первой ошибке.
- Обработка ошибок команд без аварийного завершения приложения.

## Требования

- JDK 21 или выше.

## Запуск

### Через скрипты запуска

Linux/macOS:

```
./run.sh
```

Windows:

```
run-windows.bat
```


## Параметры командной строки

| Параметр | Обязательный | Описание |
|---|---|---|
| `--vfs <путь>` | да | Путь к CSV-файлу VFS или к каталогу с CSV |
| `--script <путь>` | нет | Путь к стартовому скрипту |

Если `--vfs` указывает на каталог, загрузчик ищет в нём `vfs.csv`,
а при его отсутствии — первый по алфавиту файл `*.csv`.

## Команды

| Команда | Описание |
|---|---|
| `ls [путь]` | Вывести содержимое каталога VFS |
| `cd <путь>` | Перейти в каталог VFS |
| `pwd` | Показать текущий каталог VFS |
| `clear` | Очистить экран консоли |
| `echo [текст...]` | Вывести текст в консоль |
| `chmod <режим> <путь>` | Изменить права доступа узла VFS |
| `touch <путь>` | Создать пустой файл, если его нет |
| `conf-dump` | Показать параметры в формате ключ-значение |
| `vfs-save <путь>` | Сохранить состояние VFS в CSV-файл |
| `exit` | Закрыть приложение |

### Примеры

```
pwd
ls
cd /home
pwd
ls
echo "Hello from echo"
echo one two three
touch /home/newfile.txt
chmod 600 /home/newfile.txt
ls /home
vfs-save ./vfs/saved.csv
cd /no/such/path
```

## Формат VFS

Источником VFS является CSV-файл со следующей структурой:

```csv
type,path,content,encoding,permissions
dir,/,,,755
dir,/home,,,755
file,/home/readme.txt,Hello,plain,644
file,/bin/data.bin,AAECAwQ=,base64,644
```

- `type` — `dir` для каталога, `file` для файла.
- `path` — абсолютный путь внутри VFS.
- `content` — содержимое файла. Для `base64` — закодированные данные.
- `encoding` — `plain` или `base64`.
- `permissions` — трёхзначный восьмеричный режим. Столбец
  необязателен: если он отсутствует, каталоги получают `755`,
  файлы — `644`.

Строки, начинающиеся с `#`, и пустые строки игнорируются.

## Стартовые скрипты



| Скрипт | Назначение |
|---|---|
| `scripts/test-minimal.txt` | Минимальная VFS: `ls`, `cd`, `conf-dump` |
| `scripts/test-multiple.txt` | Несколько файлов и `vfs-save` |
| `scripts/test-all.txt` | Обход глубокого дерева каталогов |
| `scripts/test-error.txt` | Обработка ошибки при `cd /no/such/path` |
| `scripts/test-commands.txt` | Команды этапа 4: `pwd`, `clear`, `echo` |
| `scripts/test-additional.txt` | Команды этапа 5: `chmod`, `touch`, ошибки |

Скрипт `test-commands.txt`:

```
# тестирование команд этапа 4
pwd
ls
cd /home
pwd
ls
echo "Hello from echo"
echo
echo one two three
cd ..
pwd
clear
pwd
cd /no/such/path
```

Скрипт `test-additional.txt`:

```
# тестирование команд этапа 5
pwd
ls
touch /home/newfile.txt
ls /home
touch /home/newfile.txt
chmod 600 /home/newfile.txt
chmod 700 /home
ls /home
pwd
vfs-save ./vfs/saved-stage5.csv
chmod 999 /home/newfile.txt
```

В обоих скриптах ошибочные команды стоят последними, поэтому все
успешные операции выполняются до остановки скрипта.

## Тестирование

В проекте есть тесты для конфигурации и загрузчика VFS. Запуск:

```
java -cp out/production/KY_PART_1 ConsoleAppTest
java -cp out/production/KY_PART_1 VfsCsvLoaderTest
```

Ожидаемый вывод:

```text
Console tests passed
VFS tests passed
```
