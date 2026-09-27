import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class ConsoleApp {
    private final JTextArea textArea;
    private final JTextField inputField;
    private final String user;

    private static final int FRAME_HEIGHT = 500;
    private static final int FRAME_WIDTH = 800;
    private static final int FONT_SIZE = 14;

    public ConsoleApp() {
        String username = System.getProperty("user.name");
        String hostname;

        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostname = "localhost";
        }
        String frameName = "Эмулятор - " + username + "@" + hostname;
        user = username + "@" + hostname + ":";

        JFrame frame = new JFrame(frameName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setLayout(new BorderLayout());

        textArea = new JTextArea();
        textArea.setBackground(Color.BLACK);
        textArea.setForeground(Color.GREEN);
        textArea.setCaretColor(Color.GREEN);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, FONT_SIZE));
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        inputField = new JTextField();
        inputField.setBackground(Color.BLACK);
        inputField.setForeground(Color.WHITE);
        inputField.setCaretColor(Color.WHITE);
        inputField.setFont(new Font("Monospaced", Font.PLAIN, FONT_SIZE));
        inputField.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.DARK_GRAY));

        JPanel inputPanel = new JPanel(new BorderLayout());
        JLabel promptLabel = new JLabel(user);
        promptLabel.setBackground(Color.BLACK);
        promptLabel.setForeground(Color.CYAN);
        promptLabel.setOpaque(true);
        promptLabel.setFont(new Font("Monospaced", Font.PLAIN, FONT_SIZE));

        inputPanel.add(promptLabel, BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String command = inputField.getText().trim();
                    processCommand(command);
                    inputField.setText("");
                }
            }
        });
        textArea.append("Console!\n");
        frame.setVisible(true);
        inputField.requestFocusInWindow();
    }


    private void processCommand(String command) {
        if (command.isEmpty()) return;


        textArea.append(user + command + "\n");

        String argument = getArguments(command);
        switch (getCommand(command.toLowerCase())) {
                case "ls":
                    textArea.append("ls\n");
                    textArea.append((argument != null ? getArguments(command) : "Command Error!") + "\n");
                    break;
                case "cd":
                    textArea.append("cd\n");
                    textArea.append((argument != null ? getArguments(command) : "Command Error!") + "\n");
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

    private String getArguments(String command){
        if(!command.contains(" "))return null;
        String argument = command.substring(command.indexOf(" ")+1);
        if(argument.isEmpty()) return null;
        return argument;
    }

    private String parser(String command){
        if (command == null) return null;
        int start = command.indexOf('"');
        if (start == -1) return null;
        int end = command.indexOf('"', start + 1);
        if (end == -1) return null;
        return command.substring(start + 1, end);
    }
}