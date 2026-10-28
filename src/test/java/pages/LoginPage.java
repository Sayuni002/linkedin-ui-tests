package pages;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import config.Config;

public class LoginPage {

    private static final String LOGIN_URL = "https://www.linkedin.com/login";
    private static final String HOME_URL = "https://www.linkedin.com/";

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Several alternatives because LinkedIn serves different login page variants.
    private final By emailInput = By.cssSelector(
            "#username, input[name='session_key'], #session_key, input[autocomplete='username'], input[type='email']");
    private final By passwordInput = By.cssSelector(
            "#password, input[name='session_password'], #session_password, input[type='password']");
    // Match the button by its text so we never click some other "submit" button on the page.
    private final By signInButton = By.xpath(
            "//button[@data-id='sign-in-form__submit-btn'] "
            + "| //button[@type='submit' and normalize-space(.)='Sign in'] "
            + "| //button[normalize-space(.)='Sign in']");
    // Error element ids/classes differ between the /login page and the home-page sign-in form,
    // so match by id/class/role containing "error" and also by the message text itself.
    private final By errorMessage = By.xpath(
            "//*[(contains(@id, 'error') or contains(@class, 'error') or @role='alert') "
            + "and string-length(normalize-space(.)) > 0] "
            + "| //*[self::p or self::span or self::div]"
            + "[contains(normalize-space(.), 'Wrong email or password') "
            + "or contains(normalize-space(.), 'email or password') "
            + "or contains(normalize-space(.), 'right password') "
            + "or contains(normalize-space(.), 'incorrect password') "
            + "or contains(normalize-space(.), 'wrong password')]");

    // Things that can sit in front of the form.
    private final By cookieAccept = By.cssSelector(
            "button[action-type='ACCEPT'], button[data-tracking-control-name*='accept']");
    private final By signInWithEmail = By.xpath(
            "//button[contains(normalize-space(.), 'Sign in with email')] "
            + "| //a[contains(normalize-space(.), 'Sign in with email')]");
    private final By anotherAccount = By.xpath(
            "//button[contains(., 'another account')] | //a[contains(., 'another account')]");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    /** Opens the login form. Never bypasses CAPTCHAs: if one appears it waits for you to solve it manually. */
    public void open() {
        driver.get(LOGIN_URL);
        if (waitForEmailField(8)) {
            return;
        }
        // LinkedIn may show "Continue with Google / Microsoft / Sign in with email" first.
        dismissObstacles();
        if (waitForEmailField(10)) {
            return;
        }

        driver.get(HOME_URL);
        dismissObstacles();
        if (waitForEmailField(10)) {
            return;
        }

        dumpDiagnostics();
        int seconds = Integer.parseInt(
                Config.get("manual.verification.timeout.seconds", "MANUAL_TIMEOUT", "180"));
        System.out.println("[LoginPage] Login form not found. URL=" + driver.getCurrentUrl()
                + " | Title=" + driver.getTitle());
        System.out.println("[LoginPage] If a CAPTCHA / security check is shown, complete it MANUALLY now ("
                + seconds + "s). Do not close the browser.");
        if (!waitForEmailField(seconds)) {
            throw new IllegalStateException("Login form never appeared. URL=" + driver.getCurrentUrl()
                    + " Title=" + driver.getTitle() + " (see screenshots/ folder for page_*.png/html)");
        }
    }

    public void login(String email, String password) {
        WebElement emailField = waitForVisible(emailInput, 20);
        emailField.clear();
        emailField.sendKeys(email);

        WebElement passwordField = waitForVisible(passwordInput, 20);
        passwordField.clear();
        passwordField.sendKeys(password);

        String urlBefore = driver.getCurrentUrl();

        // 1) Click the "Sign in" button
        WebElement button = null;
        try {
            button = waitForVisible(signInButton, 10);
        } catch (TimeoutException ignored) {
            // handled below by the Enter-key fallback
        }
        if (button != null) {
            try {
                button.click();
            } catch (WebDriverException e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);
            }
        }

        // 2) If nothing happened (no page change, no error, form still there), press Enter once
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5)).until(d ->
                    !d.getCurrentUrl().equals(urlBefore)
                            || anyDisplayed(errorMessage)
                            || !anyDisplayed(passwordInput));
        } catch (TimeoutException e) {
            System.out.println("[LoginPage] Sign in click had no effect - pressing Enter in the password field.");
            passwordField.sendKeys(Keys.ENTER);
        }
    }

    /** Waits for the feed. A security checkpoint is NOT bypassed: the test waits for you to finish it manually. */
    public void waitForLoginResult() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20))
                    .until(d -> d.getCurrentUrl().contains("/feed") || isCheckpoint());
        } catch (TimeoutException ignored) {
            // assertions in the test will report the real state
        }
        if (isCheckpoint()) {
            int seconds = Integer.parseInt(
                    Config.get("manual.verification.timeout.seconds", "MANUAL_TIMEOUT", "180"));
            System.out.println("[LoginPage] Security checkpoint detected. Complete it MANUALLY in the browser ("
                    + seconds + "s).");
            new WebDriverWait(driver, Duration.ofSeconds(seconds)).until(ExpectedConditions.urlContains("/feed"));
        }
    }

    public boolean isErrorDisplayed() {
        try {
            return waitForVisible(errorMessage, 15).isDisplayed();
        } catch (TimeoutException e) {
            dumpDiagnostics();   // save what the page looked like, to inspect the real error element
            return false;
        }
    }

    // ---------------------------------------------------------------- helpers

    /** Returns the first element matching the locator that is actually visible (ignores hidden duplicates). */
    private WebElement waitForVisible(By locator, int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds)).until(d -> {
            for (WebElement el : d.findElements(locator)) {
                try {
                    if (el.isDisplayed() && el.isEnabled()) {
                        return el;
                    }
                } catch (WebDriverException ignored) {
                    // element went stale, keep looking
                }
            }
            return null;
        });
    }

    private boolean anyDisplayed(By locator) {
        try {
            for (WebElement el : driver.findElements(locator)) {
                if (el.isDisplayed()) {
                    return true;
                }
            }
        } catch (WebDriverException ignored) {
            // treat as not displayed
        }
        return false;
    }

    private boolean waitForEmailField(int seconds) {
        try {
            waitForVisible(emailInput, seconds);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    private void dismissObstacles() {
        clickIfPresent(cookieAccept);
        clickIfPresent(anotherAccount);
        clickIfPresent(signInWithEmail);
    }

    private void clickIfPresent(By locator) {
        try {
            for (WebElement el : driver.findElements(locator)) {
                if (el.isDisplayed() && el.isEnabled()) {
                    el.click();
                    return;
                }
            }
        } catch (WebDriverException ignored) {
            // optional element, ignore
        }
    }

    private boolean isCheckpoint() {
        String url = driver.getCurrentUrl();
        return url.contains("/checkpoint") || url.contains("/challenge");
    }

    private void dumpDiagnostics() {
        try {
            Path dir = Path.of("screenshots");
            Files.createDirectories(dir);
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            File png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(png.toPath(), dir.resolve("page_" + time + ".png"), StandardCopyOption.REPLACE_EXISTING);
            Files.writeString(dir.resolve("page_" + time + ".html"), driver.getPageSource());
        } catch (Exception e) {
            System.out.println("[LoginPage] Could not save diagnostics: " + e.getMessage());
        }
    }
}