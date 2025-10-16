package net.asaken1021.vmmanager.util.vm;

import jakarta.xml.bind.JAXBException;
import net.asaken1021.vmmanager.util.xml.DomainXMLParser;
import net.asaken1021.vmmanager.util.xml.XMLType;
import net.asaken1021.vmmanager.util.xml.annotation.metadata.MetadataXML;

public class VMFolderPath {
    private String vmFolderPath;

    public VMFolderPath(String xmlDesc) throws JAXBException {
        MetadataXML metadataXML = new DomainXMLParser(xmlDesc, XMLType.TYPE_METADATA).parseMetadataXML();

        this.vmFolderPath = metadataXML.getVmmData().getVmFolderPath();
    }

    public String getVmFolderPath() {
        return this.vmFolderPath;
    }

    public void setVmFolderPath(String vmFolderPath) {
        this.vmFolderPath = vmFolderPath;
    }
}
