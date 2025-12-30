package dev.themeinerlp.minecraftotel.cloudnet;

public final class CloudNetServiceId {
    private String taskName;
    private String name;
    private String nameSplitter;
    private String uniqueId;
    private Integer taskServiceId;

    public String taskName() {
        return taskName;
    }

    public String name() {
        return name;
    }

    public String nameSplitter() {
        return nameSplitter;
    }

    public String uniqueId() {
        return uniqueId;
    }

    public Integer taskServiceId() {
        return taskServiceId;
    }

    public boolean isEmpty() {
        return taskName == null
                && name == null
                && nameSplitter == null
                && uniqueId == null
                && taskServiceId == null;
    }
}
