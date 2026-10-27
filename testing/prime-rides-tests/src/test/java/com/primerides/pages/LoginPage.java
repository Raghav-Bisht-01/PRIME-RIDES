package com.primerides.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public LoginPage waitUntilLoaded() {
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

    public void openRegistration() {
        driver.findElement(By.cssSelector(".auth-switch a[href='/register']")).click();
    }
}
