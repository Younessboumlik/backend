package com.myshop.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.time.Duration;

public class SeleniumConfig {
    
    private static final String SELENIUM_REMOTE_URL = System.getenv("SELENIUM_REMOTE_URL");
    private static final String APP_URL = System.getenv("APP_URL");
    private static final boolean IS_CI = "true".equals(System.getenv("CI"));
    
    public static boolean isCI() {
        return IS_CI;
    }
    
    public static String getAppUrl() {
        return APP_URL != null && !APP_URL.isEmpty() ? APP_URL : "http://localhost:5173";
    }
    
    public static WebDriver createDriver() {
        ChromeOptions options = new ChromeOptions();
        
        if (IS_CI) {
            // Mode CI - Headless avec Selenium Grid
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--remote-allow-origins=*");
            
            try {
                System.out.println("🌐 Using Remote WebDriver: " + SELENIUM_REMOTE_URL);
                WebDriver driver = new RemoteWebDriver(new URL(SELENIUM_REMOTE_URL), options);
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
                return driver;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create Remote WebDriver", e);
            }
        } else {
            // Mode Local - Visible
            options.addArguments("--remote-allow-origins=*");
            System.out.println("💻 Using Local ChromeDriver");
            WebDriver driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            return driver;
        }
    }
    
    public static Duration getWaitTimeout() {
        return IS_CI ? Duration.ofSeconds(30) : Duration.ofSeconds(10);
    }
}