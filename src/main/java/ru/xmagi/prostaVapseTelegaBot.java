/**
 * @author UserName
 * @description Сделанный бот для перевода транслита от NapalmX... в киррилицу
 */
package ru.xmagi;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineQueryResult;
import com.pengrad.telegrambot.model.request.InlineQueryResultArticle;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.AnswerInlineQuery;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.pengrad.telegrambot.response.SendResponse;

import java.io.*;
import java.util.List;
import java.util.Properties;



/*
    @description здесь статичные переменные.
    - defConfigPath путь до конфига, где можно изменить bot-token и ID последнего сообщения полученного.
    - alphabit - русскоязычный алфавит
    - alphabitEng буквы английского что меняются на alphabit
    - engPreInit тут уже более сложные "мыслеформы" например ja меняется на я. По индексу. Можно заменить на HashMap, но йава в этом плане сложнее kotlin'а из-за перегрузки операторов. пускай будет как есть.
    - RusPreInit  то на что меняется встречаемый текст в engpreinit. Слова в translit методе проходят в lowerCase.
*/
class prostaVapseTelegaBotSet {
    public static final String defConfigPath = "local.config";
    public static final String alphabit[] = new String[]{
            "а", "б", "в", "г", "д", "е", "ё", "з", "с", "и", "й", "к", "л", "м", "н", "о", "п", "р", "с", "т", "у", "ф", "х", "ц", "ь"

    };
    public static final char alphabitEng[] = new char[]{
            'a', 'b', 'v', 'g', 'd', 'e', 'e', 'z', 'с', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'r', 's', 't', 'u', 'f', 'x', 'c', '\''
    };
    public static final String EngPreInit[] = new String[] {
            "yu",
            "ya",
            "yo",
            "jo",
            "zh",
            "shh",
            "sh",
            "ch",
            "tvz",
            "mjz",
            "je",
            "ju",
            "ja"
    };
    public static final String RusPreInit[] = new String[] {
      "ю",
      "я",
      "ё",
      "ё",
      "ж", "щ", "ш", "ч", "ъ", "ь", "э", "ю","я"
    };
}

/**
    readUpdates - проверяет обновления из телеграмма. далее вызывает внутренние приватные методы.
    пустой конструктор использует данные из local.config
 */
public class prostaVapseTelegaBot {
    private TelegramBot mBot;
    private Properties mProp;
    private int mLastID = 0;
    private void initBot(String token) {
        if (mBot == null) mProp = new Properties();
        mBot = new TelegramBot(token);//.Builder(token).debug().build();;
        mProp.setProperty("BOT_TOKEN", token);
        String _lastUpdateID = mProp.getProperty("lastUpdateID");
        Integer _lastID = Integer.valueOf(_lastUpdateID == null ? "0" : _lastUpdateID);
        mLastID = _lastID;
    }
    /**
     * обновить конфиг
     * @throws IOException if can't to write config
     */
    public void updateProp() throws IOException {
        mProp.setProperty("lastUpdateID", Integer.valueOf(mLastID).toString());
        mProp.store(new FileOutputStream(getFilePropetry()), null);
    }

    /**
     * читает конфиг
     * @return File
     * @throws IOException
     */
    private File getFilePropetry() throws IOException {
        File f = new File(prostaVapseTelegaBotSet.defConfigPath);
        if (!f.exists()) {
            f.createNewFile();
        }
        return f;
    }
    public prostaVapseTelegaBot() {
        mProp = new Properties();
        String botToken;
        try {
            // InputStream in = getClass().getResourceAsStream(prostaVapseTelegaBotSet.defConfigPath);
            mProp.load(new FileInputStream(getFilePropetry()));
            botToken = mProp.getProperty("BOT_TOKEN");
        } catch (IOException e) {
            botToken = "";
        }
        initBot(botToken);
    }
    private void updateDo(Update update) {
    try {
        System.out.println("Update is:");
        System.out.println(update.toString());
        Message msg = update.message();
        SendMessage request = new SendMessage(msg.chat().id(), translit(msg.text()))
                .parseMode(ParseMode.HTML)
                .disableWebPagePreview(true)
                .disableNotification(true);
        SendResponse sendResponse = mBot.execute(request);
        boolean ok = sendResponse.isOk();
        System.out.println("send update is " + ok);
    } catch (Exception _e) {}
    }
    /**
        Транслит.
     */
    private String translit(String from) {
        StringBuilder sb = new StringBuilder();
        String str = from.toLowerCase();
        for(var i = 0; i < prostaVapseTelegaBotSet.EngPreInit.length; i++) {
            str = str.replace(prostaVapseTelegaBotSet.EngPreInit[i], prostaVapseTelegaBotSet.RusPreInit[i]);
        }
        maincycle:
        for (var ch : str.toCharArray()) {
            for (int i = 0; i < prostaVapseTelegaBotSet.alphabitEng.length; i++) {
                if (ch == prostaVapseTelegaBotSet.alphabitEng[i]) {
                    var appendData = prostaVapseTelegaBotSet.alphabit[i];
                    sb.append(appendData);
                    continue maincycle;
                }
            }
            sb.append(ch);
        }
        return sb.toString();
    }
    private void updateInline(InlineQuery iq) {
        System.out.println("is inline query");
        System.out.println( iq.toString() );
        String text = iq.query();
        String translited =  translit(text);
        InlineQueryResult r2 = new InlineQueryResultArticle("id", translited, translited);
        mBot.execute(
                new AnswerInlineQuery(iq.id(), new InlineQueryResult[]{r2})
                        .cacheTime(10)
                        .isPersonal(false)
                        .nextOffset("offset")
                        .switchPmParameter("pmParam")
                        .switchPmText(text)
        );
    }
    /**
     *  Тут происходит чтение и вызов методов updateInline для inline сообщения/ updateDo для обычного текста отправленного боту.
     */

    public void readUpdates() {
        // System.out.printf("Last ID %d\n~~~~~~~~~~\n", mLastID);
        var _updates = new GetUpdates().limit(100).offset(mLastID).timeout(0);
        GetUpdatesResponse updatesResponse = mBot.execute(_updates);

        List<Update> updates = updatesResponse.updates();
        updateHandler:
        for (var update : updates) {
            Integer updateId = update.updateId();
            InlineQuery inlineQuery = update.inlineQuery();

            if (inlineQuery != null ) updateInline(inlineQuery);
            else updateDo(update);

            if (mLastID <= updateId) mLastID = updateId + 1;
        }
        // System.out.println("~~~~~~~~~");

    }
    /**
    @param token String
    inited bot by token
     */
    public prostaVapseTelegaBot(String token) {
        initBot(token);
    }
}
