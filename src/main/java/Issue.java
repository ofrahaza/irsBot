import Dictionary.IssueType;

import java.time.LocalDate;

public class Issue {
    private String refID;
    private IssueType type;
    private LocalDate date;
    private String classification;
    private String status;

    public Issue(String refID, LocalDate date, String classification, String status) {
        this.refID = refID;
        this.date = date;
        this.classification = classification;
        this.status = status;
    }



    public String getRefID() {
        return refID;
    }

    public void setRefID(String refID) {
        this.refID = refID;
    }

    public IssueType getType() {
        return type;
    }

    public void setType(IssueType type) {
        this.type = type;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }


}
