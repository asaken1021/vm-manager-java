package net.asaken1021.vmmanager.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;

import net.asaken1021.vmmanager.util.common.vm.VMDisk;
import net.asaken1021.vmmanager.util.common.vm.VMDomain;
import net.asaken1021.vmmanager.util.common.vm.VMGraphics;
import net.asaken1021.vmmanager.util.common.vm.VMNetworkInterface;
import net.asaken1021.vmmanager.util.common.vm.VMVideo;
import net.asaken1021.vmmanager.util.common.xml.DomainXMLBuilder;

public class VMManager {
    private Connect conn;

    public VMManager(String uri) throws ConnectException {
        try {
            Connect.setErrorCallback(new ErrorCallback());
            this.conn = new Connect(uri);
        } catch (LibvirtException e) {
            throw new ConnectException(e);
        }
    }

    public Connect getConnect() {
        return this.conn;
    }

    public void disconnect() throws LibvirtException {
        this.conn.close();
    }

    public List<String> getVmNames() throws DomainLookupException {
        List<String> vmNames = new ArrayList<String>();
        int[] vmIds;

        try {
            for (String name : this.conn.listDefinedDomains()) {
                vmNames.add(name);
            }

            vmIds = this.conn.listDomains();

            for (int vmId : vmIds) {
                vmNames.add(this.conn.domainLookupByID(vmId).getName());
            }

            return vmNames;
        } catch (LibvirtException e) {
            throw new DomainLookupException(e);
        }
    }

    public List<String> getHostInterfaces() throws LibvirtException {
        return Arrays.asList(this.conn.listInterfaces());
    }

    public VMDomain createVm(String name, int cpus, long ram, List<VMDisk> disks,
    List<VMNetworkInterface> networkInterfaces, VMGraphics graphics, VMVideo video)
    throws DomainCreateException {
        try {
            DomainXMLBuilder builder = new DomainXMLBuilder(name, cpus, ram, disks, networkInterfaces, graphics, video);
            String xml = builder.buildXML();
            this.conn.domainDefineXML(xml);

            return new VMDomain(this.conn, name);
        } catch (ParserConfigurationException | TransformerException | LibvirtException | DomainLookupException e) {
            throw new DomainCreateException(e);
        }
    }

    public VMDomain getVm(String name) throws DomainLookupException {
        return new VMDomain(this.conn, name);
    }

    public VMDomain getVm(UUID uuid) throws DomainLookupException {
        return new VMDomain(this.conn, uuid);
    }

    public void deleteVm(String name) throws DomainDeleteException {
        try {
            this.conn.domainLookupByName(name).undefine(55);
        } catch (LibvirtException e) {
            throw new DomainDeleteException(e);
        }
    }

    public void startVm(String name) throws DomainLookupException, DomainStartException {
        try {
            getVm(name).startVm();
        } catch (LibvirtException e) {
            throw new DomainStartException(e);
        }
    }

    public void stopVm(String name) throws DomainLookupException, DomainStopException {
        try {
            getVm(name).stopVm();
        } catch (LibvirtException e) {
            throw new DomainStopException(e);
        }
    }
}
