package pages;

import core.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CareersQaPage {
    public static final String QA_PAGE_URL = "https://insiderone.com/careers/quality-assurance/";

    private final WebDriver driver;

    private final By seeAllQaJobsBtn = By.xpath("//a[normalize-space()='See all QA jobs'] | //button[normalize-space()='See all QA jobs']");

    public CareersQaPage(WebDriver driver) {
        this.driver = driver;
    }

    public void open() {
        driver.get(QA_PAGE_URL);
    }

    public boolean isSeeAllJobsVisible(Waits waits) {
        return waits.visible(seeAllQaJobsBtn).isDisplayed();
    }

    public void clickSeeAllJobs(Waits waits) {
        waits.clickable(seeAllQaJobsBtn).click();
    }

    // Example usage of robust waiting methods
    public void clickSeeAllJobsRobust(Waits waits) {
        // Wait for any loader to disappear first
        waits.waitForInvisibility(By.cssSelector("[class*='loading'], [class*='spinner']"));
        
        // Click with scroll and retry for dynamic rendering issues
        waits.clickWithRetry(seeAllQaJobsBtn, 3);
    }
}