package Dictionary;

public enum IssueClassification {
    ALL("ALL"),
    JUST_A_HINT("Just a hint"),
    MINOR("Minor"),
    MEDIUM("Medium"),
    CRITICAL("Critical");

    private String name;

    IssueClassification(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
