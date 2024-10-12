package net.asaken1021.vmmanager.util;

import jakarta.xml.bind.JAXBException;
import net.asaken1021.vmmanager.util.video.VideoType;
import net.asaken1021.vmmanager.util.video.xml.VideoXML;
import net.asaken1021.vmmanager.util.xml.DomainXMLParser;
import net.asaken1021.vmmanager.util.xml.XMLType;

public class VMVideo {
    private VideoType videoType;
    private int vram;

    public VMVideo(VideoType videoType) {
        if (videoType.equals(VideoType.VIDEO_VIRTIO)) {
            this.videoType = videoType;
        }
    }

    public VMVideo(VideoType videoType, int vram) {
        if (videoType.equals(VideoType.VIDEO_VGA)) {
            this.videoType = videoType;
            this.vram = vram;
        }
    }

    public VMVideo(String xmlDesc) throws JAXBException {
        VideoXML videoXML = new DomainXMLParser(xmlDesc, XMLType.TYPE_VIDEO).parseVideoXML();

        this.videoType = VideoType.getTypeByString(videoXML.getModel().getType());
        if (this.videoType.equals(VideoType.VIDEO_VGA)) {
            this.vram = videoXML.getModel().getVram();
        }
    }

    public VideoType getType() {
        return this.videoType;
    }

    public void setType(VideoType videoType) {
        this.videoType = videoType;
    }

    public int getVram() {
        if (this.videoType.equals(VideoType.VIDEO_VGA)) {
            return this.vram;
        }

        return 0;
    }

    public void setVram(int vram) {
        if (this.videoType.equals(VideoType.VIDEO_VGA)) {
            this.vram = vram;
        }
    }
}
