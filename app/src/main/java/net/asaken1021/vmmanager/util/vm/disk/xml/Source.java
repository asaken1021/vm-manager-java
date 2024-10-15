package net.asaken1021.vmmanager.util.vm.disk.xml;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
public class Source {
    @XmlAttribute
    private String pool;

    @XmlAttribute
    private String volume;

    @XmlAttribute
    private String file;

    public String getPool() {
        return this.pool;
    }

    public void setPool(String pool) {
        this.pool = pool;
    }

    public String getVolume() {
        return this.volume;
    }
    
    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getFile() {
        return this.file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
