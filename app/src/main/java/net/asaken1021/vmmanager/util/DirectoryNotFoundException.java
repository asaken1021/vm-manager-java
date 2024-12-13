package net.asaken1021.vmmanager.util;

public class DirectoryNotFoundException extends Exception {
    public DirectoryNotFoundException() {
        super("指定されたディレクトリが見つかりません");
    }
}
