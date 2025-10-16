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
import flak.annotations.Options;
import flak.annotations.Post;
import flak.annotations.Put;
import flak.annotations.Route;
import flak.jackson.JSON;
import jakarta.websocket.server.ServerEndpointConfig;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer;
import org.libvirt.LibvirtException;

import net.asaken1021.vmmanager.util.*;
import net.asaken1021.vmmanager.util.vm.*;
import net.asaken1021.vmmanager.util.webapi.*;

public class WebApiApp {
    private VMManager vmm;
    private String uri;
    private String diskImagesPath;
    private String isoImagesPath;
    private String allowOrigin;

    private App webApp;

    public WebApiApp(String uri, String diskImagesPath, String isoImagesPath, String allowOrigin) {
        this.uri = uri;
        this.diskImagesPath = diskImagesPath;
        this.isoImagesPath = isoImagesPath;
        this.allowOrigin = allowOrigin;

        try {
            this.vmm = new VMManager(this.uri);
        } catch (ConnectException e) {
            printError(e.getLocalizedMessage());
        }
    }

    public WebApiApp(VMManager vmm, String diskImagesPath, String isoImagesPath, String allowOrigin) {
        this.vmm = vmm;
        this.diskImagesPath = diskImagesPath;
        this.isoImagesPath = isoImagesPath;
        this.allowOrigin = allowOrigin;
    }

    public void run() {
        try {
            this.webApp = Flak.createHttpApp(8080);
            webApp.scan(new WebApiApp(this.vmm, this.diskImagesPath, this.isoImagesPath, this.allowOrigin));
            webApp.start();

            Server webSocketServer = new Server();
            ServerConnector connector = new ServerConnector(webSocketServer);
            connector.setPort(8081);
            webSocketServer.addConnector(connector);

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            webSocketServer.setHandler(context);

            JakartaWebSocketServletContainerInitializer.configure(context, (servletContext, webSocketContainer) -> {
                VncProxyConfigurator vncProxyConfigurator = new VncProxyConfigurator(this.uri);
                ServerEndpointConfig config = ServerEndpointConfig.Builder
                    .create(VncProxyEndpoint.class, "/vnc/{vmUuid}")
                    .configurator(vncProxyConfigurator)
                    .build();

                webSocketContainer.addEndpoint(config);
            });

            webSocketServer.start();
            System.out.println("VNC Proxy WebSocket Server started.");
            webSocketServer.join();
        } catch (Exception e) {
            e.printStackTrace();
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

        response = addCrossOriginResponse(response);

        try {
            for (String name : this.vmm.getVmNames()) {
                VMDomain vmDomain = this.vmm.getVm(name);
                vm = new HashMap<String, Object>();
                vm.put("uuid", vmDomain.getVmUUID().toString());
                vm.put("name", vmDomain.getVmName());
                vm.put("vm_folder_path", vmDomain.getVmFolderPath());
                vm.put("state", vmDomain.getVmPowerState().getStateText());
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

        response = addCrossOriginResponse(response);

        try {
            vmDomain = this.vmm.getVm(UUID.fromString(uuid));
        } catch (DomainLookupException e) {
            response.setStatus(404);
            putError(data, e);
            return data;
        }

        vm.put("uuid", vmDomain.getVmUUID().toString());
        vm.put("name", vmDomain.getVmName());
        vm.put("vm_folder_path", vmDomain.getVmFolderPath());
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

        nestedDatas = new ArrayList<Map<String, Object>>();
        for (VMBoot boot : vmDomain.getVmBoots()) {
            nestedData = new LinkedHashMap<String, Object>();

            nestedData.put("dev", boot.getDev());

            nestedDatas.add(nestedData);
        }
        vm.put("boot_order", nestedDatas);

        data.put("vm", vm);

        return data;
    }

    @Route("/isoimages")
    @JSON
    public Map<String, Object> getIsoImages(Response response) {
        Map<String, Object> data = new HashMap<String, Object>();
        List<String> fileNames = new ArrayList<String>();

        response = addCrossOriginResponse(response);

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

        response = addCrossOriginResponse(response);

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

        response = addCrossOriginResponse(response);

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

        response = addCrossOriginResponse(response);
        
        data = createVMIntr(request, "", response);
        
        return data;
    }
    
    @Route("/vms/:uuid")
    @Put
    @JSON
    public Map<String, Object> modifyVm(Map<String, Object> request, String uuid, Response response) {
        Map<String, Object> data = new HashMap<String, Object>();

        response = addCrossOriginResponse(response);

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

        response = addCrossOriginResponse(response);

        data = deleteVMIntr(uuid, response);

        return data;
    }

    @Route("/vms/:uuid/vmfolderpath")
    @Put
    @JSON
    public Map<String, Object> setVmFolderPath(String uuid, Map<String, Object> request, Response response) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();

        Object vmFolderPath = request.get("vm_folder_path");
        String vmFolderPathString = "/";

        response = addCrossOriginResponse(response);

        if (vmFolderPath instanceof String) {
            vmFolderPathString = (String) vmFolderPath;
        }

        try {
            this.vmm.modifyVmFolderPath(UUID.fromString(uuid), vmFolderPathString);
        } catch (LibvirtException e) {
            response.setStatus(400);
            putError(data, e);
            return data;
        }

        return data;
    }

    @Route("/vms/:uuid/state")
    @JSON
    public Map<String, Object> getVmStateByUUID(String uuid, Response response) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        VMDomain vmDomain;

        response = addCrossOriginResponse(response);

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

        response = addCrossOriginResponse(response);

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

        response = addCrossOriginResponse(response);

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
    
    @Route("/vms")
    @Options
    public void vmsOptions(Response response) {
        response = addCrossOriginResponse(response);
    }

    @Route("/vms/:uuid")
    @Options
    public void vmsUUIDOptions(String uuid, Response response) {
        response = addCrossOriginResponse(response);
    }

    @Route("/vms/:uuid/vmfolderpath")
    @Options
    public void vmsUUIDVmFolderPathOptions(String uuid, Response response) {
        response = addCrossOriginResponse(response);
    }

    @Route("/vms/:uuid/state")
    @Options
    public void vmsUUIDStateOptions(String uuid, Response response) {
        response = addCrossOriginResponse(response);
    }

    @Route("/vms/:uuid/vnc")
    @Options
    public void vmsUUIDVncOptions(String uuid, Response response) {
        response = addCrossOriginResponse(response);
    }

    @Route("/isoimages")
    @Options
    public void isoImagesOptions(Response response) {
        response = addCrossOriginResponse(response);
    }

    @Route("/isoimages/folders")
    @Options
    public void isoImagesFoldersOptions(Response response) {
        response = addCrossOriginResponse(response);
    }

    @Route("/isoimages/files/*folder")
    @Options
    public void isoImagesFilesFolderOptions(String folder, Response response) {
        response = addCrossOriginResponse(response);
    }
    
    private Map<String, Object> createVMIntr(Map<String, Object> request, String uuid, Response response) {
        Map<String, Object> data = new HashMap<String, Object>();
        String vmName = "";
        int vmCpus = 0;
        long vmRam = 0;
        VMRamUnit ramUnit = VMRamUnit.RAM_MiB;
        LinkedHashMap<VMDisk, Integer> vmDisks = new LinkedHashMap<VMDisk, Integer>();
        List<VMNetworkInterface> vmNetworkInterfaces = new ArrayList<VMNetworkInterface>();
        VMGraphics vmGraphics;
        VMVideo vmVideo;
        List<VMBoot> vmBoots = new ArrayList<VMBoot>();
        String vmFolderPath = "";

        VMDomain domain;
        
        try {
            Map<?, ?> vmMap = JSONObjectParser.parseMap(request, "vm");
            vmName = JSONObjectParser.parseString(vmMap, "name");
            vmCpus = JSONObjectParser.parseInteger(vmMap, "cpus").intValue();
            vmRam = JSONObjectParser.parseInteger(vmMap, "ram").longValue();
            ramUnit = VMRamUnit.getUnitByString(JSONObjectParser.parseString(vmMap, "ram_unit"));
            vmFolderPath = JSONObjectParser.parseString(vmMap, "vm_folder_path");

            for (Object diskObject : JSONObjectParser.parseList(vmMap, "disks", true)) {
                if (diskObject instanceof Map<?, ?>) {
                    Map<?, ?> diskMap = (Map<?, ?>) diskObject;

                    String filePath;
                    String diskType = JSONObjectParser.parseString(diskMap, "device");
                    String fileType;
                    if (diskType.equals("disk")) {
                        filePath = this.diskImagesPath + File.separatorChar + JSONObjectParser.parseString(diskMap, "file_path");
                        fileType = "qcow2";
                    } else if (diskType.equals("cdrom")) {
                        filePath = JSONObjectParser.parseString(diskMap, "file_path");
                        fileType = "raw";
                    } else {
                        filePath = JSONObjectParser.parseString(diskMap, "file_path");
                        fileType = "";
                    }

                    String diskDev = JSONObjectParser.parseString(diskMap, "target_dev");
                    String diskBus = JSONObjectParser.parseString(diskMap, "target_bus");
                    int diskSize;
                    if (diskMap.containsKey("size")) {
                        diskSize = JSONObjectParser.parseInteger(diskMap, "size").intValue();
                    } else {
                        diskSize = 0;
                    }

                    vmDisks.put(new VMDisk(diskType, "file", "qemu", fileType, filePath, diskDev, diskBus), diskSize);
                }
            }

            for (Object ifaceObject : JSONObjectParser.parseList(vmMap, "interfaces", true)) {
                if (ifaceObject instanceof Map<?, ?>) {
                    Map<?, ?> ifaceMap = (Map<?, ?>) ifaceObject;

                    String macAddress = JSONObjectParser.parseString(ifaceMap, "mac_address");
                    String type = JSONObjectParser.parseString(ifaceMap, "type");
                    String source = JSONObjectParser.parseString(ifaceMap, "source");
                    String model = JSONObjectParser.parseString(ifaceMap, "model");

                    vmNetworkInterfaces.add(new VMNetworkInterface(macAddress, source, model, NetworkInterfaceType.getTypeByString(type), this.vmm));
                }
            }

            for (Object bootObject : JSONObjectParser.parseList(vmMap, "boot_order", true)) {
                if (bootObject instanceof Map<?, ?>) {
                    Map<?, ?> bootMap = (Map<?, ?>) bootObject;

                    String bootDev = JSONObjectParser.parseString(bootMap, "dev");

                    vmBoots.add(new VMBoot(bootDev, true));
                }
            }

            vmVideo = new VMVideo(VideoType.VIDEO_VIRTIO);
            vmGraphics = new VMGraphics("vnc", -1);

            if (uuid.isEmpty()) {
                domain = this.vmm.createVm(vmName, vmCpus, vmRam, ramUnit, vmDisks, vmNetworkInterfaces, vmGraphics, vmVideo, vmBoots, vmFolderPath);
            } else {
                domain = this.vmm.createVm(UUID.fromString(uuid), vmName, vmCpus, vmRam, ramUnit, vmDisks, vmNetworkInterfaces, vmGraphics, vmVideo, vmBoots, vmFolderPath);
            }
        } catch (JSONParseException | TypeNotFoundException | FileNotFoundException | InterfaceNotFoundException | DomainCreateException e) {
            response.setStatus(400);
            putError(data, e);
            return data;
        }

        data.put("status", "OK");
        data.put("vm", getVmByUUID(domain.getVmUUID().toString(), response).get("vm"));

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

    private Response addCrossOriginResponse(Response response) {
        if (!response.hasResponseHeader("Access-Control-Allow-Origin")) {
            response.addHeader("Access-Control-Allow-Origin", this.allowOrigin);
        }
        if (!response.hasResponseHeader("Access-Control-Allow-Methods")) {
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        }
        if (!response.hasResponseHeader("Access-Control-Allow-Headers")) {
            response.addHeader("Access-Control-Allow-Headers", "Content-Type");
        }

        return response;
    }

    private void putError(Map<String, Object> data, Exception e) {
        data.put("error", e.getLocalizedMessage());
    }
}
