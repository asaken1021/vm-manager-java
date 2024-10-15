package net.asaken1021.vmmanager.util;

import jakarta.xml.bind.JAXBException;
import net.asaken1021.vmmanager.util.xml.XMLType;
import net.asaken1021.vmmanager.util.vm.networkinterface.*;
import net.asaken1021.vmmanager.util.xml.DomainXMLParser;

public class VMNetworkInterface {
    private String macAddress;
    private String source;
    private String model;
    private InterfaceType interfaceType;

    public VMNetworkInterface(String macAddress, String source, String model, InterfaceType interfaceType) {
        this.macAddress = macAddress;
        this.source = source;
        this.model = model;
        this.interfaceType = interfaceType;
    }

    public VMNetworkInterface(String xmlDesc) throws JAXBException {
        NetworkInterfaceXML interfaceXML = new DomainXMLParser(xmlDesc, XMLType.TYPE_NETWORKINTERFACE).parseNetworkInterfaceXML();

        this.macAddress = interfaceXML.getMac().getAddress();
        this.interfaceType = InterfaceType.getTypeByString(interfaceXML.getType());
        if (this.interfaceType.equals(InterfaceType.IF_NETWORK)) {
            this.source = interfaceXML.getSource().getNetwork();
        } else if (this.interfaceType.equals(InterfaceType.IF_BRIDGE)) {
            this.source = interfaceXML.getSource().getBridge();
        }
        this.model = interfaceXML.getModel().getType();
    }

    public String getMacAddress() {
        return this.macAddress;
    }
    
    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }
    
    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }
    
    public String getModel() {
        return this.model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    
    public InterfaceType getInterfaceType() {
        return this.interfaceType;
    }
    
    public void setInterfaceType(InterfaceType interfaceType) {
        this.interfaceType = interfaceType;
    }
    
}
