package net.asaken1021.vmmanager.util.webapi;

public class JSONParseException extends Exception {
    public JSONParseException() {
        super("JSONデータの解析に失敗しました");
    }

    public JSONParseException(String when, String what) {
        super("JSONデータの解析に失敗しました: 関数: " + when + ", キー: " + what);
    }
}
