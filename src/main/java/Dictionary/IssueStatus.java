package Dictionary;

public enum IssueStatus {
    ALL("ALL"),
    ESCALATION("ESCALATION"),
    IN_VALIDATION("in validation"),
    MORE_INFO_REQUESTED("more info requested"),
    NO_ERROR("no error (CR/WAD)"),
    SOLVED("solved"),
    TEST_PASSED("test passed"),
    TEST_FAILED("test failed"),
    TESTING("testing"),
    REJECTED("rejected"),
    PROCESSING("processing"),
    NOT_REPRODUCIBLE("not reproducible"),
    REPORTED("reported"),
    ACCEPTED("accepted"),
    NOT_OK("not OK"),
    NOT_ACCEPTED("not accepted"),
    RELEASED("released");

    public String name;

    IssueStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
