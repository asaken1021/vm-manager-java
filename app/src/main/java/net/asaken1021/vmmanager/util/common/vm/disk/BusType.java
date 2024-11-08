package net.asaken1021.vmmanager.util.common.vm.disk;

import net.asaken1021.vmmanager.util.TypeNotFoundException;

public enum BusType {
    BUS_SATA("sata"),
    BUS_SCSI("scsi"),
    BUS_VIRTIO("virtio");

    private String typeText;
    
    private BusType(String typeText) {
        this.typeText = typeText;
    }

    public String getTypeText() {
        return this.typeText;
    }

    public static BusType getTypeByString(String type) throws TypeNotFoundException {
        if (type.equals(BusType.BUS_SATA.getTypeText())) {
            return BusType.BUS_SATA;
        } else if (type.equals(BusType.BUS_SCSI.getTypeText())) {
            return BusType.BUS_SCSI;
        } else if (type.equals(BusType.BUS_VIRTIO.getTypeText())) {
            return BusType.BUS_VIRTIO;
        }

        throw new TypeNotFoundException();
    }
}
