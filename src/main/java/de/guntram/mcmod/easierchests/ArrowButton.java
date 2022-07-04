package de.guntram.mcmod.easierchests;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.guntram.mcmod.easierchests.interfaces.SlotClicker;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraftforge.items.IItemHandler;

import java.awt.*;

public class ArrowButton {

    public final int posX, posY, index;
    public final IItemHandler inventory;
    public final SlotClicker slotClicker;
    public final AbstractContainerScreen screen;
    public final boolean isChest;

    private static int PLAYERINVCOLS = 9;           // let's not make those final; maybe we'll need compatibility
    private static int PLAYERINVROWS = 4;           // with some mod at some point which changes these.
    private final Arrow type;
    private static int SIZE = 18;

    public ArrowButton(int indexAtRowOrColumn, int posX, int posY, IItemHandler inventory, SlotClicker slotClicker, AbstractContainerScreen screen, Arrow type) {
        this.posX = posX;
        this.posY = posY;
        this.index = indexAtRowOrColumn;
        this.inventory = inventory;
        this.slotClicker = slotClicker;
        this.screen = screen;
        this.isChest = type.isChest();
        this.type = type;
    }

    public boolean hovered(int mouseX, int mouseY) {
        Point mouse = new Point(mouseX, mouseY);
        Rectangle button = new Rectangle(posX, posY, SIZE, SIZE);
        return button.contains(mouse);
    }

    public void click() {
        if (type.isSorter())
            ExtendedGuiChest.sortInventory(slotClicker, isChest, inventory);
        if (type.isMatcher())
            ExtendedGuiChest.moveMatchingItems(screen, isChest);

        if (type.isMover()) {
            if (type.isMoverCol())
                clickSlotsInColumn();
            else if (type.isMoverRow())
                clickSlotsInRow();
        }
    }

    public void draw(PoseStack stack, int mouseX, int mouseY) {
        if (type == Arrow.SORT_PLAYER)
            ExtendedGuiChest.drawPlayerInventoryBroom(stack, screen, posX, posY, mouseX, mouseY);
        if (slotClicker.isSupportedScreenHandler(screen.getMenu())) {
            if (type == Arrow.SORT_CHEST)
                ExtendedGuiChest.drawChestInventoryBroom(stack, screen, posX, posY, mouseX, mouseY);
            else if (type == Arrow.MATCH_PLAYER)
                ExtendedGuiChest.drawPlayerInventoryAllUp(stack, screen, posX, posY, mouseX, mouseY);
            else if (type == Arrow.MATCH_CHEST)
                ExtendedGuiChest.drawChestInventoryAllDown(stack, screen, posX, posY, mouseX, mouseY);
            else if (type.isMover()) {
                GlStateManager._enableBlend();
                RenderSystem.setShaderTexture(0, ExtendedGuiChest.ICONS);
                int i = type.isChest() ? 1 : 9;
                ExtendedGuiChest.drawTexturedModalRectWithMouseHighlight(screen, stack, posX, posY, i * SIZE, 2 * SIZE, SIZE, SIZE, mouseX, mouseY);       // arrow down above chests
                GlStateManager._disableBlend();
            }
        }
    }

    private void clickSlotsInRow() {
        int cols = slotClicker.getSlotColumnCount();
        int rows = slotClicker.getSlotRowCountAll();

        int firstSlot;

        if (isChest) {      // we're in the chest
            firstSlot = index * cols;
        } else {
            firstSlot = rows * cols + (index - rows) * PLAYERINVCOLS;
            cols = PLAYERINVCOLS;
        }

        for (int slot = firstSlot; slot < firstSlot + cols; slot++)
            if (Screen.hasShiftDown() || !FrozenSlotDatabase.isSlotFrozen(slotClicker.EasierChests$playerInventoryIndexFromSlotIndex(slot))) {
                slotClick(slot, 0, ClickType.QUICK_MOVE);
            }

    }

    private void clickSlotsInColumn() {
        int cols = slotClicker.getSlotColumnCount();
        int rows = slotClicker.getSlotRowCountAll();
        int first, count;

        if (isChest) {
            first = index;
            count = rows;
        } else {
            first = rows * cols + index;
            count = PLAYERINVROWS;
            cols = PLAYERINVCOLS;
        }
        for (int i = 0; i < count; i++) {
            int slot = first + i * cols;
            if (Screen.hasShiftDown() || !FrozenSlotDatabase.isSlotFrozen(slotClicker.EasierChests$playerInventoryIndexFromSlotIndex(slot)))
                slotClick(slot, 0, ClickType.QUICK_MOVE);
        }
    }

    private void slotClick(int slot, int mouseButton, ClickType clickType) {
        slotClicker.EasierChests$onMouseClick(null, slot, mouseButton, clickType);
    }

    public enum Arrow {
        SORT_CHEST,
        SORT_PLAYER,
        MATCH_CHEST,
        MATCH_PLAYER,
        MOVE_CHEST_ROW, MOVE_CHEST_COL,
        MOVE_PLAYER_ROW, MOVE_PLAYER_COL;

        public boolean isSorter() {
            return switch (this) {
                case SORT_PLAYER, SORT_CHEST -> true;
                default -> false;
            };
        }

        public boolean isChest() {
            return switch (this) {
                case MATCH_CHEST, MOVE_CHEST_COL, MOVE_CHEST_ROW, SORT_CHEST -> true;
                default -> false;
            };
        }

        public boolean isMatcher() {
            return switch (this) {
                case MATCH_CHEST, MATCH_PLAYER -> true;
                default -> false;
            };
        }

        public boolean isMover() {
            return isMoverRow() || isMoverCol();
        }

        public boolean isMoverCol() {
            return switch (this) {
                case MOVE_CHEST_COL, MOVE_PLAYER_COL -> true;
                default -> false;
            };
        }

        public boolean isMoverRow() {
            return switch (this) {
                case MOVE_CHEST_ROW, MOVE_PLAYER_ROW -> true;
                default -> false;
            };
        }
    }


}
