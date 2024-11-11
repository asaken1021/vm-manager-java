package net.asaken1021.vmmanager.util.webapi;

public class IsoImagesNotSpecifiedException extends Exception {
    public IsoImagesNotSpecifiedException() {
        super("ISOイメージのディレクトリパスが指定されていません");
    }
}
