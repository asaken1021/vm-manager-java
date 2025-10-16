package net.asaken1021.vmmanager.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;

import net.asaken1021.vmmanager.util.vm.*;
import net.asaken1021.vmmanager.util.xml.DomainXMLBuilder;

public class VMManager {
    private Connect conn;
    private DiskUtil diskUtil;

    public VMManager(String uri) throws ConnectException {
        try {
            Connect.setErrorCallback(new ErrorCallback());
            this.conn = new Connect(uri);
        } catch (LibvirtException e) {
            throw new ConnectException(e);
        }

        this.diskUtil = new DiskUtil(this.conn);
    }

    public Connect getConnect() {
        return this.conn;
    }

    public DiskUtil getDiskUtil() {
        return this.diskUtil;
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

    public VMDomain createVm(String name, int cpus, long ram, VMRamUnit ramUnit, LinkedHashMap<VMDisk, Integer> disks,
    List<VMNetworkInterface> networkInterfaces, VMGraphics graphics, VMVideo video, List<VMBoot> vmBoots, String vmFolderPath)
    throws DomainCreateException {
        try {
            List<VMDisk> disksList = new ArrayList<VMDisk>(disks.keySet());
            DomainXMLBuilder builder = new DomainXMLBuilder(name, cpus, ram, ramUnit, disksList, networkInterfaces, graphics, video, vmBoots, vmFolderPath);
            String xml = builder.buildXML();

            for (VMDisk disk : disksList) {
                if (disk.getDevice().equals("disk")) {
                    if (!new File(disk.getSourceFile()).exists()) {
                        this.diskUtil.createDisk(disk.getSourceFile(), disks.get(disk).intValue());
                    }
                }
            }

            this.conn.domainDefineXML(xml);

            return new VMDomain(this.conn, name);
        } catch (ParserConfigurationException | TransformerException | LibvirtException | DomainLookupException |
        DiskCreateException | DirectoryNotFoundException | FileAlreadyExistsException e) {
            throw new DomainCreateException(e);
        }
    }

    public VMDomain createVm(UUID uuid, String name, int cpus, long ram, VMRamUnit ramUnit, LinkedHashMap<VMDisk, Integer> disks,
    List<VMNetworkInterface> networkInterfaces, VMGraphics graphics, VMVideo video, List<VMBoot> vmBoots, String vmFolderPath)
    throws DomainCreateException {
        try {
            List<VMDisk> disksList = new ArrayList<VMDisk>(disks.keySet());
            DomainXMLBuilder builder = new DomainXMLBuilder(uuid, name, cpus, ram, ramUnit, disksList, networkInterfaces, graphics, video, vmBoots, vmFolderPath);
            String xml = builder.buildXML();

            for (VMDisk disk : disksList) {
                if (disk.getDevice().equals("disk")) {
                    if (!new File(disk.getSourceFile()).exists()) {
                        this.diskUtil.createDisk(disk.getSourceFile(), disks.get(disk).intValue());
                    }
                }
            }
            
            this.conn.domainDefineXML(xml);

            return new VMDomain(this.conn, name);
        } catch (ParserConfigurationException | TransformerException | LibvirtException | DomainLookupException  |
        DiskCreateException | DirectoryNotFoundException | FileAlreadyExistsException e) {
            throw new DomainCreateException(e);
        }
    }

    public VMDomain getVm(String name) throws DomainLookupException {
        return new VMDomain(this.conn, name);
    }

    public VMDomain getVm(UUID uuid) throws DomainLookupException {
        return new VMDomain(this.conn, uuid);
    }

    public void modifyVm(UUID uuid, String name, int cpus, long ram, VMRamUnit ramUnit, LinkedHashMap<VMDisk, Integer> disks,
    List<VMNetworkInterface> networkInterfaces, VMGraphics graphics, VMVideo video, List<VMBoot> vmBoots, String vmFolderPath)
    throws DomainCreateException, DomainDeleteException {
        deleteVm(uuid);
        createVm(uuid, name, cpus, ram, ramUnit, disks, networkInterfaces, graphics, video, vmBoots, vmFolderPath);
    }

    public void deleteVm(UUID uuid) throws DomainDeleteException {
        try {
            this.conn.domainLookupByUUID(uuid).undefine(55);
        } catch (LibvirtException e) {
            throw new DomainDeleteException(e);
        }
    }

    public void startVm(UUID uuid) throws DomainLookupException, DomainStartException {
        try {
            getVm(uuid).startVm();
        } catch (LibvirtException e) {
            throw new DomainStartException(e);
        }
    }

    public void stopVm(UUID uuid) throws DomainLookupException, DomainStopException {
        try {
            getVm(uuid).stopVm();
        } catch (LibvirtException e) {
            throw new DomainStopException(e);
        }
    }
}
