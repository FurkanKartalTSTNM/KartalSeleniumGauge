package org.example.step;

import com.thoughtworks.gauge.Step;
import org.example.base.BaseTest;
import org.json.*;
import org.openqa.selenium.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.*;

import javax.swing.*;
import java.awt.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BaseSteps extends BaseTest {

    WebDriverWait wait = new WebDriverWait(driver, 10);
    long pollingEveryValue;

    public BaseSteps() {
        initMap(getFileList());
    }

    public void checkElementVisible2(By by, long timeout) {

        assertTrue(isElementVisible(by, timeout), by.toString() + " elementi görüntülenemedi.");
    }
    public boolean isElementVisible(By by, long timeout){

        try {
            setFluentWait(timeout).until(ExpectedConditions.visibilityOfElementLocated(by));
            logger.info("true");
            return true;
        } catch (Exception e) {
            logger.info("false" + " " + e.getMessage());
            return false;
        }
    }

    @Step({"Wait <key> element",
            "<key> li elementi bekle"})
    public void waitElement(String key) {
        try {
            By byElement = getBy(key);
            wait.until(ExpectedConditions.presenceOfElementLocated(byElement));
        } catch (TimeoutException t) {
            return;
        }
    }

    public List<WebElement> findElements(String key) {

        return driver.findElements(getElementInfoToBy(findElementInfoByKey(key)));
    }
    public WebElement findElement(String key) {
        By infoParam =getElementInfoToBy(findElementInfoByKey(key));
        WebDriverWait webDriverWait = new WebDriverWait(driver, 120);
        WebElement webElement = webDriverWait.until(ExpectedConditions.presenceOfElementLocated(infoParam));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'})", webElement);
        return webElement;
    }
    //CLİCK

    public void clickElement(WebElement element) {
        element.click();
    }

    public FluentWait<WebDriver> setFluentWait(long timeout){

        FluentWait<WebDriver> fluentWait = new FluentWait<WebDriver>(driver);
        fluentWait.withTimeout(Duration.ofSeconds(timeout))
                .pollingEvery(Duration.ofMillis(pollingEveryValue))
                .ignoring(NoSuchElementException.class);
        return fluentWait;
    }









    @Step({"Click to element <key>", "<key> li elemente tıkla"})
    public void clickElement(String key) {
        WebElement element = driver.findElement(getBy(key));
        waitForPageToLoad();
        checkElementVisible2(getBy(key), 30);
        waitElement(key);
        wait.until(ExpectedConditions.elementToBeClickable(element));
        List<WebElement> elements = findElements(key);

        if (elements.size() == 0) {

            logger.info(key + " Element Not Exist or Not Working ");

        }
        if (elements.size() > 0) {

            clickElement(findElement(key));
            logger.info("Clicked to the  " + key + "  element.");;
        }
    }

    @Step({"<url> urle git", "<url> go to url"})
    public void goToUrl(String url) {
        driver.navigate().to(url);
    }

    @Step({"<key> sn bekle",
            "Wait for <key> sec"})
    public void waitSeconds(String key) throws InterruptedException {
        Thread.sleep(Integer.parseInt(key) * 2000);

    }

    @Step({"<key> li elementi görünür olana kadar bekle",
            "Wait until element with <key> is visible"})
    public void waitVisible(String key) {
        try {
            By byElement = getBy(key);
            wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(byElement));
        } catch (TimeoutException t) {
            return;
        }
    }

    @Step({"Sayfanın yüklenmesini bekle"})
    public void waitForPageToLoad() {
        ExpectedCondition<Boolean> pageLoadCondition = driver ->
                ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");

        WebDriverWait wait = new WebDriverWait(driver, 120);
        wait.until(pageLoadCondition);
    }

    @Step({"Send keys to element with key <key> and text <text>",
            "<key> li elemente <text> degerini yaz"})
    public void sendKeyWithDeger(String key, String text) {
        By byElement = getBy(key);
        wait.until(ExpectedConditions.presenceOfElementLocated(byElement));
        waitForPageToLoad();
        checkElementVisible2(getBy(key), 30);
        waitElement(key);
        WebElement element = driver.findElement(byElement);
        element.clear();
        if (text.startsWith("Deger_")) {
            element.sendKeys(getValue(text));
            System.out.println("text = " + getValue(text));
        } else {
            element.sendKeys(text);
        }
    }

}


