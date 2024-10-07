package groundbreaking.gigachat.commands.args;

import groundbreaking.gigachat.GigaChat;
import groundbreaking.gigachat.collections.PmSounds;
import groundbreaking.gigachat.commands.ArgsConstructor;
import groundbreaking.gigachat.database.DatabaseQueries;
import groundbreaking.gigachat.utils.config.values.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class SetPmSoundArgument extends ArgsConstructor {

    private final Messages messages;

    public SetPmSoundArgument(final GigaChat plugin, String name, String permission) {
        super(name, permission);
        this.messages = plugin.getMessages();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String[] args) {

        if (args.length != 3) {
            sender.sendMessage(messages.getSetpmsoundUsageError());
            return true;
        }

        final Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(messages.getPlayerNotFound());
            return true;
        }

        if (args[2].equalsIgnoreCase("none")) {
            PmSounds.remove(target.getName());
            DatabaseQueries.removePlayerFromPmSounds(target.getName());

            sender.sendMessage(messages.getTargetPmSoundRemoved()
                    .replace("{player}", target.getName())
            );
            if (!messages.getPmSoundRemoved().isEmpty()) {
                target.sendMessage(messages.getPmSoundRemoved());
            }

            return true;
        }

        final Sound sound;
        try {
            sound = Sound.valueOf(args[2]);
        } catch (IllegalArgumentException ignore) {
            sender.sendMessage(messages.getSoundNotFound());
            return true;
        }

        PmSounds.setSound(target.getName(), sound.name());

        sender.sendMessage(messages.getTargetPmSoundSet()
                .replace("{player}", target.getName())
                .replace("{sound}", sound.name())
        );

        if (!messages.getPmSoundSet().isEmpty()) {
            target.sendMessage(messages.getPmSoundSet()
                    .replace("{sound}", sound.name())
            );
        }

        return true;
    }
}
