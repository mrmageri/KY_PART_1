package vfs;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

/**
 * Загрузчик VFS из CSV-файла.
 * Поддерживает путь к CSV-файлу и путь к каталогу с CSV.
 */
public final class VfsCsvLoader {

    private static final String DEFAULT_CSV = "vfs.csv";
    private static final String CSV_EXT = ".csv";

    private VfsCsvLoader() {
    }

    /**
     * Загружает VFS из CSV.
     *
     * @param vfsPath путь к CSV-файлу или каталогу с CSV
     * @return VFS
     * @throws VfsException при ошибке чтения или формата
     */
    public static Vfs load(Path vfsPath) throws VfsException {
        Path csv = resolveCsv(vfsPath);
        List<String> lines = readLines(csv);
        return parseLines(lines);
    }

    private static Path resolveCsv(Path vfsPath) throws VfsException {
        if (vfsPath == null) {
            throw new VfsException("Путь к VFS не задан");
        }
        if (Files.isRegularFile(vfsPath)) {
            return vfsPath;
        }
        if (Files.isDirectory(vfsPath)) {
            Path found = findCsv(vfsPath);
            if (found == null) {
                throw new VfsException("В каталоге нет CSV-файла: " + vfsPath);
            }
            return found;
        }
        throw new VfsException("Файл VFS не найден: " + vfsPath);
    }

    private static Path findCsv(Path dir) throws VfsException {
        Path preferred = dir.resolve(DEFAULT_CSV);
        if (Files.isRegularFile(preferred)) {
            return preferred;
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(CSV_EXT))
                    .sorted()
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            throw new VfsException("Не удалось прочитать каталог VFS: "
                    + e.getMessage(), e);
        }
    }

    private static List<String> readLines(Path csv) throws VfsException {
        try {
            return Files.readAllLines(csv, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new VfsException("Не удалось прочитать VFS: "
                    + e.getMessage(), e);
        }
    }

    private static Vfs parseLines(List<String> lines) throws VfsException {
        Vfs vfs = new Vfs();
        boolean firstRow = true;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            List<String> fields = parseCsvLine(line, i + 1);
            if (firstRow) {
                firstRow = false;
                if (fields.get(0).equalsIgnoreCase("type")) {
                    continue;
                }
            }
            loadRow(vfs, fields, i + 1);
        }
        return vfs;
    }

    private static void loadRow(Vfs vfs, List<String> fields, int lineNo)
            throws VfsException {
        if (fields.size() < 2) {
            throw new VfsException("Неверный формат CSV, строка " + lineNo);
        }
        String type = fields.get(0).trim();
        String path = fields.get(1).trim();
        String content = fields.size() > 2 ? fields.get(2) : "";
        String encoding = fields.size() > 3 ? fields.get(3).trim() : "plain";
        if ("dir".equalsIgnoreCase(type)) {
            vfs.ensureDirectory(path);
        } else if ("file".equalsIgnoreCase(type)) {
            boolean base64 = "base64".equalsIgnoreCase(encoding);
            vfs.addFile(path, decode(content, base64, lineNo), base64);
        } else {
            throw new VfsException("Неизвестный тип узла: " + type);
        }
    }

    private static byte[] decode(String content, boolean base64, int lineNo)
            throws VfsException {
        if (!base64) {
            return content.getBytes(StandardCharsets.UTF_8);
        }
        try {
            return Base64.getDecoder().decode(content);
        } catch (IllegalArgumentException e) {
            throw new VfsException("Неверный base64, строка " + lineNo, e);
        }
    }

    private static List<String> parseCsvLine(String line, int lineNo)
            throws VfsException {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(ch);
                }
            } else if (ch == '"') {
                inQuotes = true;
            } else if (ch == ',') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        if (inQuotes) {
            throw new VfsException("Незакрытая кавычка, строка " + lineNo);
        }
        fields.add(current.toString());
        return fields;
    }
}