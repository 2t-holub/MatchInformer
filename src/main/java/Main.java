import bot.Bot;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        Properties props = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream("config.properties");
        try {
            props.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bot bot = new Bot(props);
        bot.checkMatches();
    }
}
