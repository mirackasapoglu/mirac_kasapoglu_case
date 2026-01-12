package core;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class BaseTest {

    protected WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        driver = DriverFactory.createDriver();
        // No implicit waits; explicit waits are used via Waits helper
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        // Browser'ı açık bırakmak için quit driver çağrısı yorum satırına alındı
        // DriverFactory.quitDriver(driver);
    }
}