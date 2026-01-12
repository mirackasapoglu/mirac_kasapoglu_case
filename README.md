# Insider One QA Assessment (Java + Selenium + TestNG)

## Prerequisites
- Java 17+
- Maven 3.8+
- Chrome browser

## How to Run
1. Navigate to the project folder:
   ```bash
   cd insiderone-qa-assessment
   ```
2. Execute tests:
   ```bash
   mvn clean test
   ```

## Project Structure
- src/test/java/core/
  - DriverFactory.java
  - BaseTest.java
  - Waits.java
- src/test/java/pages/
  - HomePage.java
  - CareersQaPage.java
  - JobsListingPage.java
- src/test/java/tests/
  - QaJobsE2ETest.java

## Notes
- Uses WebDriverManager for automatic ChromeDriver provisioning.
- Explicit waits (WebDriverWait) are used; no implicit waits or Thread.sleep.
- Page Object Model (POM) architecture.
- Tests verify QA job listings and redirection flow to Lever application page.