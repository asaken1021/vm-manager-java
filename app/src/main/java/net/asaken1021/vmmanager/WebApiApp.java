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
import flak.annotations.Route;
import flak.jackson.JSON;

import net.asaken1021.vmmanager.util.ConnectException;
import net.asaken1021.vmmanager.util.DomainLookupException;
import net.asaken1021.vmmanager.util.VMManager;
import net.asaken1021.vmmanager.util.vm.VMDisk;
import net.asaken1021.vmmanager.util.vm.VMDomain;
import net.asaken1021.vmmanager.util.vm.VMNetworkInterface;
import net.asaken1021.vmmanager.util.vm.VMRamUnit;

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
        Map<String, Object> data = new HashMap<String, Object>();
        List<Map<String, Object>> vms = new ArrayList<Map<String, Object>>();
        Map<String, Object> vm = new LinkedHashMap<String, Object>();

        try {
            for (String name : this.vmm.getVmNames()) {
                vm = new HashMap<String, Object>();
                vm.put("uuid", this.vmm.getVm(name).getVmUUID().toString());
                vm.put("name", name);
                vms.add(vm);
            }
            data.put("vms", vms);
        } catch (DomainLookupException e) {
            data.put("error", e.getLocalizedMessage()); // err
        }

        return data;
    }

    @Route("/vms/:uuid")
    @JSON
    public Map<String, Object> getVmByUUID(String uuid) {
        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> vm = new LinkedHashMap<String, Object>();
        List<Map<String, Object>> nestedDatas = new ArrayList<Map<String, Object>>();
        Map<String, Object> nestedData = new LinkedHashMap<String, Object>();
        VMDomain vmDomain;

        try {
            vmDomain = this.vmm.getVm(UUID.fromString(uuid));
        } catch (DomainLookupException e) {
            data.put("error", e.getLocalizedMessage());
            return data; // err
        }

        vm.put("uuid", vmDomain.getVmUUID().toString());
        vm.put("name", vmDomain.getVmName());
        vm.put("state", vmDomain.getVmStateString());
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
    public Map<String, Object> getIsoImages() {
        Map<String, Object> data = new HashMap<String, Object>();
        List<String> fileNames = new ArrayList<String>();

        if (this.isoImagesPath.isEmpty()) {
            return data; // err
        }

        try {
            Files.walk(Paths.get(this.isoImagesPath)).filter((path) -> {
                return Files.isRegularFile(path);
            }).forEach((path) -> {
                fileNames.add(path.toString());
            });
        } catch (IOException e) {
            return data; // err
        }

        data.put("files", fileNames);

        return data;
    }

    @Route("/isoimages/folders")
    @JSON
    public Map<String, Object> getIsoImageFolders() {
        Map<String, Object> data = new HashMap<String, Object>();
        List<String> folderNames = new ArrayList<String>();

        if (this.isoImagesPath.isEmpty()) {
            return data; // err
        }

        try {
            Files.walk(Paths.get(this.isoImagesPath)).filter((path) -> {
                return Files.isDirectory(path);
            }).forEach((path) -> {
                folderNames.add(path.toString());
            });
        } catch (IOException e) {
            return data; // err
        }

        data.put("folders", folderNames);

        return data;
    }

    @Route("/isoimages/files/*folder")
    @JSON
    public Map<String, Object> getIsoImageFiles(String folder) {
        Map<String, Object> data = new HashMap<String, Object>();
        List<String> fileNames = new ArrayList<String>();

        if (this.isoImagesPath.isEmpty()) {
            return data; // err
        }

        if (!folder.endsWith("/")) {
            folder += "/";
        }

        if (!folder.startsWith(this.isoImagesPath)) {
            return data; // err
        }

        try {
            Files.walk(Paths.get(folder), 1).filter((path) -> {
                return Files.isRegularFile(path);
            }).forEach((path) -> {
                fileNames.add(path.toString());
            });
        } catch (IOException e) {
            return data; // err
        }

        data.put("files", fileNames);

        return data;
    }
}
