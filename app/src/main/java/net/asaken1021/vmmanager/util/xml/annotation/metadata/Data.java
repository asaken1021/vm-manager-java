package net.asaken1021.vmmanager.util.xml.annotation.metadata;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class Data {
    @XmlElement(name="vm_folder_path", namespace="https://github.com/asaken1021/vm-manager-java")
    private String vmFolderPath;

    public String getVmFolderPath() {
        return this.vmFolderPath;
    }

    public void setVmFolderPath(String vmFolderPath) {
        this.vmFolderPath = vmFolderPath;
    }
}