package com.primerides.tests;

import com.primerides.base.BaseTest;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class ApiContractTests extends BaseTest {
    @BeforeMethod
    public void openApplicationOrigin() {
        driver.get(baseUrl);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> postJson(String path, Object payload) {
        Object result = ((JavascriptExecutor) driver).executeAsyncScript("""
                const [path, payload, done] = [arguments[0], arguments[1], arguments[arguments.length - 1]];
                fetch(path, {
                  method: 'POST',
                  headers: { 'Content-Type': 'application/json' },
                  body: JSON.stringify(payload)
                }).then(async response => done({status: response.status, body: await response.text()}))
                  .catch(error => done({networkError: String(error)}));
                """, path, payload);
        return (Map<String, Object>) result;
    }

    private Map<String, Object> casePayload(Object... fields) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int i = 0; i < fields.length; i += 2) {
            payload.put((String) fields[i], fields[i + 1]);
        }
        return payload;
    }

    private void assertStatus(Map<String, Object> response, int expected, String label) {
        Assert.assertNotNull(response.get("status"), label + " network failure: " + response.get("networkError"));
        Assert.assertEquals(((Number) response.get("status")).intValue(), expected,
                label + " response body: " + response.get("body"));
    }

    @DataProvider(name = "badBookingRequests")
    public Object[][] badBookingRequests() {
        return new Object[][]{
                {"Booking rejects empty body", casePayload(), 400},
                {"Booking rejects JSON array", java.util.List.of("not", "an", "object"), 400},
                {"Booking requires user id", casePayload("vehicle_id", 1, "start_datetime", "2027-01-01T10:00", "end_datetime", "2027-01-02T10:00"), 400},
                {"Booking requires vehicle id", casePayload("user_id", 1, "start_datetime", "2027-01-01T10:00", "end_datetime", "2027-01-02T10:00"), 400},
                {"Booking requires pickup time", casePayload("user_id", 1, "vehicle_id", 1, "end_datetime", "2027-01-02T10:00"), 400},
                {"Booking requires return time", casePayload("user_id", 1, "vehicle_id", 1, "start_datetime", "2027-01-01T10:00"), 400},
                {"Booking rejects text user id", casePayload("user_id", "not-a-number", "vehicle_id", 1, "start_datetime", "2027-01-01T10:00", "end_datetime", "2027-01-02T10:00"), 400},
                {"Booking rejects zero user id", casePayload("user_id", 0, "vehicle_id", 1, "start_datetime", "2027-01-01T10:00", "end_datetime", "2027-01-02T10:00"), 400},
                {"Booking rejects negative user id", casePayload("user_id", -1, "vehicle_id", 1, "start_datetime", "2027-01-01T10:00", "end_datetime", "2027-01-02T10:00"), 400},
                {"Booking rejects text vehicle id", casePayload("user_id", 1, "vehicle_id", "car", "start_datetime", "2027-01-01T10:00", "end_datetime", "2027-01-02T10:00"), 400},
                {"Booking rejects zero vehicle id", casePayload("user_id", 1, "vehicle_id", 0, "start_datetime", "2027-01-01T10:00", "end_datetime", "2027-01-02T10:00"), 400},
                {"Booking rejects negative vehicle id", casePayload("user_id", 1, "vehicle_id", -9, "start_datetime", "2027-01-01T10:00", "end_datetime", "2027-01-02T10:00"), 400},
                {"Booking rejects unsupported rental type", validBooking("weekly"), 400},
                {"Booking rejects empty rental type", validBooking(""), 400},
                {"Booking rejects null rental type", validBooking(null), 400},
                {"Booking rejects malformed pickup", casePayload("user_id", 1, "vehicle_id", 1, "start_datetime", "tomorrow", "end_datetime", "2027-01-02T10:00"), 400},
                {"Booking rejects malformed return", casePayload("user_id", 1, "vehicle_id", 1, "start_datetime", "2027-01-01T10:00", "end_datetime", "tomorrow"), 400},
                {"Booking rejects numeric pickup", casePayload("user_id", 1, "vehicle_id", 1, "start_datetime", 20270101, "end_datetime", "2027-01-02T10:00"), 400},
                {"Booking rejects null return", casePayload("user_id", 1, "vehicle_id", 1, "start_datetime", "2027-01-01T10:00", "end_datetime", null), 400},
                {"Booking rejects equal times", casePayload("user_id", 1, "vehicle_id", 1, "start_datetime", "2027-01-01T10:00", "end_datetime", "2027-01-01T10:00"), 400},
                {"Booking rejects reversed times", casePayload("user_id", 1, "vehicle_id", 1, "start_datetime", "2027-01-02T10:00", "end_datetime", "2027-01-01T10:00"), 400},
                {"Booking rejects boolean pickup", casePayload("user_id", 1, "vehicle_id", 1, "start_datetime", true, "end_datetime", "2027-01-02T10:00"), 400},
        };
    }

    private Map<String, Object> validBooking(Object type) {
        return casePayload("user_id", 1, "vehicle_id", 1,
                "start_datetime", "2027-01-01T10:00", "end_datetime", "2027-01-02T10:00",
                "booking_type", type);
    }

    @Test(dataProvider = "badBookingRequests", description = "Booking API rejects invalid payloads")
    public void bookingValidationReturnsBadRequest(String caseName, Object payload, int status) {
        assertStatus(postJson("/api/bookings/", payload), status, caseName);
    }

    @DataProvider(name = "badRegistrationRequests")
    public Object[][] badRegistrationRequests() {
        return new Object[][]{
                {"Registration rejects empty body", casePayload()},
                {"Registration rejects JSON array", java.util.List.of("not", "an", "object")},
                {"Registration requires name", casePayload("email", "rider@example.com", "phone", "9876543210", "password", "secret1")},
                {"Registration requires email", casePayload("name", "Rider", "phone", "9876543210", "password", "secret1")},
                {"Registration requires phone", casePayload("name", "Rider", "email", "rider@example.com", "password", "secret1")},
                {"Registration requires password", casePayload("name", "Rider", "email", "rider@example.com", "phone", "9876543210")},
                {"Registration rejects whitespace name", casePayload("name", "   ", "email", "rider@example.com", "phone", "9876543210", "password", "secret1")},
                {"Registration rejects whitespace email", casePayload("name", "Rider", "email", "   ", "phone", "9876543210", "password", "secret1")},
                {"Registration rejects whitespace phone", casePayload("name", "Rider", "email", "rider@example.com", "phone", "   ", "password", "secret1")},
                {"Registration rejects numeric name", casePayload("name", 42, "email", "rider@example.com", "phone", "9876543210", "password", "secret1")},
                {"Registration rejects numeric email", casePayload("name", "Rider", "email", 123, "phone", "9876543210", "password", "secret1")},
                {"Registration rejects numeric phone", casePayload("name", "Rider", "email", "rider@example.com", "phone", 9876543210L, "password", "secret1")},
                {"Registration rejects numeric password", casePayload("name", "Rider", "email", "rider@example.com", "phone", "9876543210", "password", 123456)},
                {"Registration rejects null name", casePayload("name", null, "email", "rider@example.com", "phone", "9876543210", "password", "secret1")},
                {"Registration rejects short password: empty", registration("" )},
                {"Registration rejects short password: one char", registration("a")},
                {"Registration rejects short password: three chars", registration("abc")},
                {"Registration rejects short password: five chars", registration("abcde")},
        };
    }

    private Map<String, Object> registration(String password) {
        return casePayload("name", "Rider", "email", "rider@example.com", "phone", "9876543210", "password", password);
    }

    @Test(dataProvider = "badRegistrationRequests", description = "Registration API rejects invalid payloads")
    public void registrationValidationReturnsBadRequest(String caseName, Object payload) {
        assertStatus(postJson("/api/auth/register", payload), 400, caseName);
    }

    @DataProvider(name = "badLoginRequests")
    public Object[][] badLoginRequests() {
        return new Object[][]{
                {"Login rejects empty body", casePayload(), 400},
                {"Login rejects JSON array", java.util.List.of("no", "credentials"), 400},
                {"Login requires email", casePayload("password", "secret1"), 400},
                {"Login requires password", casePayload("email", "rider@example.com"), 400},
                {"Login rejects whitespace email", casePayload("email", "   ", "password", "secret1"), 400},
                {"Whitespace password fails authentication", casePayload("email", "rider@example.com", "password", "   "), 401},
                {"Login rejects non-text email", casePayload("email", 123, "password", "secret1"), 400},
                {"Login rejects non-text password", casePayload("email", "rider@example.com", "password", 123456), 400},
        };
    }

    @Test(dataProvider = "badLoginRequests", description = "Login API rejects invalid payloads")
    public void loginValidationReturnsExpectedStatus(String caseName, Object payload, int expectedStatus) {
        assertStatus(postJson("/api/auth/login", payload), expectedStatus, caseName);
    }

    @Test(description = "Vehicle listing API returns a JSON vehicle collection")
    public void vehicleListingContractIsValid() {
        Object result = ((JavascriptExecutor) driver).executeAsyncScript("""
                const done = arguments[arguments.length - 1];
                fetch('/api/vehicles/').then(async response => {
                  let body = null;
                  try { body = await response.json(); } catch (_) {}
                  done({status: response.status, body});
                }).catch(error => done({networkError: String(error)}));
                """);
        @SuppressWarnings("unchecked") Map<String, Object> response = (Map<String, Object>) result;
        Assert.assertNotNull(response.get("status"), "Vehicle API network error: " + response.get("networkError"));
        Assert.assertEquals(((Number) response.get("status")).intValue(), 200);
        Assert.assertTrue(response.get("body") instanceof Map, "Vehicle response should be a JSON object");
        Assert.assertTrue(((Map<?, ?>) response.get("body")).get("vehicles") instanceof java.util.List,
                "Vehicle response should contain a vehicles array");
    }
}
