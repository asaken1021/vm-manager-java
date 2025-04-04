package net.asaken1021.vmmanager;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

import org.libvirt.LibvirtException;

import net.asaken1021.vmmanager.util.*;
import net.asaken1021.vmmanager.util.vm.*;

public class CliApp {
    private VMManager vmm;
    private int select = 0;
    private Scanner scanner = new Scanner(System.in);

    private List<String> vmNames;

    private String vmName;
    private int vmCpus;
    private long vmRam;
    private LinkedHashMap<VMDisk, Integer> vmDisks;
    private List<VMNetworkInterface> vmNetworkInterfaces;
    private VMGraphics vmGraphics;
    private VMVideo vmVideo;
    private List<VMBoot> vmBoots;

    private VMDomain domain;

    private String uri;

    public CliApp(String uri) {
        this.uri = uri;
    }

    public void run() {
        try {
            vmm = new VMManager(uri);
        } catch (ConnectException e) {
            printError(e.getLocalizedMessage());
            scanner.close();
            return;
        }

        while (select >= 0) {
            vmNames = new ArrayList<String>();

            vmName = "";
            vmCpus = 0;
            vmRam = 0;
            vmDisks = new LinkedHashMap<VMDisk, Integer>();
            vmNetworkInterfaces = new ArrayList<VMNetworkInterface>();

            printLine();

            System.out.println("メニュー");
            System.out.println("仮想マシンの作成:  1");
            System.out.println("仮想マシンの一覧:  2");
            System.out.println("仮想マシンの情報:  3");
            System.out.println("仮想マシンの削除:  4");
            System.out.println("仮想マシンの操作:  5");
            System.out.println("終了            : -1");
            System.out.print("選択肢を入力 > ");

            try {
                select = scanner.nextInt();

                printLine();

                switch (select) {
                    case 1:
                        System.out.println("仮想マシン作成");

                        System.out.print("- 仮想マシン名 > ");
                        vmName = scanner.next();

                        System.out.print("- CPU数 > ");
                        vmCpus = scanner.nextInt();

                        System.out.print("- RAM(MiB) > ");
                        vmRam = scanner.nextLong();

                        System.out.println("- 仮想ディスクの追加");
                        vmDisks = createVmDisks(scanner);

                        System.out.println("- ネットワークインターフェイスの追加");
                        vmNetworkInterfaces = createVmNetworkInterfaces(scanner);
                        
                        vmVideo = new VMVideo(VideoType.VIDEO_VIRTIO);
                        vmGraphics = new VMGraphics("vnc", -1);

                        System.out.println("- 起動順序の設定");
                        vmBoots = createVmBoots(scanner);
                        
                        domain = vmm.createVm(vmName, vmCpus, vmRam, VMRamUnit.RAM_MiB, vmDisks, vmNetworkInterfaces, vmGraphics, vmVideo, vmBoots);
                        System.out.println("仮想マシン " + domain.getVmName() + " を作成しました");
                        break;
                    case 2:
                        System.out.println("仮想マシンの一覧");
                        vmNames = vmm.getVmNames();
                        for (String name : vmNames) {
                            System.out.println("- " + name);
                        }
                        break;
                    case 3:
                        System.out.print("仮想マシン名 > ");
                        vmName = scanner.next();

                        System.out.println("仮想マシン情報");

                        domain = vmm.getVm(vmName);

                        System.out.println("- 仮想マシン名  : " + domain.getVmName());
                        System.out.println("- 電源状態      : " + domain.getVmPowerState().getStateText());
                        System.out.println("- CPU数         : " + domain.getVmCpus());
                        System.out.println("- RAM(MiB)      : " + domain.getVmRamSize(VMRamUnit.RAM_MiB));
                        System.out.println("- 仮想ディスク  : ");
                        for (VMDisk disk : domain.getVmDisks()) {
                            System.out.println("- - ファイルパス: " + disk.getSourceFile());
                            System.out.println("- - ディスク情報: " + disk.getDevice() + ", "
                                + disk.getDriverType() + ", " + disk.getTargetDev() + ", " + disk.getTargetBus());
                        }
                        System.out.println("- ネットワーク  :");
                        for (VMNetworkInterface iface : domain.getVmNetworkInterfaces()) {
                            System.out.println("- - MACアドレス : " + iface.getMacAddress());
                            System.out.println("- - タイプ      : " + iface.getInterfaceType().getTypeText());
                            System.out.println("- - ソース      : " + iface.getSource());
                        }
                        System.out.println("- グラフィックス:");
                        System.out.println("- - 接続タイプ  : " + domain.getVmGraphics().getGraphicsType());
                        System.out.println("- 画面出力      :");
                        System.out.println("- - 出力デバイス: " + domain.getVmVideo().getType().getText());
                        System.out.println("- 起動順序      : ");
                        for (VMBoot vmBoot : domain.getVmBoots()) {
                            System.out.println("- - デバイス種別: " + vmBoot.getDev());
                        }
                        break;
                    case 4:
                        System.out.print("仮想マシン名 > ");
                        vmName = scanner.next();
                        vmm.deleteVm(this.vmm.getVm(vmName).getVmUUID());
                        System.out.println("仮想マシンを削除しました");
                        break;
                    case 5:
                        System.out.print("仮想マシン名 > ");
                        vmName = scanner.next();
                        controlVm(scanner, vmm, vmName);
                }
            } catch (DomainCreateException | DomainLookupException | DomainDeleteException | FileNotFoundException
            | TypeNotFoundException | InterfaceNotFoundException | DomainStartException | DomainStopException e) {
                printError(e.getLocalizedMessage());
            } catch (InputMismatchException e) {
                printLine();
                printError("入力に誤りがあります");
                scanner = new Scanner(System.in);
            }
        }

        scanner.close();
        try {
            vmm.disconnect();
        } catch (LibvirtException e) {
            e.printStackTrace();
        }
    }

    private void printLine() {
        for (int i = 0; i < 50; i++) {
            System.out.print("-");
        }
        System.out.println();
    }

    private void printError(String message) {
        System.err.println("エラー: " + message);
    }

    private LinkedHashMap<VMDisk, Integer> createVmDisks(Scanner scanner) throws FileNotFoundException {
        LinkedHashMap<VMDisk, Integer> vmDisks = new LinkedHashMap<VMDisk, Integer>();
        boolean addDisk = true;
        String filePath, fileType, diskType, diskDev, diskBus, select;
        int diskSize;

        while (addDisk) {
            System.out.println("- * 仮想ディスクが指定されたパスに無い場合は作成され，");
            System.out.println("- * 既に存在する場合はそれが使用されます．");

            System.out.print("- - 仮想ディスクファイルの絶対パス > ");
            filePath = scanner.next();

            System.out.print("- - 仮想ディスクファイルの種類 [qcow2/raw] > ");
            fileType = scanner.next();

            System.out.print("- - 仮想ディスクの種類 [disk/cdrom] > ");
            diskType = scanner.next();

            if (diskType.equals("disk") && !this.vmm.getDiskUtil().checkDiskExists(filePath)) {
                System.out.print("- - 仮想ディスクのサイズ(整数, GB) > ");
                diskSize = scanner.nextInt();
            } else {
                diskSize = 0;
            }

            System.out.print("- - 仮想ディスクのバスタイプ [virtio/sata/scsi] > ");
            diskBus = scanner.next();

            System.out.print("- - 仮想ディスクのデバイス名 [sdX/vdX] > ");
            diskDev = scanner.next();

            vmDisks.put(new VMDisk(diskType, "file", "qemu", fileType, filePath, diskDev, diskBus), diskSize);

            System.out.print("- さらに仮想ディスクを追加しますか? [y/n] > ");
            select = scanner.next();

            if (select.equalsIgnoreCase("n")) {
                addDisk = false;
            }
        }

        return vmDisks;
    }

    private List<VMNetworkInterface> createVmNetworkInterfaces(Scanner scanner)
    throws TypeNotFoundException, InterfaceNotFoundException {
        List<VMNetworkInterface> vmNetworkInterfaces = new ArrayList<VMNetworkInterface>();
        boolean addInterface = true;
        String macAddress, source, model, type, select;

        while (addInterface) {
            System.out.print("- - MACアドレス (\"0\"ならランダム) > ");
            macAddress = scanner.next();

            System.out.print("- - 接続方法 [network/bridge] > ");
            type = scanner.next();

            System.out.print("- - 接続先 (ネットワークやブリッジの名前) > ");
            source = scanner.next();

            System.out.print("- - インターフェイスモデル [virtio/e1000/e1000e] > ");
            model = scanner.next();

            if (macAddress.equals("0")) {
                macAddress = "";
            }

            vmNetworkInterfaces.add(new VMNetworkInterface(macAddress, source, model, NetworkInterfaceType.getTypeByString(type), vmm));

            System.out.print("- さらにインターフェイスを追加しますか? [y/n] > ");
            select = scanner.next();

            if (select.equalsIgnoreCase("n")) {
                addInterface = false;
            }
        }

        return vmNetworkInterfaces;
    }

    private List<VMBoot> createVmBoots(Scanner scanner) {
        List<VMBoot> vmBoots = new ArrayList<VMBoot>();
        boolean addBoot = true;
        String dev, select;
        int bootOrderNum = 1;

        while(addBoot) {
            System.out.print("- - "+ bootOrderNum + "番目の起動デバイス名 [hd/cdrom] > ");
            dev = scanner.next();

            vmBoots.add(new VMBoot(dev, true));

            System.out.print("- さらに起動デバイスを追加しますか? [y/n] > ");
            select = scanner.next();

            if (select.equalsIgnoreCase("n")) {
                addBoot = false;
            }

            bootOrderNum++;
        }

        return vmBoots;
    }

    private void controlVm(Scanner scanner, VMManager vmm, String name)
    throws DomainLookupException, DomainStartException, DomainStopException {
        int select;

        VMDomain domain = vmm.getVm(name);

        System.out.println("- 操作を選択");
        System.out.println("- 起動: 1");
        System.out.println("- 強制停止: 2");
        System.out.print("- 選択肢を入力 > ");
        select = scanner.nextInt();

        switch (select) {
            case 1:
                vmm.startVm(domain.getVmUUID());
                System.out.println("仮想マシン " + domain.getVmName() + " を起動しました");
                break;
            case 2:
                vmm.stopVm(domain.getVmUUID());
                System.out.println("仮想マシン " + domain.getVmName() + " を強制停止しました");
                break;
            default:
                System.out.println("無効な選択です");
                break;
        }
    }
}
