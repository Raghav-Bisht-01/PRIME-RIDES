package com.primerides.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class RegisterPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public RegisterPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public RegisterPage waitUntilLoaded() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".auth-card h1")));
        return this;
    }

    public String heading() {
        return driver.findElement(By.cssSelector(".auth-card h1")).getText();
    }

    public boolean formIsInvalid() {
        return !(Boolean) ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return document.querySelector('form.auth-card').checkValidity()");
    }

    public boolean phonePatternRejects(String value) {
        WebElement phone = driver.findElement(By.name("phone"));
        phone.clear();
        phone.sendKeys(value);
        return !(Boolean) ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return arguments[0].checkValidity()", phone);
    }

    public boolean passwordMinLengthRejects(String value) {
        WebElement password = driver.findElement(By.name("password"));
        password.clear();
        password.sendKeys(value);
        return !(Boolean) ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return arguments[0].checkValidity()", password);
    }
}
