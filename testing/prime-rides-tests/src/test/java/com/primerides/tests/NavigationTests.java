package com.primerides.tests;

import com.primerides.base.BaseTest;
import com.primerides.pages.HomePage;
import com.primerides.pages.LoginPage;
import com.primerides.pages.RegisterPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NavigationTests extends BaseTest {
    @Test(description = "Home page displays the Prime Rides hero")
    public void homePageLoads() {
        HomePage home = new HomePage(driver).open(baseUrl);
        Assert.assertTrue(home.heroText().contains("Find your next great ride"));
        Assert.assertEquals(driver.getTitle(), "PRIME-RIDES");
    }

    @Test(description = "Log in button opens the login page")
    public void loginNavigationWorks() {
        new HomePage(driver).open(baseUrl).openLogin();
        LoginPage login = new LoginPage(driver).waitUntilLoaded();
        Assert.assertEquals(login.heading(), "Welcome back!");
        Assert.assertTrue(driver.getCurrentUrl().endsWith("/login"));
    }

    @Test(description = "Sign up button opens the registration page")
    public void registrationNavigationWorks() {
        new HomePage(driver).open(baseUrl).openRegistration();
        RegisterPage register = new RegisterPage(driver).waitUntilLoaded();
        Assert.assertEquals(register.heading(), "Create your account");
        Assert.assertTrue(driver.getCurrentUrl().endsWith("/register"));
    }

    @Test(description = "Login page sign up link opens registration")
    public void loginPageCanNavigateToRegistration() {
        new HomePage(driver).open(baseUrl).openLogin();
        new LoginPage(driver).waitUntilLoaded().openRegistration();
        Assert.assertEquals(new RegisterPage(driver).waitUntilLoaded().heading(), "Create your account");
    }

    @Test(description = "Explore vehicles link navigates to the vehicle section")
    public void exploreVehiclesLinkWorks() {
        new HomePage(driver).open(baseUrl).exploreVehicles();
        Assert.assertTrue(driver.getCurrentUrl().endsWith("#vehicles"));
    }
}
