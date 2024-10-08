package net.asaken1021.vmmanager.util;

import java.util.UUID;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainInfo;
import org.libvirt.LibvirtException;

public class vmDomain {
    private Connect conn;
    private Domain dom;
    private DomainInfo domInfo;

    public vmDomain(Connect conn, String name) throws LibvirtException {
        this.conn = conn;
        this.dom = this.conn.domainLookupByName(name);
        this.domInfo = this.dom.getInfo();
    }

    public vmDomain(Connect conn, UUID uuid) throws LibvirtException {
        this.conn = conn;
        this.dom = this.conn.domainLookupByUUID(uuid);
        this.domInfo = this.dom.getInfo();
    }

    private String getVmUUIDString() throws LibvirtException {
        return this.dom.getUUIDString();
    }

    private DomainInfo.DomainState getVmState() {
        return this.domInfo.state;
    }

    public String getVmName() throws LibvirtException {
        return this.dom.getName();
    }

    public UUID getVmUUID() throws LibvirtException {
        return UUID.fromString(getVmUUIDString());
    }

    public String getVmStateString() {
        String powerState;

        switch (getVmState()) {
            case DomainInfo.DomainState.VIR_DOMAIN_SHUTOFF:
                powerState = "Shut off";
                break;
            case DomainInfo.DomainState.VIR_DOMAIN_RUNNING:
                powerState = "Running";
                break;
            default:
                powerState = "Other state";
                break;
        }

        return powerState;
    }

    public int getVmCpus() {
        return this.domInfo.nrVirtCpu;
    }

    public long getVmRamSize(vmRamUnit unit) {
        if (unit.equals(vmRamUnit.RAM_MiB)) {
            return this.domInfo.maxMem / 1024;
        } else if (unit.equals(vmRamUnit.RAM_GiB)) {
            return this.domInfo.maxMem / 1024 / 1024;
        } else {
            return this.domInfo.maxMem;
        }
    }
}
