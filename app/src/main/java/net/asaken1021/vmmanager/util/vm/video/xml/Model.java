package net.asaken1021.vmmanager.util.vm.video.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class Model {
    @XmlAttribute(name="type")
    private String type;

    @XmlAttribute(name="vram")
    private int vram;

    @XmlAttribute(name="heads")
    private int heads;

    @XmlAttribute(name="primary")
    private String primary;

    public String getType() {
        return this.type;
    }
    
    public void setType(String type) {
        this.type = type;
    }

    public int getVram() {
        return this.vram;
    }

    public void setVram(int vram) {
        this.vram = vram;
    }
    
    public int getHeads() {
        return this.heads;
    }
    
    public void setHeads(int heads) {
        this.heads = heads;
    }
    
    public String getPrimary() {
        return this.primary;
    }
    
    public void setPrimary(String primary) {
        this.primary = primary;
    }    
}
