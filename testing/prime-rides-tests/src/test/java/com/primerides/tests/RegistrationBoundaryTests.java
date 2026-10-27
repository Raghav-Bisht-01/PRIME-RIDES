package com.primerides.tests;

import com.primerides.base.BaseTest;
import com.primerides.pages.RegisterPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class RegistrationBoundaryTests extends BaseTest {
    private RegisterPage openRegistration() {
        driver.get(baseUrl + "/register");
        return new RegisterPage(driver).waitUntilLoaded();
    }

    private void fillValidForm() {
        driver.findElement(By.name("name")).sendKeys("Test Rider");
        driver.findElement(By.name("email")).sendKeys("rider@example.com");
        driver.findElement(By.name("phone")).sendKeys("9876543210");
        driver.findElement(By.name("password")).sendKeys("secret1");
    }

    private boolean formIsValid() {
        return (Boolean) ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return document.querySelector('form.auth-card').checkValidity()");
    }

    @DataProvider(name = "registrationEmails")
    public Object[][] registrationEmails() {
        return new Object[][]{
                {"Email valid: simple address", "alex@example.com", true},
                {"Email valid: first and last name", "first.last@example.co.in", true},
                {"Email valid: plus tag", "user+tag@sample.org", true},
                {"Email valid: underscore", "name_1@domain.com", true},
                {"Email valid: hyphen", "hyphen-name@domain.net", true},
                {"Email valid: digits", "customer123@rentals.in", true},
                {"Email valid: uppercase", "UPPER@EXAMPLE.COM", true},
                {"Email valid: short local part", "x@y.io", true},
                {"Email valid: subdomain", "name@sub.domain.co", true},
                {"Email valid: compound local part", "a-b_c+d@foo.bar", true},
                {"Email valid: long public suffix", "person@example.travel", true},
                {"Email valid: numeric local part", "12345@example.org", true},
                {"Email invalid: empty", "", false},
                {"Email invalid: no at sign", "plainaddress", false},
                {"Email invalid: missing local part", "@example.com", false},
                {"Email invalid: missing domain", "name@", false},
                {"Email invalid: repeated at sign", "name@@example.com", false},
                {"Email invalid: local whitespace", "first last@example.com", false},
                {"Email invalid: domain whitespace", "name@last name.com", false},
                {"Email accepted by browser: leading local dot", ".name@example.com", true},
                {"Email accepted by browser: trailing local dot", "name.@example.com", true},
                {"Email invalid: missing domain label", "name@.com", false},
                {"Email invalid: missing suffix", "name@example.", false},
                {"Email invalid: embedded angle brackets", "<name>@example.com", false},
        };
    }

    @Test(dataProvider = "registrationEmails", description = "Registration email input validity")
    public void emailBoundaryIsEnforced(String caseName, String email, boolean expectedValid) {
        openRegistration();
        fillValidForm();
        WebElement field = driver.findElement(By.name("email"));
        field.clear();
        field.sendKeys(email);
        Assert.assertEquals(formIsValid(), expectedValid, caseName);
    }

    @DataProvider(name = "registrationPhones")
    public Object[][] registrationPhones() {
        return new Object[][]{
                {"Phone valid: standard", "9876543210", true},
                {"Phone valid: all zeroes", "0000000000", true},
                {"Phone valid: repeated digits", "1111111111", true},
                {"Phone valid: alternating digits", "9090909090", true},
                {"Phone valid: starts with one", "1234567890", true},
                {"Phone invalid: empty", "", false},
                {"Phone invalid: one digit", "1", false},
                {"Phone invalid: five digits", "12345", false},
                {"Phone invalid: nine digits", "123456789", false},
                {"Phone invalid: eleven digits", "12345678901", false},
                {"Phone invalid: letters", "abcdefghij", false},
                {"Phone invalid: letters mixed in", "98765abc10", false},
                {"Phone invalid: spaces", "98765 43210", false},
                {"Phone invalid: leading space", " 9876543210", false},
                {"Phone invalid: trailing space", "9876543210 ", false},
                {"Phone invalid: country prefix", "+919876543210", false},
                {"Phone invalid: hyphens", "98765-43210", false},
                {"Phone invalid: parentheses", "(987)6543210", false},
                {"Phone invalid: decimal", "987654321.0", false},
                {"Phone invalid: full width digits", "９８７６５４３２１０", false},
        };
    }

    @Test(dataProvider = "registrationPhones", description = "Registration phone pattern boundary")
    public void phoneBoundaryIsEnforced(String caseName, String phone, boolean expectedValid) {
        openRegistration();
        fillValidForm();
        WebElement field = driver.findElement(By.name("phone"));
        field.clear();
        field.sendKeys(phone);
        Assert.assertEquals(formIsValid(), expectedValid, caseName);
    }

    @DataProvider(name = "registrationPasswords")
    public Object[][] registrationPasswords() {
        return new Object[][]{
                {"Password invalid: empty", "", false},
                {"Password invalid: one character", "a", false},
                {"Password invalid: two characters", "ab", false},
                {"Password invalid: three characters", "abc", false},
                {"Password invalid: four characters", "abcd", false},
                {"Password invalid: five characters", "abcde", false},
                {"Password invalid: five unicode characters", "ééééé", false},
                {"Password valid: six characters", "secret", true},
                {"Password valid: seven characters", "secret1", true},
                {"Password valid: spaces", "      ", true},
                {"Password valid: punctuation", "!@#$%^", true},
                {"Password valid: mixed case", "AbCdEf", true},
                {"Password valid: digits", "123456", true},
                {"Password valid: unicode", "éééééé", true},
                {"Password valid: long value", "a-long-password-value-123456789", true},
        };
    }

    @Test(dataProvider = "registrationPasswords", description = "Registration password minimum length")
    public void passwordMinimumLengthIsEnforced(String caseName, String password, boolean expectedValid) {
        openRegistration();
        fillValidForm();
        WebElement field = driver.findElement(By.name("password"));
        field.clear();
        field.sendKeys(password);
        Assert.assertEquals(formIsValid(), expectedValid, caseName);
    }

    @Test(dataProvider = "requiredRegistrationFields", description = "Each registration field is required")
    public void eachRegistrationFieldIsRequired(String fieldName) {
        openRegistration();
        fillValidForm();
        driver.findElement(By.name(fieldName)).clear();
        Assert.assertFalse(formIsValid(), fieldName + " should be required");
    }

    @DataProvider(name = "requiredRegistrationFields")
    public Object[][] requiredRegistrationFields() {
        return new Object[][]{{"name"}, {"email"}, {"phone"}, {"password"}};
    }

    @Test(description = "Whitespace-only full name is rejected before sending the registration request")
    public void whitespaceOnlyNameIsRejected() {
        openRegistration();
        driver.findElement(By.name("name")).sendKeys("   ");
        driver.findElement(By.name("email")).sendKeys("rider@example.com");
        driver.findElement(By.name("phone")).sendKeys("9876543210");
        driver.findElement(By.name("password")).sendKeys("secret1");
        driver.findElement(By.cssSelector("button.auth-submit")).click();
        Assert.assertEquals(driver.findElement(By.cssSelector(".auth-error")).getText(), "Please enter your full name.");
    }
}
