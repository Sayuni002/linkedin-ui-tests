package pages;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** The LinkedIn feed page (shown after login) with the global search box. */
public class HomePage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Accessibility attributes change less often than generated CSS classes.
    private final By searchBox = By.cssSelector("input[placeholder*='Search'], input[aria-label*='Search']");

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(20));
    }

    public boolean isSearchBoxDisplayed() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(searchBox)).isDisplayed();
    }

    public SearchResultsPage search(String term) {
        WebElement box = wait.until(ExpectedConditions.elementToBeClickable(searchBox));
        box.click();
        box.clear();
        box.sendKeys(term, Keys.ENTER);
        return new SearchResultsPage(driver);
    }
}
