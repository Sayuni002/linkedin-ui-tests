# LinkedIn UI Test Suite (Selenium + TestNG + POM)

Automated UI tests for LinkedIn: login (valid and invalid), global search, and opening the first search result.
Course: IT3153 Software Quality Assurance - KDU.

## Requirements
- Java 17 or later
- Maven 3.8+
- Google Chrome (default) or Firefox
- Internet access (Selenium Manager downloads the browser driver automatically)

## Configure credentials (use your OWN dedicated test account)
1. Copy `config.properties.example` to `config.properties` (in the project root, next to `pom.xml`).
2. Fill in your test account email and password.
3. `config.properties` is in `.gitignore` and must never be committed.

Alternative: set environment variables `LINKEDIN_EMAIL` and `LINKEDIN_PASSWORD`
(PowerShell: `$env:LINKEDIN_EMAIL="..."; $env:LINKEDIN_PASSWORD="..."`).
Environment variables take priority over the file.

## Run the tests
```
mvn clean test
```
Run the failure-screenshot demo (TC_05, fails on purpose):
```
mvn test -DsuiteXmlFile=testng-demo.xml
```
Use Firefox: set `browser=firefox` in `config.properties`.
Screenshots are saved to `screenshots/` (PASS_ and FAIL_ prefixes).

## CAPTCHA / verification
If LinkedIn shows a CAPTCHA or security checkpoint, the tests do **not** try to bypass it.
They pause (default 180 s, `manual.verification.timeout.seconds`) so you can complete the step manually
in the browser window, then continue. Run the suite sparingly to avoid account restrictions.

## Structure
```
src/test/java
  base/BaseTest.java            browser start/stop, shared login helper
  config/Config.java            reads env variables / config.properties
  pages/LoginPage.java          login page object
  pages/HomePage.java           feed page + global search box
  pages/SearchResultsPage.java  search results
  pages/ResultPage.java         opened profile/company page
  tests/LoginTest.java          TC_01, TC_02
  tests/SearchTest.java         TC_03, TC_04
  tests/FailureDemoTest.java    TC_05 (demo only)
  listeners/TestListener.java   automatic screenshots
testng.xml / testng-demo.xml    suites
```

## Notes
LinkedIn changes its page structure often. If a locator breaks, update it in the matching Page class only.

## Troubleshooting
- **Browser opens but nothing is typed:** the login form was not found. Check `screenshots/page_*.png` and
  `page_*.html` (saved automatically) to see what LinkedIn showed (CAPTCHA, security check, different page).
  Complete any CAPTCHA manually - the test waits for you.
- **`mvn` says no POM:** run it in the folder that contains `pom.xml`.
- **`Missing 'linkedin.email'`:** create `config.properties` next to `pom.xml` (copy `config.properties.example`).
- Run a single test while debugging to avoid account restrictions: `mvn test -Dtest=LoginTest#testValidLogin`
