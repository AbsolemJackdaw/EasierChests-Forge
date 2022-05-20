package de.guntram.mcmod.easierchests;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class ConfigurationHandler {

    public static final Codec<ConfigurationHandler> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(
                    Codec.BOOL.fieldOf("largeChests").forGetter(configurationHandler -> configurationHandler.largeChests),
                    Codec.BOOL.fieldOf("halfSizeButtons").forGetter(configurationHandler -> configurationHandler.halfSizeButtons),
                    Codec.BOOL.fieldOf("transparent").forGetter(configurationHandler -> configurationHandler.transparent),
                    Codec.BOOL.fieldOf("enableSearch").forGetter(configurationHandler -> configurationHandler.enableSearch),
                    Codec.BOOL.fieldOf("enableRowButtons").forGetter(configurationHandler -> configurationHandler.enableRowButtons),
                    Codec.BOOL.fieldOf("enableColumnButtons").forGetter(configurationHandler -> configurationHandler.enableColumnButtons),
                    Codec.STRING.fieldOf("matchHighlightColor").forGetter(configurationHandler -> configurationHandler.matchHighlightColor)
            ).apply(builder, ConfigurationHandler::new));

    private static ConfigurationHandler DEFAULT = new ConfigurationHandler(false, false, true, true, true, true, "4000ff00");


    private static ConfigurationHandler instance;

    private final boolean largeChests;
    private final boolean halfSizeButtons;
    private final boolean transparent;
    private boolean enableSearch;
    private final boolean enableRowButtons;
    private final boolean enableColumnButtons;
    private final String matchHighlightColor;

    public ConfigurationHandler(boolean largeChests,
                                boolean halfSizeButtons,
                                boolean transparent,
                                boolean enableSearch,
                                boolean enableRowButtons,
                                boolean enableColumnButtons,
                                String matchHighlightColor) {
        this.largeChests = largeChests;
        this.halfSizeButtons = halfSizeButtons;
        this.transparent = transparent;
        this.enableSearch = enableSearch;
        this.enableRowButtons = enableRowButtons;
        this.enableColumnButtons = enableColumnButtons;
        this.matchHighlightColor = matchHighlightColor;
    }


    public static ConfigurationHandler getInstance() {
        return getInstance(false, false);
    }

    public static ConfigurationHandler getInstance(boolean serialize, boolean recreate) {
        if (instance == null || serialize || recreate) {
            instance = readConfig(recreate);
        }

        return instance;
    }

    private static ConfigurationHandler readConfig(boolean recreate) {
        final Path path = FMLPaths.CONFIGDIR.get().resolve("easierchests.json");

        if (!path.toFile().exists() || recreate) {
            JsonElement jsonElement = CODEC.encodeStart(JsonOps.INSTANCE, DEFAULT).result().get();

            try {
                Files.createDirectories(path.getParent());
                Files.write(path, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(jsonElement).getBytes());
            } catch (IOException e) {
                EasierChests.LOGGER.error(e.toString());
            }
        }
        EasierChests.LOGGER.info(String.format("\"%s\" was read.", path.toString()));

        try {
            return CODEC.decode(JsonOps.INSTANCE, new JsonParser().parse(new FileReader(path.toFile()))).result().orElseThrow(RuntimeException::new).getFirst();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return DEFAULT;
    }

    public boolean largeChests() {
        return largeChests;
    }

    public boolean halfSizeButtons() {
        return halfSizeButtons;
    }

    public boolean transparent() {
        return transparent;
    }

    public boolean enableSearch() {
        return enableSearch;
    }

    public boolean enableRowButtons() {
        return enableRowButtons;
    }

    public boolean enableColumnButtons() {
        return enableColumnButtons;
    }

    public String matchHighlightColor() {
        return matchHighlightColor;
    }

    public static void toggleSearchBox() {
        getInstance().enableSearch = !getInstance().enableSearch;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ConfigurationHandler) obj;
        return this.largeChests == that.largeChests &&
                this.halfSizeButtons == that.halfSizeButtons &&
                this.transparent == that.transparent &&
                this.enableSearch == that.enableSearch &&
                this.enableRowButtons == that.enableRowButtons &&
                this.enableColumnButtons == that.enableColumnButtons &&
                Objects.equals(this.matchHighlightColor, that.matchHighlightColor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(largeChests, halfSizeButtons, transparent, enableSearch, enableRowButtons, enableColumnButtons, matchHighlightColor);
    }

    @Override
    public String toString() {
        return "ConfigurationHandler[" +
                "largeChests=" + largeChests + ", " +
                "halfSizeButtons=" + halfSizeButtons + ", " +
                "transparent=" + transparent + ", " +
                "enableSearch=" + enableSearch + ", " +
                "enableRowButtons=" + enableRowButtons + ", " +
                "enableColumnButtons=" + enableColumnButtons + ", " +
                "matchHighlightColor=" + matchHighlightColor + ']';
    }


}