package tests;

import base.BaseTest;
import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

/**
 * Deliberately fails (TC_05) to demonstrate the automatic screenshot on failure.
 * Run only with:  mvn test -DsuiteXmlFile=testng-demo.xml
 */
public class FailureDemoTest extends BaseTest {

    @Test(description = "TC_05 - Wrong locator triggers an automatic failure screenshot")
    public void testFailureScreenshotDemo() {
        driver.get("https://www.linkedin.com/login");
        new WebDriverWait(driver, Duration.ofSeconds(3))
                .until(ExpectedConditions.visibilityOfElementLocated(By.id("this-element-does-not-exist")));
    }
}
