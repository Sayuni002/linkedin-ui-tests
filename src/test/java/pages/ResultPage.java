package pages;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** A profile or company page opened from the search results. */
public class ResultPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    public ResultPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void waitForUrlContaining(String path) {
        wait.until(ExpectedConditions.urlContains(path));
    }

    /**
     * Returns the page heading. LinkedIn profile/company pages use an h1 or an h2 for the name
     * depending on the layout, so try h1, then h2 inside the main area, and finally fall back to the page title.
     */
    public String getHeadingText() {
        String text = visibleText(By.cssSelector("main h1, h1"), 12);
        if (text == null) {
            text = visibleText(By.cssSelector("main h2"), 8);
        }
        if (text == null) {
            text = driver.getTitle();
        }
        System.out.println("[ResultPage] Heading/title: " + text);
        return text == null ? "" : text.trim();
    }

    private String visibleText(By locator, int seconds) {
        try {
            return new WebDriverWait(driver, Duration.ofSeconds(seconds)).until(d -> {
                for (WebElement el : d.findElements(locator)) {
                    try {
                        if (el.isDisplayed() && !el.getText().isBlank()) {
                            return el.getText();
                        }
                    } catch (WebDriverException ignored) {
                        // stale element, keep looking
                    }
                }
                return null;
            });
        } catch (TimeoutException e) {
            return null;
        }
    }
}