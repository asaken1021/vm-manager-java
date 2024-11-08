package net.asaken1021.vmmanager.util.common.vm;

import jakarta.xml.bind.JAXBException;
import org.libvirt.LibvirtException;

import net.asaken1021.vmmanager.util.InterfaceNotFoundException;
import net.asaken1021.vmmanager.util.TypeNotFoundException;
import net.asaken1021.vmmanager.util.VMManager;
import net.asaken1021.vmmanager.util.common.vm.networkinterface.InterfaceType;
import net.asaken1021.vmmanager.util.common.vm.networkinterface.xml.NetworkInterfaceXML;
import net.asaken1021.vmmanager.util.common.xml.DomainXMLParser;
import net.asaken1021.vmmanager.util.common.xml.XMLType;

public class VMNetworkInterface {
    private String macAddress;
    private String source;
    private String model;
    private InterfaceType interfaceType;

    public VMNetworkInterface(String macAddress, String source, String model, InterfaceType interfaceType, VMManager vmm) throws InterfaceNotFoundException {
        this.macAddress = macAddress;
        this.source = source;
        this.model = model;
        this.interfaceType = interfaceType;

        if (this.interfaceType.equals(InterfaceType.IF_BRIDGE)) {
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
