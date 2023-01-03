package ru.xmagi;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;

import static java.lang.Thread.sleep;

/**
 * @author Username
 * главная часть с запуском бота. любой текст, если он имеется передается, а именно первый аргумент, как токен.
 * его можно взять у BotFather
 * изменить паузу можно в sleep в виде миллисекунд.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException, NoSuchFieldException, IllegalAccessException {
        String token = ""; // deftoken
        prostaVapseTelegaBot bot;
        if (args.length > 0) {
            token = args[0];
            if (token == "config") token = "";
        }
        System.setProperty("file.encoding","UTF-8");
        // var bot = new prostaVapseTelegaBot() ; // можно передать токен в конструктор.

        if(token.isEmpty()) bot = new prostaVapseTelegaBot();
        else bot = new prostaVapseTelegaBot(token);

        while(true){
            sleep(1000); // пауза 1000 миллисекунд
            bot.readUpdates();
            try {
                bot.updateProp();
            } catch(IOException e) {
                System.out.println("Can't to update bot propetries: " + e.toString());
            }
        }
    }
}