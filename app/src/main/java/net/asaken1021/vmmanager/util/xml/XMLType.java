package net.asaken1021.vmmanager.util.xml;

public enum XMLType {
    TYPE_BOOT("/domain/os/boot"),
    TYPE_DISK("/domain/devices/disk"),
    TYPE_GRAPHICS("/domain/devices/graphics"),
    TYPE_NETWORKINTERFACE("/domain/devices/interface"),
    TYPE_VIDEO("/domain/devices/video");

    private String xPath;

    private XMLType(String xPath) {
        this.xPath = xPath;
    }

    public String getXPath() {
        return this.xPath;
    }
}
