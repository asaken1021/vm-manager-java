package net.asaken1021.vmmanager.util;

public class DomainNotRunningException extends Exception {
    public DomainNotRunningException() {
        super("指定された仮想マシンは起動していません");
    }
}
