package groundbreaking.gigachat.utils.updateschecker;

import groundbreaking.gigachat.GigaChat;
import org.bukkit.configuration.ConfigurationSection;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class UpdatesChecker {

    private final GigaChat plugin;

    public UpdatesChecker(final GigaChat plugin) {
        this.plugin = plugin;
    }

    public void check() {
        final ConfigurationSection updates = this.plugin.getConfig().getConfigurationSection("updates");
        if (!updates.getBoolean("check")) {
            this.plugin.getMyLogger().warning("Updates checker wes disabled, but it's not recommend by the author to do it!");
            return;
        }

        final HttpClient httpClient = HttpClient.newHttpClient();
        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://raw.githubusercontent.com/groundbreakingmc/GigaChat/main/update.txt"))
                .build();

        final CompletableFuture<HttpResponse<String>> responseFuture = httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());

        responseFuture.thenAccept(response -> {
            if (response.statusCode() == 200) {
                final String[] body = response.body().split("\n", 2);
                final String[] versionInfo = body[0].split("->");
                if (this.isHigher(versionInfo[0])) {
                    this.plugin.getMyLogger().info(body[1]);
                    if (!updates.getBoolean("auto-download")) {
                        this.downloadJar(versionInfo[1]);
                    }
                    return;
                }

                this.plugin.getMyLogger().info("\u001b[92mNo updates were found!\u001b[0m");
            } else {
                this.plugin.getMyLogger().warning("\u001b[31mCheck was canceled with response code: \u001b[91m" + response.statusCode() + "\u001b[31m.\u001b[0m");
                this.plugin.getMyLogger().warning("\u001b[31mPlease create an issue \u001b[94https://github.com/groundbreakingmc/GagiChat/issues \u001b[31mand report this error.\u001b[0m");
            }
        }).join();
    }

    private boolean isHigher(final String newVersion) {
        final String pluginVersion = plugin.getDescription().getVersion();
        if (!newVersion.contains("beta")) {
            return true;
        }

        final int currentVersionNum = Integer.parseInt(pluginVersion.replace("-beta", "").replace(".", ""));
        final int newVersionNum = Integer.parseInt(newVersion.replace("-beta", "").replace(".", ""));

        return currentVersionNum < newVersionNum;
    }

    private void downloadJar(final String downloadLink) {
        try {
            final HttpClient client = HttpClient.newHttpClient();
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(downloadLink))
                    .build();

            final HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() == 200) {
                final Path savePath = Paths.get(plugin.getDataFolder().toPath().toString().replace("GigaChat", "update"));
                try (final InputStream inputStream = response.body();
                        final FileOutputStream outputStream = new FileOutputStream(savePath.toFile())) {
                    inputStream.transferTo(outputStream);
                }
            } else {
                this.plugin.getMyLogger().warning("\u001b[31mJar downloading was canceled with response code: \u001b[91m" + response.statusCode() + "\u001b[31m.\u001b[0m");
                this.plugin.getMyLogger().warning("\u001b[31mPlease create an issue \u001b[94https://github.com/groundbreakingmc/GagiChat/issues \u001b[31mand report this error.\u001b[0m");
            }
        } catch (final InterruptedException | IOException ex) {
            ex.printStackTrace();
        }
    }
}
