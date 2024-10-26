package groundbreaking.gigachat.constructors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor @Getter
public abstract class ArgsConstructor {

    private final String name;
    private final String permission;

    public abstract boolean execute(@NotNull CommandSender sender, @NotNull String[] args);
}
