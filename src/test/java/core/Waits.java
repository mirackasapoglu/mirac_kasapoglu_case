package core;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class Waits {
    private final WebDriver driver;
    private final Duration timeout;

    public Waits(WebDriver driver, Duration timeout) {
        this.driver = driver;
        this.timeout = timeout;
    }

    public WebDriverWait getWait() {
        return new WebDriverWait(driver, timeout);
    }

    // Existing methods for backward compatibility
    public WebElement visible(By locator) {
        return getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public List<WebElement> allVisible(By locator) {
        return getWait().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    public WebElement present(By locator) {
        return getWait().until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public WebElement clickable(By locator) {
        return getWait().until(ExpectedConditions.elementToBeClickable(locator));
    }

    public WebElement clickable(WebElement element) {
        return getWait().until(ExpectedConditions.elementToBeClickable(element));
    }

    public boolean urlContains(String fraction) {
        return getWait().until(ExpectedConditions.urlContains(fraction));
    }

    public <T> T until(ExpectedCondition<T> condition) {
        return getWait().until(condition);
    }

    // New robust synchronization methods

    /**
     * Wait for element to become visible
     */
    public WebElement waitForVisibility(By locator) {
        return getWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /**
     * Wait for element to become clickable
     */
    public WebElement waitForClickability(By locator) {
        return getWait().until(ExpectedConditions.elementToBeClickable(locator));
    }

    /**
     * Scroll element into view and click
     */
    public void clickWithScroll(By locator) {
        WebElement element = waitForClickability(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
        getWait().until(ExpectedConditions.elementToBeClickable(element));
        element.click();
    }

    /**
     * Click with retry handling StaleElementReferenceException and ElementClickInterceptedException
     */
    public void clickWithRetry(By locator, int retryCount) {
        int attempts = 0;
        while (attempts < retryCount) {
            try {
                WebElement element = waitForClickability(locator);
                scrollIntoView(element);
                element.click();
                return;
            } catch (StaleElementReferenceException e) {
                attempts++;
                if (attempts >= retryCount) {
                    throw new RuntimeException("Failed to click element after " + retryCount + " attempts due to stale element", e);
                }
            } catch (ElementClickInterceptedException e) {
                attempts++;
                if (attempts >= retryCount) {
                    throw new RuntimeException("Failed to click element after " + retryCount + " attempts due to element being intercepted", e);
                }
            }
        }
    }

    /**
     * Wait for element to become invisible (useful for overlays/loaders)
     */
    public boolean waitForInvisibility(By locator) {
        return getWait().until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    /**
     * Click using JavaScript executor as fallback
     */
    public void jsClick(By locator) {
        WebElement element = waitForVisibility(locator);
        scrollIntoView(element);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    /**
     * Click using Selenium Actions API
     */
    public void actionClick(By locator) {
        WebElement element = waitForClickability(locator);
        scrollIntoView(element);
        Actions actions = new Actions(driver);
        actions.moveToElement(element).click().perform();
    }

    /**
     * Wait for element count to be more than specified count
     */
    public boolean waitForElementCountMoreThan(By locator, int count) {
        return getWait().until((ExpectedCondition<Boolean>) webDriver -> 
            webDriver.findElements(locator).size() > count);
    }

    /**
     * Scroll element into view using JavaScript
     */
    private void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'auto', block: 'center', inline: 'nearest'});", element);
    }
}
