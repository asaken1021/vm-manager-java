package net.asaken1021.vmmanager;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;

import net.asaken1021.vmmanager.util.*;
import net.asaken1021.vmmanager.util.vm.networkinterface.InterfaceType;
import net.asaken1021.vmmanager.util.vm.video.VideoType;

public class App {
    public static void main(String[] args) {
        try {
            VMManager vmm = new VMManager("qemu:///system");
            int select = 0;
            Scanner scanner = new Scanner(System.in);

            List<String> vmNames;

            String vmName;
            int vmCpus;
            long vmRam;
            String vmDiskPath;
            List<VMDisk> vmDisks = new ArrayList<VMDisk>();
            List<VMNetworkInterface> vmNetworkInterfaces = new ArrayList<VMNetworkInterface>();
            VMGraphics vmGraphics;
            VMVideo vmVideo;

            VMDomain domain;

            while (select >= 0) {
                printLine();

                System.out.println("MENU");
                System.out.println("Create VM    :  1");
                System.out.println("Get VMs List :  2");
                System.out.println("Get VM Info  :  3");
                System.out.println("Delete VM    :  4");
                System.out.println("Exit         : -1");
                System.out.print("Select > ");
                select = scanner.nextInt();

                printLine();

                switch (select) {
                    case 1:
                        System.out.print("VM Name > ");
                        vmName = scanner.next();
                        System.out.print("VM Cpus > ");
                        vmCpus = scanner.nextInt();
                        System.out.print("VM Ram Size (MiB) > ");
                        vmRam = scanner.nextLong() * 1024;
                        System.out.print("VM Disk path > ");
                        vmDiskPath = scanner.next();
                        vmDisks.add(new VMDisk("disk", "file", "qemu", "qcow2", vmDiskPath, "vda", "virtio"));
                        vmNetworkInterfaces.add(new VMNetworkInterface(null, "virbr0", "virtio", InterfaceType.IF_BRIDGE));
                        vmVideo = new VMVideo(VideoType.VIDEO_VIRTIO);
                        vmGraphics = new VMGraphics("vnc", -1);
                        vmm.createVm(vmName, vmCpus, vmRam, vmDisks, vmNetworkInterfaces, vmGraphics, vmVideo);
                        break;
                    case 2:
                        vmNames = vmm.getVmNames();
                        for (String name : vmNames) {
                            System.out.println(name);
                        }
                        break;
                    case 3:
                        System.out.print("VM Name > ");
                        vmName = scanner.next();
                        domain = vmm.getVm(vmName);
                        System.out.println("VM Name : " + domain.getVmName());
                        System.out.println("vCPUs   : " + domain.getVmCpus());
                        System.out.println("RAM(MiB): " + domain.getVmRamSize(VMRamUnit.RAM_MiB));
                        System.out.println("Disks   : ");
                        for (VMDisk disk : domain.getVmDisks()) {
                            System.out.println("  Disk Path: " + disk.getSourceFile());
                            System.out.println("  Disk Type: " + disk.getType());
                        }
                        System.out.println("Network :");
                        for (VMNetworkInterface iface : domain.getVmNetworkInterfaces()) {
                            System.out.println("  Interface Mac Address: " + iface.getMacAddress());
                            System.out.println("  Interface Type       : " + iface.getInterfaceType().getText());
                            System.out.println("  Interface Source     : " + iface.getSource());
                        }
                        System.out.println("Graphics:");
                        System.out.println("  Graphics Type: " + domain.getVmGraphics().getGraphicsType());
                        System.out.println("Video   :");
                        System.out.println("  Video Type: " + domain.getVmVideo().getType().getText());
                        break;
                    case 4:
                        System.out.print("VM Name > ");
                        vmName = scanner.next();
                        if (vmm.deleteVm(vmName)) {
                            System.out.println("Deleted Successfully");
                        } else {
                            System.out.println("Failed to delete vm");
                        }
                        break;
                }
            }

            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printLine() {
        for (int i = 0; i < 50; i++) {
            System.out.print("-");
        }
        System.out.println();
    }
}
