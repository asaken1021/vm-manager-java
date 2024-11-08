package net.asaken1021.vmmanager.util.common.vm.networkinterface;

import net.asaken1021.vmmanager.util.TypeNotFoundException;

public enum InterfaceType {
    IF_BRIDGE("bridge"),
    IF_NETWORK("network");

    private String typeText;

    private InterfaceType(String typeText) {
        this.typeText = typeText;
    }

    public String getTypeText() {
        return this.typeText;
    }

    public static InterfaceType getTypeByString(String type) throws TypeNotFoundException {
        if (type.equals(InterfaceType.IF_NETWORK.getTypeText())) {
            return InterfaceType.IF_NETWORK;
        } else if (type.equals(InterfaceType.IF_BRIDGE.getTypeText())) {
            return InterfaceType.IF_BRIDGE;
        }
        
        throw new TypeNotFoundException();
    }
}
