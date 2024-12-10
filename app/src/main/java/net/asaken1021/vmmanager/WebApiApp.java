package net.asaken1021.vmmanager;

import java.io.File;
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
import flak.Response;
import flak.annotations.Delete;
import flak.annotations.Post;
import flak.annotations.Put;
import flak.annotations.Route;
import flak.jackson.JSON;

import net.asaken1021.vmmanager.util.ConnectException;
import net.asaken1021.vmmanager.util.DomainCreateException;
import net.asaken1021.vmmanager.util.DomainDeleteException;
import net.asaken1021.vmmanager.util.DomainLookupException;
import net.asaken1021.vmmanager.util.DomainNotRunningException;
import net.asaken1021.vmmanager.util.DomainPowerState;
import net.asaken1021.vmmanager.util.DomainStartException;
import net.asaken1021.vmmanager.util.DomainStopException;
import net.asaken1021.vmmanager.util.FileNotFoundException;
import net.asaken1021.vmmanager.util.InterfaceNotFoundException;
import net.asaken1021.vmmanager.util.InvalidPowerStateException;
import net.asaken1021.vmmanager.util.TypeNotFoundException;
import net.asaken1021.vmmanager.util.VMManager;
import net.asaken1021.vmmanager.util.common.vm.VMDisk;
import net.asaken1021.vmmanager.util.common.vm.VMDomain;
import net.asaken1021.vmmanager.util.common.vm.VMGraphics;
import net.asaken1021.vmmanager.util.common.vm.VMNetworkInterface;
import net.asaken1021.vmmanager.util.common.vm.VMRamUnit;
import net.asaken1021.vmmanager.util.common.vm.VMVideo;
import net.asaken1021.vmmanager.util.common.vm.networkinterface.InterfaceType;
import net.asaken1021.vmmanager.util.common.vm.video.VideoType;
import net.asaken1021.vmmanager.util.webapi.BadRequestException;
import net.asaken1021.vmmanager.util.webapi.IsoImagesNotSpecifiedException;

public class WebApiApp {
    private VMManager vmm;
    private String uri;
    private String isoImagesPath;
    private String allowOrigin;

    private App webApp;

    public WebApiApp(String uri, String isoImagesPath, String allowOrigin) {
        this.uri = uri;
        this.isoImagesPath = isoImagesPath;
        this.allowOrigin = allowOrigin;

        try {
            this.vmm = new VMManager(this.uri);
        } catch (ConnectException e) {
            printError(e.getLocalizedMessage());
        }
    }

    public WebApiApp(VMManager vmm, String isoImagesPath, String allowOrigin) {
        this.vmm = vmm;
        this.isoImagesPath = isoImagesPath;
        this.allowOrigin = allowOrigin;
    }

    public void run() {
        try {
            this.webApp = Flak.createHttpApp(8080);
            webApp.scan(new WebApiApp(this.vmm, this.isoImagesPath, this.allowOrigin));
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
    public Map<String, Object> getVms(Response response) {
        Map<String, Object> data = new HashMap<String, Object>();
        List<Map<String, Object>> vms = new ArrayList<Map<String, Object>>();
        Map<String, Object> vm = new LinkedHashMap<String, Object>();

        response = addAccessControlAllowOrigin(response);

        try {
            for (String name : this.vmm.getVmNames()) {
                vm = new HashMap<String, Object>();
                vm.put("uuid", this.vmm.getVm(name).getVmUUID().toString());
                vm.put("name", name);
                vms.add(vm);
            }
            data.put("vms", vms);
        } catch (DomainLookupException e) {
            response.setStatus(500);
            putError(data, e);
        }

        return data;
    }

    @Route("/vms/:uuid")
    @JSON
    public Map<String, Object> getVmByUUID(String uuid, Response response) {
        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> vm = new LinkedHashMap<String, Object>();
        List<Map<String, Object>> nestedDatas = new ArrayList<Map<String, Object>>();
        Map<String, Object> nestedData = new LinkedHashMap<String, Object>();
        VMDomain vmDomain;

        response = addAccessControlAllowOrigin(response);

        try {
            vmDomain = this.vmm.getVm(UUID.fromString(uuid));
        } catch (DomainLookupException e) {
            response.setStatus(404);
            putError(data, e);
            return data;
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

        data.put("vm", vm);

        return data;
    }

    @Route("/isoimages")
    @JSON
    public Map<String, Object> getIsoImages(Response response) {
        Map<String, Object> data = new HashMap<String, Object>();
        List<String> fileNames = new ArrayList<String>();

        response = addAccessControlAllowOrigin(response);

        if (this.isoImagesPath.isEmpty()) {
            response.setStatus(500);
            putError(data, new IsoImagesNotSpecifiedException());
            return data;
        }

        try {
            Files.walk(Paths.get(this.isoImagesPath)).filter((path) -> {
                return Files.isRegularFile(path);
            }).forEach((path) -> {
                fileNames.add(path.toString());
            });
        } catch (IOException e) {
            response.setStatus(500);
            putError(data, e);
            return data;
        }

        data.put("files", fileNames);

        return data;
    }

    @Route("/isoimages/folders")
    @JSON
    public Map<String, Object> getIsoImageFolders(Response response) {
        Map<String, Object> data = new HashMap<String, Object>();
        List<String> folderNames = new ArrayList<String>();

        response = addAccessControlAllowOrigin(response);

        if (this.isoImagesPath.isEmpty()) {
            response.setStatus(500);
            putError(data, new IsoImagesNotSpecifiedException());
            return data;
        }

        try {
            Files.walk(Paths.get(this.isoImagesPath)).filter((path) -> {
                return Files.isDirectory(path);
            }).forEach((path) -> {
                folderNames.add(path.toString());
            });
        } catch (IOException e) {
            response.setStatus(500);
            putError(data, e);
            return data;
        }

        data.put("folders", folderNames);

        return data;
    }

    @Route("/isoimages/files/*folder")
    @JSON
    public Map<String, Object> getIsoImageFiles(String folder, Response response) {
        Map<String, Object> data = new HashMap<String, Object>();
        List<String> fileNames = new ArrayList<String>();

        response = addAccessControlAllowOrigin(response);

        if (this.isoImagesPath.isEmpty()) {
            response.setStatus(500);
            putError(data, new IsoImagesNotSpecifiedException());
            return data;
        }

        if (!folder.endsWith("/")) {
            folder += "/";
        }

        try {
            if (!new File(folder).getCanonicalPath().startsWith(this.isoImagesPath)) {
                response.setStatus(400);
                putError(data, new BadRequestException());
                return data;
            }
            
            Files.walk(Paths.get(folder), 1).filter((path) -> {
                return Files.isRegularFile(path);
            }).forEach((path) -> {
                fileNames.add(path.toString());
            });
        } catch (IOException e) {
            response.setStatus(500);
            putError(data, e);
            return data;
        }

        data.put("files", fileNames);

        return data;
    }

    @Route("/vms")
    @Post
    @JSON
    public Map<String, Object> createVm(Map<String, Object> request, Response response) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();

        response = addAccessControlAllowOrigin(response);
        
        data = createVMIntr(request, "", response);
        
        return data;
    }

    @Route("/vms/:uuid")
    @Put
    @JSON
    public Map<String, Object> modifyVm(Map<String, Object> request, String uuid, Response response) {
        Map<String, Object> data = new HashMap<String, Object>();

        response = addAccessControlAllowOrigin(response);

        data = deleteVMIntr(uuid, response);

        if (response.isStatusSet()) {
            return data;
        }
        
        data = createVMIntr(request, uuid, response);

        return data;
    }

    @Route("/vms/:uuid")
    @Delete
    @JSON
    public Map<String, Object> deleteVm(String uuid, Response response) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();

        response = addAccessControlAllowOrigin(response);

        data = deleteVMIntr(uuid, response);

        return data;
    }

    @Route("/vms/:uuid/state")
    @JSON
    public Map<String, Object> getVmStateByUUID(String uuid, Response response) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        VMDomain vmDomain;

        response = addAccessControlAllowOrigin(response);

        try {
            vmDomain = this.vmm.getVm(UUID.fromString(uuid));
        } catch (DomainLookupException e) {
            response.setStatus(404);
            putError(data, e);
            return data;
        }

        data.put("state", vmDomain.getVmPowerState().getStateText());

        return data;
    }

    @Route("/vms/:uuid/state")
    @Put
    @JSON
    public Map<String, Object> setVmStateByUUID(String uuid, Map<String, Object> request, Response response) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();

        Object state = request.get("state");
        String stateString = "";
        UUID vmUUID;

        response = addAccessControlAllowOrigin(response);

        if (state instanceof String) {
            stateString = (String) state;
        }

        try {
            vmUUID = this.vmm.getVm(UUID.fromString(uuid)).getVmUUID();

            switch (DomainPowerState.getStateByString(stateString)) {
                case POWER_RUNNING:
                    this.vmm.startVm(vmUUID);
                    break;
                case POWER_SHUTOFF:
                    this.vmm.stopVm(vmUUID);
                    break;
                default:
                    throw new InvalidPowerStateException();
            }

            data.put("state", stateString);
        } catch (DomainLookupException e) {
            response.setStatus(404);
            putError(data, e);
        } catch (DomainStartException | DomainStopException e) {
            response.setStatus(500);
            putError(data, e);
        } catch (InvalidPowerStateException e) {
            response.setStatus(400);
            putError(data, e);
        }

        return data;
    }

    @Route("/vms/:uuid/vnc")
    @JSON
    public Map<String, Object> getVMVNC(String uuid, Response response) {
        Map<String, Object> data = new HashMap<String, Object>();
        VMDomain domain;

        response = addAccessControlAllowOrigin(response);

        try {
            domain = this.vmm.getVm(UUID.fromString(uuid));
        } catch (DomainLookupException e) {
            response.setStatus(404);
            putError(data, e);
            return data;
        }

        if (!domain.getVmPowerState().equals(DomainPowerState.POWER_RUNNING)) {
            response.setStatus(400);
            putError(data, new BadRequestException(new DomainNotRunningException()));
            return data;
        }

        data.put("address", domain.getVmGraphics().getAddress());
        data.put("port", domain.getVmGraphics().getPort());

        return data;
    }

    private Map<String, Object> createVMIntr(Map<String, Object> request, String uuid, Response response) {
        Map<String, Object> data = new HashMap<String, Object>();
        String vmName = "";
        int vmCpus = 0;
        long vmRam = 0;
        VMRamUnit ramUnit = VMRamUnit.RAM_MiB;
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

            tmp = vmMap.get("ram_unit");
            if (tmp instanceof String) {
                try {
                    ramUnit = VMRamUnit.getUnitByString((String) tmp);
                } catch (TypeNotFoundException e) {
                    response.setStatus(400);
                    putError(data, e);
                    return data;
                }
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
                            response.setStatus(400);
                            putError(data, e);
                            return data;
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
                            response.setStatus(400);
                            putError(data, e);
                            return data;
                        }
                    }
                }
            }
        }

        vmVideo = new VMVideo(VideoType.VIDEO_VIRTIO);
        vmGraphics = new VMGraphics("vnc", -1);

        try {
            if (uuid.isEmpty()) {
                domain = this.vmm.createVm(vmName, vmCpus, vmRam, ramUnit, vmDisks, vmNetworkInterfaces, vmGraphics, vmVideo);
            } else {
                domain = this.vmm.createVm(UUID.fromString(uuid), vmName, vmCpus, vmRam, ramUnit, vmDisks, vmNetworkInterfaces, vmGraphics, vmVideo);
            }
        } catch (DomainCreateException e) {
            response.setStatus(400);
            putError(data, e);
            return data;
        }

        data.put("status", "OK");
        data.put("vm", getVmByUUID(domain.getVmUUID().toString(), response));

        return data;
    }

    private Map<String, Object> deleteVMIntr(String uuid, Response response) {
        Map<String, Object> data = new HashMap<String, Object>();
        VMDomain vmDomain;

        try {
            vmDomain = this.vmm.getVm(UUID.fromString(uuid));
        } catch (DomainLookupException e) {
            response.setStatus(404);
            putError(data, e);
            return data;
        }

        try {
            this.vmm.deleteVm(vmDomain.getVmUUID());
        } catch (DomainDeleteException e) {
            response.setStatus(400);
            putError(data, e);
            return data;
        }

        data.put("status", "OK");

        return data;
    }

    private Response addAccessControlAllowOrigin(Response response) {
        if (!response.hasResponseHeader("Access-Control-Allow-Origin")) {
            response.addHeader("Access-Control-Allow-Origin", this.allowOrigin);
        }

        return response;
    }

    private void putError(Map<String, Object> data, Exception e) {
        data.put("error", e.getLocalizedMessage());
    }
}
