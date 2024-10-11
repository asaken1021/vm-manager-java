package net.asaken1021.vmmanager.util.disk.xml;

import jakarta.xml.bind.annotation.*;

@XmlRootElement(name="disk")
@XmlAccessorType(XmlAccessType.FIELD)
public class DiskXML {
    @XmlAttribute(name="type")
    private String type;

    @XmlAttribute(name="device")
    private String device;

    @XmlElement(name="driver")
    private Driver driver;

    @XmlElement(name="source")
    private Source source;

    @XmlElement(name="target")
    private Target target;

    @XmlElement(name="readonly")
    private ReadOnly readOnly;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDevice() {
        return this.device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public Driver getDriver() {
        return this.driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Source getSource() {
        return this.source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Target getTarget() {
        return this.target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public ReadOnly getReadOnly() {
        return this.readOnly;
    }

    public void setReadOnly(boolean isReadonly) {
        if (isReadonly) {
            this.readOnly = new ReadOnly();
        } else {
            this.readOnly = null;
        }
    }
}
