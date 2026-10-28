package tests;

import base.BaseTest;
import config.Config;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.LoginPage;

public class LoginTest extends BaseTest {

    @Test(description = "TC_01 - Valid login shows the feed page")
    public void testValidLogin() {
        HomePage home = loginAsValidUser();

        Assert.assertTrue(driver.getCurrentUrl().contains("/feed"),
                "Expected the feed URL but was: " + driver.getCurrentUrl());
        Assert.assertTrue(home.isSearchBoxDisplayed(), "Search box should be visible after login");
    }

    @Test(description = "TC_02 - Invalid password shows an error message")
    public void testInvalidPasswordLogin() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.open();
        loginPage.login(Config.required("linkedin.email", "LINKEDIN_EMAIL"),
                        Config.get("linkedin.wrong.password", "LINKEDIN_WRONG_PASSWORD", "WrongPassword@123"));

        Assert.assertTrue(loginPage.isErrorDisplayed(), "An error message should be displayed");
        Assert.assertFalse(driver.getCurrentUrl().contains("/feed"), "User must not reach the feed");
    }
}
