import config.EmulatorConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Эмулятор консоли с графическим интерфейсом на Swing.
 * Поддерживает команды ls, cd, exit, conf-dump.
 */
public class ConsoleApp {

    private final JTextArea textArea;
    private final JTextField inputField;
    private final String user;
    private final EmulatorConfig config;

    private static final int FRAME_HEIGHT = 500;
    private static final int FRAME_WIDTH = 800;
    private static final int FONT_SIZE = 14;
    private static final int QUOTE_PAIR = 2;

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
        dumpConfigAtStartup();

        frame.setVisible(true);
        inputField.requestFocusInWindow();
        runScript();
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
            case "conf-dump" -> runConfDump();
            case "exit" -> runExit();
            default -> runUnknown(command);
        };
        textArea.setCaretPosition(textArea.getDocument().getLength());
        return result;
    }

    private boolean runLs(List<String> args) {
        textArea.append("ls\n");
        return printArgs(args);
    }

    private boolean runCd(List<String> args) {
        textArea.append("cd\n");
        return printArgs(args);
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
        long quoteCount = command.chars().filter(ch -> ch == '"').count();
        if (quoteCount % QUOTE_PAIR != 0) {
            return null;
        }
        String[] parts = command.trim().split("\\s+");
        List<String> result = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            result.add(parts[i].contains("\"")
                    ? parseQuoted(parts[i])
                    : parts[i]);
        }
        return result.isEmpty() ? null : result;
    }

    /**
     * Извлекает текст, заключённый в двойные кавычки.
     *
     * @param command строка для разбора
     * @return содержимое кавычек или {@code null}, если кавычек нет
     */
    private String parseQuoted(String command) {
        if (command == null) {
            return null;
        }
        int start = command.indexOf('"');
        if (start == -1) {
            return null;
        }
        int end = command.indexOf('"', start + 1);
        if (end == -1) {
            return null;
        }
        return command.substring(0, start) + command.substring(start + 1, end);
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