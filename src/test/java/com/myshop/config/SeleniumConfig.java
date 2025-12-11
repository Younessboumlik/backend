package com.myshop.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.net.URL;
import java.net.MalformedURLException;
import java.time.Duration;

public class SeleniumConfig {
    
    private static final String SELENIUM_REMOTE_URL = System.getenv("SELENIUM_REMOTE_URL");
    private static final String APP_URL = System.getenv("APP_URL");
    private static final boolean IS_CI = "true".equals(System.getenv("CI"));
    
    /**
     * Vérifie si on est dans un environnement CI
     */
    public static boolean isCI() {
        return IS_CI;
    }
    
    /**
     * Retourne l'URL de l'application
     */
    public static String getAppUrl() {
        if (APP_URL != null && !APP_URL.isEmpty()) {
            return APP_URL;
        }
        return "http://localhost:5173";
    }
    
    /**
     * Crée une instance de WebDriver adaptée à l'environnement
     */
    public static WebDriver createDriver() {
        ChromeOptions options = getChromeOptions();
        
        if (IS_CI && SELENIUM_REMOTE_URL != null) {
            // Environnement CI avec Selenium Grid
            try {
                System.out.println("Using Remote WebDriver at: " + SELENIUM_REMOTE_URL);
                RemoteWebDriver driver = new RemoteWebDriver(new URL(SELENIUM_REMOTE_URL), options);
                driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
                driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
                return driver;
            } catch (MalformedURLException e) {
                throw new RuntimeException("Invalid Selenium Remote URL: " + SELENIUM_REMOTE_URL, e);
            }
        } else {
            // Environnement local
            System.out.println("Using Local ChromeDriver");
            WebDriverManager.chromedriver().setup();
            ChromeDriver driver = new ChromeDriver(options);
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            return driver;
        }
    }
    
    /**
     * Configure les options Chrome pour CI et local
     */
    private static ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        
        if (IS_CI) {
            // Options pour CI (headless obligatoire)
            options.addArguments("--headless=new");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-software-rasterizer");
            options.addArguments("--remote-allow-origins=*");
            System.out.println("Chrome running in HEADLESS mode (CI)");
        } else {
            // Options pour local (mode visible)
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--disable-blink-features=AutomationControlled");
            System.out.println("Chrome running in VISIBLE mode (Local)");
        }
        
        // Options communes
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        
        return options;
    }
    
    /**
     * Retourne le timeout pour les attentes explicites
     */
    public static Duration getWaitTimeout() {
        return IS_CI ? Duration.ofSeconds(30) : Duration.ofSeconds(10);
    }
    
    /**
     * Affiche les informations de configuration
     */
    public static void printConfig() {
        System.out.println("=== Selenium Configuration ===");
        System.out.println("Environment: " + (IS_CI ? "CI/CD" : "Local"));
        System.out.println("App URL: " + getAppUrl());
        System.out.println("Remote URL: " + (SELENIUM_REMOTE_URL != null ? SELENIUM_REMOTE_URL : "Not used"));
        System.out.println("Wait Timeout: " + getWaitTimeout().getSeconds() + "s");
        System.out.println("=============================");
    }
}