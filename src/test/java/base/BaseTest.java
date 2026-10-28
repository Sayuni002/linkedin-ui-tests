package base;

import config.Config;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import pages.HomePage;
import pages.LoginPage;

/** Starts a fresh browser before every test and closes it afterwards. */
public class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        String browser = Config.get("browser", "BROWSER", "chrome").toLowerCase();
        if (browser.equals("firefox")) {
            driver = new FirefoxDriver();          // Selenium Manager finds GeckoDriver
            driver.manage().window().maximize();
        } else {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--start-maximized", "--disable-notifications");
            options.setPageLoadStrategy(PageLoadStrategy.EAGER);   // do not wait for every ad/tracker to load
            // Stop Chrome's own password-manager popups from covering the page
            Map<String, Object> prefs = new HashMap<>();
            prefs.put("credentials_enable_service", false);
            prefs.put("profile.password_manager_enabled", false);
            options.setExperimentalOption("prefs", prefs);
            driver = new ChromeDriver(options);    // Selenium Manager finds ChromeDriver
        }
        // Only explicit waits are used (no implicit waits, no Thread.sleep)
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    public WebDriver getDriver() {
        return driver;
    }

    /** Shared helper: logs in with the configured test account and returns the feed page. */
    protected HomePage loginAsValidUser() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.login(Config.required("linkedin.email", "LINKEDIN_EMAIL"),
                        Config.required("linkedin.password", "LINKEDIN_PASSWORD"));
        loginPage.waitForLoginResult();
        return new HomePage(driver);
    }
}
