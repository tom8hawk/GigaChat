package groundbreaking.gigachat.commands;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor @Getter
public abstract class ArgsConstructor {

    private String name, permission;

    abstract public boolean execute(@NotNull CommandSender sender, @NotNull String[] args);
}
