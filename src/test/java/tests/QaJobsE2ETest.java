package tests;

import core.BaseTest;
import core.Waits;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.CareersQaPage;
import pages.HomePage;
import pages.JobsListingPage;

import java.time.Duration;
import java.util.List;

public class QaJobsE2ETest extends BaseTest {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(15);

    @Test
    public void testQaJobsFlow() {
        Waits waits = new Waits(driver, DEFAULT_TIMEOUT);
        String originalWindowHandle = driver.getWindowHandle();

        // Step 1: Open homepage and verify key elements are visible
        HomePage homePage = new HomePage(driver);
        homePage.open();
        homePage.acceptCookiesIfPresent(waits);
        Assert.assertTrue(homePage.isHeaderVisible(waits), "Homepage header should be visible");
        Assert.assertTrue(homePage.isHeroVisible(waits), "Homepage hero section should be visible");
        Assert.assertTrue(homePage.isFooterVisible(waits), "Homepage footer should be visible");

        // Step 2: Go to QA careers page
        CareersQaPage careersQaPage = new CareersQaPage(driver);
        careersQaPage.open();
        Assert.assertTrue(careersQaPage.isSeeAllJobsVisible(waits), 
            "See all QA jobs button should be visible");

        // Step 3: Click "See all QA jobs"
        careersQaPage.clickSeeAllJobs(waits);

        // Step 4: Apply filters - Wait for Department filter first, then Location
        JobsListingPage jobsListingPage = new JobsListingPage(driver);
        // Accept cookies if banner is present on this page
        jobsListingPage.acceptCookiesIfPresent(waits);
        // Wait for Department filter to be visible first (ensures page is ready)
        jobsListingPage.waitForDepartmentFilterReady(waits);
        // Then apply location filter
        jobsListingPage.applyLocationFilter(waits, "Istanbul, Turkiye");
        // Finally apply department filter
        jobsListingPage.applyDepartmentFilter(waits, "Quality Assurance");

        // Step 5: Wait for list refresh and verify job list is displayed (non-empty)
        jobsListingPage.waitForListRefresh(waits);
        List<JobsListingPage.JobCard> jobCards = jobsListingPage.getJobCards(waits);
        Assert.assertFalse(jobCards.isEmpty(), "Job list should not be empty after applying filters");

        // Step 6: Basic validation - job list is not empty (detailed validation can be added later)

        // Step 7: Click "View Role" on the first job card
        JobsListingPage.JobCard firstJobCard = jobCards.get(0);
        // Wait for View Role button to be clickable (no scroll on this page)
        WebElement viewRoleElement = firstJobCard.getViewRoleElement();
        waits.clickable(viewRoleElement);
        // Try normal click first, fallback to JS click if needed
        try {
            viewRoleElement.click();
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            // Fallback to JavaScript click (without scrolling)
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", viewRoleElement);
        }

        // Step 8: Verify redirect to Lever application form (URL contains "lever")
        // Handle new tab/window if opened - stay on Lever page, don't go back
        int windowCount = driver.getWindowHandles().size();
        if (windowCount > 1) {
            // New tab/window was opened - switch to it and stay there
            waits.until(webDriver -> {
                for (String windowHandle : webDriver.getWindowHandles()) {
                    if (!windowHandle.equals(originalWindowHandle)) {
                        webDriver.switchTo().window(windowHandle);
                        return true;
                    }
                }
                return false;
            });
            // Wait for page to load and verify URL contains "lever"
            waits.urlContains("lever");
            Assert.assertTrue(driver.getCurrentUrl().contains("lever"),
                "URL should contain 'lever' after clicking View Role. Current URL: " + driver.getCurrentUrl());
        } else {
            // Same window navigation - verify URL
            waits.urlContains("lever");
            Assert.assertTrue(driver.getCurrentUrl().contains("lever"),
                "URL should contain 'lever' after clicking View Role. Current URL: " + driver.getCurrentUrl());
        }
        
        // Wait for page to be fully loaded
        waits.getWait().until((org.openqa.selenium.support.ui.ExpectedCondition<Boolean>) webDriver -> {
            org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) webDriver;
            return "complete".equals(js.executeScript("return document.readyState"));
        });
        
        // Scroll down to view more content (smooth scroll)
        ((JavascriptExecutor) driver).executeScript(
            "window.scrollTo({top: document.body.scrollHeight, behavior: 'smooth'});"
        );
        
        // Wait 10 seconds
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
