package net.asaken1021.vmmanager.util.vm.networkinterface;

public enum InterfaceType {
    IF_NETWORK("network"),
    IF_BRIDGE("bridge");

    private String text;

    private InterfaceType(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public static InterfaceType getTypeByString(String type) {
        if (type.equals(InterfaceType.IF_NETWORK.getText())) {
            return InterfaceType.IF_NETWORK;
        } else if (type.equals(InterfaceType.IF_BRIDGE.getText())) {
            return InterfaceType.IF_BRIDGE;
        }
        
        return null;
    }
}
