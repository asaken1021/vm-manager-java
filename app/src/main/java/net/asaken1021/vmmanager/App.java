package net.asaken1021.vmmanager;

import org.libvirt.LibvirtException;

import net.asaken1021.vmmanager.util.vmDomain;
import net.asaken1021.vmmanager.util.vmManager;
import net.asaken1021.vmmanager.util.vmRamUnit;

public class App {
    public static void main(String[] args) throws LibvirtException {
        vmManager vmm = new vmManager("qemu:///system");

        System.out.println("VM Count: " + vmm.getVmNames().length);

        printLine();

        for (String vmName : vmm.getVmNames()) {
            vmDomain dom = new vmDomain(vmm.getConnect(), vmName);

            System.out.println(
                "Name: " + dom.getVmName() + "\n" +
                "UUID: " + dom.getVmUUID().toString() + "\n" +
                "State: " + dom.getVmStateString() + "\n" +
                "vCPUs: " + dom.getVmCpus() + ", RAM: " + dom.getVmRamSize(vmRamUnit.RAM_MiB) + "MiB"
            );

            printLine();
        }
    }

    private static void printLine() {
        for (int i = 0; i < 50; i++) {
            System.out.print("-");
        }
        System.out.println();
    }
}
