package net.asaken1021.vmmanager.util;

public class InvalidPowerStateException extends Exception {
    public InvalidPowerStateException() {
        super("正しくない電源状態の値です");
    }
}
