package pages;

import java.net.URI;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SearchResultsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Result links that point to a person (/in/) or a company (/company/) page.
    private final By resultLinks = By.cssSelector("main a[href*='/in/'], main a[href*='/company/']");

    public SearchResultsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public void waitForResults() {
        wait.until(ExpectedConditions.urlContains("/search/results/"));
        firstResult();   // waits until at least one result is visible
    }

    public boolean isFirstResultDisplayed() {
        return firstResult().isDisplayed();
    }

    private WebElement firstResult() {
        return wait.until(d -> d.findElements(resultLinks).stream()
                .filter(WebElement::isDisplayed)
                .findFirst()
                .orElse(null));
    }

    /** Clicks the first result and returns its URL path (e.g. /in/some-name/) for verification. */
    public String openFirstResult() {
        WebElement first = firstResult();
        String path = URI.create(first.getAttribute("href")).getPath();
        wait.until(ExpectedConditions.elementToBeClickable(first)).click();
        return path;
    }
}
