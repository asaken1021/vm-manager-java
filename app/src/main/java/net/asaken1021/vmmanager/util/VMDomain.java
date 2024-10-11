package net.asaken1021.vmmanager.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.DomainInfo;
import org.libvirt.LibvirtException;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

public class VMDomain {
    private Connect conn;
    private Domain dom;
    private DomainInfo domInfo;
    
    private String vmName;
    private int vmCpus;
    private long vmRam;
    private List<VMDisk> vmDisks;
    private List<VMNetworkInterface> vmNetworkInterfaces;

    // public vmDomain(Connect conn, String name, int cpus, long ram, List<vmDisk> disks, List<vmNetworkInterface> networkInterfaces) throws LibvirtException {
    //     this.conn = conn;
    //     this.vmName = name;
    //     this.vmCpus = cpus;
    //     this.vmRam = ram;
    //     this.vmDisks = disks;
    //     this.vmNetworkInterfaces = networkInterfaces;
    // }

    public VMDomain(Connect conn, String name) throws LibvirtException {
        this.conn = conn;
        this.dom = this.conn.domainLookupByName(name);
        this.domInfo = this.dom.getInfo();

        this.vmName = this.dom.getName();
        this.vmCpus = this.domInfo.nrVirtCpu;
        this.vmRam = this.domInfo.maxMem;
        this.vmDisks = parseVmDisks(this.dom);
        this.vmNetworkInterfaces = parseVmNetworkInterfaces(this.dom);
    }

    public VMDomain(Connect conn, UUID uuid) throws LibvirtException {
        this.conn = conn;
        this.dom = this.conn.domainLookupByUUID(uuid);
        this.domInfo = this.dom.getInfo();

        this.vmName = this.dom.getName();
        this.vmCpus = this.domInfo.nrVirtCpu;
        this.vmRam = this.domInfo.maxMem;
        this.vmDisks = parseVmDisks(this.dom);
        this.vmNetworkInterfaces = parseVmNetworkInterfaces(this.dom);
    }

    private List<VMDisk> parseVmDisks(Domain dom) {
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document document;
        XPathFactory xPathFactory;
        XPath xPath;
        NodeList diskNodes;

        List<VMDisk> vmDisks = new ArrayList<VMDisk>();

        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            document = builder.parse(new InputSource(new StringReader(dom.getXMLDesc(0))));

            xPathFactory = XPathFactory.newInstance();
            xPath = xPathFactory.newXPath();
            diskNodes = (NodeList)xPath.evaluate("/domain/devices/disk", document, XPathConstants.NODESET);

            for (int i = 0; i < diskNodes.getLength(); i++) {
                vmDisks.add(new VMDisk(nodeToString(diskNodes.item(i))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vmDisks;
    }

    private List<VMNetworkInterface> parseVmNetworkInterfaces(Domain dom) {
        DocumentBuilderFactory factory;
        DocumentBuilder builder;
        Document document;
        XPathFactory xPathFactory;
        XPath xPath;
        NodeList interfaceNodes;

        List<VMNetworkInterface> vmNetworkInterfaces = new ArrayList<VMNetworkInterface>();

        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            document = builder.parse(new InputSource(new StringReader(dom.getXMLDesc(0))));

            xPathFactory = XPathFactory.newInstance();
            xPath = xPathFactory.newXPath();
            interfaceNodes = (NodeList)xPath.evaluate("/domain/devices/interface", document, XPathConstants.NODESET);

            for (int i = 0; i < interfaceNodes.getLength(); i++) {
                vmNetworkInterfaces.add(new VMNetworkInterface(nodeToString(interfaceNodes.item(i))));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vmNetworkInterfaces;
    }

    private String nodeToString(Node node) {
        TransformerFactory transformerFactory;
        Transformer transformer;
        DOMSource domSource;
        StringWriter writer;
        StreamResult result;

        try {
            transformerFactory = TransformerFactory.newInstance();
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            domSource = new DOMSource(node);
            writer = new StringWriter();
            result = new StreamResult(writer);

            transformer.transform(domSource, result);

            return writer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private String getVmUUIDString() throws LibvirtException {
        return this.dom.getUUIDString();
    }

    private DomainInfo.DomainState getVmState() {
        return this.domInfo.state;
    }

    public String getVmName() throws LibvirtException {
        return this.vmName;
    }

    public UUID getVmUUID() throws LibvirtException {
        return UUID.fromString(getVmUUIDString());
    }

    public String getVmStateString() {
        String powerState;

        switch (getVmState()) {
            case DomainInfo.DomainState.VIR_DOMAIN_SHUTOFF:
                powerState = "Shut off";
                break;
            case DomainInfo.DomainState.VIR_DOMAIN_RUNNING:
                powerState = "Running";
                break;
            default:
                powerState = "Other state";
                break;
        }

        return powerState;
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
}
