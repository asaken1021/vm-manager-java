package net.asaken1021.vmmanager.util.vm;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import jakarta.xml.bind.JAXBException;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainInfo;
import org.libvirt.DomainInterface;
import org.libvirt.LibvirtException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.asaken1021.vmmanager.util.DomainLookupException;
import net.asaken1021.vmmanager.util.DomainPowerState;
import net.asaken1021.vmmanager.util.FileNotFoundException;
import net.asaken1021.vmmanager.util.TypeNotFoundException;
import net.asaken1021.vmmanager.util.XMLParserException;
import net.asaken1021.vmmanager.util.xml.XMLType;

public class VMDomain {
    private Connect conn;
    private Domain dom;
    private DomainInfo domInfo;

    private String vmName;
    private int vmCpus;
    private long vmRam;
    private List<VMDisk> vmDisks;
    private List<VMNetworkInterface> vmNetworkInterfaces;
    private VMGraphics vmGraphics;
    private VMVideo vmVideo;

    public VMDomain(Connect conn, String name) throws DomainLookupException {
        this.conn = conn;
        try {
            this.dom = this.conn.domainLookupByName(name);
            initData(this.dom);
        } catch (LibvirtException | XMLParserException e) {
            throw new DomainLookupException(e);
        }
    }

    public VMDomain(Connect conn, UUID uuid) throws DomainLookupException {
        this.conn = conn;
        try {
            this.dom = this.conn.domainLookupByUUID(uuid);
            initData(this.dom);
        } catch (LibvirtException | XMLParserException e) {
            throw new DomainLookupException(e);
        }
    }

    private void initData(Domain dom) throws LibvirtException, XMLParserException {
        this.domInfo = dom.getInfo();

        this.vmName = dom.getName();
        this.vmCpus = this.domInfo.nrVirtCpu;
        this.vmRam = this.domInfo.maxMem;
        this.vmDisks = parseVmDisks(dom);
        this.vmNetworkInterfaces = parseVmNetworkInterfaces(dom);
        this.vmGraphics = parseVmGraphics(dom);
        this.vmVideo = parseVmVideo(dom);
    }

    private List<VMDisk> parseVmDisks(Domain dom) throws XMLParserException {
        List<VMDisk> vmDisks = new ArrayList<VMDisk>();

        try {
            for (String vmDiskXML : parseXMLNodes(dom.getXMLDesc(0), XMLType.TYPE_DISK)) {
                vmDisks.add(new VMDisk(vmDiskXML));
            }
        } catch (LibvirtException | JAXBException | FileNotFoundException e) {
            throw new XMLParserException(e);
        }

        return vmDisks;
    }

    private List<VMNetworkInterface> parseVmNetworkInterfaces(Domain dom) throws XMLParserException {
        List<VMNetworkInterface> vmNetworkInterfaces = new ArrayList<VMNetworkInterface>();

        try {
            for (String vmNetworkInterfaceXML : parseXMLNodes(dom.getXMLDesc(0), XMLType.TYPE_NETWORKINTERFACE)) {
                vmNetworkInterfaces.add(new VMNetworkInterface(vmNetworkInterfaceXML));
            }
        } catch (LibvirtException | JAXBException | TypeNotFoundException e) {
            throw new XMLParserException(e);
        }

        return vmNetworkInterfaces;
    }

    private VMGraphics parseVmGraphics(Domain dom) throws XMLParserException {
        List<String> vmGraphicsXML;
        VMGraphics vmGraphics;

        try {
            vmGraphicsXML = parseXMLNodes(dom.getXMLDesc(0), XMLType.TYPE_GRAPHICS);

            if (vmGraphicsXML.size() == 1) {
                vmGraphics = new VMGraphics(vmGraphicsXML.get(0));
            } else {
                return null;
            }
        } catch (LibvirtException | JAXBException e) {
            throw new XMLParserException(e);
        }

        return vmGraphics;
    }

    private VMVideo parseVmVideo(Domain dom) throws XMLParserException {
        List<String> vmVideoXML;
        VMVideo vmVideo;

        try {
            vmVideoXML = parseXMLNodes(dom.getXMLDesc(0), XMLType.TYPE_VIDEO);

            if (vmVideoXML.size() != 1) {
                return null;
            } else {
                vmVideo = new VMVideo(vmVideoXML.get(0));
            }
        } catch (LibvirtException | JAXBException | TypeNotFoundException e) {
            throw new XMLParserException(e);
        }

        return vmVideo;
    }

    private List<String> parseXMLNodes(String xmlDesc, XMLType xmlType) throws XMLParserException {
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document document;
        XPathFactory xPathFactory;
        XPath xPath;
        NodeList nodeList;

        List<String> xmlNodes = new ArrayList<String>();

        factory = DocumentBuilderFactory.newInstance();

        try {
            builder = factory.newDocumentBuilder();
            document = builder.parse(new InputSource(new StringReader(xmlDesc)));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new XMLParserException(e);
        }

        xPathFactory = XPathFactory.newInstance();
        xPath = xPathFactory.newXPath();

        try {
            nodeList = (NodeList) xPath.evaluate(xmlType.getXPath(), document, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); i++) {
                xmlNodes.add(nodeToString(nodeList.item(i)));
            }
        } catch (XPathExpressionException | TransformerException e) {
            throw new XMLParserException(e);
        }

        return xmlNodes;
    }

    private String nodeToString(Node node) throws TransformerException {
        TransformerFactory transformerFactory;
        Transformer transformer;
        DOMSource domSource;
        StringWriter writer;
        StreamResult result;

        transformerFactory = TransformerFactory.newInstance();
        transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        domSource = new DOMSource(node);
        writer = new StringWriter();
        result = new StreamResult(writer);

        transformer.transform(domSource, result);

        return writer.toString();
    }

    private String getVmUUIDString() {
        try {
            return this.dom.getUUIDString();
        } catch (LibvirtException e) {
            return "";
        }
    }

    private List<DomainInterface> getDomainInterfaces() {
        List<DomainInterface> domainInterfaces = new ArrayList<DomainInterface>();

        try {
            domainInterfaces = new ArrayList<DomainInterface>(this.dom
                    .interfaceAddresses(Domain.InterfaceAddressesSource.VIR_DOMAIN_INTERFACE_ADDRESSES_SRC_ARP, 0));
            domainInterfaces = new ArrayList<DomainInterface>(this.dom
                    .interfaceAddresses(Domain.InterfaceAddressesSource.VIR_DOMAIN_INTERFACE_ADDRESSES_SRC_AGENT, 0));

            return domainInterfaces;
        } catch (LibvirtException e) {
            return domainInterfaces;
        }
    }

    public void startVm() throws LibvirtException {
        this.dom.create();
    }

    public void shutdownVm() throws LibvirtException {
        this.dom.shutdown();
    }

    public void stopVm() throws LibvirtException {
        this.dom.destroy();
    }

    public String getVmName() {
        return this.vmName;
    }

    public UUID getVmUUID() {
        return UUID.fromString(getVmUUIDString());
    }

    public DomainPowerState getVmPowerState() {
        return DomainPowerState.getStateByDomainState(this.domInfo.state);
    }

    public int getVmCpus() {
        return this.vmCpus;
    }

    public long getVmRamSize(VMRamUnit unit) {
        if (unit.equals(VMRamUnit.RAM_MiB)) {
            return this.vmRam / 1024;
        } else if (unit.equals(VMRamUnit.RAM_GiB)) {
            return this.vmRam / 1024 / 1024;
        } else {
            return this.vmRam;
        }
    }

    public List<VMDisk> getVmDisks() {
        return this.vmDisks;
    }

    public List<VMNetworkInterface> getVmNetworkInterfaces() {
        return this.vmNetworkInterfaces;
    }

    public VMGraphics getVmGraphics() {
        return this.vmGraphics;
    }

    public VMVideo getVmVideo() {
        return this.vmVideo;
    }

    public List<String> getInterfaceAddresses(String macAddress) {
        List<String> addresses = new ArrayList<String>();
        List<DomainInterface> domainInterfaces = getDomainInterfaces();

        if (domainInterfaces.size() == 0) {
            return addresses;
        }

        for (DomainInterface domainInterface : domainInterfaces) {
            if (domainInterface.hwAddr.equals(macAddress)) {
                for (DomainInterface.InterfaceAddress ifaceAddress : domainInterface.addrs) {
                    addresses.add(ifaceAddress.address.getHostAddress());
                }
            }
        }

        return addresses;
    }
}
