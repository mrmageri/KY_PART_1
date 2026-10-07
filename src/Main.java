import config.EmulatorConfig;

import javax.swing.*;
/**
 * Запускает эмулятор с параметрами командной строки.
 *
 * @param args аргументы: --vfs &lt;путь&gt; [--script &lt;путь&gt;]
 */
void main(String[] args) {
    EmulatorConfig config = EmulatorConfig.parse(args);
    SwingUtilities.invokeLater(() -> new ConsoleApp(config));
}
