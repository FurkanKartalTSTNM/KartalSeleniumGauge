package org.example.base;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.gauge.AfterScenario;
import com.thoughtworks.gauge.BeforeScenario;
import org.apache.commons.lang3.StringUtils;
import org.example.model.ElementInfo;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.fail;


public class BaseTest {

    protected static WebDriver driver;
    protected static Actions actions;
    protected Logger logger = LoggerFactory.getLogger(getClass());
    DesiredCapabilities capabilities;
    ChromeOptions chromeOptions;
    FirefoxOptions firefoxOptions;

    String browserName = "chrome";
    String selectPlatform = "mac";


    private static final String DEFAULT_DIRECTORY_PATH = "elementValues";
    static ConcurrentMap<String, Object> elementMapList = new ConcurrentHashMap<>();

    @BeforeScenario
    public void setUp() {
        logger.info("************************************  BeforeScenario  ************************************");
        try {
            if (StringUtils.isEmpty(System.getenv("key"))) {
                logger.info("Local cihazda " + selectPlatform + " ortamında " + browserName + " browserında test ayağa kalkacak");
                if ("win".equalsIgnoreCase(selectPlatform)) {
                    if ("chrome".equalsIgnoreCase(browserName)) {
                        chromeOptions = chromeOptions();
                        driver = new ChromeDriver(chromeOptions);
                    } else if ("firefox".equalsIgnoreCase(browserName)) {
                        driver = new FirefoxDriver(firefoxOptions());
                    }
                } else if ("mac".equalsIgnoreCase(selectPlatform)) {
                    if ("chrome".equalsIgnoreCase(browserName)) {
                        chromeOptions = chromeOptions();
                        driver = new ChromeDriver(chromeOptions);
                    } else if ("firefox".equalsIgnoreCase(browserName)) {
                        driver = new FirefoxDriver(firefoxOptions());
                    }
                }
                actions = new Actions(driver);
                driver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
                driver.get("https://www.tatilbudur.com/");



            } else {
                logger.info("************************************   Testiniumda test ayağa kalkacak   ************************************");
                ChromeOptions options = new ChromeOptions();
                capabilities = DesiredCapabilities.chrome();
                options.setExperimentalOption("w3c", false);
                options.addArguments("disable-translate");
                options.addArguments("--disable-notifications");
                Map<String, Object> prefs = new HashMap<>();
                options.setExperimentalOption("prefs", prefs);
                capabilities.setCapability(ChromeOptions.CAPABILITY, options);
                capabilities.setCapability("key", System.getenv("key"));
                browserName = System.getenv("browser");
                driver = new RemoteWebDriver(new URL("http://host.docker.internal:4444/wd/hub"), capabilities);
                actions = new Actions(driver);
                driver.get("https://www.tatilbudur.com/");

            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @AfterScenario
    public void tearDown() {
        driver.quit();
    }

    public void initMap(File[] fileList) {
        Type elementType = new TypeToken<List<ElementInfo>>() {
        }.getType();
        Gson gson = new Gson();
        List<ElementInfo> elementInfoList = null;
        for (File file : fileList) {
            try {
                elementInfoList = gson
                        .fromJson(new FileReader(file), elementType);
                elementInfoList.parallelStream()
                        .forEach(elementInfo -> elementMapList.put(elementInfo.getKey(), elementInfo));
            } catch (FileNotFoundException e) {
                logger.warn("{} not found", e);
            }
        }
    }

    public File[] getFileList() {
        File[] fileList = new File(
                this.getClass().getClassLoader().getResource(DEFAULT_DIRECTORY_PATH).getFile())
                .listFiles(pathname -> !pathname.isDirectory() && pathname.getName().endsWith(".json"));
        if (fileList == null) {
            logger.warn(
                    "File Directory Is Not Found! Please Check Directory Location. Default Directory Path = {}",
                    DEFAULT_DIRECTORY_PATH);
            throw new NullPointerException();
        }
        return fileList;
    }

    /**
     * Set Chrome options
     *
     * @return the chrome options
     */
    public ChromeOptions chromeOptions() {
        chromeOptions = new ChromeOptions();
        LoggingPreferences loggingprefs = new LoggingPreferences();
        loggingprefs.enable(LogType.BROWSER, Level.ALL);
        loggingprefs.enable(LogType.CLIENT, Level.ALL);
        loggingprefs.enable(LogType.PERFORMANCE, Level.ALL);
        loggingprefs.enable(LogType.PROFILER, Level.ALL);
        capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(CapabilityType.LOGGING_PREFS, loggingprefs);
        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        chromeOptions.setCapability("goog:loggingPrefs", loggingprefs);
        chromeOptions.setExperimentalOption("prefs", prefs);
        // Bu satır W3C uyumunu aktif hale getirir
        chromeOptions.setExperimentalOption("w3c", true);
        String downloadFilepath = "./desktop";
        prefs.put("profile.default_content_setting_values.notifications", 2);
        prefs.put("profile.default_content_settings.popups", 0);
        prefs.put("download.default_directory", downloadFilepath);
        //chromeOptions.addArguments("--kiosk");
        chromeOptions.addArguments("--incognito");
        chromeOptions.addArguments("--disable-notifications");
        chromeOptions.addArguments("--window-size=1920,1080");

        //chromeOptions.addArguments("--start-fullscreen");
        // chromeOptions.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        // chromeOptions.setExperimentalOption("useAutomationExtension", false);
        System.setProperty("webdriver.chrome.driver", "web_driver/chromedriver");
        chromeOptions.merge(capabilities);
        return chromeOptions;

    }



    /**
     * Set Firefox options
     *
     * @return the firefox options
     */
    public FirefoxOptions firefoxOptions() {
        firefoxOptions = new FirefoxOptions();
        capabilities = DesiredCapabilities.firefox();
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        firefoxOptions.addArguments("--kiosk");
        firefoxOptions.addArguments("--disable-notifications");
        firefoxOptions.addArguments("--start-fullscreen");
        FirefoxProfile profile = new FirefoxProfile();
        capabilities.setCapability(FirefoxDriver.PROFILE, profile);
        capabilities.setCapability("marionette", true);
        capabilities.setCapability("networkConnectionEnabled", true); // veya false olursa internet kapalı gibi tepki
        capabilities.setCapability("pageLoadStrategy", "eager");
        capabilities.setCapability("unhandledPromptBehavior", "ignore"); // veya "dismiss" veya "accept"
        // Bu satır W3C uyumunu aktif hale getirir
        firefoxOptions.setCapability("moz:firefoxOptions", capabilities);
        String version = (String) capabilities.getCapability("moz:geckodriverVersion");
        System.out.println("GeckoDriver Version: " + version);
        firefoxOptions.merge(capabilities);
        System.setProperty("webdriver.gecko.driver", "web_driver/geckodriver");
        return firefoxOptions;
    }
    public static By getElementInfoToBy(ElementInfo elementInfo) {
        By by = null;
        String elementInfoValue = elementInfo.getValue();
        switch (elementInfo.getType()){
            case "css":
                by = By.cssSelector(elementInfoValue);
                break;
            case "id":
                by = By.id(elementInfoValue);
                break;
            case "xpath":
                by = By.xpath(elementInfoValue);
                break;
            case "class":
                by = By.className(elementInfoValue);
                break;
            case "tagName":
                by = By.tagName(elementInfoValue);
                break;
            case "name":
                by = By.name(elementInfoValue);
                break;
            default:
                throw new NullPointerException("Element tipi hatalı");
        }
        return by;
    }

    public static ElementInfo findElementInfoByKey(String key) {

        if(!elementMapList.containsKey(key)){
            fail(key + " adına sahip element bulunamadı. Lütfen kontrol ediniz.");
        }
        return (ElementInfo) elementMapList.get(key);
    }
    public By getBy(String key){

        By by = getElementInfoToBy(findElementInfoByKey(key));
        logger.info(key + " elementi " + by.toString() + " by değerine sahip");
        return by;
    }

    public void saveValue(String key, String value) {
        elementMapList.put(key, value);
    }

    public String getValue(String key) {
        return elementMapList.get(key).toString();
    }



}
