package net.asaken1021.vmmanager.util.graphics.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="graphics")
@XmlAccessorType(XmlAccessType.FIELD)
public class GraphicsXML {
    @XmlAttribute(name="type")
    private String type;

    @XmlAttribute(name="port")
    private int port;

    @XmlAttribute(name="autoport")
    private String autoport;

    @XmlAttribute(name="listen")
    private String listenAddress;

    @XmlElement(name="listen")
    private Listen listen;

    public String getType() {
        return this.type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public int getPort() {
        return this.port;
    }
    
    public void setPort(int port) {
        this.port = port;
    }
    
    public String getAutoport() {
        return this.autoport;
    }
    
    public void setAutoport(String autoport) {
        this.autoport = autoport;
    }
    
    public String getListenAddress() {
        return this.listenAddress;
    }
    
    public void setListenAddress(String listenAddress) {
        this.listenAddress = listenAddress;
    }
    
    public Listen getListen() {
        return this.listen;
    }
    
    public void setListen(Listen listen) {
        this.listen = listen;
    }    
}
