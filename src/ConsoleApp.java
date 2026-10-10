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
        boolean result = switch (name) {
            case "ls" -> runLs(args);
            case "cd" -> runCd(args);
            case "pwd" -> runPwd();
            case "clear" -> runClear();
            case "echo" -> runEcho(args);
            case "conf-dump" -> runConfDump();
            case "vfs-save" -> runVfsSave(args);
            case "exit" -> runExit();
            default -> runUnknown(command);
        };
        textArea.setCaretPosition(textArea.getDocument().getLength());
        return result;
    }

    private boolean runLs(List<String> args) {
        if (vfs == null) {
            textArea.append("VFS is not loaded\n");
            return false;
        }
        if (args != null && args.size() > 1) {
            textArea.append("ls: too many arguments\n");
            return false;
        }
        String path = args == null || args.isEmpty()
                ? vfs.getCurrentPath()
                : args.get(0);
        try {
            String listing = vfs.list(path);
            textArea.append(listing);
            if (!listing.isEmpty() && !listing.endsWith("\n")) {
                textArea.append("\n");
            }
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
        if (args == null || args.size() != 1) {
            textArea.append("cd: missing operand\n");
            return false;
        }
        try {
            vfs.changeDirectory(args.get(0));
            return true;
        } catch (VfsException e) {
            textArea.append("cd: " + e.getMessage() + "\n");
            return false;
        }
    }

    private boolean runVfsSave(List<String> args) {
        if (vfs == null) {
            textArea.append("VFS is not loaded\n");
            return false;
        }
        if (args == null || args.size() != 1) {
            textArea.append("vfs-save: missing path\n");
            return false;
        }
        try {
            vfs.save(Path.of(args.get(0)));
            textArea.append("VFS saved to " + args.get(0) + "\n");
            return true;
        } catch (VfsException e) {
            textArea.append("vfs-save: " + e.getMessage() + "\n");
            return false;
        }
    }

    private boolean printArgs(List<String> args) {
        if (args == null) {
            textArea.append("Command Error!\n");
            return false;
        }
        for (String item : args) {
            textArea.append(item + " ");
        }
        textArea.append("\n");
        return true;
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
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < command.length(); i++) {
            char ch = command.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (Character.isWhitespace(ch) && !inQuotes) {
                if (!current.isEmpty()) {
                    result.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(ch);
            }
        }
        if (inQuotes) {
            return null;
        }
        if (!current.isEmpty()) {
            result.add(current.toString());
        }
        if (result.isEmpty()) {
            return null;
        }
        result.removeFirst();
        return result.isEmpty() ? null : result;
    }

    private void runScript() {
        if (config.scriptPath() == null) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(config.scriptPath());
            for (String line : lines) {
                String command = line.trim();
                if (command.isEmpty() || command.startsWith("#")) {
                    continue;
                }
                if (!processCommand(command)) {
                    textArea.append(
                            "Script stopped due to error: " + command + "\n");
                    return;
                }
            }
        } catch (IOException e) {
            textArea.append("Script error: " + e.getMessage() + "\n");
        }
    }
}