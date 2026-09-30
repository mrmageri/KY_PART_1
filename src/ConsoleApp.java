import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


/**
 * Эмулятор консоли с графическим интерфейсом на Swing.
 * Поддерживает команды ls, cd и exit.
 */
public class ConsoleApp {
    private final JTextArea textArea;
    private final JTextField inputField;
    private final String user;

    private static final int FRAME_HEIGHT = 500;
    private static final int FRAME_WIDTH = 800;
    private static final int FONT_SIZE = 14;

    /**
     * Создаёт окно эмулятора, инициализирует компоненты
     * и подписывается на события ввода.
     */
    public ConsoleApp() {
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

        frame.setVisible(true);
        inputField.requestFocusInWindow();
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
        field.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.DARK_GRAY));
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


    private void processCommand(String command) {
        if (command.isEmpty()) return;


        textArea.append(user + command + "\n");

        List<String> argument = getArguments(command);
        switch (getCommand(command.toLowerCase())) {
                case "ls":
                    textArea.append("ls\n");
                    if (argument != null) {
                        argument.forEach(item -> textArea.append(item + " "));
                    } else {
                        textArea.append("Command Error!");
                    }
                    textArea.append("\n");
                    break;
                case "cd":
                    textArea.append("cd\n");
                    if (argument != null) {
                        argument.forEach(item -> textArea.append(item + " "));
                    } else {
                        textArea.append("Command Error!");
                    }
                    break;
                case "exit":
                    System.exit(0);
                    break;
                default:
                    textArea.append("Error: command '" + command + "' not found.\n");
                    break;

        }

        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
    private String getCommand(String command){
        if(command.contains(" ")){
            int commandStart = 0;
            int commandEnd = command.indexOf(" ");
            return command.substring(commandStart,commandEnd);
        } else {
            return command;
        }
    }

    private List<String> getArguments(String command) {
        if (!command.contains(" ")) return null;
        long quoteCount = command.chars().filter(ch -> ch == '"').count();
        if (quoteCount % 2 != 0) return null;
        String[] parts = command.trim().split("\\s+");
        List<String> res = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            res.add(parts[i].contains("\"")? parseQuoted(parts[i]) : parts[i]);
        }
        return res.isEmpty() ? null : res;
    }
    /**
     * Извлекает текст, заключённый в двойные кавычки.
     *
     * @param command строка для разбора
     * @return содержимое кавычек или {@code null}, если кавычек нет
     */
    private String parseQuoted (String command){
        if (command == null) return null;
        int start = command.indexOf('"');
        if (start == -1) return null;
        int end = command.indexOf('"', start + 1);
        if (end == -1) return null;
        return command.substring(0,start) + command.substring(start + 1, end);
    }
}