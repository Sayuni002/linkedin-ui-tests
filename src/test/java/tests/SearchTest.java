package tests;

import base.BaseTest;
import config.Config;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.ResultPage;
import pages.SearchResultsPage;

public class SearchTest extends BaseTest {

    private final String searchTerm = Config.get("search.term", "SEARCH_TERM", "software testing");

    @Test(description = "TC_03 - Global search shows a results page")
    public void testSearch() {
        HomePage home = loginAsValidUser();
        SearchResultsPage results = home.search(searchTerm);
        results.waitForResults();

        Assert.assertTrue(driver.getCurrentUrl().contains("/search/results/"),
                "Expected a search results URL but was: " + driver.getCurrentUrl());
        Assert.assertTrue(results.isFirstResultDisplayed(), "At least one result should be visible");
    }

    @Test(description = "TC_04 - Clicking the first result opens the matching page")
    public void testClickFirstResult() {
        HomePage home = loginAsValidUser();
        SearchResultsPage results = home.search(searchTerm);
        results.waitForResults();

        String expectedPath = results.openFirstResult();

        ResultPage resultPage = new ResultPage(driver);
        resultPage.waitForUrlContaining(expectedPath);
        Assert.assertTrue(driver.getCurrentUrl().contains(expectedPath),
                "Expected URL to contain " + expectedPath + " but was " + driver.getCurrentUrl());
        Assert.assertFalse(resultPage.getHeadingText().isBlank(), "Page heading should not be empty");
    }
}
