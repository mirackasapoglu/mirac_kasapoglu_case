package pages;

import core.Waits;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JobsListingPage {
    private final WebDriver driver;

    // Native <select> element locator for Location filter
    private final By locationSelect = By.xpath("//select[@id='filter-by-location']");

    private final By departmentFilter = By.xpath("//select[@id='filter-by-department' or @name='filter-by-department'] | //label[normalize-space()='Department']/following::select[1]");
    private final By jobListContainer = By.xpath("//div[contains(@class,'positions') or contains(@class,'jobs-list') or contains(@class,'position-list') or contains(@class,'careers') or contains(@class,'jobs')]");
    private final By jobCards = By.xpath("//div[contains(@class,'position') or contains(@class,'job') or contains(@class,'list-item') or contains(@class,'role')]");

    public JobsListingPage(WebDriver driver) {
        this.driver = driver;
    }

    public void applyLocationFilter(Waits waits, String locationText) {
        waitForPageReady(waits);
        selectLocation(waits, locationText);
    }

    // Wait for page to be ready before interacting with dropdowns
    private void waitForPageReady(Waits waits) {
        // Wait for page to be fully loaded (document.readyState)
        waits.getWait().until((org.openqa.selenium.support.ui.ExpectedCondition<Boolean>) webDriver -> {
            org.openqa.selenium.JavascriptExecutor js = (org.openqa.selenium.JavascriptExecutor) webDriver;
            return "complete".equals(js.executeScript("return document.readyState"));
        });
        
        // Wait for any loaders/overlays to disappear
        waits.waitForInvisibility(By.cssSelector("[class*='loading'], [class*='spinner'], [class*='overlay']"));
        
        // Wait for location select element to be present and visible with extended timeout
        org.openqa.selenium.support.ui.WebDriverWait extendedWait = new org.openqa.selenium.support.ui.WebDriverWait(
            driver, java.time.Duration.ofSeconds(60));
        extendedWait.until(ExpectedConditions.presenceOfElementLocated(locationSelect));
        extendedWait.until(ExpectedConditions.visibilityOfElementLocated(locationSelect));
        extendedWait.until(ExpectedConditions.elementToBeClickable(locationSelect));
    }

    // Select location from native <select> element using Selenium Select class
    public void selectLocation(Waits waits, String locationText) {
        // Wait for select element to be visible
        WebElement selectElement = waits.waitForVisibility(locationSelect);
        
        // Scroll select element into view
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({behavior: 'auto', block: 'center', inline: 'nearest'});", 
            selectElement
        );
        
        // Wait for element to be clickable after scrolling
        waits.getWait().until(ExpectedConditions.elementToBeClickable(selectElement));
        
        // Wait for options to load by checking if the desired option exists
        org.openqa.selenium.support.ui.WebDriverWait extendedWait = new org.openqa.selenium.support.ui.WebDriverWait(
            driver, java.time.Duration.ofSeconds(60));
        extendedWait.until((org.openqa.selenium.support.ui.ExpectedCondition<Boolean>) webDriver -> {
            try {
                org.openqa.selenium.WebElement freshSelect = webDriver.findElement(locationSelect);
                org.openqa.selenium.support.ui.Select tempSelect = new org.openqa.selenium.support.ui.Select(freshSelect);
                java.util.List<org.openqa.selenium.WebElement> options = tempSelect.getOptions();
                if (options == null || options.isEmpty()) {
                    return false;
                }
                // Check if the desired option exists
                for (org.openqa.selenium.WebElement option : options) {
                    String optionText = option.getText().trim();
                    if (optionText.equals(locationText)) {
                        return true;
                    }
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        });
        
        // Use Selenium Select class to select by visible text
        WebElement freshSelectElement = driver.findElement(locationSelect);
        Select select = new Select(freshSelectElement);
        select.selectByVisibleText(locationText);
    }

    public void waitForDepartmentFilterReady(Waits waits) {
        // Wait for department filter to be visible (ensures page is ready)
        waits.getWait().until(ExpectedConditions.presenceOfElementLocated(departmentFilter));
        waits.getWait().until(ExpectedConditions.visibilityOfElementLocated(departmentFilter));
    }

    public void applyDepartmentFilter(Waits waits, String departmentText) {
        WebElement filter = waits.present(departmentFilter);
        applyFilterValue(waits, filter, departmentText);
    }

    private void applyFilterValue(Waits waits, WebElement filter, String valueText) {
        String tag = filter.getTagName().toLowerCase();
        if ("select".equals(tag)) {
            Select select = new Select(filter);
            select.selectByVisibleText(valueText);
        } else {
            // Custom dropdowns: click to open and choose desired option
            waits.clickable(filter).click();
            // Wait for dropdown to open and find the option
            By option = By.xpath(
                "//div[contains(@class,'option') or contains(@class,'menu') or contains(@class,'dropdown')]" +
                "//*[normalize-space()='" + valueText + "' or contains(text(),'" + valueText + "')] | " +
                "//li[normalize-space()='" + valueText + "' or contains(text(),'" + valueText + "')] | " +
                "//button[normalize-space()='" + valueText + "' or contains(text(),'" + valueText + "')] | " +
                "//a[normalize-space()='" + valueText + "' or contains(text(),'" + valueText + "')]"
            );
            WebElement optEl = waits.clickable(option);
            optEl.click();
        }
    }

    public void waitForListRefresh(Waits waits) {
        // Wait for job list container to be visible
        waits.until(ExpectedConditions.visibilityOfElementLocated(jobListContainer));
        
        // Extended wait for job cards to load (60 seconds)
        org.openqa.selenium.support.ui.WebDriverWait extendedWait = new org.openqa.selenium.support.ui.WebDriverWait(
            driver, java.time.Duration.ofSeconds(60));
        
        // Wait for at least one job card to be present
        extendedWait.until(ExpectedConditions.numberOfElementsToBeMoreThan(jobCards, 0));
        
        // Wait for at least one job card to be visible and fully rendered with text content
        extendedWait.until((org.openqa.selenium.support.ui.ExpectedCondition<Boolean>) webDriver -> {
            try {
                java.util.List<org.openqa.selenium.WebElement> cards = webDriver.findElements(jobCards);
                if (cards.isEmpty()) {
                    return false;
                }
                // Check if at least one card has visible text (indicating it's fully loaded)
                for (org.openqa.selenium.WebElement card : cards) {
                    if (card.isDisplayed()) {
                        String cardText = card.getText().trim();
                        if (!cardText.isEmpty() && cardText.length() > 10) { // Ensure meaningful content
                            return true;
                        }
                    }
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        });
    }

    public List<JobCard> getJobCards(Waits waits) {
        List<WebElement> cards = waits.until(ExpectedConditions.presenceOfAllElementsLocatedBy(jobCards));
        List<WebElement> visibleCards = cards.stream().filter(WebElement::isDisplayed).collect(Collectors.toList());
        List<JobCard> result = new ArrayList<>();
        for (WebElement card : visibleCards) {
            result.add(new JobCard(card));
        }
        return result;
    }

    public static class JobCard {
        private final WebElement container;

        private final By titleLocator = By.cssSelector("*[class*='title'], h2, h3, h4");
        private final By departmentLocator = By.cssSelector("*[class*='department'], *[class*='dept']");
        private final By locationLocator = By.cssSelector("*[class*='location'], *[class*='loc']");
        private final By viewRoleBtn = By.xpath(".//a[contains(@class,'btn') and (normalize-space()='View Role' or contains(.,'View Role'))] | .//a[normalize-space()='View Role' or contains(.,'View Role')] | .//button[normalize-space()='View Role' or contains(.,'View Role')]");

        public JobCard(WebElement container) {
            this.container = container;
        }

        public String getTitle() {
            try {
                WebElement titleElement = container.findElement(titleLocator);
                String text = titleElement.getText().trim();
                if (!text.isEmpty()) {
                    return text;
                }
            } catch (NoSuchElementException e) {
                // Continue to fallback
            }
            // Fallback: try to find first heading element
            try {
                WebElement heading = container.findElement(By.cssSelector("h1, h2, h3, h4, h5, h6"));
                String text = heading.getText().trim();
                if (!text.isEmpty()) {
                    return text;
                }
            } catch (NoSuchElementException ex) {
                // Continue to text extraction
            }
            // Final fallback: extract title from container text (usually first line)
            String fullText = container.getText().trim();
            if (!fullText.isEmpty()) {
                // Get first non-empty line as title
                String[] lines = fullText.split("\\r?\\n");
                for (String line : lines) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.equalsIgnoreCase("View Role")) {
                        return line;
                    }
                }
            }
            return "";
        }

        public String getDepartment() {
            try {
                WebElement deptElement = container.findElement(departmentLocator);
                return deptElement.getText().trim();
            } catch (NoSuchElementException e) {
                // Fallback: look for text containing "Quality Assurance" or "QA" or "Department"
                String containerText = container.getText();
                // Try to extract department from container text if possible
                return containerText;
            }
        }

        public String getLocation() {
            try {
                WebElement locElement = container.findElement(locationLocator);
                return locElement.getText().trim();
            } catch (NoSuchElementException e) {
                // Fallback: look for text containing location keywords
                String containerText = container.getText();
                // Try to extract location from container text if possible
                return containerText;
            }
        }

        public WebElement getViewRoleElement() {
            return container.findElement(viewRoleBtn);
        }

        public String getFullText() {
            return container.getText();
        }
    }
}