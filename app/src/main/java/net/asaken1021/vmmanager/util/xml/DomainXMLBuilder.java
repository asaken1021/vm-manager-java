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
import net.asaken1021.vmmanager.util.VMGraphics;
import net.asaken1021.vmmanager.util.VMNetworkInterface;
import net.asaken1021.vmmanager.util.VMVideo;

public class DomainXMLBuilder {
    private String vmName;
    private int vmCpus;
    private long vmRam;
    private List<VMDisk> vmDisks;
    private List<VMNetworkInterface> vmNetworkInterfaces;
    private VMGraphics vmGraphics;
    private VMVideo vmVideo;

    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;
    private Document document;
    private TransformerFactory tFactory;
    private Transformer transformer;

    public DomainXMLBuilder(String vmName, int vmCpus, long vmRam, List<VMDisk> vmDisks,
    List<VMNetworkInterface> vmNetworkInterfaces, VMGraphics vmGraphics, VMVideo vmVideo)
    throws ParserConfigurationException, TransformerConfigurationException {
        this.vmName = vmName;
        this.vmCpus = vmCpus;
        this.vmRam = vmRam;
        this.vmDisks = new ArrayList<VMDisk>(vmDisks);
        this.vmNetworkInterfaces = new ArrayList<VMNetworkInterface>(vmNetworkInterfaces);
        this.vmGraphics = vmGraphics;
        this.vmVideo = vmVideo;

        this.factory = DocumentBuilderFactory.newInstance();
        this.builder = this.factory.newDocumentBuilder();
        this.document = this.builder.newDocument();
        this.tFactory = TransformerFactory.newInstance();
        this.transformer = this.tFactory.newTransformer();
    }

    public String buildXML() throws TransformerException {
        Element domain = this.document.createElement("domain");
        domain.setAttribute("type", "kvm");
        this.document.appendChild(domain);

        Element name = this.document.createElement("name");
        name.appendChild(this.document.createTextNode(this.vmName));
        domain.appendChild(name);

        Element vcpu = this.document.createElement("vcpu");
        vcpu.setAttribute("placement", "static");
        vcpu.appendChild(this.document.createTextNode(String.valueOf(this.vmCpus)));
        domain.appendChild(vcpu);

        Element cpu = this.document.createElement("cpu");
        cpu.setAttribute("mode", "host-passthrough");
        Element topology = this.document.createElement("topology");
        topology.setAttribute("sockets", "1");
        topology.setAttribute("dies", "1");
        topology.setAttribute("clusters", "1");
        topology.setAttribute("cores", String.valueOf(this.vmCpus));
        topology.setAttribute("threads", "1");
        cpu.appendChild(topology);
        domain.appendChild(cpu);

        Element memory = this.document.createElement("memory");
        memory.setAttribute("unit", "KiB");
        memory.appendChild(this.document.createTextNode(String.valueOf(this.vmRam)));
        domain.appendChild(memory);

        Element os = this.document.createElement("os");
        Element type = this.document.createElement("type");
        type.setAttribute("arch", "x86_64");
        type.setAttribute("machine", "q35");
        type.appendChild(this.document.createTextNode("hvm"));
        os.appendChild(type);
        Element loader = this.document.createElement("loader");
        loader.setAttribute("readonly", "yes");
        loader.setAttribute("type", "pflash");
        loader.appendChild(this.document.createTextNode("/usr/share/edk2/ovmf/OVMF_CODE.fd"));
        os.appendChild(loader);
        Element nvram = this.document.createElement("nvram");
        nvram.setAttribute("template", "/usr/share/edk2/ovmf/OVMF_VARS.fd");
        nvram.appendChild(this.document.createTextNode("/var/lib/libvirt/qemu/nvram/" + this.vmName + "_VARS.fd"));
        os.appendChild(nvram);
        Element boot = this.document.createElement("boot");
        boot.setAttribute("dev", "hd");
        os.appendChild(boot);
        domain.appendChild(os);

        Element features = this.document.createElement("features");
        Element acpi = this.document.createElement("acpi");
        features.appendChild(acpi);
        domain.appendChild(features);

        Element devices = this.document.createElement("devices");
        
        for (VMDisk vmdisk : this.vmDisks) {
            Element disk = this.document.createElement("disk");
            disk.setAttribute("type", vmdisk.getType());
            disk.setAttribute("device", vmdisk.getDevice());

            Element driver = this.document.createElement("driver");
            driver.setAttribute("name", vmdisk.getDriverName());
            driver.setAttribute("type", vmdisk.getDriverType());
            disk.appendChild(driver);

            Element source = this.document.createElement("source");
            source.setAttribute("file", vmdisk.getSourceFile());
            disk.appendChild(source);

            Element target = this.document.createElement("target");
            target.setAttribute("dev", vmdisk.getTargetDev());
            target.setAttribute("bus", vmdisk.getTargetBus());
            disk.appendChild(target);

            devices.appendChild(disk);
        }

        for (VMNetworkInterface vminterface : this.vmNetworkInterfaces) {
            Element netinterface = this.document.createElement("interface");
            netinterface.setAttribute("type", vminterface.getInterfaceType().getText());

            if (Objects.nonNull(vminterface.getMacAddress()) && !vminterface.getMacAddress().isEmpty()) {
                Element mac = this.document.createElement("mac");
                mac.setAttribute("address", vminterface.getMacAddress());
                netinterface.appendChild(mac);
            }

            Element source = this.document.createElement("source");
            source.setAttribute(vminterface.getInterfaceType().getText(), vminterface.getSource());
            netinterface.appendChild(source);

            Element model = this.document.createElement("model");
            model.setAttribute("type", vminterface.getModel());
            netinterface.appendChild(model);

            devices.appendChild(netinterface);
        }

        Element graphics = this.document.createElement("graphics");
        Element graphicsListen = this.document.createElement("listen");
        graphics.setAttribute("type", vmGraphics.getGraphicsType());
        graphics.setAttribute("port", String.valueOf(vmGraphics.getPort()));
        graphics.setAttribute("autoport", "yes");
        graphics.setAttribute("listen", vmGraphics.getAddress());
        graphicsListen.setAttribute("type", vmGraphics.getListenType());
        graphicsListen.setAttribute("address", vmGraphics.getAddress());
        graphics.appendChild(graphicsListen);
        devices.appendChild(graphics);

        Element video = this.document.createElement("video");
        Element videoModel = this.document.createElement("model");
        videoModel.setAttribute("type", vmVideo.getType().getText());
        videoModel.setAttribute("vram", String.valueOf(vmVideo.getVram()));
        videoModel.setAttribute("heads", "1");
        videoModel.setAttribute("primary", "yes");
        video.appendChild(videoModel);
        devices.appendChild(video);

        domain.appendChild(devices);

        DOMSource source = new DOMSource(this.document);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        this.transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        this.transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        this.transformer.transform(source, result);

        return writer.getBuffer().toString();
    }
}
