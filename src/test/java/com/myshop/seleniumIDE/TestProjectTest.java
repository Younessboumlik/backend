package com.myshop.seleniumIDE;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.interactions.Actions;
import com.myshop.config.SeleniumConfig;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestProjectTest {
    
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static JavascriptExecutor js;
    private Map<String, Object> vars;
    
    @BeforeAll
    static void setUpAll() {
        SeleniumConfig.printConfig();
        driver = SeleniumConfig.createDriver();
        wait = new WebDriverWait(driver, SeleniumConfig.getWaitTimeout());
        js = (JavascriptExecutor) driver;
    }
    
    @BeforeEach
    void setUp() {
        vars = new HashMap<>();
    }
    
    @AfterEach
    void tearDownEach(TestInfo testInfo) {
        if (testInfo.getTags().contains("screenshot") || 
            testInfo.getTestMethod().map(m -> m.isAnnotationPresent(Tag.class)).orElse(false)) {
            takeScreenshot(testInfo.getDisplayName());
        }
    }
    
    @AfterAll
    static void tearDownAll() {
        if (driver != null) {
            driver.quit();
        }
    }
    
    @Test
    @Order(1)
    @Tag("screenshot")
    @DisplayName("Test complet du parcours e-commerce")
    void testCompleteFlow() {
        try {
            // 1. Accéder à la page d'accueil
            System.out.println("Step 1: Navigating to home page...");
            driver.get(SeleniumConfig.getAppUrl());
            driver.manage().window().setSize(new Dimension(760, 720));
            
            // 2. Cliquer sur le deuxième produit
            System.out.println("Step 2: Selecting product...");
            WebElement productCard = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".card:nth-child(2) > .btn")
            ));
            productCard.click();
            
            // 3. Scroll vers le haut
            js.executeScript("window.scrollTo(0,0)");
            Thread.sleep(500);
            
            // 4. Entrer la quantité
            System.out.println("Step 3: Entering quantity...");
            WebElement quantityInput = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("input:nth-child(1)")
            ));
            quantityInput.clear();
            quantityInput.sendKeys("2");
            
            // 5. Ajouter au panier
            System.out.println("Step 4: Adding to cart...");
            WebElement addToCartBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".btn-primary")
            ));
            addToCartBtn.click();
            Thread.sleep(1000);
            
            // 6. Sélectionner un joueur dans le dropdown
            System.out.println("Step 5: Selecting player...");
            WebElement dropdown = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("div:nth-child(5) > select")
            ));
            dropdown.click();
            
            WebElement option = dropdown.findElement(By.xpath("//option[contains(text(), 'Youness Boumlik')]"));
            option.click();
            
            // 7. Confirmer l'ajout
            WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".btn-primary")
            ));
            confirmBtn.click();
            Thread.sleep(1000);
            
            // 8. Aller au panier
            System.out.println("Step 6: Going to cart...");
            WebElement cartIcon = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("span:nth-child(1)")
            ));
            cartIcon.click();
            
            // 9. Aller au checkout
            System.out.println("Step 7: Going to checkout...");
            WebElement checkoutLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("Checkout")
            ));
            checkoutLink.click();
            Thread.sleep(1000);
            
            // 10. Remplir le formulaire de livraison
            System.out.println("Step 8: Filling shipping form...");
            fillShippingForm();
            
            // 11. Sélectionner le mode de paiement
            System.out.println("Step 9: Selecting payment method...");
            selectPaymentMethod();
            
            // 12. Confirmer la commande
            System.out.println("Step 10: Confirming order...");
            WebElement confirmOrderBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector(".btn")
            ));
            confirmOrderBtn.click();
            Thread.sleep(2000);
            
            // 13. Aller aux commandes
            System.out.println("Step 11: Going to orders...");
            WebElement ordersLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.linkText("Mes commandes")
            ));
            ordersLink.click();
            Thread.sleep(1000);
            
            // 14. Vérifier que la commande existe
            System.out.println("Step 12: Verifying order...");
            WebElement orderBtn = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".btn")
            ));
            assertNotNull(orderBtn, "Order button should be present");
            
            System.out.println("✅ Test completed successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
            takeScreenshot("error");
            fail("Test failed: " + e.getMessage());
        }
    }
    
    private void fillShippingForm() throws InterruptedException {
        // Nom
        WebElement nameInput = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("input:nth-child(1)")
        ));
        nameInput.clear();
        nameInput.sendKeys("youness");
        
        // Adresse
        WebElement addressInput = driver.findElement(By.cssSelector("input:nth-child(2)"));
        addressInput.clear();
        addressInput.sendKeys("sidi moumen");
        
        // Téléphone
        WebElement phoneInput = driver.findElement(By.cssSelector("input:nth-child(3)"));
        phoneInput.clear();
        phoneInput.sendKeys("055555");
        
        Thread.sleep(500);
    }
    
    private void selectPaymentMethod() throws InterruptedException {
        // Mode de paiement
        WebElement paymentDropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("select:nth-child(5)")
        ));
        paymentDropdown.click();
        paymentDropdown.findElement(By.xpath("//option[. = 'Paiement en ligne']")).click();
        
        Thread.sleep(500);
        
        // Méthode de paiement
        WebElement methodDropdown = wait.until(ExpectedConditions.elementToBeClickable(
            By.cssSelector("select:nth-child(6)")
        ));
        methodDropdown.click();
        methodDropdown.findElement(By.xpath("//option[. = 'PayPal']")).click();
        
        Thread.sleep(500);
    }
    
    private void takeScreenshot(String testName) {
        try {
            File screenshotDir = new File("target/screenshots");
            if (!screenshotDir.exists()) {
                screenshotDir.mkdirs();
            }
            
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String fileName = String.format("test-%s-%d.png", 
                testName.replaceAll("[^a-zA-Z0-9]", "_"), 
                System.currentTimeMillis());
            
            Files.copy(screenshot.toPath(), 
                Paths.get("target/screenshots/" + fileName));
            
            System.out.println("📸 Screenshot saved: " + fileName);
        } catch (IOException e) {
            System.err.println("Failed to take screenshot: " + e.getMessage());
        }
    }
}