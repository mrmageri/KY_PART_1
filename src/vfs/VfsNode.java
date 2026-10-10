package vfs;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Узел виртуальной файловой системы: каталог или файл.
 */
public class VfsNode {

    private static final String DEFAULT_DIR_PERMISSIONS = "755";
    private static final String DEFAULT_FILE_PERMISSIONS = "644";

    private final String name;
    private final boolean directory;
    private final Map<String, VfsNode> children = new TreeMap<>();
    private VfsNode parent;
    private byte[] data = new byte[0];
    private boolean base64;
    private String permissions;

    /**
     * Создаёт узел.
     *
     * @param name имя
     * @param directory признак каталога
     */
    public VfsNode(String name, boolean directory) {
        this.name = name;
        this.directory = directory;
        this.permissions = directory
                ? DEFAULT_DIR_PERMISSIONS
                : DEFAULT_FILE_PERMISSIONS;
    }

    public String getName() {
        return name;
    }

    public boolean isDirectory() {
        return directory;
    }

    public VfsNode getParent() {
        return parent;
    }

    public void setParent(VfsNode parent) {
        this.parent = parent;
    }

    public void addChild(VfsNode child) {
        children.put(child.getName(), child);
    }

    public VfsNode getChild(String childName) {
        return children.get(childName);
    }

    public Collection<VfsNode> getChildren() {
        return children.values();
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data == null ? new byte[0] : data;
    }

    public boolean isBase64() {
        return base64;
    }

    public void setBase64(boolean base64) {
        this.base64 = base64;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }
}