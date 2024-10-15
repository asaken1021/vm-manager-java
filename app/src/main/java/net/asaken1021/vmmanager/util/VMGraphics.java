package net.asaken1021.vmmanager.util;

import jakarta.xml.bind.JAXBException;
import net.asaken1021.vmmanager.util.graphics.xml.GraphicsXML;
import net.asaken1021.vmmanager.util.xml.DomainXMLParser;
import net.asaken1021.vmmanager.util.xml.XMLType;

public class VMGraphics {
    private String graphicsType;
    private int port;
    private String listenType;
    private String address;

    public VMGraphics(String type, int port) {
        this.graphicsType = type;
        this.port = port;
        this.listenType = "address";
        this.address = "";
    }

    public VMGraphics(String xmlDesc) throws JAXBException {
        GraphicsXML graphicsXML = new DomainXMLParser(xmlDesc, XMLType.TYPE_GRAPHICS).parseGraphicsXML();

        this.graphicsType = graphicsXML.getType();
        this.port = graphicsXML.getPort();
        this.listenType = graphicsXML.getListen().getType();
        this.address = graphicsXML.getListenAddress();
    }
    
    public String getGraphicsType() {
        return this.graphicsType;
    }
    
    public void setGraphicsType(String graphicsType) {
        this.graphicsType = graphicsType;
    }
    
    public int getPort() {
        return this.port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getListenType() {
        return this.listenType;
    }
    
    public void setListenType(String listenType) {
        this.listenType = listenType;
    }
    
    public String getAddress() {
        return this.address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }    
}
