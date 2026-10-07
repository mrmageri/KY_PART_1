package config;

import java.nio.file.Path;

/**
 * Конфигурация эмулятора из аргументов командной строки.
 *
 * @param vfsPath путь к VFS
 * @param scriptPath путь к скрипту или {@code null}
 */
public record EmulatorConfig(Path vfsPath, Path scriptPath) {

    private static final String VFS_OPTION = "--vfs";
    private static final String SCRIPT_OPTION = "--script";

    /**
     * Разбирает аргументы командной строки.
     *
     * @param args аргументы
     * @return конфигурация
     */
    public static EmulatorConfig parse(String[] args) {
        Path vfs = null;
        Path script = null;
        int i = 0;
        while (i < args.length) {
            String option = args[i];
            if (VFS_OPTION.equals(option)) {
                vfs = readPath(args, ++i, VFS_OPTION);
            } else if (SCRIPT_OPTION.equals(option)) {
                script = readPath(args, ++i, SCRIPT_OPTION);
            } else {
                throw new IllegalArgumentException(
                        "Неизвестный параметр: " + option);
            }
            i++;
        }
        if (vfs == null) {
            throw new IllegalArgumentException(
                    "Параметр " + VFS_OPTION + " обязателен");
        }
        return new EmulatorConfig(vfs, script);
    }

    private static Path readPath(String[] args, int index, String option) {
        if (index >= args.length) {
            throw new IllegalArgumentException(
                    "Не указан путь для " + option);
        }
        return Path.of(args[index]);
    }
}