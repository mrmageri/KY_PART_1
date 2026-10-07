# K-Y-part-1

Эмулятор консоли на Java (Swing) с поддержкой параметров
командной строки и стартовых скриптов.

## Возможности

- Команды `ls`, `cd`, `exit`, `conf-dump`
- Параметры `--vfs` и `--script`
- Отладочный вывод конфигурации при запуске
- Выполнение скрипта с остановкой при первой ошибке

## Требования

- JDK 21+

## Параметры командной строки

| Параметр | Обязательный | Описание |
|---|---|---|
| `--vfs <путь>` | да | Путь к VFS |
| `--script <путь>` | нет | Путь к стартовому скрипту |

## Команды

| Команда | Описание |
|---|---|
| `ls` | Вывести аргументы |
| `cd <путь>` | Вывести аргументы |
| `conf-dump` | Показать параметры в формате ключ-значение |
| `exit` | Закрыть приложение |


## Запуск

```bash
java -cp out/production/KY_PART_1 Main --vfs ./vfs
java -cp out/production/KY_PART_1 Main --vfs ./vfs --script ./scripts/test-all.txt
```

## Тесты

```bash
javac -d out/production/KY_PART_1 tests/ConsoleAppTest.java
java -cp out/production/KY_PART_1 ConsoleAppTest
```

Ожидаемый вывод: `Все тесты пройдены`.

## Скрипты ОС

- `scripts/run.sh` — Linux/macOS
- `scripts/run-windows.bat` — Windows

Оба тестируют запуск с `--vfs` и с `--vfs --script`.

## Скрипты эмулятора

- `scripts/test-all.txt` — успешный сценарий
- `scripts/test-error.txt` — остановка при первой ошибке

## Пример

```
=== Configuration ===
vfs.path=./vfs
script.path=./scripts/test-all.txt
=====================
roman@Mageri-2:ls
ls

roman@Mageri-2:conf-dump
vfs.path=./vfs
script.path=./scripts/test-all.txt
```