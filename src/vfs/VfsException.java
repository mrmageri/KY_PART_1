package vfs;

/**
 * Ошибка работы с виртуальной файловой системой.
 */
public class VfsException extends Exception {

    /**
     * Создаёт исключение с сообщением.
     *
     * @param message сообщение
     */
    public VfsException(String message) {
        super(message);
    }

    /**
     * Создаёт исключение с сообщением и причиной.
     *
     * @param message сообщение
     * @param cause причина
     */
    public VfsException(String message, Throwable cause) {
        super(message, cause);
    }
}
