package net.asaken1021.vmmanager.util.vm.networkinterface.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class Source {
    @XmlAttribute(name="bridge")
    private String bridge;

    @XmlAttribute(name="network")
    private String network;

    public String getBridge() {
        return this.bridge;
    }

    public void setBridge(String bridge) {
        this.bridge = bridge;
    }

    public String getNetwork() {
        return this.network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }
}
