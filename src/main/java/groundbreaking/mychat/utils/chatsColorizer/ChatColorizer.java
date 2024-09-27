package groundbreaking.mychat.utils.chatsColorizer;

import groundbreaking.mychat.MyChat;
import org.bukkit.entity.Player;

public class ChatColorizer extends AbstractColorizer {

    public ChatColorizer(MyChat plugin) {
        super(plugin.getColorizer("use-minimessage-for-chats"), plugin.getInfos());
    }

    @Override
    public String colorize(Player player, String message) {
        if (player.hasPermission("mychat.chat.hex") && is16OrAbove) {
            return hexColorizer.colorize(message);
        }

        final char[] letters = message.toCharArray();
        for (int i = 0; i < letters.length; i++) {
            if (letters[i] == COLOR_CHAR) {
                final char code = letters[i + 1];
                if (COLOR_CODES.contains(code) && player.hasPermission("mychat.chat.color." + code)) {
                    letters[i++] = '§';
                    letters[i] = Character.toLowerCase(letters[i]);
                }
                else if (STYLE_CODES.contains(code) && player.hasPermission("mychat.chat.style." + code)) {
                    letters[i++] = '§';
                    letters[i] = Character.toLowerCase(letters[i]);
                }
            }
        }

        return new String(letters);
    }
}
