package de.guntram.mcmod.easierchests.mixins;

import de.guntram.mcmod.easierchests.ExtendedGuiChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MenuScreens.class)
public class GuiContainerMixin {
    
    private static final Logger LOGGER = LogManager.getLogger();

    @Inject(method="create", at=@At("HEAD"), cancellable = true)
    private static void checkChestScreen(MenuType type, Minecraft client, 
            int any, Component component, CallbackInfo ci) {
        LOGGER.debug("Trying to open container: "+type+" with name "+component.getString());
        if (type == MenuType.GENERIC_9x1 
        ||  type == MenuType.GENERIC_9x2
        ||  type == MenuType.GENERIC_9x3
        ||  type == MenuType.GENERIC_9x4
        ||  type == MenuType.GENERIC_9x5
        ||  type == MenuType.GENERIC_9x6) {
            ChestMenu container = (ChestMenu) type.create(any, client.player.getInventory());
            ExtendedGuiChest screen = new ExtendedGuiChest(container, 
                    client.player.getInventory(), component,
                    container.getRowCount());
            client.player.containerMenu = container;
            client.setScreen(screen);
            LOGGER.debug("(my chest)");
            ci.cancel();
        } else if (type == MenuType.SHULKER_BOX) {
            ShulkerBoxMenu container = MenuType.SHULKER_BOX.create(any, client.player.getInventory());
            ExtendedGuiChest screen = new ExtendedGuiChest(container,
                    client.player.getInventory(), component);
            client.player.containerMenu = container;
            client.setScreen(screen);
            LOGGER.debug("(my shulker)");
            ci.cancel();
        } else {
            LOGGER.debug("(not me)");
        }
    }
}
