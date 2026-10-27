package com.primerides.tests;

import com.primerides.base.BaseTest;
import com.primerides.pages.LoginPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class LoginBoundaryTests extends BaseTest {
    private void openLogin() {
        driver.get(baseUrl + "/login");
        new LoginPage(driver).waitUntilLoaded();
    }

    private boolean formIsValid() {
        return (Boolean) ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return document.querySelector('form.auth-card').checkValidity()");
    }

    @DataProvider(name = "loginEmails")
    public Object[][] loginEmails() {
        return new Object[][]{
                {"Login email valid: standard", "alex@example.com", true},
                {"Login email valid: plus tag", "user+tag@example.co.in", true},
                {"Login email valid: uppercase", "RIDER@EXAMPLE.COM", true},
                {"Login email valid: subdomain", "qa@sub.example.org", true},
                {"Login email valid: underscore", "name_1@domain.net", true},
                {"Login email valid: hyphen", "first-last@domain.com", true},
                {"Login email valid: digits", "12345@example.org", true},
                {"Login email valid: short address", "x@y.io", true},
                {"Login email valid: long suffix", "rider@example.travel", true},
                {"Login email valid: compound local", "a-b_c+d@foo.bar", true},
                {"Login email invalid: empty", "", false},
                {"Login email invalid: no at sign", "plainaddress", false},
                {"Login email invalid: missing local", "@example.com", false},
                {"Login email invalid: missing domain", "name@", false},
                {"Login email invalid: repeated at", "name@@example.com", false},
                {"Login email invalid: local space", "first last@example.com", false},
                {"Login email invalid: domain space", "name@last name.com", false},
                {"Login email accepted by browser: leading local dot", ".name@example.com", true},
                {"Login email accepted by browser: trailing local dot", "name.@example.com", true},
                {"Login email invalid: missing domain label", "name@.com", false},
                {"Login email invalid: missing suffix", "name@example.", false},
                {"Login email invalid: angle brackets", "<name>@example.com", false},
        };
    }

    @Test(dataProvider = "loginEmails", description = "Login email input validity")
    public void loginEmailBoundaryIsEnforced(String caseName, String email, boolean expectedValid) {
        openLogin();
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("secret1");
        WebElement field = driver.findElement(By.cssSelector("input[type='email']"));
        field.sendKeys(email);
        Assert.assertEquals(formIsValid(), expectedValid, caseName);
    }

    @DataProvider(name = "loginRequiredFields")
    public Object[][] loginRequiredFields() {
        return new Object[][]{{"email"}, {"password"}};
    }

    @Test(dataProvider = "loginRequiredFields", description = "Login fields are required")
    public void loginRejectsEachEmptyRequiredField(String fieldName) {
        openLogin();
        driver.findElement(By.cssSelector("input[type='email']")).sendKeys("rider@example.com");
        driver.findElement(By.cssSelector("input[type='password']")).sendKeys("secret1");
        driver.findElement(By.cssSelector("email".equals(fieldName)
                ? "input[type='email']" : "input[type='password']")).clear();
        Assert.assertFalse(formIsValid(), fieldName + " should be required");
    }

    @Test(description = "Login password is masked")
    public void loginPasswordIsMasked() {
        openLogin();
        Assert.assertEquals(driver.findElement(By.cssSelector("input[type='password']")).getAttribute("type"), "password");
    }
}
