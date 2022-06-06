package de.guntram.mcmod.easierchests;

import de.guntram.mcmod.easierchests.storagemodapi.ChestGuiInfo;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.File;
import java.util.HashMap;

@Mod(EasierChests.MODID)
public class EasierChests {
    static final String MODID = "easierchests";
    static final String MODNAME = "EasierChests";

    private static final String category = "key.categories.easierchests";

    public static KeyMapping keySortChest, keyMoveToChest,
            keySortPlInv, keyMoveToPlInv,
            keySearchBox;
    public static Logger LOGGER = LogManager.getLogger(EasierChests.class);
    private static HashMap<String, ChestGuiInfo> modHelpers = new HashMap<>();

    public EasierChests() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> "useClientOnly", (ver, remote) -> true));

        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::clientInit);

    }

    public static void registerMod(String screenHandlerClassName, ChestGuiInfo helper) {
        modHelpers.put(screenHandlerClassName, helper);
    }

    public static void registerMod(String modName, String screenHandlerClassName, String helperClassName) {
        try {
            Class.forName(screenHandlerClassName);
            ChestGuiInfo helper = (ChestGuiInfo) Class.forName(helperClassName).getDeclaredConstructor().newInstance();
            registerMod(screenHandlerClassName, helper);
            LOGGER.info("EasierChests enabling support for " + modName);
        } catch (Exception ex) {
            LOGGER.info("EasierChests did not find mod " + modName + ", not enabling support");
        }
    }

    public static ChestGuiInfo getHelperForHandler(AbstractContainerMenu handler) {
        return modHelpers.get(handler.getClass().getCanonicalName());
    }

    public void clientInit(FMLClientSetupEvent clientSetupEvent) {
//        CrowdinTranslate.downloadTranslations(MODID);
        ConfigurationHandler.getInstance();

        FrozenSlotDatabase.init(new File("config"));

        keySortChest = registerKey("sortchest", GLFW.GLFW_KEY_KP_7);
        keyMoveToChest = registerKey("matchup", GLFW.GLFW_KEY_KP_8);
        keySortPlInv = registerKey("sortplayer", GLFW.GLFW_KEY_KP_1);
        keyMoveToPlInv = registerKey("matchdown", GLFW.GLFW_KEY_KP_2);
        keySearchBox = registerKey("searchbox", GLFW.GLFW_KEY_UNKNOWN);

        registerMod("inmis", "draylar.inmis.ui.BackpackScreenHandler", "de.guntram.mcmod.easierchests.storagemodapi.InmisHelper");
        registerMod("Reinforced", "atonkish.reinfcore.screen.ReinforcedStorageScreenHandler", "de.guntram.mcmod.easierchests.storagemodapi.ReinforcedHelper");
        registerMod("Expanded Storage", "ninjaphenix.container_library.api.inventory.AbstractHandler", "de.guntram.mcmod.easierchests.storagemodapi.ExpandedStorageHelper");
    }

    private KeyMapping registerKey(String key, int code) {
        KeyMapping result = new KeyMapping("key.easierchests." + key, code, category);
        ClientRegistry.registerKeyBinding(result);
        return result;
    }
}
