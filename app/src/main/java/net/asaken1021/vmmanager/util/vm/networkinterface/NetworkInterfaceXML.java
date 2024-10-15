package net.asaken1021.vmmanager.util.vm.networkinterface;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import net.asaken1021.vmmanager.util.vm.networkinterface.xml.Mac;
import net.asaken1021.vmmanager.util.vm.networkinterface.xml.Model;
import net.asaken1021.vmmanager.util.vm.networkinterface.xml.Source;

@XmlRootElement(name="interface")
@XmlAccessorType(XmlAccessType.FIELD)
public class NetworkInterfaceXML {
    @XmlAttribute(name="type")
    private String type;

    @XmlElement(name="mac")
    private Mac mac;

    @XmlElement(name="source")
    private Source source;

    @XmlElement(name="model")
    private Model model;

    public String getType() {
        return this.type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Mac getMac() {
        return this.mac;
    }
    
    public void setMac(Mac mac) {
        this.mac = mac;
    }
    
    public Source getSource() {
        return this.source;
    }
    
    public void setSource(Source source) {
        this.source = source;
    }
    
    public Model getModel() {
        return this.model;
    }
    
    public void setModel(Model model) {
        this.model = model;
    }
}
