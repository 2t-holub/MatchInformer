package bot;

import dao.Dao;
import models.Match;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import services.Parser;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class Bot extends TelegramLongPollingBot {
    public static Properties properties = new Properties();
    public static Dao dao;
    public static Parser parser = new Parser();

    public Bot(Properties props) {
        this.properties = props;
        dao = new Dao();
    }

    public void checkMatches() {
        System.out.println(getBotUsername());
        try {
            while (true) {
                checkNoneToFutureMatches();
                Thread.sleep(60000);
                checkFutureToLiveMatches();
                Thread.sleep(60000);
                checkLiveToPastMatches();
                Thread.sleep(60000);
                checkFutureToPastMatches();
                Thread.sleep(60000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void checkNoneToFutureMatches() {
        List<Match> tempMatches = parser.parseMatches();
        tempMatches.stream().forEach(match -> dao.writeMatch(match.getLink()));
        System.out.println("The check [None -> Future] has finished!");
    }

    private void checkFutureToLiveMatches() {
        ArrayList<Match> futureMatches = dao.getFutureMatches();
        List<Match> tempMatches = new ArrayList<>();
        if (futureMatches.size() > 0) {
            tempMatches = parser.getLiveMatches(futureMatches);
        }
        for (int i = 0; i < tempMatches.size(); i++) {
            dao.changeMatchToLive(tempMatches.get(i));
            File imageFile = parser.getStartMatchScreenShot(properties.getProperty("linkAddition") + tempMatches.get(i).getLink());
            sendPhoto(properties.getProperty("mainAdminChatId"), imageFile,
                    "⚔️ The match " + tempMatches.get(i).getTeam1() + " vs. " + tempMatches.get(i).getTeam2() + " has started!");
        }
        System.out.println("The check [Future -> Live] has finished!");
    }

    private void checkLiveToPastMatches() {
        List<Match> liveMatches = dao.getLiveMatches();
        List<Match> tempMatches = new ArrayList<>();
        if (liveMatches.size() > 0) {
            tempMatches = parser.getPastMatches(liveMatches);
        }
        for (int i = 0; i < tempMatches.size(); i++) {
            dao.changeMatchToPast(tempMatches.get(i));
            File[] imageFiles = parser.getPastMatchScreenShots(properties.getProperty("linkAddition") + tempMatches.get(i).getLink());
            String textWith0Image = tempMatches.get(i).getResultText();
            String textWith1Image = tempMatches.get(i).getPlayerText();
            sendPhoto(properties.getProperty("mainAdminChatId"), imageFiles[0], textWith0Image);
            sendPhoto(properties.getProperty("mainAdminChatId"), imageFiles[1], textWith1Image);
        }
        System.out.println("The check [Live -> Past] has finished!");
    }

    private void checkFutureToPastMatches() {
        List<Match> futureMatches = dao.getFutureMatches();
        List<Match> pastMatches = new ArrayList<>();
        if (futureMatches.size() > 0) {
            pastMatches = parser.getPastMatches(futureMatches);
        }
        for (int i = 0; i < pastMatches.size(); i++) {
            dao.changeMatchToPast(pastMatches.get(i));
        }
        System.out.println("The check [Future -> Past] has finished!");
    }

    public void sendPhoto(String chatId, File imageFile, String text) {
        SendPhoto sendPhoto = new SendPhoto().setChatId(chatId);
        sendPhoto.setCaption(text);
        try {
            sendPhoto.setPhoto(imageFile);
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return;
    }

    @Override
    public void onUpdateReceived(Update update) {

    }

    @Override
    public String getBotUsername() {
        return properties.getProperty("botUserName");
    }

    @Override
    public String getBotToken() {
        return properties.getProperty("botToken");
    }
}
