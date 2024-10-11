package net.asaken1021.vmmanager.util;

import java.util.ArrayList;
import java.util.List;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;

import net.asaken1021.vmmanager.util.xml.DomainXMLBuilder;

public class VMManager {
    private Connect conn;

    public VMManager(String uri) throws LibvirtException {
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

    public List<String> getVmNames() throws LibvirtException {
        List<String> vmNames = new ArrayList<String>();
        int[] vmIds;

        for (String name : this.conn.listDefinedDomains()) {
            vmNames.add(name);
        }

        vmIds = this.conn.listDomains();

        for (int vmId : vmIds) {
            vmNames.add(this.conn.domainLookupByID(vmId).getName());
        }

        return vmNames;
    }

    public VMDomain createVm(String name, int cpus, long ram, List<VMDisk> disks, List<VMNetworkInterface> networkInterfaces) {
        try {
            DomainXMLBuilder builder = new DomainXMLBuilder(name, cpus, ram, disks, networkInterfaces);
            String xml = builder.buildXML();
            System.out.println(xml);
            this.conn.domainDefineXML(xml);

            return new VMDomain(this.conn, name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public VMDomain getVm(String name) throws LibvirtException {
        System.out.println(this.conn.domainLookupByName(name).getXMLDesc(0));
        return new VMDomain(this.conn, name);
    }

    public boolean deleteVm(String name) {
        try {
            this.conn.domainLookupByName(name).undefine();
            return true;
        } catch (LibvirtException e) {
        }

        return false;
    }
}
