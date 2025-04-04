package net.asaken1021.vmmanager.util.vm;

import jakarta.xml.bind.JAXBException;
import org.libvirt.LibvirtException;

import net.asaken1021.vmmanager.util.*;
import net.asaken1021.vmmanager.util.xml.*;
import net.asaken1021.vmmanager.util.xml.annotation.networkinterface.NetworkInterfaceXML;

public class VMNetworkInterface {
    private String macAddress;
    private String source;
    private String model;
    private NetworkInterfaceType interfaceType;

    public VMNetworkInterface(String macAddress, String source, String model, NetworkInterfaceType interfaceType, VMManager vmm) throws InterfaceNotFoundException {
        this.macAddress = macAddress;
        this.source = source;
        this.model = model;
        this.interfaceType = interfaceType;

        if (this.interfaceType.equals(NetworkInterfaceType.IF_BRIDGE)) {
            try {
                if (!vmm.getHostInterfaces().contains(this.source)) {
                    throw new InterfaceNotFoundException();
                }
            } catch (LibvirtException e) {
                throw new InterfaceNotFoundException(e);
            }
        }
    }

    public VMNetworkInterface(String xmlDesc) throws JAXBException, TypeNotFoundException {
        NetworkInterfaceXML interfaceXML = new DomainXMLParser(xmlDesc, XMLType.TYPE_NETWORKINTERFACE).parseNetworkInterfaceXML();

        this.macAddress = interfaceXML.getMac().getAddress();
        this.interfaceType = NetworkInterfaceType.getTypeByString(interfaceXML.getType());
        if (this.interfaceType.equals(NetworkInterfaceType.IF_NETWORK)) {
            this.source = interfaceXML.getSource().getNetwork();
        } else if (this.interfaceType.equals(NetworkInterfaceType.IF_BRIDGE)) {
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
    
    public NetworkInterfaceType getInterfaceType() {
        return this.interfaceType;
    }
    
    public void setInterfaceType(NetworkInterfaceType interfaceType) {
        this.interfaceType = interfaceType;
    }
    
}
