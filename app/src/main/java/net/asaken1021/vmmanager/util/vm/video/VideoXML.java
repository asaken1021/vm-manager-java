package net.asaken1021.vmmanager.util.vm.video;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import net.asaken1021.vmmanager.util.vm.video.xml.Model;

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
