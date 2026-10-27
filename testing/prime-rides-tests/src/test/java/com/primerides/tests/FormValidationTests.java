package com.primerides.tests;

import com.primerides.base.BaseTest;
import com.primerides.pages.HomePage;
import com.primerides.pages.LoginPage;
import com.primerides.pages.RegisterPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FormValidationTests extends BaseTest {
    @Test(description = "Login form requires email and password")
    public void loginFieldsAreRequired() {
        new HomePage(driver).open(baseUrl).openLogin();
        Assert.assertTrue(new LoginPage(driver).waitUntilLoaded().formIsInvalid());
    }

    @Test(description = "Registration form requires all account fields")
    public void registrationFieldsAreRequired() {
        new HomePage(driver).open(baseUrl).openRegistration();
        Assert.assertTrue(new RegisterPage(driver).waitUntilLoaded().formIsInvalid());
    }

    @Test(description = "Registration accepts exactly ten digits for a phone number")
    public void phoneNumberMustHaveTenDigits() {
        new HomePage(driver).open(baseUrl).openRegistration();
        RegisterPage register = new RegisterPage(driver).waitUntilLoaded();
        Assert.assertTrue(register.phonePatternRejects("12345"), "A short phone number should be rejected");
        Assert.assertFalse(register.phonePatternRejects("9876543210"), "A ten-digit phone number should be accepted");
    }

    @Test(description = "Registration password must be at least six characters")
    public void passwordMustMeetMinimumLength() {
        new HomePage(driver).open(baseUrl).openRegistration();
        RegisterPage register = new RegisterPage(driver).waitUntilLoaded();
        Assert.assertTrue(register.passwordMinLengthRejects("abc"), "A short password should be rejected");
        Assert.assertFalse(register.passwordMinLengthRejects("secret1"), "A seven-character password should be accepted");
    }
}
