package com.example.demo;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/audit")
public class AuditController {

    @GetMapping("/getImg")
    public ResponseEntity<byte[]> getImg() {
        try {
            File imageFile = new File("screenshots/audit-failure_1751921876870.png");
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .body(imageBytes);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/run")
    public ResponseEntity<byte[]> runAudit() {
        WebDriverManager.firefoxdriver().setup();
        WebDriver driver = new FirefoxDriver();

        try {
            driver.get("https://www.selenium.dev/selenium/web/web-form.html");
            driver.manage().window().maximize();

            // Fill out the input field with an incorrect value
            WebElement input = driver.findElement(By.name("my-text"));
            input.clear();
            input.sendKeys("WRONG VALUE");

            WebElement input2 = driver.findElement(By.name("my-password"));
            input2.clear();
            input2.sendKeys("WRONG VALUE");

            // Simulate clicking submit (optional)
            //driver.findElement(By.tagName("form")).submit();
            //Thread.sleep(2000); // wait for the form to submit

            // Simulate audit check (expecting "Expected Value")
            String expected = "Expected Value";
            String actual = input.getAttribute("value");

            if (!expected.equals(actual)) {
                highlightFailures(driver, List.of(input, input2), "audit-failure");
//                return ResponseEntity.ok("Audit failed, screenshot captured.");

                return ResponseEntity
                        .ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(highlightFailures(driver, List.of(input, input2), "audit-failure"));
            }


        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(null);
        } finally {
            driver.quit();
        }
        return null;
    }

    private byte[] highlightFailures(WebDriver driver, List<WebElement> elements, String name) {
        try {
            File screenshotFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            BufferedImage img = ImageIO.read(screenshotFile);

            Graphics2D g = img.createGraphics();
            g.setColor(Color.RED);
            g.setStroke(new BasicStroke(3));

            for (WebElement el : elements) {
                Point p = el.getLocation();
                Dimension d = el.getSize();
                //g.drawLine(p.getX(), p.getY(), d.getWidth(),  p.getY());
                g.drawRect(p.getX(), p.getY(), d.getWidth(), d.getHeight());
            }

            g.dispose();

            String outputDir = "screenshots";
            new File(outputDir).mkdirs();
            String path = outputDir + "/" + name + "_" + System.currentTimeMillis() + ".png";
            var file = new File(path);
            ImageIO.write(img, "png", file);
            return Files.readAllBytes(file.toPath());


        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
