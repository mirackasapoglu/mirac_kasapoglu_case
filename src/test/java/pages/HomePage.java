package pages;

import core.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class HomePage {
    public static final String BASE_URL = "https://insiderone.com";

    private final WebDriver driver;

    private final By header = By.cssSelector("header");
    private final By footer = By.cssSelector("footer");
    private final By heroHeading = By.cssSelector("main h1, .hero h1, h1");

    // Cookie banner locators
    private final By cookieBannerContainer = By.xpath(
        "//div[contains(@class, 'cli-bar-container')] | " +
        "//div[contains(@class, 'cookie') or contains(@class, 'consent') or contains(@id, 'cookie') or contains(@id, 'consent')] | " +
        "//div[@role='dialog' and (contains(., 'cookie') or contains(., 'Cookie'))] | " +
        "//*[contains(@class, 'cookie-banner') or contains(@class, 'cookie-consent') or contains(@id, 'cookie-banner')]"
    );
    private final By acceptAllButton = By.xpath(
        "//a[@id='wt-cli-accept-all-btn'] | " +
        "//button[@id='wt-cli-accept-all-btn'] | " +
        "//button[normalize-space()='Accept All' or contains(., 'Accept All')] | " +
        "//a[normalize-space()='Accept All' or contains(., 'Accept All')] | " +
        "//button[contains(@class, 'accept') and (contains(., 'All') or contains(., 'all'))] | " +
        "//button[contains(text(), 'Accept')] | " +
        "//a[contains(text(), 'Accept')]"
    );

    public HomePage(WebDriver driver) {
        this.driver = driver;
    }

    public void open() {
        driver.get(BASE_URL);
    }

    public boolean isHeaderVisible(Waits waits) {
        WebElement el = waits.visible(header);
        return el.isDisplayed();
    }

    public boolean isHeroVisible(Waits waits) {
        WebElement el = waits.visible(heroHeading);
        return el.isDisplayed();
    }

    public boolean isFooterVisible(Waits waits) {
        WebElement el = waits.visible(footer);
        return el.isDisplayed();
    }

    // Accept cookies if banner is present
    public void acceptCookiesIfPresent(Waits waits) {
        try {
            // Use extended wait for banner to appear (cookie banners can take time to load)
            org.openqa.selenium.support.ui.WebDriverWait extendedWait = new org.openqa.selenium.support.ui.WebDriverWait(
                driver, java.time.Duration.ofSeconds(15));
            
            // Wait for cookie banner to appear and be visible
            WebElement banner = null;
            try {
                banner = extendedWait.until(ExpectedConditions.presenceOfElementLocated(cookieBannerContainer));
                // Wait for banner to be visible
                extendedWait.until(ExpectedConditions.visibilityOf(banner));
                if (!banner.isDisplayed()) {
                    return; // Banner not visible
                }
            } catch (org.openqa.selenium.TimeoutException e) {
                return; // No banner found - continue silently
            }
            
            // Wait for Accept All button - try multiple approaches
            WebElement acceptButton = null;
            try {
                // First try: find button by ID (most reliable)
                acceptButton = extendedWait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//a[@id='wt-cli-accept-all-btn'] | //button[@id='wt-cli-accept-all-btn']")));
            } catch (org.openqa.selenium.TimeoutException e) {
                // Second try: find button globally
                try {
                    acceptButton = extendedWait.until(ExpectedConditions.elementToBeClickable(acceptAllButton));
                } catch (org.openqa.selenium.TimeoutException ex) {
                    // Third try: find button inside banner
                    try {
                        acceptButton = extendedWait.until(ExpectedConditions.elementToBeClickable(
                            banner.findElement(By.xpath(".//a[@id='wt-cli-accept-all-btn'] | .//button[@id='wt-cli-accept-all-btn'] | .//button[contains(text(), 'Accept')] | .//a[contains(text(), 'Accept')]"))));
                    } catch (Exception ex2) {
                        return; // Button not found - continue silently
                    }
                }
            }
            
            // Ensure button is visible and clickable
            extendedWait.until(ExpectedConditions.visibilityOf(acceptButton));
            extendedWait.until(ExpectedConditions.elementToBeClickable(acceptButton));
            
            // Click with retry handling - try multiple methods
            boolean clicked = false;
            try {
                // Method 1: Normal click
                acceptButton.click();
                clicked = true;
            } catch (org.openqa.selenium.ElementClickInterceptedException e) {
                try {
                    // Method 2: JavaScript click
                    ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].click();", acceptButton);
                    clicked = true;
                } catch (Exception ex) {
                    // Method 3: Actions click
                    try {
                        org.openqa.selenium.interactions.Actions actions = new org.openqa.selenium.interactions.Actions(driver);
                        actions.moveToElement(acceptButton).click().perform();
                        clicked = true;
                    } catch (Exception ex2) {
                        // All methods failed
                    }
                }
            }
            
            // Wait for banner to disappear if click was successful
            if (clicked) {
                try {
                    extendedWait.until(ExpectedConditions.invisibilityOf(banner));
                } catch (org.openqa.selenium.TimeoutException e) {
                    // Banner might still be visible, but continue
                }
            }
        } catch (org.openqa.selenium.NoSuchElementException | org.openqa.selenium.TimeoutException e) {
            // Banner not present or already dismissed - continue silently
        } catch (Exception e) {
            // Any other exception - continue silently (banner might not be present)
        }
    }
}