package de.guntram.mcmod.easierchests.mixins;

import com.mojang.blaze3d.vertex.PoseStack;
import de.guntram.mcmod.easierchests.*;
import de.guntram.mcmod.easierchests.interfaces.SlotClicker;
import de.guntram.mcmod.easierchests.storagemodapi.ChestGuiInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.inventory.*;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen implements SlotClicker {

    private static final int PLAYERSLOTS = 36;      // # of slots in player inventory -> so not in container
    private static int PLAYERINVCOLS = 9;           // let's not make those final; maybe we'll need compatibility
    private static int PLAYERINVROWS = 4;           // with some mod at some point which changes these.

    private EditBox searchWidget;
    private EditBox fallBack = new EditBox(font, 0, 0, 0, 0, TextComponent.EMPTY);
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

    @Shadow
    protected int slotColor;

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

    @Inject(method = "init", at = @At("RETURN"))
    protected void injectIntoInit(CallbackInfo ci) {
        fallBack.active = fallBack.visible = false;
        makeSearchBox();
        makeArrows();
    }

    private List<ArrowButton> arrows = new ArrayList<>();

    private void makeArrows() {
        arrows.clear();
        Slot slot = menu.getSlot(0);
        IItemHandler chestInventory = slot instanceof SlotItemHandler handler ? handler.getItemHandler() : new InvWrapper(slot.container);
        IItemHandler playerInventory = new InvWrapper(minecraft.player.getInventory());
        AbstractContainerScreen acs = (AbstractContainerScreen) (Object) this;
        if (ConfigurationHandler.getInstance().enableColumnButtons()) {

            //column arrows chest
            int startx = (leftPos + imageWidth / 2) - (18 / 2) * this.getSlotColumnCount();

            for (int i = 0; i < this.getSlotColumnCount(); i++) {
                arrows.add(new ArrowButton(i, startx + i * 18, topPos + -18, chestInventory, this, acs, ArrowButton.Arrow.MOVE_CHEST_COL));
            }
            //column arrows player inventory
            startx = (leftPos + imageWidth / 2) - 9 * 9;
            for (int i = 0; i < PLAYERINVCOLS; i++) {
                arrows.add(new ArrowButton(i, startx + i * 18, topPos + 40 + (this.getSlotRowCount() + 4) * 18, playerInventory, this, acs, ArrowButton.Arrow.MOVE_PLAYER_COL));
            }
        }

        if (ConfigurationHandler.getInstance().enableRowButtons()) {
            //base player index buttons on actual size of inventory. the given amount by the interface is the visible amount of slots
            int actualRows = getSlotRowCountAll();
            for (int i = 0; i < PLAYERINVROWS; i++) {
                arrows.add(new ArrowButton(i + actualRows, leftPos + -18, topPos + 28 + (i + this.getSlotRowCount()) * 18, playerInventory, this, acs, ArrowButton.Arrow.MOVE_PLAYER_ROW));
            }

            int modOffset = getArrowOffset();

            for (int i = 0; i < actualRows/*this.getSlotRowCount()*/; i++) {
                arrows.add(new ArrowButton(i, leftPos + -18 - modOffset, topPos + 17 + i * 18, chestInventory, this, acs, ArrowButton.Arrow.MOVE_CHEST_ROW));
            }
        }

        arrows.add(new ArrowButton(0, leftPos + imageWidth, topPos + imageHeight - 30 - 2 * 18, chestInventory, this, acs, ArrowButton.Arrow.MATCH_PLAYER));
        arrows.add(new ArrowButton(0, leftPos + imageWidth, topPos + 17 + 18, chestInventory, this, acs, ArrowButton.Arrow.MATCH_CHEST));
        arrows.add(new ArrowButton(0, leftPos + imageWidth, topPos + 17, chestInventory, this, acs, ArrowButton.Arrow.SORT_CHEST));
        arrows.add(new ArrowButton(0, leftPos + imageWidth, topPos + imageHeight - 30 - 3 * 18, chestInventory, this, acs, ArrowButton.Arrow.SORT_PLAYER));

    }

    private void makeSearchBox() {
        if (ConfigurationHandler.getInstance().enableSearch()) {
            if (searchWidget == null) {
                searchWidget = new EditBox(font, leftPos + imageWidth - 85, topPos + 3, 80, 12, new TextComponent("Search"));
            } else {
                searchWidget.x = leftPos + imageWidth - 85;
                searchWidget.y = topPos + 3;
            }
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void EasierChests$checkMyButtons(double mouseX, double mouseY, int mouseButton, CallbackInfoReturnable cir) {

        if (isSupportedScreenHandler(menu)
                && ConfigurationHandler.getInstance().enableSearch()
                && searchWidget != null && searchWidget.mouseClicked(mouseX, mouseY, mouseButton)) {
            cir.setReturnValue(true);
            cir.cancel();
            return;
        }
        for (ArrowButton arrow : arrows) {
            if (arrow.hovered((int) mouseX, (int) mouseY)) {
                arrow.click();
                cir.setReturnValue(true);
                cir.cancel();
                break;
            }
        }

        if (!isSupportedScreenHandler(menu)) {
            return;
        }
        if (Screen.hasShiftDown() && mouseButton == 2 && checkForToggleFrozen(mouseX, mouseY)) {
            cir.setReturnValue(true);
            cir.cancel();
            return;
        }
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


    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void EasierChests$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable cir) {
        AbstractContainerScreen hScreen = (AbstractContainerScreen) (Screen) this;

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return;
        }

        if (isSupportedScreenHandler(menu)
                && ConfigurationHandler.getInstance().enableSearch()
                && searchWidget != null && searchWidget.canConsumeInput()) {
            boolean value = searchWidget.keyPressed(keyCode, scanCode, modifiers);
            cir.setReturnValue(value);
            cir.cancel();
            return;
        }

        if (EasierChests.keySortPlInv.matches(keyCode, scanCode)) {
            ExtendedGuiChest.sortInventory(this, false, new InvWrapper(Minecraft.getInstance().player.getInventory()));
            cir.setReturnValue(true);
            cir.cancel();
        } else if (!isSupportedScreenHandler(menu)) {
            return;
        } else if (EasierChests.keyMoveToChest.matches(keyCode, scanCode)) {
            ExtendedGuiChest.moveMatchingItems(hScreen, false);
            cir.setReturnValue(true);
            cir.cancel();
        } else if (EasierChests.keySortChest.matches(keyCode, scanCode)) {
            Slot slot = menu.getSlot(0);
            IItemHandler inv = slot instanceof SlotItemHandler handler ? handler.getItemHandler() : new InvWrapper(slot.container);
            ExtendedGuiChest.sortInventory(this, true, inv);
            cir.setReturnValue(true);
            cir.cancel();
        } else if (EasierChests.keyMoveToPlInv.matches(keyCode, scanCode)) {
            ExtendedGuiChest.moveMatchingItems(hScreen, true);
            cir.setReturnValue(true);
            cir.cancel();
        } else if (EasierChests.keySearchBox.matches(keyCode, scanCode)) {
            ConfigurationHandler.getInstance().toggleSearchBox();
            if (ConfigurationHandler.getInstance().enableSearch())
                makeSearchBox();
            else searchWidget = null;
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (isSupportedScreenHandler(menu)
                && ConfigurationHandler.getInstance().enableSearch()
                && searchWidget != null && searchWidget.canConsumeInput()) {
            return searchWidget.charTyped(chr, keyCode);
        }
        return super.charTyped(chr, keyCode);
    }

    @Override
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
    @Override
    public int getSlotRowCount() {
        int size = menu.slots.size() - PLAYERSLOTS;
        if (ConfigurationHandler.getInstance().largeChests()) {
            ChestGuiInfo helper = EasierChests.getHelperForHandler(menu);
            if (helper != null) {
                int rows = helper.getRows((AbstractContainerScreen<?>) (Object) this);
                if (rows != -1) {
                    return rows;
                }
            }
            return size / getSlotColumnCount();
        }
        return Math.min(6, size / PLAYERINVCOLS);
    }

    @Override
    public int getSlotRowCountAll() {
        if (ConfigurationHandler.getInstance().largeChests()) {
            ChestGuiInfo helper = EasierChests.getHelperForHandler(menu);
            if (helper != null) {
                int rows = helper.getRowsAll((AbstractContainerScreen<?>) (Object) this);
                if (rows != -1)
                    return rows;
            }
        }
        return getSlotRowCount();
    }

    @Override
    public int getSlotColumnCount() {
        int size = menu.slots.size() - PLAYERSLOTS;
        if (ConfigurationHandler.getInstance().largeChests()) {
            ChestGuiInfo helper = EasierChests.getHelperForHandler(menu);
            if (helper != null) {
                int cols = helper.getColumns((AbstractContainerScreen<?>) (Object) this);
                if (cols != -1) {
                    return cols;
                }
            }
            return (size <= 81 ? PLAYERINVCOLS : size / PLAYERINVCOLS);
        }
        return PLAYERINVCOLS;
    }

    @Override
    public EditBox getWidget() {
        return searchWidget == null ? fallBack : searchWidget;
    }

    @Override
    public int getArrowOffset() {
        ChestGuiInfo helper = EasierChests.getHelperForHandler(menu);
        return helper == null ? 0 : helper.getArrowOffset();
    }

    @Override
    public List<ArrowButton> getButtons() {
        return arrows;
    }
}
