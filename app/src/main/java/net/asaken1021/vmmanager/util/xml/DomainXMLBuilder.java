package net.asaken1021.vmmanager.util.xml;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.asaken1021.vmmanager.util.VMDisk;
import net.asaken1021.vmmanager.util.VMNetworkInterface;

public class DomainXMLBuilder {
    private String vmName;
    private int vmCpus;
    private long vmRam;
    private List<VMDisk> vmDisks;
    private List<VMNetworkInterface> vmNetworkInterfaces;

    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;
    private Document document;
    private TransformerFactory tFactory;
    private Transformer transformer;

    public DomainXMLBuilder(String vmName, int vmCpus, long vmRam, List<VMDisk> vmDisks,
    List<VMNetworkInterface> vmNetworkInterfaces) throws ParserConfigurationException, TransformerConfigurationException {
        this.vmName = vmName;
        this.vmCpus = vmCpus;
        this.vmRam = vmRam;
        this.vmDisks = new ArrayList<VMDisk>(vmDisks);
        this.vmNetworkInterfaces = new ArrayList<VMNetworkInterface>(vmNetworkInterfaces);

        this.factory = DocumentBuilderFactory.newInstance();
        this.builder = this.factory.newDocumentBuilder();
        this.document = builder.newDocument();
        this.tFactory = TransformerFactory.newInstance();
        this.transformer = tFactory.newTransformer();
    }

    public String buildXML() throws TransformerException {
        Element domain = document.createElement("domain");
        domain.setAttribute("type", "kvm");
        document.appendChild(domain);

        Element name = document.createElement("name");
        name.appendChild(document.createTextNode(this.vmName));
        domain.appendChild(name);

        Element vcpu = document.createElement("vcpu");
        vcpu.setAttribute("placement", "static");
        vcpu.appendChild(document.createTextNode(String.valueOf(this.vmCpus)));
        domain.appendChild(vcpu);

        Element cpu = document.createElement("cpu");
        cpu.setAttribute("mode", "host-passthrough");
        Element topology = document.createElement("topology");
        topology.setAttribute("sockets", "1");
        topology.setAttribute("dies", "1");
        topology.setAttribute("clusters", "1");
        topology.setAttribute("cores", String.valueOf(this.vmCpus));
        topology.setAttribute("threads", "1");
        cpu.appendChild(topology);
        domain.appendChild(cpu);

        Element memory = document.createElement("memory");
        memory.setAttribute("unit", "KiB");
        memory.appendChild(document.createTextNode(String.valueOf(this.vmRam)));
        domain.appendChild(memory);

        Element os = document.createElement("os");
        Element type = document.createElement("type");
        type.setAttribute("arch", "x86_64");
        type.setAttribute("machine", "q35");
        type.appendChild(document.createTextNode("hvm"));
        os.appendChild(type);
        Element loader = document.createElement("loader");
        loader.setAttribute("readonly", "yes");
        loader.setAttribute("type", "pflash");
        loader.appendChild(document.createTextNode("/usr/share/edk2/ovmf/OVMF_CODE.fd"));
        os.appendChild(loader);
        Element nvram = document.createElement("nvram");
        nvram.setAttribute("template", "/usr/share/edk2/ovmf/OVMF_VARS.fd");
        nvram.appendChild(document.createTextNode("/var/lib/libvirt/qemu/nvram/" + this.vmName + "_VARS.fd"));
        os.appendChild(nvram);
        Element boot = document.createElement("boot");
        boot.setAttribute("dev", "hd");
        os.appendChild(boot);
        domain.appendChild(os);

        Element features = document.createElement("features");
        Element acpi = document.createElement("acpi");
        features.appendChild(acpi);
        domain.appendChild(features);

        Element devices = document.createElement("devices");
        
        for (VMDisk vmdisk : this.vmDisks) {
            Element disk = document.createElement("disk");
            disk.setAttribute("type", vmdisk.getType());
            disk.setAttribute("device", vmdisk.getDevice());

            Element driver = document.createElement("driver");
            driver.setAttribute("name", vmdisk.getDriverName());
            driver.setAttribute("type", vmdisk.getDriverType());
            disk.appendChild(driver);

            Element source = document.createElement("source");
            source.setAttribute("file", vmdisk.getSourceFile());
            disk.appendChild(source);

            Element target = document.createElement("target");
            target.setAttribute("dev", vmdisk.getTargetDev());
            target.setAttribute("bus", vmdisk.getTargetBus());
            disk.appendChild(target);

            devices.appendChild(disk);
        }

        for (VMNetworkInterface vminterface : this.vmNetworkInterfaces) {
            Element netinterface = document.createElement("interface");
            netinterface.setAttribute("type", vminterface.getInterfaceType().getText());

            if (Objects.nonNull(vminterface.getMacAddress()) && !vminterface.getMacAddress().isEmpty()) {
                Element mac = document.createElement("mac");
                mac.setAttribute("address", vminterface.getMacAddress());
                netinterface.appendChild(mac);
            }

            Element source = document.createElement("source");
            source.setAttribute(vminterface.getInterfaceType().getText(), vminterface.getSource());
            netinterface.appendChild(source);

            Element model = document.createElement("model");
            model.setAttribute("type", vminterface.getModel());
            netinterface.appendChild(model);

            devices.appendChild(netinterface);
        }

        domain.appendChild(devices);

        DOMSource source = new DOMSource(document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);

        return writer.getBuffer().toString();
    }
}
