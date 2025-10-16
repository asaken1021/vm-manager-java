package net.asaken1021.vmmanager.util.xml.annotation.metadata;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="metadata")
@XmlAccessorType(XmlAccessType.FIELD)
public class MetadataXML {
    @XmlElement(name="data", namespace="https://github.com/asaken1021/vm-manager-java")
    private Data data;

    public Data getVmmData() {
        return this.data;
    }

    public void setVmmData(Data data) {
        this.data = data;
    }
}
