package com.primerides.tests;

import com.primerides.base.BaseTest;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;

public class VisualAndResponsiveTests extends BaseTest {
    @BeforeMethod
    public void openHomeAtDesktopSize() {
        driver.manage().window().setSize(new Dimension(1440, 1000));
        driver.get(baseUrl);
    }

    @DataProvider(name = "viewports")
    public Object[][] viewports() {
        return new Object[][]{
                {"Small phone 320px", 320, 740},
                {"Phone 375px", 375, 812},
                {"Large phone 430px", 430, 900},
                {"Tablet 768px", 768, 1024},
                {"Laptop 1024px", 1024, 768},
                {"Desktop 1440px", 1440, 1000},
        };
    }

    @Test(dataProvider = "viewports", description = "Page fits supported viewport without horizontal overflow")
    public void layoutFitsViewport(String viewportName, int width, int height) {
        driver.manage().window().setSize(new Dimension(width, height));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long viewportWidth = ((Number) js.executeScript("return window.innerWidth")).longValue();
        long documentWidth = ((Number) js.executeScript("return document.documentElement.scrollWidth")).longValue();
        Assert.assertTrue(documentWidth <= viewportWidth,
                viewportName + " has horizontal overflow: document=" + documentWidth + ", viewport=" + viewportWidth);
    }

    @DataProvider(name = "vehiclePhotos")
    public Object[][] vehiclePhotos() {
        return new Object[][]{
                {"Car photo", "/images/cars/rental-suv.jpg"},
                {"Motorcycle photo", "/images/bikes/rental-motorcycle.jpg"},
                {"Scooter photo", "/images/scooties/rental-scooter.jpg"},
        };
    }

    @Test(dataProvider = "vehiclePhotos", description = "Generated vehicle photograph loads from local assets")
    public void vehiclePhotoLoads(String label, String path) {
        Object raw = ((JavascriptExecutor) driver).executeAsyncScript("""
                const [path, done] = [arguments[0], arguments[arguments.length - 1]];
                const image = new Image();
                image.onload = () => done({width: image.naturalWidth, height: image.naturalHeight});
                image.onerror = () => done({error: 'Image failed to load'});
                image.src = path;
                """, path);
        @SuppressWarnings("unchecked") Map<String, Object> image = (Map<String, Object>) raw;
        Assert.assertNull(image.get("error"), label + " did not load");
        Assert.assertTrue(((Number) image.get("width")).intValue() > 0, label + " has no intrinsic width");
        Assert.assertTrue(((Number) image.get("height")).intValue() > 0, label + " has no intrinsic height");
    }

    @DataProvider(name = "homeSections")
    public Object[][] homeSections() {
        return new Object[][]{{"home"}, {"vehicles"}, {"why-us"}};
    }

    @Test(dataProvider = "homeSections", description = "Home page section is present and non-empty")
    public void homeSectionIsPresent(String sectionId) {
        Object exists = ((JavascriptExecutor) driver).executeScript(
                "const s=document.getElementById(arguments[0]); return !!s && s.getBoundingClientRect().height > 0",
                sectionId);
        Assert.assertEquals(exists, Boolean.TRUE, "Missing or collapsed section: " + sectionId);
    }

    @Test(description = "Decorative hero texture is applied")
    public void heroTextureIsApplied() {
        String image = (String) ((JavascriptExecutor) driver).executeScript(
                "return getComputedStyle(document.querySelector('.hero'), '::before').backgroundImage");
        Assert.assertTrue(image.contains("radial-gradient"), "Expected the subtle dot texture in the hero");
    }

    @Test(description = "Home page exposes the expected primary navigation links")
    public void primaryNavigationHasThreeLinks() {
        long count = ((Number) ((JavascriptExecutor) driver).executeScript(
                "return document.querySelectorAll('.nav-links a').length")).longValue();
        Assert.assertEquals(count, 3L);
    }
}
