package net.asaken1021.vmmanager.util.vm;

import jakarta.xml.bind.JAXBException;
import net.asaken1021.vmmanager.util.xml.*;
import net.asaken1021.vmmanager.util.xml.annotation.boot.BootXML;

public class VMBoot {
    private String dev;

    public VMBoot(String dev, boolean notXML) {
        if (notXML) {
            this.dev = dev;
        }
    }

    public VMBoot(String xmlDesc) throws JAXBException {
        BootXML bootXML = new DomainXMLParser(xmlDesc, XMLType.TYPE_BOOT).parseBootXML();

        this.dev = bootXML.getDev();
    }

    public String getDev() {
        return this.dev;
    }

    public void setDev(String dev) {
        this.dev = dev;
    }
}
