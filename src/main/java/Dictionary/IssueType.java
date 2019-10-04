package Dictionary;

public enum IssueType {
    WNOG("WNOG"),
    GPN("GPN"),
    GPNA("GPN-A");

    private String name;

    IssueType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
