package net.asaken1021.vmmanager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import flak.App;
import flak.Flak;
import flak.annotations.Post;
import flak.annotations.Route;
import flak.jackson.JSON;

import net.asaken1021.vmmanager.util.ConnectException;
import net.asaken1021.vmmanager.util.DomainCreateException;
import net.asaken1021.vmmanager.util.DomainLookupException;
import net.asaken1021.vmmanager.util.DomainPowerState;
import net.asaken1021.vmmanager.util.DomainStartException;
import net.asaken1021.vmmanager.util.DomainStopException;
import net.asaken1021.vmmanager.util.FileNotFoundException;
import net.asaken1021.vmmanager.util.InterfaceNotFoundException;
import net.asaken1021.vmmanager.util.InvalidPowerStateException;
import net.asaken1021.vmmanager.util.TypeNotFoundException;
import net.asaken1021.vmmanager.util.VMManager;
import net.asaken1021.vmmanager.util.vm.VMDisk;
import net.asaken1021.vmmanager.util.vm.VMDomain;
import net.asaken1021.vmmanager.util.vm.VMGraphics;
import net.asaken1021.vmmanager.util.vm.VMNetworkInterface;
import net.asaken1021.vmmanager.util.vm.VMRamUnit;
import net.asaken1021.vmmanager.util.vm.VMVideo;
import net.asaken1021.vmmanager.util.vm.networkinterface.InterfaceType;
import net.asaken1021.vmmanager.util.vm.video.VideoType;

public class WebApiApp {
    private VMManager vmm;
    private String uri;
    private String isoImagesPath;

    private App webApp;

    public WebApiApp(String uri, String isoImagesPath) {
        this.uri = uri;
        this.isoImagesPath = isoImagesPath;
        try {
            this.vmm = new VMManager(this.uri);
        } catch (ConnectException e) {
            printError(e.getLocalizedMessage());
        }
    }

    public WebApiApp(VMManager vmm, String isoImagesPath) {
        this.vmm = vmm;
        this.isoImagesPath = isoImagesPath;
    }

    public void run() {
        try {
            this.webApp = Flak.createHttpApp(8080);
            webApp.scan(new WebApiApp(this.vmm, this.isoImagesPath));
            webApp.start();
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException | IOException e) {
            printError(e.getLocalizedMessage());
        }
    }

    private void printError(String message) {
        System.err.println("エラー: " + message);
    }

    @Route("/")
    public String index() {
        return "index";
    }

    @Route("/vms")
    @JSON
    public Map<String, Object> getVms() {
        Map<String, Object> response = new HashMap<String, Object>();
        List<Map<String, Object>> vms = new ArrayList<Map<String, Object>>();
        Map<String, Object> vm = new LinkedHashMap<String, Object>();

        try {
            for (String name : this.vmm.getVmNames()) {
                vm = new HashMap<String, Object>();
                vm.put("uuid", this.vmm.getVm(name).getVmUUID().toString());
                vm.put("name", name);
                vms.add(vm);
            }
            response.put("vms", vms);
        } catch (DomainLookupException e) {
            response.put("error", e.getLocalizedMessage()); // err
        }

        return response;
    }

    @Route("/vms/:uuid")
    @JSON
    public Map<String, Object> getVmByUUID(String uuid) {
        Map<String, Object> response = new HashMap<String, Object>();
        Map<String, Object> vm = new LinkedHashMap<String, Object>();
        List<Map<String, Object>> nestedDatas = new ArrayList<Map<String, Object>>();
        Map<String, Object> nestedData = new LinkedHashMap<String, Object>();
        VMDomain vmDomain;

        try {
            vmDomain = this.vmm.getVm(UUID.fromString(uuid));
        } catch (DomainLookupException e) {
            response.put("error", e.getLocalizedMessage());
            return response; // err
        }

        vm.put("uuid", vmDomain.getVmUUID().toString());
        vm.put("name", vmDomain.getVmName());
        vm.put("state", vmDomain.getVmPowerState().getStateText());
        vm.put("cpus", vmDomain.getVmCpus());
        vm.put("ram", vmDomain.getVmRamSize(VMRamUnit.RAM_MiB));
        vm.put("ram_unit", VMRamUnit.RAM_MiB.getUnitText());

        nestedDatas = new ArrayList<Map<String, Object>>();
        for (VMDisk disk : vmDomain.getVmDisks()) {
            nestedData = new LinkedHashMap<String, Object>();

            nestedData.put("file_path", disk.getSourceFile());
            nestedData.put("device", disk.getDevice());
            nestedData.put("target_dev", disk.getTargetDev());
            nestedData.put("target_bus", disk.getTargetBus());

            nestedDatas.add(nestedData);
        }
        vm.put("disks", nestedDatas);

        nestedDatas = new ArrayList<Map<String, Object>>();
        for (VMNetworkInterface iface : vmDomain.getVmNetworkInterfaces()) {
            nestedData = new LinkedHashMap<String, Object>();

            nestedData.put("mac_address", iface.getMacAddress());
            nestedData.put("type", iface.getInterfaceType().getTypeText());
            nestedData.put("source", iface.getSource());
            nestedData.put("model", iface.getModel());
            nestedData.put("addresses", vmDomain.getInterfaceAddresses(iface.getMacAddress()));

            nestedDatas.add(nestedData);
        }
        vm.put("interfaces", nestedDatas);

        response.put("vm", vm);

        return response;
    }

    @Route("/isoimages")
    @JSON
    public Map<String, Object> getIsoImages() {
        Map<String, Object> response = new HashMap<String, Object>();
        List<String> fileNames = new ArrayList<String>();

        if (this.isoImagesPath.isEmpty()) {
            return response; // err
        }

        try {
            Files.walk(Paths.get(this.isoImagesPath)).filter((path) -> {
                return Files.isRegularFile(path);
            }).forEach((path) -> {
                fileNames.add(path.toString());
            });
        } catch (IOException e) {
            return response; // err
        }

        response.put("files", fileNames);

        return response;
    }

    @Route("/isoimages/folders")
    @JSON
    public Map<String, Object> getIsoImageFolders() {
        Map<String, Object> response = new HashMap<String, Object>();
        List<String> folderNames = new ArrayList<String>();

        if (this.isoImagesPath.isEmpty()) {
            return response; // err
        }

        try {
            Files.walk(Paths.get(this.isoImagesPath)).filter((path) -> {
                return Files.isDirectory(path);
            }).forEach((path) -> {
                folderNames.add(path.toString());
            });
        } catch (IOException e) {
            return response; // err
        }

        response.put("folders", folderNames);

        return response;
    }

    @Route("/isoimages/files/*folder")
    @JSON
    public Map<String, Object> getIsoImageFiles(String folder) {
        Map<String, Object> response = new HashMap<String, Object>();
        List<String> fileNames = new ArrayList<String>();

        if (this.isoImagesPath.isEmpty()) {
            return response; // err
        }

        if (!folder.endsWith("/")) {
            folder += "/";
        }

        if (!folder.startsWith(this.isoImagesPath)) {
            return response; // err
        }

        try {
            Files.walk(Paths.get(folder), 1).filter((path) -> {
                return Files.isRegularFile(path);
            }).forEach((path) -> {
                fileNames.add(path.toString());
            });
        } catch (IOException e) {
            return response; // err
        }

        response.put("files", fileNames);

        return response;
    }

    @Route("/vms")
    @Post
    @JSON
    public Map<String, Object> createVm(Map<String, Object> request) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        String vmName = "";
        int vmCpus = 0;
        long vmRam = 0;
        List<VMDisk> vmDisks = new ArrayList<VMDisk>();
        List<VMNetworkInterface> vmNetworkInterfaces = new ArrayList<VMNetworkInterface>();
        VMGraphics vmGraphics;
        VMVideo vmVideo;

        VMDomain domain;

        Object vm = request.get("vm");
        Object tmp;
        if (vm instanceof Map<?, ?>) {
            Map<?, ?> vmMap = (Map<?, ?>) vm;

            tmp = vmMap.get("name");
            if (tmp instanceof String) {
                vmName = (String) tmp;
            }

            tmp = vmMap.get("cpus");
            if (tmp instanceof Integer) {
                vmCpus = (Integer) tmp;
            }

            tmp = vmMap.get("ram");
            if (tmp instanceof Integer) {
                vmRam = ((Integer) tmp).longValue();
            }

            tmp = vmMap.get("disks");
            if (tmp instanceof List<?>) {
                for (Object diskList : (List<?>) tmp) {
                    if (diskList instanceof Map<?, ?>) {
                        Map<?, ?> diskMap = (Map<?, ?>) diskList;
                        Object diskData;
                        String filePath = "";
                        String fileType = "";
                        String diskType = "";
                        String diskDev = "";
                        String diskBus = "";

                        diskData = diskMap.get("file_path");
                        if (diskData instanceof String) {
                            filePath = (String) diskData;
                        }

                        diskData = diskMap.get("device");
                        if (diskData instanceof String) {
                            diskType = (String) diskData;
                            if (diskType.equals("disk")) {
                                fileType = "qcow2";
                            } else if (diskType.equals("cdrom")) {
                                fileType = "raw";
                            } else {
                                fileType = "";
                            }
                        }

                        diskData = diskMap.get("target_dev");
                        if (diskData instanceof String) {
                            diskDev = (String) diskData;
                        }

                        diskData = diskMap.get("target_bus");
                        if (diskData instanceof String) {
                            diskBus = (String) diskData;
                        }

                        try {
                            vmDisks.add(new VMDisk(diskType, "file", "qemu", fileType, filePath, diskDev, diskBus));
                        } catch (FileNotFoundException e) {
                            response.put("error", e.getLocalizedMessage());
                            return response; // err
                        }
                    }
                }
            }

            tmp = vmMap.get("interfaces");
            if (tmp instanceof List<?>) {
                for (Object ifaceList : (List<?>) tmp) {
                    if (ifaceList instanceof Map<?, ?>) {
                        Map<?, ?> ifaceMap = (Map<?, ?>) ifaceList;
                        Object ifaceData;
                        String macAddress = "";
                        String source = "";
                        String model = "";
                        String type = "";

                        ifaceData = ifaceMap.get("mac_address");
                        if (ifaceData instanceof String) {
                            macAddress = (String) ifaceData;
                        }

                        ifaceData = ifaceMap.get("type");
                        if (ifaceData instanceof String) {
                            type = (String) ifaceData;
                        }

                        ifaceData = ifaceMap.get("source");
                        if (ifaceData instanceof String) {
                            source = (String) ifaceData;
                        }

                        ifaceData = ifaceMap.get("model");
                        if (ifaceData instanceof String) {
                            model = (String) ifaceData;
                        }

                        try {
                            vmNetworkInterfaces.add(new VMNetworkInterface(macAddress, source, model, InterfaceType.getTypeByString(type), this.vmm));
                        } catch (InterfaceNotFoundException | TypeNotFoundException e) {
                            response.put("error", e.getLocalizedMessage());
                            return response; // err
                        }
                    }
                }
            }
        }

        vmVideo = new VMVideo(VideoType.VIDEO_VIRTIO);
        vmGraphics = new VMGraphics("vnc", -1);

        try {
            domain = this.vmm.createVm(vmName, vmCpus, vmRam, vmDisks, vmNetworkInterfaces, vmGraphics, vmVideo);
        } catch (DomainCreateException e) {
            response.put("error", e.getLocalizedMessage());
            return response; // err
        }

        response.put("staus", "OK");
        response.put("vm", getVmByUUID(domain.getVmUUID().toString()));
        
        return response;
    }

    @Route("/vms/:uuid/state")
    @JSON
    public Map<String, Object> getVmStateByUUID(String uuid) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        VMDomain vmDomain;

        try {
            vmDomain = this.vmm.getVm(UUID.fromString(uuid));
        } catch (DomainLookupException e) {
            response.put("error", e.getLocalizedMessage());
            return response; // err
        }

        response.put("state", vmDomain.getVmPowerState().getStateText());

        return response;
    }

    @Route("/vms/:uuid/state")
    @Post
    @JSON
    public Map<String, Object> setVmStateByUUID(String uuid, Map<String, Object> request) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();

        Object state = request.get("state");
        String stateString = "";
        String vmName = "";

        if (state instanceof String) {
            stateString = (String) state;
        }

        try {
            vmName = this.vmm.getVm(UUID.fromString(uuid)).getVmName();

            switch (DomainPowerState.getStateByString(stateString)) {
                case POWER_RUNNING:
                    this.vmm.startVm(vmName);
                    break;
                case POWER_SHUTOFF:
                    this.vmm.stopVm(vmName);
                    break;
                default:
                    throw new InvalidPowerStateException();
            }

            response.put("state", stateString);
        } catch (DomainLookupException | DomainStartException | DomainStopException | InvalidPowerStateException e) {
            response.put("error", e.getLocalizedMessage());
            return response; // err
        }

        return response;
    }
}
