package net.asaken1021.vmmanager.util.xml;

import java.io.StringReader;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import net.asaken1021.vmmanager.util.xml.annotation.boot.BootXML;
import net.asaken1021.vmmanager.util.xml.annotation.disk.DiskXML;
import net.asaken1021.vmmanager.util.xml.annotation.graphics.GraphicsXML;
import net.asaken1021.vmmanager.util.xml.annotation.networkinterface.NetworkInterfaceXML;
import net.asaken1021.vmmanager.util.xml.annotation.video.VideoXML;

public class DomainXMLParser {
    private String xmlDesc;
    private XMLType xmlType;
    private JAXBContext jaxbContext;
    private Unmarshaller unmarshaller;

    public DomainXMLParser(String xmlDesc, XMLType xmlType) throws JAXBException {
        this.xmlDesc = xmlDesc;
        this.xmlType = xmlType;

        switch (this.xmlType) {
            case XMLType.TYPE_BOOT:
                this.jaxbContext = JAXBContext.newInstance(BootXML.class);
                break;
            case XMLType.TYPE_DISK:
                this.jaxbContext = JAXBContext.newInstance(DiskXML.class);
                break;
            case XMLType.TYPE_NETWORKINTERFACE:
                this.jaxbContext = JAXBContext.newInstance(NetworkInterfaceXML.class);
                break;
            case XMLType.TYPE_GRAPHICS:
                this.jaxbContext = JAXBContext.newInstance(GraphicsXML.class);
                break;
            case XMLType.TYPE_VIDEO:
                this.jaxbContext = JAXBContext.newInstance(VideoXML.class);
            default:
                break;
        }
        this.unmarshaller = jaxbContext.createUnmarshaller();
    }

    public BootXML parseBootXML() throws JAXBException {
        return (BootXML)unmarshaller.unmarshal(new StringReader(this.xmlDesc));
    }

    public DiskXML parseDiskXML() throws JAXBException {
        return (DiskXML)unmarshaller.unmarshal(new StringReader(this.xmlDesc));
    }

    public NetworkInterfaceXML parseNetworkInterfaceXML() throws JAXBException {
        return (NetworkInterfaceXML)unmarshaller.unmarshal(new StringReader(this.xmlDesc));
    }

    public GraphicsXML parseGraphicsXML() throws JAXBException {
        return (GraphicsXML)unmarshaller.unmarshal(new StringReader(this.xmlDesc));
    }

    public VideoXML parseVideoXML() throws JAXBException {
        return (VideoXML)unmarshaller.unmarshal(new StringReader(this.xmlDesc));
    }
}
