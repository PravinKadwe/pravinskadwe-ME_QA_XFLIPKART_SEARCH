package demo;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import demo.wrappers.Wrappers;

public class TestCases {
    private static final Logger log = LoggerFactory.getLogger(TestCases.class);
    RemoteWebDriver driver;
    Wrappers wrapper;
    
    @BeforeTest
    public void startBrowser()
    {
        System.setProperty("java.util.logging.config.file", "logging.properties");
        try {

            final DesiredCapabilities capabilities = new DesiredCapabilities();
            capabilities.setBrowserName("chrome");
            driver = new RemoteWebDriver(new URL("http://localhost:8082/wd/hub"), capabilities);
            driver.manage().window().maximize();
            driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);

            driver.get("https://www.flipkart.com");

            wrapper = new Wrappers(driver);

            // wrapper.click(By.xpath("//button[contains(text(),'✕')]"));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    @AfterTest
    public void endTest()
    {
        driver.close();
        driver.quit();

    }



    @Test
    public void testCase01() throws InterruptedException {
        // Search for "Washing Machine"
        wrapper.type(By.name("q"), "Washing Machine");
        wrapper.click(By.cssSelector("button[type='submit']"));
    
        wrapper.click(By.xpath("//div[contains(text(),'Popularity')]"));

        wrapper.waitForElement(By.xpath("//span[contains(@id,'productRating')]"));
    
        List<WebElement> items = wrapper.findElements(By.xpath("//div[@class='DOjaWF gdgoEp']/div[@class='cPHDOP col-12-12']/div[@class='_75nlfW']"));
        int count = 1;
        double checkrating = 4.0;
    
        for (WebElement item : items) {
            String ratingText = item.findElement(By.xpath(".//span[contains(@id,'productRating')]")).getText();
                    
            double rating  = Double.parseDouble(ratingText);

            if (rating >= checkrating) {
                ++count;
            }

        }
    
        System.out.println("Count of items with <= 4 stars: " + count);
    }


    @Test
    public void testCase02() throws InterruptedException {
        // Search for "iPhone"
        wrapper.type(By.name("q"), "iPhone");
        wrapper.click(By.cssSelector("button[type='submit']"));
    
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[@class='DOjaWF gdgoEp']/div[@class='cPHDOP col-12-12']/div[1]/div[contains(@data-id,'MOB')]")));
    
        List<WebElement> items = wrapper.findElements(By.xpath("//div[@class='DOjaWF gdgoEp']/div[@class='cPHDOP col-12-12']/div[1]/div[contains(@data-id,'MOB')]"));
        
        for (WebElement item : items) {
            List<WebElement> discountElements = item.findElements(By.xpath(".//div[@class='UkUFwK']/span"));
            
            if (!discountElements.isEmpty()) {
                
                String discountText = discountElements.get(0).getText().replace("% off", "").trim();
                
                int discount = Integer.parseInt(discountText);
                
                if (discount > 17) {
                    
                    String title = item.findElement(By.xpath(".//div[@class='KzDlHZ']")).getText();
                    System.out.println("Title: " + title + ", Discount: " + discount + "%");
                }
            }
        }
    }
    

    @Test
    public void testCase03() throws InterruptedException {
        // Search for "Coffee Mug"
        wrapper.type(By.name("q"), "Coffee Mug");
        wrapper.click(By.cssSelector("button[type='submit']"));

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));

        Thread.sleep(2000);
    
        wrapper.click(By.xpath("//div[contains(text(),'4★ & above')]"));

        // wrapper.click(By.xpath("//div[contains(text(),'Popularity')]"));

        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//div[contains(@data-id,'MUG')]")));
    
        List<WebElement> items = wrapper.findElements(By.xpath("//div[contains(@data-id,'MUG')]"));

        int count = 1; 
        int i = 1;
        for (WebElement item : items) {
            try {

                while (i <= items.size()) {
                    // Refetch the element before interacting
                    item = wrapper.findElement(By.xpath("(//div[contains(@data-id,'MUG')])["+i+"]"));

                    Thread.sleep(2000);
                    
                    boolean checkreviewText = item.findElement(By.xpath(".//span[@class='Wphh3N']")).isDisplayed();
                    
                    if (checkreviewText) {
                        String reviewText = item.findElement(By.xpath(".//span[@class='Wphh3N']")).getText();
                        // System.out.println("reviewText: " + reviewText);
            
                        String cleanedReviewText = reviewText.replaceAll("[(),]", "").trim();
                        double reviewCount = Double.parseDouble(cleanedReviewText);
                        
                        if (reviewCount > 1000 && count <= 5) {
                            count++;
                            
                            String imageURL = item.findElement(By.xpath(".//div[@class='slAVV4']//a[1]")).getAttribute("href");
                            String title = item.findElement(By.xpath(".//div[@class='slAVV4']//a[2]")).getAttribute("title");
                            
                            System.out.println("Title: " + title + ", Image URL: " + imageURL);
                        } else if (count > 5){
                            break;
                        }
                    }
                    i++;
                } 

            } catch (StaleElementReferenceException e) {
                // Handle the stale element exception by refetching the element or logging the error
                System.out.println("Encountered StaleElementReferenceException. Retrying...");
            }
            
            Thread.sleep(2000);
        }
        
    }


}