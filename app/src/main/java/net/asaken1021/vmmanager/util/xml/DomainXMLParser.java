package net.asaken1021.vmmanager.util.xml;

import java.io.StringReader;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import net.asaken1021.vmmanager.util.disk.xml.DiskXML;
import net.asaken1021.vmmanager.util.networkinterface.xml.NetworkInterfaceXML;

public class DomainXMLParser {
    private String xmlDesc;
    private XMLType xmlType;
    private JAXBContext jaxbContext;
    private Unmarshaller unmarshaller;

    public DomainXMLParser(String xmlDesc, XMLType xmlType) throws JAXBException {
        this.xmlDesc = xmlDesc;
        this.xmlType = xmlType;

        if (this.xmlType.equals(XMLType.TYPE_DISK)) {
            this.jaxbContext = JAXBContext.newInstance(DiskXML.class);
        } else if (this.xmlType.equals(XMLType.TYPE_NETWORKINTERFACE)) {
            this.jaxbContext = JAXBContext.newInstance(NetworkInterfaceXML.class);
        }
        this.unmarshaller = jaxbContext.createUnmarshaller();
    }

    public DiskXML parseDiskXML() throws JAXBException {
        return (DiskXML)unmarshaller.unmarshal(new StringReader(this.xmlDesc));
    }

    public NetworkInterfaceXML parseNetworkInterfaceXML() throws JAXBException {
        return (NetworkInterfaceXML)unmarshaller.unmarshal(new StringReader(this.xmlDesc));
    }
}
