import config.EmulatorConfig;

import java.nio.file.Path;

/**
 * Тесты для EmulatorConfig.
 */
public class ConsoleAppTest {

    static void main() {
        testParseBoth();
        testParseOnlyVfs();
        testMissingVfs();
        System.out.println("Console tests passed");
    }

    private static void testParseBoth() {
        EmulatorConfig c = EmulatorConfig.parse(new String[]{
                "--vfs", "./vfs",
                "--script", "./scripts/test-all.txt"});
        assertEq(c.vfsPath(), Path.of("./vfs"));
        assertEq(c.scriptPath(), Path.of("./scripts/test-all.txt"));
    }

    private static void testParseOnlyVfs() {
        EmulatorConfig c = EmulatorConfig.parse(new String[]{
                "--vfs", "./vfs"});
        assertEq(c.vfsPath(), Path.of("./vfs"));
        if (c.scriptPath() != null) {
            throw new AssertionError("scriptPath должен быть null");
        }
    }

    private static void testMissingVfs() {
        try {
            EmulatorConfig.parse(new String[]{});
            throw new AssertionError("Ожидалось исключение");
        } catch (IllegalArgumentException expected) {
            // ок
        }
    }

    private static void assertEq(Object a, Object b) {
        if (!a.equals(b)) {
            throw new AssertionError(a + " != " + b);
        }
    }
}
