package net.asaken1021.vmmanager.util.video;

public enum VideoType {
    VIDEO_VIRTIO("virtio"),
    VIDEO_VGA("vga");

    private String text;

    private VideoType(String text) {
        this.text = text;
    }

    public String getText() {
        return this.text;
    }

    public static VideoType getTypeByString(String type) {
        if (type.equals(VideoType.VIDEO_VIRTIO.getText())) {
            return VideoType.VIDEO_VIRTIO;
        } else if (type.equals(VideoType.VIDEO_VGA.getText())) {
            return VideoType.VIDEO_VGA;
        }

        return null;
    }
}
