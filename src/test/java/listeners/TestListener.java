package listeners;

import base.BaseTest;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestListener;
import org.testng.ITestResult;

/** Saves a screenshot into screenshots/ when a test fails (FAIL_) and when it passes (PASS_). */
public class TestListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        capture(result, "FAIL");
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        capture(result, "PASS");
    }

    private void capture(ITestResult result, String prefix) {
        Object instance = result.getInstance();
        if (!(instance instanceof BaseTest base) || base.getDriver() == null) {
            return;
        }
        try {
            File src = ((TakesScreenshot) base.getDriver()).getScreenshotAs(OutputType.FILE);
            Path dir = Path.of("screenshots");
            Files.createDirectories(dir);
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path dest = dir.resolve(prefix + "_" + result.getName() + "_" + time + ".png");
            Files.copy(src.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("[TestListener] Screenshot saved: " + dest.toAbsolutePath());
        } catch (Exception e) {
            System.out.println("[TestListener] Could not take screenshot: " + e.getMessage());
        }
    }
}
