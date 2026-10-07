import config.EmulatorConfig;

import javax.swing.*;
/**
 * Запускает эмулятор.
 *
 * @param args аргументы командной строки
 */
void main(String[] args) {
    EmulatorConfig config = EmulatorConfig.parse(args);
    SwingUtilities.invokeLater(() -> new ConsoleApp(config));
}
