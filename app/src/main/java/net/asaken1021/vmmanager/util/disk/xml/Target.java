package net.asaken1021.vmmanager.util.disk.xml;

import jakarta.xml.bind.annotation.*;

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
