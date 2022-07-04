package de.guntram.mcmod.easierchests;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.guntram.mcmod.easierchests.interfaces.SlotClicker;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EasierChests.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RenderGuiBackgroundHook {
    private static final int PLAYERSLOTS = 36;      // # of slots in player inventory -> so not in container

    @SubscribeEvent
    public static void renderEvent(ContainerScreenEvent.DrawBackground event) {
        AbstractContainerScreen hScreen = event.getContainerScreen();

        if (hScreen instanceof SlotClicker mixinScreen) {

            EditBox searchWidget = mixinScreen.getWidget();
            PoseStack stack = event.getPoseStack();
            int mouseX = event.getMouseX();
            int mouseY = event.getMouseY();
            int leftPos = event.getContainerScreen().getGuiLeft();
            int topPos = event.getContainerScreen().getGuiTop();
            AbstractContainerMenu menu = event.getContainerScreen().getMenu();

            mixinScreen.getButtons().forEach(arrowButton -> arrowButton.draw(stack, mouseX, mouseY));

            if (mixinScreen.isSupportedScreenHandler(menu)) {

                RenderSystem.setShaderTexture(0, ExtendedGuiChest.ICONS);      // because tooltip rendering will have changed the texture to letters
                for (int i = 0; i < PLAYERSLOTS; i++) {
                    if (Screen.hasShiftDown() && FrozenSlotDatabase.isSlotFrozen(i)) {
                        Slot slot = menu.slots.get(mixinScreen.EasierChests$slotIndexfromPlayerInventoryIndex(i));
                        hScreen.blit(stack, leftPos + slot.x, topPos + slot.y, 7 * 18 + 1, 3 * 18 + 1, 16, 16);               // stop sign
                    }
                }

                if (ConfigurationHandler.getInstance().enableSearch() && searchWidget != null) {

                    searchWidget.render(stack, mouseX, mouseY, /*delta*/0);

                    String search = searchWidget.getValue().toLowerCase();
                    if (!search.isEmpty()) {
                        int highlight = (int) Long.parseLong(ConfigurationHandler.getInstance().matchHighlightColor().toUpperCase(), 16);
                        for (int i = 0; i < event.getContainerScreen().getMenu().slots.size(); i++) {
                            Slot slot = event.getContainerScreen().getMenu().slots.get(i);
                            Item item = slot.getItem().getItem();
                            if (item == Items.AIR) {
                                continue;
                            }
                            if (I18n.get(item.getDescriptionId()).toLowerCase().contains(search)) {
                                GuiComponent.fill(stack, leftPos + slot.x - 1, topPos + slot.y - 1, leftPos + slot.x + 18 - 1, topPos + slot.y + 18 - 1, highlight);
                            }
                        }
                    }
                }
            }
        }
    }

}
