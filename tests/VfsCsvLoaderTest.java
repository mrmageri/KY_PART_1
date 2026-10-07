import vfs.Vfs;
import vfs.VfsCsvLoader;
import vfs.VfsException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * Тесты загрузчика VFS из CSV.
 */
public class VfsCsvLoaderTest {

    public static void main(String[] args) throws Exception {
        testLoadMinimal();
        testLoadBase64();
        testMissingFile();
        testSaveAndLoad();
        System.out.println("VFS tests passed");
    }

    private static void testLoadMinimal() throws Exception {
        Path csv = Files.createTempFile("vfs-min", ".csv");
        Files.writeString(csv,
                "type,path,content,encoding\n" +
                        "dir,/home,,\n" +
                        "file,/home/readme.txt,Hello,plain\n",
                StandardCharsets.UTF_8);

        Vfs vfs = VfsCsvLoader.load(csv);
        assertEq(vfs.getCurrentPath(), "/");
        String listing = vfs.list("/home");
        if (!listing.contains("readme.txt")) {
            throw new AssertionError("readme.txt not found");
        }
        Files.deleteIfExists(csv);
    }

    private static void testLoadBase64() throws Exception {
        Path csv = Files.createTempFile("vfs-b64", ".csv");
        byte[] data = new byte[]{0, 1, 2, 3, 4};
        String b64 = Base64.getEncoder().encodeToString(data);
        Files.writeString(csv,
                "type,path,content,encoding\n" +
                        "file,/bin/data.bin," + b64 + ",base64\n",
                StandardCharsets.UTF_8);

        Vfs vfs = VfsCsvLoader.load(csv);
        // Проверяем, что загрузка не упала.
        if (vfs == null) {
            throw new AssertionError("VFS is null");
        }
        Files.deleteIfExists(csv);
    }

    private static void testMissingFile() {
        try {
            VfsCsvLoader.load(Path.of("no-such-file.csv"));
            throw new AssertionError("Expected VfsException");
        } catch (VfsException expected) {
            // ок
        }
    }

    private static void testSaveAndLoad() throws Exception {
        Path csv = Files.createTempFile("vfs-save", ".csv");
        Files.writeString(csv,
                "type,path,content,encoding\n" +
                        "dir,/home,,\n" +
                        "file,/home/a.txt,A,plain\n",
                StandardCharsets.UTF_8);

        Vfs vfs = VfsCsvLoader.load(csv);
        Path saved = Files.createTempFile("vfs-saved", ".csv");
        vfs.save(saved);

        Vfs loaded = VfsCsvLoader.load(saved);
        String listing = loaded.list("/home");
        if (!listing.contains("a.txt")) {
            throw new AssertionError("a.txt not found after save/load");
        }
        Files.deleteIfExists(csv);
        Files.deleteIfExists(saved);
    }

    private static void assertEq(Object a, Object b) {
        if (!a.equals(b)) {
            throw new AssertionError(a + " != " + b);
        }
    }
}