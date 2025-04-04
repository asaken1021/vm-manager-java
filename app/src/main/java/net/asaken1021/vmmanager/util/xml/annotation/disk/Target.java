package net.asaken1021.vmmanager.util.xml.annotation.disk;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class Target {
    @XmlAttribute
    private String dev;

    @XmlAttribute
    private String bus;

    public String getDev() {
        return this.dev;
    }

    public void setDev(String dev) {
        this.dev = dev;
    }

    public String getBus() {
        return this.bus;
    }

    public void setBus(String bus) {
        this.bus = bus;
    }
}
