import config.EmulatorConfig;
import vfs.Vfs;
import vfs.VfsCsvLoader;
import vfs.VfsException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Эмулятор консоли с графическим интерфейсом на Swing.
 */
public class ConsoleApp {

    private final JTextArea textArea;
    private final JTextField inputField;
    private final String user;
    private final EmulatorConfig config;
    private Vfs vfs;

    private static final int FRAME_HEIGHT = 500;
    private static final int FRAME_WIDTH = 800;
    private static final int FONT_SIZE = 14;
    private static final int MIN_SIZE = 2;
    private static final int SINGLE_ARGUMENT = 1;
    private static final int MIN_FLAG_ARG_LENGTH = 2;
    private static final int PERMISSION_MODE_LENGTH = 3;
    private static final int PERMISSION_READ_BIT = 4;
    private static final int PERMISSION_WRITE_BIT = 2;
    private static final int PERMISSION_EXEC_BIT = 1;
    private static final long KILOBYTE = 1024L;
    private static final long MEGABYTE = KILOBYTE * KILOBYTE;
    private static final long GIGABYTE = MEGABYTE * KILOBYTE;
    private static final String HIDDEN_PREFIX = ".";

    /**
     * Создаёт окно эмулятора с заданной конфигурацией.
     *
     * @param config конфигурация из аргументов командной строки
     */
    public ConsoleApp(EmulatorConfig config) {
        this.config = config;
        String username = System.getProperty("user.name");
        String hostname = resolveHostname();
        String frameName = "Эмулятор - " + username + "@" + hostname;
        user = username + "@" + hostname + ":";

        JFrame frame = createFrame(frameName);
        textArea = createTextArea();
        inputField = createInputField();

        frame.add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.add(createInputPanel(), BorderLayout.SOUTH);

        setupEnterListener();
        textArea.append("Console!\n");
        loadVfs();
        dumpConfigAtStartup();

        frame.setVisible(true);
        inputField.requestFocusInWindow();
        runScript();
    }

    private static final class LsOptions {
        boolean all;
        boolean longFormat;
        boolean humanReadable;
        boolean help;
        String path;
    }

    private void loadVfs() {
        try {
            vfs = VfsCsvLoader.load(config.vfsPath());
            textArea.append("VFS loaded: " + config.vfsPath() + "\n");
        } catch (VfsException e) {
            vfs = null;
            textArea.append("VFS load error: " + e.getMessage() + "\n");
        }
    }

    private String resolveHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    private JFrame createFrame(String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setLayout(new BorderLayout());
        return frame;
    }

    private JTextArea createTextArea() {
        JTextArea area = new JTextArea();
        area.setBackground(Color.BLACK);
        area.setForeground(Color.GREEN);
        area.setCaretColor(Color.GREEN);
        area.setFont(new Font("Monospaced", Font.PLAIN, FONT_SIZE));
        area.setEditable(false);
        return area;
    }

    private JTextField createInputField() {
        JTextField field = new JTextField();
        field.setBackground(Color.BLACK);
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setFont(new Font("Monospaced", Font.PLAIN, FONT_SIZE));
        field.setBorder(BorderFactory.createMatteBorder(
                1, 0, 0, 0, Color.DARK_GRAY));
        return field;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel prompt = new JLabel(user);
        prompt.setBackground(Color.BLACK);
        prompt.setForeground(Color.CYAN);
        prompt.setOpaque(true);
        prompt.setFont(new Font("Monospaced", Font.PLAIN, FONT_SIZE));
        panel.add(prompt, BorderLayout.WEST);
        panel.add(inputField, BorderLayout.CENTER);
        return panel;
    }

    private void setupEnterListener() {
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    processCommand(inputField.getText().trim());
                    inputField.setText("");
                }
            }
        });
    }

    private void dumpConfigAtStartup() {
        textArea.append("=== Configuration ===\n");
        textArea.append("vfs.path=" + config.vfsPath() + "\n");
        textArea.append("script.path=" + scriptPathText() + "\n");
        textArea.append("vfs.loaded=" + (vfs != null) + "\n");
        if (vfs != null) {
            textArea.append("vfs.current=" + vfs.getCurrentPath() + "\n");
        }
        textArea.append("=====================\n");
    }

    private String scriptPathText() {
        return config.scriptPath() != null
                ? config.scriptPath().toString()
                : "none";
    }

    /**
     * Выполняет команду эмулятора.
     *
     * @param command строка команды
     * @return {@code true}, если без ошибок
     */
    public boolean processCommand(String command) {
        if (command.isEmpty()) {
            return true;
        }
        textArea.append(user + command + "\n");
        List<String> args = getArguments(command);
        String name = getCommand(command.toLowerCase());
        boolean result = executeCommand(name, args, command);
        textArea.setCaretPosition(textArea.getDocument().getLength());
        return result;
    }

    private boolean executeCommand(String name, List<String> args, String command) {
        Boolean result = runCommonCommand(name, args);
        return result != null ? result : runExtraCommand(name, args, command);
    }

    private Boolean runCommonCommand(String name, List<String> args) {
        return switch (name) {
            case "ls" -> runLs(args);
            case "cd" -> runCd(args);
            case "pwd" -> runPwd();
            case "clear" -> runClear();
            case "echo" -> runEcho(args);
            case "exit" -> runExit();
            default -> null;
        };
    }

    private boolean runExtraCommand(String name, List<String> args, String command) {
        return switch (name) {
            case "chmod" -> runChmod(args);
            case "touch" -> runTouch(args);
            case "conf-dump" -> runConfDump();
            case "vfs-save" -> runVfsSave(args);
            default -> runUnknown(command);
        };
    }

    /**
     * Разбирает аргументы команды ls.
     *
     * @param args аргументы (может быть {@code null})
     * @return опции или {@code null} при ошибке
     */
    private LsOptions parseLsOptions(List<String> args) {
        LsOptions opts = new LsOptions();
        if (args == null) {
            return opts;
        }
        for (String arg : args) {
            if ("--help".equals(arg)) {
                opts.help = true;
            } else if (arg.startsWith("-")
                    && arg.length() >= MIN_FLAG_ARG_LENGTH) {
                if (!applyFlagCluster(arg, opts)) {
                    return null;
                }
            } else if (opts.path == null) {
                opts.path = arg;
            } else {
                return null;
            }
        }
        return opts;
    }

    /**
     * Применяет кластер коротких флагов (например, {@code -la}).
     *
     * @param arg  строка, начинающаяся с {@code -}
     * @param opts опции для изменения
     * @return {@code false}, если встретился неизвестный флаг
     */
    private boolean applyFlagCluster(String arg, LsOptions opts) {
        for (int i = 1; i < arg.length(); i++) {
            switch (arg.charAt(i)) {
                case 'a' -> opts.all = true;
                case 'l' -> opts.longFormat = true;
                case 'h' -> opts.humanReadable = true;
                default -> {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Форматирует строку прав доступа в символьный вид.
     *
     * @param mode трёхзначный восьмеричный режим (например, {@code "644"})
     * @return символьная строка (например, {@code "rw-r--r--"})
     */
    private String permissionsToSymbolic(String mode) {
        if (mode == null || mode.length() != PERMISSION_MODE_LENGTH) {
            return "---------";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < PERMISSION_MODE_LENGTH; i++) {
            int digit = mode.charAt(i) - '0';
            sb.append((digit & PERMISSION_READ_BIT) != 0 ? 'r' : '-');
            sb.append((digit & PERMISSION_WRITE_BIT) != 0 ? 'w' : '-');
            sb.append((digit & PERMISSION_EXEC_BIT) != 0 ? 'x' : '-');
        }
        return sb.toString();
    }

    /**
     * Приводит размер в байтах к человекочитаемому виду.
     *
     * @param bytes размер в байтах
     * @return строка вида {@code "1.5K"} или {@code "12M"}
     */
    private String humanSize(long bytes) {
        if (bytes < KILOBYTE) {
            return bytes + "B";
        }
        if (bytes < MEGABYTE) {
            return String.format("%.1fK", bytes / (double) KILOBYTE);
        }
        if (bytes < GIGABYTE) {
            return String.format("%.1fM", bytes / (double) MEGABYTE);
        }
        return String.format("%.1fG", bytes / (double) GIGABYTE);
    }

    /**
     * Печатает записи листинга с учётом опций.
     *
     * @param entries записи
     * @param opts    опции ls
     */
    private void printEntries(List<Vfs.VfsEntry> entries, LsOptions opts) {
        StringBuilder sb = new StringBuilder();
        for (Vfs.VfsEntry entry : entries) {
            if (!opts.all && entry.name().startsWith(HIDDEN_PREFIX)) {
                continue;
            }
            if (opts.longFormat) {
                sb.append(formatLong(entry, opts.humanReadable));
            } else {
                sb.append(entry.name());
                if (entry.directory()) {
                    sb.append('/');
                }
                sb.append('\n');
            }
        }
        textArea.append(sb.toString());
    }

    /**
     * Форматирует одну запись в стиле {@code ls -l}.
     *
     * @param entry запись
     * @param human человекочитаемый размер
     * @return строка листинга
     */
    private String formatLong(Vfs.VfsEntry entry, boolean human) {
        String size = human
                ? humanSize(entry.size())
                : Long.toString(entry.size());
        return String.format("%s%s %8s %s%s\n",
                entry.directory() ? 'd' : '-',
                permissionsToSymbolic(entry.permissions()),
                size,
                entry.name(),
                entry.directory() ? "/" : "");
    }

    /**
     * Печатает краткую справку по команде ls.
     */
    private void printLsHelp() {
        textArea.append("Usage: ls [-alh] [--help] [path]\n");
        textArea.append("  -a  show hidden files\n");
        textArea.append("  -l  long format\n");
        textArea.append("  -h  human-readable sizes (use with -l)\n");
    }

    private boolean runLs(List<String> args) {
        if (vfs == null) {
            textArea.append("VFS is not loaded\n");
            return false;
        }
        LsOptions opts = parseLsOptions(args);
        if (opts == null) {
            textArea.append("ls: invalid arguments\n");
            return false;
        }
        if (opts.help) {
            printLsHelp();
            return true;
        }
        String path = opts.path != null ? opts.path : vfs.getCurrentPath();
        try {
            printEntries(vfs.listEntries(path), opts);
            return true;
        } catch (VfsException e) {
            textArea.append("ls: " + e.getMessage() + "\n");
            return false;
        }
    }


    private boolean runCd(List<String> args) {
        if (vfs == null) {
            textArea.append("VFS is not loaded\n");
            return false;
        }
        if (args == null || args.size() != SINGLE_ARGUMENT) {
            textArea.append("cd: missing operand\n");
            return false;
        }
        try {
            vfs.changeDirectory(args.getFirst());
            return true;
        } catch (VfsException e) {
            textArea.append("cd: " + e.getMessage() + "\n");
            return false;
        }
    }

    private boolean runChmod(List<String> args) {
        if (vfs == null) {
            textArea.append("VFS is not loaded\n");
            return false;
        }
        if (args == null || args.size() != MIN_SIZE) {
            textArea.append("chmod: usage: chmod <mode> <path>\n");
            return false;
        }
        try {
            vfs.changePermissions(args.get(SINGLE_ARGUMENT), args.getFirst());
            return true;
        } catch (VfsException e) {
            textArea.append("chmod: " + e.getMessage() + "\n");
            return false;
        }
    }

    private boolean runTouch(List<String> args) {
        if (vfs == null) {
            textArea.append("VFS is not loaded\n");
            return false;
        }
        if (args == null || args.size() != SINGLE_ARGUMENT) {
            textArea.append("touch: missing operand\n");
            return false;
        }
        try {
            vfs.touch(args.getFirst());
            return true;
        } catch (VfsException e) {
            textArea.append("touch: " + e.getMessage() + "\n");
            return false;
        }
    }

    private boolean runVfsSave(List<String> args) {
        if (vfs == null) {
            textArea.append("VFS is not loaded\n");
            return false;
        }
        if (args == null || args.size() != SINGLE_ARGUMENT) {
            textArea.append("vfs-save: missing path\n");
            return false;
        }
        try {
            vfs.save(Path.of(args.getFirst()));
            textArea.append("VFS saved to " + args.getFirst() + "\n");
            return true;
        } catch (VfsException e) {
            textArea.append("vfs-save: " + e.getMessage() + "\n");
            return false;
        }
    }

    private boolean runConfDump() {
        textArea.append("vfs.path=" + config.vfsPath() + "\n");
        textArea.append("script.path=" + scriptPathText() + "\n");
        textArea.append("vfs.loaded=" + (vfs != null) + "\n");
        if (vfs != null) {
            textArea.append("vfs.current=" + vfs.getCurrentPath() + "\n");
        }
        return true;
    }

    private boolean runPwd() {
        if (vfs == null) {
            textArea.append("VFS is not loaded\n");
            return false;
        }
        textArea.append(vfs.getCurrentPath() + "\n");
        return true;
    }

    private boolean runClear() {
        textArea.setText("");
        return true;
    }

    private boolean runEcho(List<String> args) {
        if (args == null || args.isEmpty()) {
            textArea.append("\n");
            return true;
        }
        textArea.append(String.join(" ", args) + "\n");
        return true;
    }

    private boolean runExit() {
        System.exit(0);
        return true;
    }

    private boolean runUnknown(String command) {
        textArea.append("Error: command '" + command + "' not found.\n");
        return false;
    }

    private String getCommand(String command) {
        int spaceIndex = command.indexOf(' ');
        if (spaceIndex == -1) {
            return command;
        }
        return command.substring(0, spaceIndex);
    }

    private List<String> getArguments(String command) {
        if (!command.contains(" ")) {
            return null;
        }

        List<String> tokens = parseTokens(command);
        if (tokens == null || tokens.size() <= SINGLE_ARGUMENT) {
            return null;
        }

        tokens.removeFirst();
        return tokens;
    }

    private List<String> parseTokens(String command) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < command.length(); i++) {
            char ch = command.charAt(i);

            if (ch == '"') {
                inQuotes = !inQuotes;
            } else if (Character.isWhitespace(ch) && !inQuotes) {
                addToken(result, current);
            } else {
                current.append(ch);
            }
        }

        if (inQuotes) {
            return null;
        }

        addToken(result, current);
        return result;
    }

    private void addToken(List<String> result, StringBuilder current) {
        if (!current.isEmpty()) {
            result.add(current.toString());
            current.setLength(0);
        }
    }


    private void runScript() {
        if (config.scriptPath() == null) {
            return;
        }

        try {
            processLines(Files.readAllLines(config.scriptPath()));
        } catch (IOException e) {
            textArea.append("Script error: " + e.getMessage() + "\n");
        }
    }

    private void processLines(List<String> lines) {
        for (String line : lines) {
            String command = line.trim();
            if (shouldSkip(command)) {
                continue;
            }
            if (!processCommand(command)) {
                textArea.append("Script stopped due to error: " + command + "\n");
                return;
            }
        }
    }

    private boolean shouldSkip(String command) {
        return command.isEmpty() || command.startsWith("#");
    }
}
