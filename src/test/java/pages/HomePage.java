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
        "//div[contains(@class, 'cookie') or contains(@class, 'consent') or contains(@id, 'cookie') or contains(@id, 'consent')] | " +
        "//div[@role='dialog' and (contains(., 'cookie') or contains(., 'Cookie'))]"
    );
    private final By acceptAllButton = By.xpath(
        "//button[normalize-space()='Accept All' or contains(., 'Accept All')] | " +
        "//a[normalize-space()='Accept All' or contains(., 'Accept All')] | " +
        "//button[contains(@class, 'accept') and (contains(., 'All') or contains(., 'all'))]"
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
            // Check if cookie banner is present (safe check, no exception if not found)
            List<WebElement> banners = driver.findElements(cookieBannerContainer);
            if (banners.isEmpty()) {
                return;
            }

            // Check if banner is visible
            WebElement banner = banners.get(0);
            if (!banner.isDisplayed()) {
                return;
            }

            // Wait for Accept All button to be clickable
            WebElement acceptButton = waits.getWait().until(ExpectedConditions.elementToBeClickable(acceptAllButton));

            // Click with retry handling
            try {
                acceptButton.click();
            } catch (org.openqa.selenium.ElementClickInterceptedException e) {
                // Retry with scroll and click
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior: 'auto', block: 'center'});", acceptButton);
                waits.getWait().until(ExpectedConditions.elementToBeClickable(acceptButton));
                acceptButton.click();
            }

            // Wait for banner to disappear
            waits.getWait().until(ExpectedConditions.invisibilityOf(banner));
        } catch (org.openqa.selenium.NoSuchElementException | org.openqa.selenium.TimeoutException e) {
            // Banner not present or already dismissed - continue silently
        }
    }
}