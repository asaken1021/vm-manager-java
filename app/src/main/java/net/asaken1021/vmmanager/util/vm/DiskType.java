package net.asaken1021.vmmanager.util.vm;

import net.asaken1021.vmmanager.util.TypeNotFoundException;

public enum DiskType {
    DISK_CDROM("cdrom", "file", "qemu", "raw"),
    DISK_HARDDISK_RAW("disk", "file", "qemu", "raw"),
    DISK_HARDDISK_QCOW2("disk", "file", "qemu", "qcow2");

    private String diskDevice;
    private String diskType;
    private String driverName;
    private String driverType;

    private DiskType(String diskDevice, String diskType, String driverName, String driverType) {
        this.diskDevice = diskDevice;
        this.diskType = diskType;
        this.driverName = driverName;
        this.driverType = driverType;
    }

    public String getDiskDevice() {
        return this.diskDevice;
    }
    
    public String getDiskType() {
        return this.diskType;
    }
    
    public String getDriverName() {
        return this.driverName;
    }
    
    public String getDriverType() {
        return this.driverType;
    }

    public static DiskType getTypeByString(String diskDevice, String driverType) throws TypeNotFoundException {
        if (diskDevice.equals("cdrom") && driverType.equals("raw")) {
            return DiskType.DISK_CDROM;
        } else if (diskDevice.equals("disk") && driverType.equals("raw")) {
            return DiskType.DISK_HARDDISK_RAW;
        } else if (diskDevice.equals("disk") && driverType.equals("qcow2")) {
            return DiskType.DISK_HARDDISK_QCOW2;
        }

        throw new TypeNotFoundException();
    }
}
