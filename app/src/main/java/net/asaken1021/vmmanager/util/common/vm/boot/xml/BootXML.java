package net.asaken1021.vmmanager.util.common.vm.boot.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="boot")
@XmlAccessorType(XmlAccessType.FIELD)
public class BootXML {
    @XmlAttribute(name="dev")
    private String dev;

    public String getDev() {
        return this.dev;
    }

    public void setDev(String dev) {
        this.dev = dev;
    }
}
