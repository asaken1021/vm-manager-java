package net.asaken1021.vmmanager.util.vm;

import net.asaken1021.vmmanager.util.TypeNotFoundException;

public enum NetworkInterfaceType {
    IF_BRIDGE("bridge"),
    IF_NETWORK("network");

    private String typeText;

    private NetworkInterfaceType(String typeText) {
        this.typeText = typeText;
    }

    public String getTypeText() {
        return this.typeText;
    }

    public static NetworkInterfaceType getTypeByString(String type) throws TypeNotFoundException {
        if (type.equals(NetworkInterfaceType.IF_NETWORK.getTypeText())) {
            return NetworkInterfaceType.IF_NETWORK;
        } else if (type.equals(NetworkInterfaceType.IF_BRIDGE.getTypeText())) {
            return NetworkInterfaceType.IF_BRIDGE;
        }
        
        throw new TypeNotFoundException();
    }
}
