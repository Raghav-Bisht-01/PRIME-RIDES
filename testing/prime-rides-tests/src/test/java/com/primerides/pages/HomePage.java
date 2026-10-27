package com.primerides.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class HomePage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public HomePage open(String baseUrl) {
        driver.get(baseUrl);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".hero h1")));
        return this;
    }

    public String heroText() {
        return driver.findElement(By.cssSelector(".hero h1")).getText().replaceAll("\\s+", " ").trim();
    }

    public void openLogin() {
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".nav-actions .login-button"))).click();
    }

    public void openRegistration() {
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".nav-actions .signup-button"))).click();
    }

    public void exploreVehicles() {
        driver.findElement(By.cssSelector("a.primary-button[href='#vehicles']")).click();
    }
}
