package net.asaken1021.vmmanager.util;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;

public class vmManager {
    private Connect conn;

    public vmManager(String uri) throws LibvirtException {
        try {
            this.conn = new Connect(uri);
        } catch (LibvirtException e) {
            System.err.println("Error: Cannot connect to libvirt daemon.");
            e.printStackTrace();
        }
    }

    public Connect getConnect() {
        return this.conn;
    }

    public String[] getVmNames() throws LibvirtException {
        return conn.listDefinedDomains();
    }
}
