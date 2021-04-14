package services;

import bot.Bot;
import models.Match;
import org.apache.commons.exec.environment.EnvironmentUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Parser {
    SimpleDateFormat formatForHltv = new SimpleDateFormat("EEEE - yyyy-MM-dd", Locale.ENGLISH);

    public List<Match> parseMatches() {
        String dateStr = formatForHltv.format(new Date());
        List<Match> matches = parseMatchesByDate(dateStr);
        return matches;
    }

    private List<Match> parseMatchesByDate(String date) {
        List<Match> matches = new ArrayList<>();
        Document doc = null;
        try {
            doc = Jsoup.connect("https://www.hltv.org/matches").userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36").get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements matchElements = (Elements) doc.getElementsContainingOwnText(date);
        if (matchElements.size() == 0) {
            return matches;
        }
        Element aElement = matchElements.get(0).parent();
        for (int j = 1; j < aElement.childNodeSize() / 2; j++) {
            if (aElement.child(j).child(0).childNodeSize() == 5) {
                continue; // якщо жодна команда не визначена
            }
            if (aElement.child(j).child(0).child(1).child(0).childNodeSize() == 3 || aElement.child(j).child(0).child(1).child(1).childNodeSize() == 3) {
                continue; //якщо одна з команд не визначена
            }
            Element tempElement = aElement.child(j).child(0);
            String team1 = tempElement.child(1).child(0).child(1).ownText();
            String team2 = tempElement.child(1).child(1).child(1).ownText();
            String link = tempElement.attr("href");
            if (team1.contains("/") || team2.contains("/")) {
                continue;
            }
            link = link.replace("/matches/", "");
            Match match = new Match(link);
            matches.add(match);
            System.out.println(match);
        }
        return matches;
    }

    public ArrayList<Match> getLiveMatches(List<Match> futureMatches) {
        ArrayList<Match> liveMatches = new ArrayList<>();
        Document doc = null;
        for (int i = 0; i < futureMatches.size(); i++) {
            try {
                doc = Jsoup.connect(Bot.properties.getProperty("linkAddition")  + futureMatches.get(i).getLink()).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36").get();
            } catch (IOException e) {
                e.printStackTrace();
                return liveMatches;
            }
            Element aElement = ((Elements) doc.getElementsByAttributeValue("class", "timeAndEvent")).get(0);
            if (aElement.childNodeSize() < 5) {
                return liveMatches;
            }
            String status = aElement.child(4).ownText();
            if (!status.toLowerCase().equals("live")) {
                continue;
            }
            Elements teamsElements = (Elements) doc.getElementsByAttributeValue("class", "teamName");
            String team1 = teamsElements.get(0).ownText();
            String team2 = teamsElements.get(1).ownText();
            futureMatches.get(i).setTeam1(team1);
            futureMatches.get(i).setTeam2(team2);
            liveMatches.add(futureMatches.get(i));
        }
        return liveMatches;
    }

    public ArrayList<Match> getPastMatches(List<Match> liveMatches) {
        ArrayList<Match> pastMatches = new ArrayList<>();
        Document doc = null;
        for (int i = 0; i < liveMatches.size(); i++) {
            try {
                doc = Jsoup.connect(Bot.properties.getProperty("linkAddition") + liveMatches.get(i).getLink()).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36").get();
            } catch (IOException e) {
                e.printStackTrace();
                return pastMatches;
            }
            Elements betElements = (Elements) doc.getElementsByAttributeValue("class", "timeAndEvent");
            if (betElements.size() == 0) {
                return pastMatches;
            }
            Element aElement = betElements.get(0);
            if (aElement.childNodeSize() < 5) {
                return pastMatches;
            }
            String status = aElement.child(4).ownText();
            if (!status.equals("Match over")) {
                continue;
            }
            Elements teamsElements = (Elements) doc.getElementsByAttributeValue("class", "teamName");
            String team1 = teamsElements.get(0).ownText();
            String team2 = teamsElements.get(1).ownText();
            Elements playerElements = (Elements) doc.getElementsByAttributeValue("class", "player-nick");
            String player = playerElements.get(0).ownText();
            Elements score1Elements = (Elements) doc.getElementsByAttributeValue("class", "team1-gradient");
            int score1 = Integer.valueOf(score1Elements.get(0).child(1).ownText());
            Elements score2Elements = (Elements) doc.getElementsByAttributeValue("class", "team2-gradient");
            int score2 = Integer.valueOf(score2Elements.get(0).child(1).ownText());
            liveMatches.get(i).setTeam1(team1);
            liveMatches.get(i).setTeam2(team2);
            liveMatches.get(i).setBestPlayerNickname(player);
            liveMatches.get(i).setScore1(score1);
            liveMatches.get(i).setScore2(score2);
            pastMatches.add(liveMatches.get(i));
        }
        return pastMatches;
    }

    public static File getStartMatchScreenShot(String link) {
        boolean isServer = Boolean.valueOf(Bot.properties.getProperty("isServer"));
        WebDriver driver = null;
        File imageFile = new File("/noimage.png");
        String driverPath = "";
        if (!isServer) {
            driverPath = "AddApps/chromedriver88.exe";
            System.setProperty("webdriver.chrome.driver", driverPath);
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("window-size=1260x600");
        if (isServer) {
            try {   //GOOGLE_CHROME_SHIM GOOGLE_CHROME_BIN
                String binaryPath = EnvironmentUtils.getProcEnvironment().get("GOOGLE_CHROME_SHIM");
                System.out.println("Path: " + binaryPath);
                options.setBinary(binaryPath);
                options.addArguments("--disable-gpu");
                //options.addArguments("--disable-dev-shm-usage");
                options.addArguments("--no-sandbox");
            } catch (Exception e) {
                System.out.println("Exception in Parser.getMatchScreenShots (server part)");
            }
        }
        try {
            driver = new ChromeDriver(options);
        } catch (Exception e) {
            e.printStackTrace();
            return imageFile;
        }
        try {
            driver = new ChromeDriver();
        } catch (Exception e) {
            e.printStackTrace();
            return imageFile;
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;
        driver.get(link);
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        try {
            Thread.sleep(10000);
            new Actions(driver).moveToElement(driver.findElement(By.id("CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll")), 0,0).click().build().perform();
            WebElement ele1 = driver.findElement(By.className("lineups"));
            WebElement ele0 = driver.findElement(By.className("past-matches"));
            WebElement ele2 = driver.findElement(By.className("streams"));
            js.executeScript("arguments[0].scrollIntoView();", ele0);
            Thread.sleep(2000);
            js.executeScript("arguments[0].scrollIntoView();", ele2);
            Thread.sleep(2000);
            // capture screenshot with getScreenshotAs() of the WebElement class
            imageFile = ele1.getScreenshotAs(OutputType.FILE);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        driver.close();
        return imageFile;
    }

    public static File[] getPastMatchScreenShots(String link) {
        boolean isServer = Boolean.valueOf(Bot.properties.getProperty("isServer"));
        WebDriver driver = null;
        File[] imageFiles = new File[2];
        String driverPath = "";
        if (!isServer) {
            driverPath = "AddApps/chromedriver88.exe";
            System.setProperty("webdriver.chrome.driver", driverPath);
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("window-size=1260x600");
        if (isServer) {
            try {   //GOOGLE_CHROME_SHIM GOOGLE_CHROME_BIN
                String binaryPath = EnvironmentUtils.getProcEnvironment().get("GOOGLE_CHROME_SHIM");
                System.out.println("Path: " + binaryPath);
                options.setBinary(binaryPath);
                options.addArguments("--disable-gpu");
                //options.addArguments("--disable-dev-shm-usage");
                options.addArguments("--no-sandbox");
            } catch (Exception e) {
                System.out.println("Exception in Parser.getPastMatchScreenShots (server part)");
            }
        }
        try {
            driver = new ChromeDriver(options);
        } catch (Exception e) {
            e.printStackTrace();
            return imageFiles;
        }
        try {
            driver = new ChromeDriver();
        } catch (Exception e) {
            e.printStackTrace();
            return imageFiles;
        }
        JavascriptExecutor js = (JavascriptExecutor) driver;
        driver.get(link);
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
        try {
            Thread.sleep(10000);
            new Actions(driver).moveToElement(driver.findElement(By.id("CybotCookiebotDialogBodyLevelButtonLevelOptinAllowAll")), 0,0).click().build().perform();
            WebElement ele1 = driver.findElement(By.className("flexbox-column"));
            WebElement ele0 = driver.findElement(By.className("headline"));
            WebElement ele2 = driver.findElement(By.className("matchstats"));
            js.executeScript("arguments[0].scrollIntoView();", ele2);
            Thread.sleep(2000);
            js.executeScript("arguments[0].scrollIntoView();", ele0);
            Thread.sleep(2000);
            // capture screenshot with getScreenshotAs() of the WebElement class
            File f = ele1.getScreenshotAs(OutputType.FILE);
            imageFiles[0] = f;
            //FileUtils.copyFile(f, new File("F:\\logoScreeshot.png"));

            WebElement ele3 = driver.findElement(By.className("stats-content"));
            js.executeScript("arguments[0].scrollIntoView();", ele2);
            // capture screenshot with getScreenshotAs() of the WebElement class
            Thread.sleep(2000);
            File f2 = ele3.getScreenshotAs(OutputType.FILE);
            imageFiles[1] = f2;
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        driver.close();
        return imageFiles;
    }
}
