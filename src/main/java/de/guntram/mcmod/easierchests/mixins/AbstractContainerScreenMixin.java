package de.guntram.mcmod.easierchests.mixins;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.guntram.mcmod.easierchests.ConfigurationHandler;
import de.guntram.mcmod.easierchests.EasierChests;
import de.guntram.mcmod.easierchests.ExtendedGuiChest;
import de.guntram.mcmod.easierchests.FrozenSlotDatabase;
import de.guntram.mcmod.easierchests.interfaces.SlotClicker;
import de.guntram.mcmod.easierchests.storagemodapi.ChestGuiInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen implements SlotClicker {

    private static final int PLAYERSLOTS = 36;      // # of slots in player inventory -> so not in container
    private static int PLAYERINVCOLS = 9;           // let's not make those final; maybe we'll need compatibility
    private static int PLAYERINVROWS = 4;           // with some mod at some point which changes these.

    private static EditBox searchWidget;
    @Shadow
    @Final
    protected AbstractContainerMenu menu;
    @Shadow
    protected int leftPos, topPos, imageWidth, imageHeight;
    private boolean loggedScreenHandlerClass = false;

    protected AbstractContainerScreenMixin() {
        super(null);
    }

    @Shadow
    protected void slotClicked(Slot slot, int invSlot, int button, ClickType slotActionType) {
    }

    @Shadow
    protected void renderTooltip(PoseStack matrices, int x, int y) {
    }

    @Shadow
    protected boolean isHovering(int x, int y, int w, int h, double pX, double pY) {
        return true;
    }

    @Override
    public void EasierChests$onMouseClick(Slot slot, int invSlot, int button, ClickType slotActionType) {
        this.slotClicked(slot, invSlot, button, slotActionType);
    }

    @Override
    public int EasierChests$getPlayerInventoryStartIndex() {
        if (menu instanceof InventoryMenu) {
            return PLAYERINVCOLS;
        } else {
            return this.menu.slots.size() - PLAYERSLOTS;
        }
    }

    @Override
    public int EasierChests$playerInventoryIndexFromSlotIndex(int slot) {
        int firstSlot = EasierChests$getPlayerInventoryStartIndex();
        if (slot < firstSlot) {
            return -1;
        } else if (slot < firstSlot + (PLAYERSLOTS - PLAYERINVCOLS)) {
            return slot - firstSlot + PLAYERINVCOLS;
        } else {
            return slot - firstSlot - (PLAYERSLOTS - PLAYERINVCOLS);
        }
    }

    @Override
    public int EasierChests$slotIndexfromPlayerInventoryIndex(int slot) {
        int firstSlot = EasierChests$getPlayerInventoryStartIndex();
        if (slot < PLAYERINVCOLS) {
            return slot + firstSlot + (PLAYERSLOTS - PLAYERINVCOLS);
        } else {
            return slot + firstSlot - PLAYERINVCOLS;
        }
    }

    @Inject(method = "renderSlot", at = @At("RETURN"))
    public void EasierChests$DrawSlotIndex(PoseStack stack, Slot slot, CallbackInfo ci) {
        if (hasAltDown()) {
            this.font.draw(stack, Integer.toString(slot.index), slot.x, slot.y, 0x808090);
        }
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableDepthTest()V"))
    public void EasierChests$renderSpecialButtons(PoseStack stack, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        AbstractContainerScreen hScreen = (AbstractContainerScreen) (Object) this;

        ExtendedGuiChest.drawPlayerInventoryBroom(stack, hScreen, leftPos + imageWidth, topPos + imageHeight - 30 - 3 * 18, mouseX, mouseY);
        if (isSupportedScreenHandler(menu)) {

            int cols = getSlotColumnCount();
            int rows = getSlotRowCount();

            GlStateManager._enableBlend();
            RenderSystem.setShaderTexture(0, ExtendedGuiChest.ICONS);

            if (ConfigurationHandler.getInstance().enableColumnButtons()) {
                int startx = (leftPos + imageWidth / 2) - (18 / 2) * cols;
                for (int i = 0; i < cols; i++) {
                    ExtendedGuiChest.drawTexturedModalRectWithMouseHighlight(hScreen, stack, startx + i * 18, topPos + -18, 1 * 18, 2 * 18, 18, 18, mouseX, mouseY);       // arrow down above chests
                }
                startx = (leftPos + imageWidth / 2) - 9 * PLAYERINVCOLS;
                for (int i = 0; i < PLAYERINVCOLS; i++) {
                    ExtendedGuiChest.drawTexturedModalRectWithMouseHighlight(hScreen, stack, startx + i * 18, topPos + 40 + (rows + 4) * 18, 9 * 18, 2 * 18, 18, 18, mouseX, mouseY);       // arrow up below player inv
                }
            }

            if (ConfigurationHandler.getInstance().enableRowButtons()) {
                for (int i = 0; i < rows; i++) {
                    ExtendedGuiChest.drawTexturedModalRectWithMouseHighlight(hScreen, stack, leftPos + -18, topPos + 17 + i * 18, 1 * 18, 2 * 18, 18, 18, mouseX, mouseY);       // arrow down left of chest
                }
                for (int i = 0; i < PLAYERINVROWS; i++) {
                    ExtendedGuiChest.drawTexturedModalRectWithMouseHighlight(hScreen, stack, leftPos + -18, topPos + 28 + (i + rows) * 18, 9 * 18, 2 * 18, 18, 18, mouseX, mouseY);       // arrow up left of player inv
                }
            }

            GlStateManager._disableBlend();
            RenderSystem.setShaderTexture(0, ExtendedGuiChest.ICONS);      // because tooltip rendering will have changed the texture to letters
            for (int i = 0; i < PLAYERSLOTS; i++) {
                if (!hasShiftDown() && FrozenSlotDatabase.isSlotFrozen(i)) {
                    Slot slot = this.menu.slots.get(EasierChests$slotIndexfromPlayerInventoryIndex(i));
                    this.blit(stack, leftPos + slot.x, topPos + slot.y, 7 * 18 + 1, 3 * 18 + 1, 16, 16);               // stop sign
                }
            }

            if (ConfigurationHandler.getInstance().enableSearch()) {
                if (searchWidget == null) {
                    searchWidget = new EditBox(font, leftPos + imageWidth - 85, topPos + 3, 80, 12, Component.literal("Search"));
                } else {
                    searchWidget.x = leftPos + imageWidth - 85;
                    searchWidget.y = topPos + 3;
                }
                searchWidget.render(stack, mouseX, mouseY, delta);

                String search = searchWidget.getValue().toLowerCase();
                if (!search.isEmpty()) {
                    int highlight = (int) Long.parseLong(ConfigurationHandler.getInstance().matchHighlightColor().toUpperCase(), 16);
                    for (int i = 0; i < this.menu.slots.size(); i++) {
                        Slot slot = this.menu.slots.get(i);
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
            ExtendedGuiChest.drawPlayerInventoryAllUp(stack, hScreen, leftPos + imageWidth, topPos + imageHeight - 30 - 2 * 18, mouseX, mouseY);
            ExtendedGuiChest.drawChestInventoryBroom(stack, hScreen, leftPos + imageWidth, topPos + 17, mouseX, mouseY);
            ExtendedGuiChest.drawChestInventoryAllDown(stack, hScreen, leftPos + this.imageWidth, topPos + 17 + 18, mouseX, mouseY);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void EasierChests$checkMyButtons(double mouseX, double mouseY, int mouseButton, CallbackInfoReturnable cir) {

        if (isSupportedScreenHandler(menu)
                && ConfigurationHandler.getInstance().enableSearch()
                && searchWidget.mouseClicked(mouseX, mouseY, mouseButton)) {
            cir.setReturnValue(true);
            cir.cancel();
            return;
        }

        if (mouseX >= leftPos + imageWidth && mouseX <= leftPos + imageWidth + 18) {
            AbstractContainerScreen hScreen = (AbstractContainerScreen) (Screen) this;
            if (mouseY >= topPos + imageHeight - 30 - 3 * 18 && mouseY < topPos + imageHeight - 30 - 2 * 18) {
                ExtendedGuiChest.sortInventory(this, false, Minecraft.getInstance().player.getInventory());
                cir.setReturnValue(true);
                cir.cancel();
                return;
            } else if (!isSupportedScreenHandler(menu)) {
                return;
            } else if (mouseY >= topPos + imageHeight - 30 - 3 * 18 && mouseY < topPos + imageHeight - 30 - 1 * 18) {
                ExtendedGuiChest.moveMatchingItems(hScreen, false);
                cir.setReturnValue(true);
                cir.cancel();
                return;
            } else if (mouseY > topPos + 17 && mouseY < topPos + 17 + 18) {
                ExtendedGuiChest.sortInventory(this, true, menu.getSlot(0).container);
                cir.setReturnValue(true);
                cir.cancel();
                return;
            } else if (mouseY > topPos + 17 + 18 && mouseY < topPos + 17 + 36) {
                ExtendedGuiChest.moveMatchingItems(hScreen, true);
                cir.setReturnValue(true);
                cir.cancel();
                return;
            }
        }
        if (!isSupportedScreenHandler(menu)) {
            return;
        }
        if (mouseButton == 0 && checkForMyButtons(mouseX, mouseY)) {
            cir.setReturnValue(true);
            cir.cancel();
            return;
        }
        if (mouseButton == 2 && checkForToggleFrozen(mouseX, mouseY)) {
            cir.setReturnValue(true);
            cir.cancel();
            return;
        }
    }

    private boolean checkForMyButtons(double mouseX, double mouseY) {
        int rows = getSlotRowCount();
        int cols = getSlotColumnCount();

        if (ConfigurationHandler.getInstance().enableRowButtons() && mouseX >= leftPos - 18 && mouseX <= leftPos) { // left buttons
            int deltay = (int) mouseY - topPos;
            if (deltay < rows * 18 + 17) {                                          // chest -> down
                clickSlotsInRow((deltay - 17) / 18);
                return true;
            } else if (deltay < (rows + PLAYERINVROWS) * 18 + 28) {              // inv -> up
                clickSlotsInRow((deltay - 28) / 18);
                return true;
            }
        }
        if (ConfigurationHandler.getInstance().enableColumnButtons() && mouseX > leftPos + 7 && mouseX < leftPos + imageWidth) { // top/bottom buttons
            boolean isChest;
            int column;
            if (mouseY > topPos - 18 && mouseY < topPos) {                                      // top -> down
                int startx = leftPos + imageWidth / 2 - (18 / 2) * cols;
                isChest = true;
                column = ((int) mouseX - startx) / 18;
                if (column < 0 || column >= cols) {
                    return false;
                }
            } else if (mouseY > topPos + 40 + (rows + PLAYERINVROWS) * 18 && mouseY < topPos + 40 + (rows + PLAYERINVROWS) * 18 + 18) {
                int startx = leftPos + imageWidth / 2 - (18 / 2) * PLAYERINVCOLS;
                isChest = false;
                column = ((int) mouseX - startx) / 18;
                if (column < 0 || column > PLAYERINVCOLS) {
                    return false;
                }
            } else {
                return false;
            }
            clickSlotsInColumn(column, isChest);
            return true;
        }
        return false;
    }

    private boolean checkForToggleFrozen(double mouseX, double mouseY) {
        for (int i = 0; i < this.menu.slots.size(); ++i) {
            int invIndex = this.EasierChests$playerInventoryIndexFromSlotIndex(i);
            if (invIndex == -1)
                continue;
            Slot slot = this.menu.slots.get(i);
            if (isHovering(slot.x, slot.y, 16, 16, mouseX, mouseY)) {
                FrozenSlotDatabase.setSlotFrozen(invIndex, !FrozenSlotDatabase.isSlotFrozen(invIndex));
                return true;
            }
        }
        return false;
    }

    private void clickSlotsInRow(int row) {
        int rows = getSlotRowCount();
        int cols = getSlotColumnCount();
        int firstSlot;

        if (row <= rows) {      // we're in the chest
            firstSlot = row * cols;
        } else {
            firstSlot = rows * cols + (row - rows) * PLAYERINVCOLS;
            cols = PLAYERINVCOLS;
        }

        for (int slot = firstSlot; slot < firstSlot + cols; slot++)
            if (hasShiftDown() || !FrozenSlotDatabase.isSlotFrozen(EasierChests$playerInventoryIndexFromSlotIndex(slot))) {
                slotClick(slot, 0, ClickType.QUICK_MOVE);
            }

    }

    private void clickSlotsInColumn(int column, boolean isChest) {
        int cols = getSlotColumnCount();
        int rows = getSlotRowCount();
        int first, count;

        if (isChest) {
            first = column;
            count = rows;
        } else {
            first = rows * cols + column;
            count = PLAYERINVROWS;
            cols = PLAYERINVCOLS;
        }
        for (int i = 0; i < count; i++) {
            int slot = first + i * cols;
            if (hasShiftDown() || !FrozenSlotDatabase.isSlotFrozen(EasierChests$playerInventoryIndexFromSlotIndex(slot)))
                slotClick(slot, 0, ClickType.QUICK_MOVE);
        }
    }

    private void slotClick(int slot, int mouseButton, ClickType clickType) {
        ((SlotClicker) this).EasierChests$onMouseClick(null, slot, mouseButton, clickType);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void EasierChests$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable cir) {
        AbstractContainerScreen hScreen = (AbstractContainerScreen) (Screen) this;

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return;
        }

        if (isSupportedScreenHandler(menu)
                && ConfigurationHandler.getInstance().enableSearch()
                && searchWidget.canConsumeInput()) {
            boolean value = searchWidget.keyPressed(keyCode, scanCode, modifiers);
            cir.setReturnValue(value);
            cir.cancel();
            return;
        }

        if (EasierChests.keySortPlInv.matches(keyCode, scanCode)) {
            ExtendedGuiChest.sortInventory(this, false, Minecraft.getInstance().player.getInventory());
            cir.setReturnValue(true);
            cir.cancel();
        } else if (!isSupportedScreenHandler(menu)) {
            return;
        } else if (EasierChests.keyMoveToChest.matches(keyCode, scanCode)) {
            ExtendedGuiChest.moveMatchingItems(hScreen, false);
            cir.setReturnValue(true);
            cir.cancel();
        } else if (EasierChests.keySortChest.matches(keyCode, scanCode)) {
            ExtendedGuiChest.sortInventory(this, true, menu.getSlot(0).container);
            cir.setReturnValue(true);
            cir.cancel();
        } else if (EasierChests.keyMoveToPlInv.matches(keyCode, scanCode)) {
            ExtendedGuiChest.moveMatchingItems(hScreen, true);
            cir.setReturnValue(true);
            cir.cancel();
        } else if (EasierChests.keySearchBox.matches(keyCode, scanCode)) {
            ConfigurationHandler.getInstance().toggleSearchBox();
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (isSupportedScreenHandler(menu)
                && ConfigurationHandler.getInstance().enableSearch()
                && searchWidget.canConsumeInput()) {
            return searchWidget.charTyped(chr, keyCode);
        }
        return super.charTyped(chr, keyCode);
    }

    public boolean isSupportedScreenHandler(AbstractContainerMenu handler) {
        if (handler == null) {      // can this happen? Make IDE happy
            return false;
        }

        if (handler instanceof ChestMenu || handler instanceof ShulkerBoxMenu) {
            return true;
        }

        if (EasierChests.getHelperForHandler(handler) != null) {
            return true;
        }

        if (!loggedScreenHandlerClass && !handler.getClass().getSimpleName().startsWith("class_")) {    // don't log MC internal classes
            LogManager.getLogger(this.getClass()).info("opening class " + handler.getClass().getSimpleName() + "/" + handler.getClass().getCanonicalName());
            loggedScreenHandlerClass = true;
        }
        return false;
    }

    /**
     * Gets the number of inventory rows in the Chest inventory.
     * This does not include the PLAYERINVROWS rows in the player inventory.
     *
     * @return the number of inventory rows
     */

    public int getSlotRowCount() {
        int size = menu.slots.size() - PLAYERSLOTS;
        if (ConfigurationHandler.getInstance().largeChests()) {
            ChestGuiInfo helper = EasierChests.getHelperForHandler(menu);
            if (helper != null) {
                int cols = helper.getRows((AbstractContainerScreen<?>) (Object) this);
                if (cols != -1) {
                    return cols;
                }
            }
            return size / getSlotColumnCount();
        }
        return Math.min(6, size / PLAYERINVCOLS);
    }

    public int getSlotColumnCount() {
        int size = menu.slots.size() - PLAYERSLOTS;
        if (ConfigurationHandler.getInstance().largeChests()) {
            ChestGuiInfo helper = EasierChests.getHelperForHandler(menu);
            if (helper != null) {
                int rows = helper.getColumns((AbstractContainerScreen<?>) (Object) this);
                if (rows != -1) {
                    return rows;
                }
            }
            return (size <= 81 ? PLAYERINVCOLS : size / PLAYERINVCOLS);
        }
        return PLAYERINVCOLS;
    }
}
