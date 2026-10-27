package com.primerides.tests;

import com.primerides.base.BaseTest;
import com.primerides.pages.HomePage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class VehicleCatalogTests extends BaseTest {
    private List<WebElement> loadAvailableVehicles() {
        new HomePage(driver).open(baseUrl);
        new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> {
            String text = d.findElement(By.id("vehicles")).getText();
            return !text.contains("Loading vehicles...");
        });

        if (!driver.findElements(By.cssSelector("#vehicles [role='alert']")).isEmpty()) {
            Assert.fail("Vehicle catalog API failed: " + driver.findElement(By.cssSelector("#vehicles [role='alert']")).getText());
        }

        List<WebElement> cards = driver.findElements(By.cssSelector(".vehicle-card"));
        if (cards.isEmpty()) {
            throw new SkipException("No available vehicle fixture data is present in the database");
        }
        return cards;
    }

    private String cardType(WebElement card) {
        return card.findElement(By.cssSelector(".vehicle-type")).getText().trim().toLowerCase(Locale.ROOT);
    }

    private void clickBookButton(WebElement card) {
        WebElement button = card.findElement(By.cssSelector(".book-button"));
        clickAfterInstantScroll(button);
    }

    private void clickAfterInstantScroll(WebElement element) {
        ((JavascriptExecutor) driver).executeScript("""
                document.documentElement.style.scrollBehavior = 'auto';
                arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});
                """, element);
        new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(ExpectedConditions.elementToBeClickable(element)).click();
    }

    private int expectedTypeCount(List<WebElement> cards, String type) {
        if ("All vehicles".equals(type)) return cards.size();
        return (int) cards.stream().filter(card -> cardType(card).equals(type.toLowerCase(Locale.ROOT))).count();
    }

    @DataProvider(name = "vehicleTypes")
    public Object[][] vehicleTypes() {
        return new Object[][]{{"All vehicles"}, {"Car"}, {"Bike"}, {"Scooty"}};
    }

    @Test(dataProvider = "vehicleTypes", description = "Vehicle category filter returns only matching cards")
    public void categoryFilterWorks(String selectedType) {
        List<WebElement> allCards = loadAvailableVehicles();
        int expected = expectedTypeCount(allCards, selectedType);
        Select filter = new Select(driver.findElement(By.cssSelector(".search-controls label:nth-child(2) select")));
        filter.selectByVisibleText(selectedType);
        List<WebElement> visibleCards = driver.findElements(By.cssSelector(".vehicle-card"));
        Assert.assertEquals(visibleCards.size(), expected, "Vehicle count mismatch for " + selectedType);
        if (!"All vehicles".equals(selectedType)) {
            for (WebElement card : visibleCards) {
                Assert.assertEquals(cardType(card), selectedType.toLowerCase(Locale.ROOT));
            }
        }
    }

    @DataProvider(name = "locations")
    public Object[][] locations() {
        return new Object[][]{
                {"All locations"}, {"Chandigarh"}, {"Ludhiana"}, {"Gurugram"}, {"Faridabad"},
                {"New Delhi"}, {"Dwarka"}, {"Jaipur"}, {"Udaipur"}, {"Lucknow"}, {"Noida"},
                {"Punjab"}, {"Haryana"}, {"Delhi"}, {"Rajasthan"}, {"Uttar Pradesh"},
        };
    }

    @Test(dataProvider = "locations", description = "Location filter returns matching cards")
    public void locationFilterWorks(String selectedLocation) {
        List<WebElement> allCards = loadAvailableVehicles();
        int expected = "All locations".equals(selectedLocation) ? allCards.size()
                : (int) allCards.stream().filter(card -> locationMatches(card, selectedLocation)).count();
        new Select(driver.findElement(By.cssSelector(".search-controls label:first-child select")))
                .selectByVisibleText(selectedLocation);
        List<WebElement> filtered = driver.findElements(By.cssSelector(".vehicle-card"));
        Assert.assertEquals(filtered.size(), expected, "Vehicle count mismatch for location " + selectedLocation);
        for (WebElement card : filtered) {
            if (!"All locations".equals(selectedLocation)) {
                Assert.assertTrue(locationMatches(card, selectedLocation), "Unexpected location in " + card.getText());
            }
        }
    }

    private boolean locationMatches(WebElement card, String location) {
        String cityAndState = card.findElement(By.cssSelector(".vehicle-details > p:nth-of-type(2)")).getText()
                .replace("📍", "").trim();
        String[] parts = cityAndState.split(",");
        return parts.length == 2 && (parts[0].trim().equalsIgnoreCase(location)
                || parts[1].trim().equalsIgnoreCase(location));
    }

    @Test(description = "Category and location filters combine")
    public void categoryAndLocationFiltersCombine() {
        List<WebElement> cards = loadAvailableVehicles();
        String selectedType = cardType(cards.get(0));
        String[] locationParts = cards.get(0).findElement(By.cssSelector(".vehicle-details > p:nth-of-type(2)")).getText()
                .replace("📍", "").trim().split(",");
        String city = locationParts[0].trim();
        new Select(driver.findElement(By.cssSelector(".search-controls label:nth-child(2) select")))
                .selectByVisibleText(selectedType.substring(0, 1).toUpperCase(Locale.ROOT) + selectedType.substring(1));
        new Select(driver.findElement(By.cssSelector(".search-controls label:first-child select"))).selectByVisibleText(city);
        List<WebElement> filtered = driver.findElements(By.cssSelector(".vehicle-card"));
        Assert.assertFalse(filtered.isEmpty(), "The selected card should remain after filtering by its own type and city");
        for (WebElement card : filtered) {
            Assert.assertEquals(cardType(card), selectedType);
            Assert.assertTrue(locationMatches(card, city));
        }
    }

    @Test(description = "Each vehicle card shows a title, price, and loaded image")
    public void vehicleCardsHaveRequiredContent() {
        List<WebElement> cards = loadAvailableVehicles();
        for (WebElement card : cards) {
            Assert.assertFalse(card.findElement(By.cssSelector("h3")).getText().isBlank(), "Vehicle title is blank");
            Assert.assertTrue(card.findElement(By.cssSelector(".vehicle-footer strong")).getText().startsWith("₹"),
                    "Vehicle price is missing the INR symbol");
            boolean imageLoaded = (Boolean) ((JavascriptExecutor) driver)
                    .executeScript("return arguments[0].complete && arguments[0].naturalWidth > 0", card.findElement(By.cssSelector(".vehicle-image img")));
            Assert.assertTrue(imageLoaded, "Vehicle image did not load for " + card.findElement(By.cssSelector("h3")).getText());
        }
    }

    @Test(description = "View ride opens a booking form and cancel closes it")
    public void vehicleCanOpenAndCloseBookingForm() {
        List<WebElement> cards = loadAvailableVehicles();
        String title = cards.get(0).findElement(By.cssSelector("h3")).getText();
        clickBookButton(cards.get(0));
        Assert.assertTrue(driver.findElement(By.cssSelector(".auth-card h2")).getText().contains(title));
        clickAfterInstantScroll(driver.findElement(By.cssSelector(".auth-card button.login-button")));
        Assert.assertTrue(driver.findElements(By.cssSelector(".auth-card h2")).isEmpty());
    }

    @Test(description = "Booking form requires pickup and return date-time")
    public void bookingDatesAreRequired() {
        List<WebElement> cards = loadAvailableVehicles();
        clickBookButton(cards.get(0));
        List<WebElement> dates = driver.findElements(By.cssSelector(".auth-card input[type='datetime-local']"));
        Assert.assertEquals(dates.size(), 2);
        for (WebElement date : dates) {
            Assert.assertEquals(date.getAttribute("required"), "true");
            Assert.assertFalse(date.getAttribute("min").isBlank(), "Date input should prevent past dates");
        }
    }

    @Test(description = "Logged-out user is redirected to login before a booking is submitted")
    public void bookingRequiresLogin() {
        List<WebElement> cards = loadAvailableVehicles();
        clickBookButton(cards.get(0));
        LocalDateTime pickup = LocalDateTime.now().plusDays(2).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime returned = pickup.plusHours(2);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
        List<WebElement> fields = driver.findElements(By.cssSelector(".auth-card input[type='datetime-local']"));
        setReactInput(fields.get(0), pickup.format(format));
        setReactInput(fields.get(1), returned.format(format));
        clickAfterInstantScroll(driver.findElement(By.cssSelector(".auth-card button[type='submit']")));
        new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> d.getCurrentUrl().endsWith("/login"));
        Assert.assertTrue(driver.getCurrentUrl().endsWith("/login"));
    }

    private void setReactInput(WebElement input, String value) {
        ((JavascriptExecutor) driver).executeScript("""
                const input = arguments[0];
                const setter = Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value').set;
                setter.call(input, arguments[1]);
                input.dispatchEvent(new Event('input', {bubbles: true}));
                input.dispatchEvent(new Event('change', {bubbles: true}));
                """, input, value);
    }
}
