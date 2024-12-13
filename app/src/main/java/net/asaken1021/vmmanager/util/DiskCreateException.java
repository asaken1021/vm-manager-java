package net.asaken1021.vmmanager.util;

public class DiskCreateException extends Exception {
    public DiskCreateException() {
        super("仮想ディスクファイルの作成に失敗しました");
    }

    public DiskCreateException(Exception e) {
        super("仮想ディスクファイルの作成に失敗しました\n" + e.getLocalizedMessage());
    }
}
