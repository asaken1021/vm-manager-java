package net.asaken1021.vmmanager.util;

import jakarta.xml.bind.JAXBException;
import net.asaken1021.vmmanager.util.xml.XMLType;
import net.asaken1021.vmmanager.util.vm.disk.DiskXML;
import net.asaken1021.vmmanager.util.xml.DomainXMLParser;

public class VMDisk {
    private String device;
    private String type;
    private String driverName;
    private String driverType;
    private String sourceFile;
    private String targetDev;
    private String targetBus;

    public VMDisk(String device, String type, String driverName, String driverType, 
        String sourceFile, String targetDev, String targetBus) {
        this.device = device;
        this.type = type;
        this.driverName = driverName;
        this.driverType = driverType;
        this.sourceFile = sourceFile;
        this.targetDev = targetDev;
        this.targetBus = targetBus;
    }

    public VMDisk(String xmlDesc) throws JAXBException {
        DiskXML diskXML = new DomainXMLParser(xmlDesc, XMLType.TYPE_DISK).parseDiskXML();

        this.device = diskXML.getDevice();
        this.type = diskXML.getType();
        this.driverName = diskXML.getDriver().getName();
        this.driverType = diskXML.getDriver().getType();
        this.sourceFile = diskXML.getSource().getFile();
        this.targetDev = diskXML.getTarget().getDev();
        this.targetBus = diskXML.getTarget().getBus();
    }

    public String getDevice() {
        return this.device;
    }
    
    public void setDevice(String device) {
        this.device = device;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getDriverName() {
        return this.driverName;
    }
    
    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }
    
    public String getDriverType() {
        return this.driverType;
    }
    
    public void setDriverType(String driverType) {
        this.driverType = driverType;
    }

    public String getSourceFile() {
        return this.sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }
    
    public String getTargetDev() {
        return this.targetDev;
    }
    
    public void setTargetDev(String targetDev) {
        this.targetDev = targetDev;
    }
    
    public String getTargetBus() {
        return this.targetBus;
    }
    
    public void setTargetBus(String targetBus) {
        this.targetBus = targetBus;
    }
}
