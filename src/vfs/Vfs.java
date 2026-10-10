package vfs;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Base64;
import java.util.Deque;

/**
 * Виртуальная файловая система в памяти.
 */
public class Vfs {

    private static final String ROOT = "/";

    private final VfsNode root = new VfsNode("", true);
    private String currentPath = ROOT;

    public String getCurrentPath() {
        return currentPath;
    }

    /**
     * Создаёт каталог и все его родительские каталоги.
     *
     * @param path путь
     * @throws VfsException если путь конфликтует с файлом
     */
    public void ensureDirectory(String path) throws VfsException {
        String normalized = normalize(path);
        if (ROOT.equals(normalized)) {
            return;
        }
        VfsNode node = root;
        for (String part : split(normalized)) {
            VfsNode child = node.getChild(part);
            if (child == null) {
                child = new VfsNode(part, true);
                child.setParent(node);
                node.addChild(child);
            } else if (!child.isDirectory()) {
                throw new VfsException("Путь не является каталогом: " + normalized);
            }
            node = child;
        }
    }

    /**
     * Добавляет или заменяет файл.
     *
     * @param path путь
     * @param data данные
     * @param base64 признак base64
     * @throws VfsException при ошибке
     */
    public void addFile(String path, byte[] data, boolean base64) throws VfsException {
        String normalized = normalize(path);
        if (ROOT.equals(normalized)) {
            throw new VfsException("Нельзя создать файл в корне");
        }
        String[] parts = split(normalized);
        String name = parts[parts.length - 1];
        String parentPath = parentPath(normalized);
        ensureDirectory(parentPath);
        VfsNode parent = resolveNode(parentPath);
        VfsNode existing = parent.getChild(name);
        if (existing != null && existing.isDirectory()) {
            throw new VfsException("Каталог уже существует: " + normalized);
        }
        VfsNode file = existing != null ? existing : new VfsNode(name, false);
        if (existing == null) {
            file.setParent(parent);
            parent.addChild(file);
        }
        file.setData(data);
        file.setBase64(base64);
    }

    /**
     * Возвращает содержимое каталога.
     *
     * @param path путь
     * @return строки листинга
     * @throws VfsException при ошибке
     */
    public String list(String path) throws VfsException {
        VfsNode node = resolveNode(path);
        if (!node.isDirectory()) {
            throw new VfsException("Не каталог: " + path);
        }
        StringBuilder out = new StringBuilder();
        for (VfsNode child : node.getChildren()) {
            out.append(child.getName());
            if (child.isDirectory()) {
                out.append('/');
            }
            out.append('\n');
        }
        return out.toString();
    }

    /**
     * Меняет текущий каталог.
     *
     * @param path путь
     * @throws VfsException при ошибке
     */
    public void changeDirectory(String path) throws VfsException {
        VfsNode node = resolveNode(path);
        if (!node.isDirectory()) {
            throw new VfsException("Не каталог: " + path);
        }
        currentPath = normalize(path);
    }

    /**
     * Сохраняет VFS в CSV.
     *
     * @param target целевой файл
     * @throws VfsException при ошибке
     */
    public void save(Path target) throws VfsException {
        try {
            Path parent = target.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (PrintWriter out = new PrintWriter(
                    Files.newBufferedWriter(target, StandardCharsets.UTF_8))) {
                out.println("type,path,content,encoding");
                out.println("dir,/,,");
                for (VfsNode child : root.getChildren()) {
                    saveNode(child, ROOT, out);
                }
            }
        } catch (IOException e) {
            throw new VfsException("Не удалось сохранить VFS: " + e.getMessage(), e);
        }
    }

    private void saveNode(VfsNode node, String parentPath, PrintWriter out) {
        String path = ROOT.equals(parentPath)
                ? ROOT + node.getName()
                : parentPath + ROOT + node.getName();
        if (node.isDirectory()) {
            out.println("dir," + csv(path) + ",,");
            for (VfsNode child : node.getChildren()) {
                saveNode(child, path, out);
            }
        } else {
            String content = node.isBase64()
                    ? Base64.getEncoder().encodeToString(node.getData())
                    : new String(node.getData(), StandardCharsets.UTF_8);
            String encoding = node.isBase64() ? "base64" : "plain";
            out.println("file," + csv(path) + "," + csv(content) + "," + encoding);
        }
    }

    private VfsNode resolveNode(String path) throws VfsException {
        String normalized = normalize(path);
        if (ROOT.equals(normalized)) {
            return root;
        }
        VfsNode node = root;
        for (String part : split(normalized)) {
            if (!node.isDirectory()) {
                throw new VfsException("Не каталог: " + normalized);
            }
            node = node.getChild(part);
            if (node == null) {
                throw new VfsException("Путь не найден: " + path);
            }
        }
        return node;
    }

    private String normalize(String path) {
        String base = path == null || path.isEmpty() ? currentPath : path;
        String combined = base.startsWith(ROOT) ? base : currentPath + ROOT + base;
        Deque<String> stack = new ArrayDeque<>();
        for (String part : combined.split(ROOT)) {
            if (part.isEmpty() || ".".equals(part)) {
                continue;
            }
            if ("..".equals(part)) {
                if (!stack.isEmpty()) {
                    stack.removeLast();
                }
            } else {
                stack.addLast(part);
            }
        }
        return stack.isEmpty() ? ROOT : ROOT + String.join(ROOT, stack);
    }

    private String parentPath(String normalized) {
        int index = normalized.lastIndexOf('/');
        return index <= 0 ? ROOT : normalized.substring(0, index);
    }

    private String[] split(String normalized) {
        String value = normalized.startsWith(ROOT)
                ? normalized.substring(1)
                : normalized;
        return value.isEmpty() ? new String[0] : value.split(ROOT);
    }

    private String csv(String value) {
        if (value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}