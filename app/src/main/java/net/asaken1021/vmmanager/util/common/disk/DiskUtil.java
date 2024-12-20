package net.asaken1021.vmmanager.util.common.disk;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.libvirt.Connect;
import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;

import net.asaken1021.vmmanager.util.DirectoryNotFoundException;
import net.asaken1021.vmmanager.util.DiskCreateException;
import net.asaken1021.vmmanager.util.FileAlreadyExistsException;

public class DiskUtil {
    private Connect conn;
    private String diskPath;

    public DiskUtil(Connect conn) {
        this.conn = conn;
    }

    public void createDisk(String diskPath, int diskSizeByGB)
    throws DiskCreateException, DirectoryNotFoundException, FileAlreadyExistsException, LibvirtException {
        this.diskPath = diskPath;

        Path dirPath = Paths.get(this.diskPath).getParent();
        Path fileName = Paths.get(this.diskPath).getFileName();

        if (!dirPath.toFile().exists()) {
            throw new DirectoryNotFoundException();
        }

        if (checkDiskExists(this.diskPath)) {
            throw new FileAlreadyExistsException();
        }

        if (diskSizeByGB <= 0) {
            throw new DiskCreateException();
        }

        String tempStoragePoolXML = 
            "<pool type='dir'>" +
                "<name>temp</name>" + 
                "<target>" + 
                    "<path>" + dirPath.toString() + "</path>" +
                "</target>" + 
            "</pool>";
        
        StoragePool pool = this.conn.storagePoolCreateXML(tempStoragePoolXML, 0);

        String tempStorageVolXML = 
            "<volume>" + 
                "<name>" + fileName.toString() + "</name>" + 
                "<allocation>0</allocation>" + 
                "<capacity unit='G'>" + diskSizeByGB + "</capacity>" + 
                "<target>" + 
                    "<format type='qcow2'/>" + 
                    "<compat>1.1</compat>" + 
                "</target>" + 
            "</volume>";

        pool.storageVolCreateXML(tempStorageVolXML, 0);

        pool.destroy();
    }

    public boolean checkDiskExists(String diskPath) {
        if (new File(diskPath).exists()) {
            return true;
        } else {
            return false;
        }
    }
}
