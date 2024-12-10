package net.asaken1021.vmmanager.util.webapi;

public class BadRequestException extends Exception {
    public BadRequestException() {
        super("リクエストが不正です");
    }

    public BadRequestException(Exception e) {
        super("リクエストが不正です\n" + e.getLocalizedMessage());
    }
}
