package net.asaken1021.vmmanager.util.xml;

public class XMLParserException extends Exception {
    public XMLParserException() {
        super("XMLの処理中に問題が発生しました");
    }

    public XMLParserException(Exception e) {
        super("XMLの処理中に問題が発生しました\n" + e.getLocalizedMessage());
    }
}
