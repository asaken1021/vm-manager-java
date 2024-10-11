package net.asaken1021.vmmanager.util.video.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="video")
@XmlAccessorType(XmlAccessType.FIELD)
public class VideoXML {
    @XmlElement(name="model")
    private Model model;

    public Model getModel() {
        return this.model;
    }

    public void setModel(Model model) {
        this.model = model;
    }
}
