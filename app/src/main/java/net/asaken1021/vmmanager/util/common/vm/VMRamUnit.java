package net.asaken1021.vmmanager.util.common.vm;

import net.asaken1021.vmmanager.util.TypeNotFoundException;

public enum VMRamUnit {
    RAM_GiB("GiB"), 
    RAM_MiB("MiB");

    private String unitText;

    private VMRamUnit(String unitText) {
        this.unitText = unitText;
    }

    public String getUnitText() {
        return this.unitText;
    }

    public static VMRamUnit getUnitByString(String unit) throws TypeNotFoundException {
        if (unit.equals(VMRamUnit.RAM_GiB.getUnitText())) {
            return VMRamUnit.RAM_GiB;
        } else if (unit.equals(VMRamUnit.RAM_MiB.getUnitText())) {
            return VMRamUnit.RAM_MiB;
        }

        throw new TypeNotFoundException();
    }
}
