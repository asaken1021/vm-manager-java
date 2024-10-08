package vm.manager.java;

import org.libvirt.*;

public class App {
    public static void main(String[] args) throws LibvirtException {
        Connect conn = null;

        try {
            conn = new Connect("qemu:///system");
        } catch (LibvirtException e) {
            System.err.println("Cannot connect to libvirt daemon.");
            e.printStackTrace();

            return;
        }

        String[] vms = conn.listDefinedDomains();

        System.out.println("VM Count: " + vms.length);

        printLine();

        for (String vm : vms) {
            Domain dom = conn.domainLookupByName(vm);

            System.out.println(
                "Name: " + dom.getName() + "\n" +
                "UUID: " + dom.getUUIDString() + "\n" +
                "State: " + getPowerStateString(dom.getInfo().state)
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

    private static String getPowerStateString(DomainInfo.DomainState state) {
        String powerState;

        switch (state) {
            case DomainInfo.DomainState.VIR_DOMAIN_SHUTOFF:
                powerState = "Shut off";
                break;
            case DomainInfo.DomainState.VIR_DOMAIN_RUNNING:
                powerState = "Running";
                break;
            default: 
                powerState = "Other state";
                break;
        }

        return powerState;
    }
}
