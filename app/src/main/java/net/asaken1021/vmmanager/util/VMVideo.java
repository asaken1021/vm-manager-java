package net.asaken1021.vmmanager.util;

import jakarta.xml.bind.JAXBException;
import net.asaken1021.vmmanager.util.vm.video.VideoType;
import net.asaken1021.vmmanager.util.vm.video.VideoXML;
import net.asaken1021.vmmanager.util.xml.DomainXMLParser;
import net.asaken1021.vmmanager.util.xml.XMLType;

public class VMVideo {
    private VideoType videoType;
    private int vram;

    public VMVideo(VideoType videoType) {
        this.videoType = videoType;
        this.vram = 16384;
    }

    public VMVideo(VideoType videoType, int vram) {
        this.videoType = videoType;
        this.vram = vram;
    }

    public VMVideo(String xmlDesc) throws JAXBException {
        VideoXML videoXML = new DomainXMLParser(xmlDesc, XMLType.TYPE_VIDEO).parseVideoXML();

        this.videoType = VideoType.getTypeByString(videoXML.getModel().getType());
        this.vram = videoXML.getModel().getVram();
    }

    public VideoType getType() {
        return this.videoType;
    }

    public void setType(VideoType videoType) {
        this.videoType = videoType;
    }

    public int getVram() {
        return this.vram;
    }

    public void setVram(int vram) {
        this.vram = vram;
    }
}
