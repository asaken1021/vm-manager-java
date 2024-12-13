package net.asaken1021.vmmanager.util;

public class FileAlreadyExistsException extends Exception {
    public FileAlreadyExistsException() {
        super("指定されたパスのファイルが既に存在します");
    }
}
