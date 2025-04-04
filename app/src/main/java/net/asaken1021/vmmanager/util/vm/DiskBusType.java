package net.asaken1021.vmmanager.util.vm;

import net.asaken1021.vmmanager.util.TypeNotFoundException;

public enum DiskBusType {
    BUS_SATA("sata"),
    BUS_SCSI("scsi"),
    BUS_VIRTIO("virtio");

    private String typeText;
    
    private DiskBusType(String typeText) {
        this.typeText = typeText;
    }

    public String getTypeText() {
        return this.typeText;
    }

    public static DiskBusType getTypeByString(String type) throws TypeNotFoundException {
        if (type.equals(DiskBusType.BUS_SATA.getTypeText())) {
            return DiskBusType.BUS_SATA;
        } else if (type.equals(DiskBusType.BUS_SCSI.getTypeText())) {
            return DiskBusType.BUS_SCSI;
        } else if (type.equals(DiskBusType.BUS_VIRTIO.getTypeText())) {
            return DiskBusType.BUS_VIRTIO;
        }

        throw new TypeNotFoundException();
    }
}
