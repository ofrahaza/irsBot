import Dictionary.IssueStatus;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.Select;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Controller extends Thread {
    public static Traces traces = new Traces();

    private static WebDriver driver;
    private FirefoxOptions options;
    private int i = 3;
    private static boolean flag = true;
    private static final String BASE_URL = "https://fci.dieboldnixdorf.com/NamosRTS/INTRO.PHP?action=startpage";
    private static final String ISSUE_URL = "https://fci.dieboldnixdorf.com/NamosRTS/INTRO.PHP?action=viewreport&errrefnum=";
    private static List<String> closedIssueList = new ArrayList<>();
    private static List<String> ignoredIssueList = new ArrayList<>();
    private static List<Issue> issueList = new ArrayList<>();

    private String type;
    private String status;
    private String classification;
    private LocalDate beginDate, endDate;

    Controller(String type, LocalDate beginDate, LocalDate endDate, String status, String classification) {
        this.type = type;
        this.beginDate = beginDate;
        this.endDate = endDate;
        this.status = status;
        this.classification = classification;
    }


    public void stopBot() {
        flag = false;
    }

    public void startBot() {
        flag = true;
    }

    public void checkFlag() throws InterruptedException {
        if (!flag) {
            throw new InterruptedException();
        }
    }

    // starting webdriver
    private void startFox() throws InterruptedException {
        checkFlag();
        options = new FirefoxOptions();
        options.setHeadless(true);
        traces.writeInfo("Starting FirefoxDriver");
        traces.writeInfo("Parameters {Issue type = " + type + ", Issue status = " + status + ", Issue classification = " + classification + ", Begin date = " + beginDate + ", End date = " + endDate + "}");
        driver = new FirefoxDriver(options);
        driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
        driver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);
        driver.get(BASE_URL);

    }

    // check login
    private void checkuser() throws InterruptedException {
        traces.writeInfo("Checking user Login");
        boolean logIn = false;
        WebElement element = null;

        try {
            checkFlag();
            element = driver.findElement(By.xpath("/html/body/center/table/tbody/tr[2]/td/a"));
            logIn = false;
            traces.writeError("User Login - FALSE");
        } catch (NoSuchElementException e) {
            logIn = true;
        }

        if (!logIn) {
            traces.writeInfo("Logging...");
            element.click();
            driver.findElement(By.id("username")).sendKeys("*");
            driver.findElement(By.id("password")).sendKeys("*");
            driver.findElement(By.id("login")).submit();
        }

    }

    private void applyFilter() throws InterruptedException {
        traces.writeInfo("Applying filter");
            try {
                checkFlag();
                Iterator<Issue> issueIterator = issueList.iterator();
                while (issueIterator.hasNext()) {
                    Issue issue = issueIterator.next();

                    if ((issue.getDate().isBefore(this.beginDate) || issue.getDate().isAfter(this.endDate)) ||
                        (!this.classification.equals("ALL") && !issue.getClassification().equals(this.classification)) ||
                        (!this.status.equals("ALL") && !issue.getStatus().equals(this.status))) {
                        issueIterator.remove();
                        continue;
                    }
                }
            } catch (Exception e) {
                traces.writeError(e.toString());
            }

        if (issueList.isEmpty() || issueList.size() == 0) {
            traces.writeError("List is empty!");

            stopDriver();
            return;
        }

            traces.writeInfo("Updated list: (count = " + issueList.size() + ")");
            for (Issue issue : issueList) {
                checkFlag();
                traces.writeInfo(issue.getRefID() + ", Date = " + issue.getDate() + ", Status = " + issue.getStatus() + ", Classification = " + issue.getClassification());
            }
    }


    private boolean firstSearch() throws InterruptedException {

        try {
            checkFlag();
            driver.switchTo().defaultContent();
            WebElement iFrameNavigator = driver.findElement(By.xpath("/html/frameset/frameset/frame[1]"));
            driver.switchTo().frame(iFrameNavigator);
            //Choose search Select and find
            Select searchDropDown = new Select(driver.findElement(By.id("report")));
            searchDropDown.selectByIndex(2);
            WebElement goSubmit = driver.findElement(By.xpath("/html/body/table/tbody/tr[2]/td/table/tbody/tr[8]/td[2]/form/input"));
            goSubmit.submit();
            WebElement currentIssue = driver.findElement(By.xpath("/html/body/table/tbody/tr[3]/td/table/tbody/tr[2]/*/a"));

            traces.writeError("Not a list page - false");
            return false;
        } catch (NoSuchElementException e) {
            traces.writeInfo("List page - true");
            return true;
        }
    }

    private void generateIssueArrayList() throws InterruptedException {
        boolean checkEnd = true;
        traces.writeInfo("Loading issue array list, waiting for full page load 1 min...");
        boolean pageLoad = false;

        while (true) {
            checkFlag();
            pageLoad = firstSearch();
            if (pageLoad) {
                break;
            }
        }

        while (true) {
            checkFlag();
            String refId, issueClassification, issueStatus;
            LocalDate issueDate = null;
            DateTimeFormatter datetime1 = DateTimeFormatter.ofPattern("MMM dd yyyy hh:mm a", Locale.ENGLISH);
            DateTimeFormatter datetime2 = DateTimeFormatter.ofPattern("MMM d yyyy hh:mm a", Locale.ENGLISH);


            try {
                driver.switchTo().defaultContent();

                //Navigate to list frame
                WebElement iFrameList = driver.findElement(By.xpath("/html/frameset/frameset/frame[2]"));
                driver.switchTo().frame(iFrameList);

                checkEnd = checkPageEnd(i);
                if (!checkEnd) {
                    break;
                }

                refId = driver.findElement(By.xpath("/html/body/table/tbody/tr[3]/td/table/tbody/tr[" + i + "]/td[2]/a")).getText();

                try {
                    issueDate = LocalDate.parse(driver.findElement(By.xpath("/html/body/table/tbody/tr[3]/td/table/tbody/tr[" + i + "]/td[8]/span")).getText(), datetime1);
                } catch (DateTimeParseException e) {
                    issueDate = LocalDate.parse(driver.findElement(By.xpath("/html/body/table/tbody/tr[3]/td/table/tbody/tr[" + i + "]/td[8]/span")).getText(), datetime2);
                }

                issueClassification = driver.findElement(By.xpath("/html/body/table/tbody/tr[3]/td/table/tbody/tr[" + i + "]/td[5]")).getText();
                issueStatus = driver.findElement(By.xpath("/html/body/table/tbody/tr[3]/td/table/tbody/tr[" + i + "]/td[6]")).getText();
                issueList.add(new Issue(refId, issueDate, issueClassification, issueStatus));
                i++;

            } catch (NoSuchElementException e) {
                traces.writeError("Error, No such element");
                e.printStackTrace();
                i++;
                continue;
            }
        }
        traces.writeInfo("List successfully generated");
    }


    private void validateAndClose() throws InterruptedException {
        if (!issueList.isEmpty() && issueList.size() != 0) {
            for (Issue issue : issueList) {
                checkFlag();
                try {
                    //Validate current issue
                    if (!issue.getRefID().startsWith(type)) {
                        traces.writeInfo("WRONG issue type, ignore");
                       ignoredIssueList.add(issue.getRefID());
                        continue;
                    }
                    driver.get(ISSUE_URL + issue.getRefID());
                    driver.switchTo().defaultContent();
                    //Navigate to list frame
                    WebElement iFrameIssue = driver.findElement(By.xpath("/html/frameset/frameset/frame[2]"));
                    driver.switchTo().frame(iFrameIssue);
                    traces.writeInfo("Validating issue " + issue.getRefID() + ", Date = " + issue.getDate());
                    changeStatus(issue.getRefID());

                } catch (NoSuchElementException e) {
                    traces.writeError("Error, No such element, continue");
                    e.printStackTrace();
                    i++;
                    continue;
                }
            }
            traces.writeInfo("All issue destroyed \\o/");
            showIssues();
            stopDriver();
        }
        else {
            return;
        }
    }

    private void showIssues() {
        if (!ignoredIssueList.isEmpty() && ignoredIssueList.size() != 0) {
            traces.writeInfo("Ignored issues: (count = " + ignoredIssueList.size() + ")");
            for (String s : ignoredIssueList) {
                traces.writeInfo(s);
            }
        } else {
            traces.writeInfo("No ignored issues");
        }

        if (!closedIssueList.isEmpty() && closedIssueList.size() != 0) {
            traces.writeInfo("Closed issues: (count = " + closedIssueList.size() + ")");
            for (String s : closedIssueList) {
                traces.writeInfo(s);
            }
        } else {
            traces.writeInfo("No closed issues");
        }
    }

    private void changeStatus(String s) throws InterruptedException {
        Select followupStatus;
        Select responsible;

        while (true) {
            checkFlag();
            try {
                String currentIssueName = driver.findElement(By.xpath("/html/body/form/table/tbody/tr[1]/td[1]/div")).getText();
                WebElement addNewCommentButton = driver.findElement(By.xpath("//*[@id=\"btnAddLocal\"]"));
                addNewCommentButton.click();

                String statusIssue = driver.findElement(By.xpath("/html/body/form/table/tbody/tr[5]/td[2]")).getText();

                traces.writeInfo("Issue status = " + statusIssue);

                if (statusIssue.equals(IssueStatus.ESCALATION.getName())) {
                    traces.writeInfo("Status issue = ESCALATION, ignore");
                    ignoredIssueList.add(currentIssueName);
                    i++;
                    break;
                }

                followupStatus = new Select(driver.findElement(By.id("newEntry[followupstatus]")));

                if (statusIssue.equals(IssueStatus.REJECTED.getName()) ||
                        statusIssue.equals(IssueStatus.MORE_INFO_REQUESTED.getName()) ||
                        statusIssue.equals(IssueStatus.TEST_PASSED.getName()) ||
                        statusIssue.equals(IssueStatus.TEST_FAILED.getName())) {
                    followupStatus.selectByValue("256");
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    traces.writeInfo("Closing issue!");

                    driver.findElement(By.id("newEntry[version]")).sendKeys("*");
                    driver.findElement(By.id("newEntry[subject]")).sendKeys("Not actual");
                    driver.findElement(By.id("newEntry[descript]")).sendKeys("Closing automatically as out of date");
                    driver.findElement(By.id("btnSubmit")).click();
                    closedIssueList.add(currentIssueName);
                    traces.writeInfo("Done, closed " + s);
                    break;
                } else if (statusIssue.equals(IssueStatus.SOLVED.getName()) || statusIssue.equals(IssueStatus.TESTING.getName())) {
                    followupStatus.selectByValue("64");
                    traces.writeInfo("Changing status of issue");

                } else if (statusIssue.equals(IssueStatus.NO_ERROR.getName()) ||
                        statusIssue.equals(IssueStatus.NOT_REPRODUCIBLE.getName())) {
                    followupStatus.selectByValue("8192");

                    traces.writeInfo("Changing status of issue");

                } else if (statusIssue.equals(IssueStatus.REPORTED.getName()) ||
                        statusIssue.equals(IssueStatus.IN_VALIDATION.getName()) ||
                        statusIssue.equals(IssueStatus.ACCEPTED.getName()) ||
                        statusIssue.equals(IssueStatus.PROCESSING.getName())) {
                    followupStatus.selectByValue("4096");
                    traces.writeInfo("Changing status of issue");
                } else {
                    traces.writeInfo("WRONG status issue, continue");
                    ignoredIssueList.add(currentIssueName);
                    i++;
                    break;
                }

                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                responsible = new Select(driver.findElement(By.id("newEntry[idResponsible]")));
                responsible.selectByValue("2059");
                driver.findElement(By.id("newEntry[version]")).sendKeys("*");
                driver.findElement(By.id("newEntry[subject]")).sendKeys("Closing");
                driver.findElement(By.id("newEntry[descript]")).sendKeys("Closing automatically as out of date");
                driver.findElement(By.xpath("//*[@id=\"btnSubmit\"]")).click();

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } catch (NoSuchElementException e) {
                traces.writeError("Error, No such element");
                e.printStackTrace();
            }
        }
    }

    // Check end of page
    private boolean checkPageEnd(int i) throws InterruptedException {
        try {
            checkFlag();
            driver.findElement(By.xpath("/html/body/table/tbody/tr[3]/td/table/tbody/tr[" + i + "]/td[2]/a"));
            traces.writeInfo("Generating issue list, count = " + (i - 2));
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public void stopDriver() {
        try {
            driver.quit();
            traces.writeError("Driver stopped!");
            Bot.stopButton.setDisable(true);
            Bot.startButton.setDisable(false);
            Bot.progressIndicator.setVisible(false);
            Bot.issueTypeDropDown.setDisable(false);
            Bot.classificationDropDown.setDisable(false);
            Bot.statusDropDown.setDisable(false);
            Bot.startDate.setDisable(false);
            Bot.endDate.setDisable(false);
        } catch (Exception e) {
            traces.writeError("Error while stopping driver!");
        }
    }

    @Override
    public void run() {
        try {
            startFox();
            checkuser();
            generateIssueArrayList();
            applyFilter();
            validateAndClose();
        } catch (InterruptedException e) {
            stopDriver();
            return;
        }
    }
}


