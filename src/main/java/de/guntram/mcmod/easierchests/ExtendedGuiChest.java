package de.guntram.mcmod.easierchests;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.guntram.mcmod.easierchests.interfaces.SlotClicker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.extensions.IForgeItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

/*
 * Warning - this code should extend ContainerScreen54 AND ShulkerBoxScreen,
 * which it can't. So we extend the superclass, and implement the few methods
 * that are in those classes (and are identical ...) ourselves. Doh.
 */

public class ExtendedGuiChest extends AbstractContainerScreen {
    private final int inventoryRows;
    public static final ResourceLocation ICONS = new ResourceLocation(EasierChests.MODID, "textures/icons.png");
    private final ResourceLocation background;
    private final boolean separateBlits;

    public ExtendedGuiChest(ChestMenu container, Inventory lowerInv, Component title,
                            int rows) {
        super(container, lowerInv, title);
        this.inventoryRows = rows;
        imageHeight = 114 + rows * 18;
        background = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");
        separateBlits = true;
    }

    public ExtendedGuiChest(ShulkerBoxMenu container, Inventory lowerInv, Component title) {
        super(container, lowerInv, title);
        inventoryRows = 3;
        background = new ResourceLocation("minecraft", "textures/gui/container/shulker_box.png");
        separateBlits = false;
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);
        renderTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
        this.font.draw(stack, this.title.getString(), 8.0F, 6.0F, 4210752);
        this.font.draw(stack, this.playerInventoryTitle, 8.0F, (float) (this.imageHeight - 96 + 2), 4210752);
    }

    /*
     * Draws the background layer of this container (behind the items).
     */
    @Override
    protected void renderBg(PoseStack stack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, background);
        if (separateBlits) {
            this.blit(stack, leftPos, topPos, 0, 0, this.imageWidth, this.inventoryRows * 18 + 17);
            this.blit(stack, leftPos, topPos + this.inventoryRows * 18 + 17, 0, 126, this.imageWidth, 96);
        } else {
            this.blit(stack, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
        }
    }

    public static void drawChestInventoryBroom(PoseStack stack, AbstractContainerScreen screen, int x, int y, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, ICONS);
        drawTexturedModalRectWithMouseHighlight(screen, stack, x, y, 11 * 18, 0 * 18, 18, 18, mouseX, mouseY);
        myTooltip(screen, stack, x, y, 18, 18, mouseX, mouseY, new TranslatableComponent("easierchests.sortchest"));
    }

    public static void drawChestInventoryAllDown(PoseStack stack, AbstractContainerScreen screen, int x, int y, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, ICONS);
        drawTexturedModalRectWithMouseHighlight(screen, stack, x, y, 0 * 18, 2 * 18, 18, 18, mouseX, mouseY);
        myTooltip(screen, stack, x, y, 18, 18, mouseX, mouseY, new TranslatableComponent("easierchests.matchdown"));
    }

    public static void drawPlayerInventoryBroom(PoseStack stack, AbstractContainerScreen screen, int x, int y, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, ICONS);
        drawTexturedModalRectWithMouseHighlight(screen, stack, x, y, 11 * 18, 0 * 18, 18, 18, mouseX, mouseY);
        myTooltip(screen, stack, x, y, 18, 18, mouseX, mouseY, new TranslatableComponent("easierchests.sortplayer"));
    }

    public static void drawPlayerInventoryAllUp(PoseStack stack, AbstractContainerScreen screen, int x, int y, int mouseX, int mouseY) {
        RenderSystem.setShaderTexture(0, ICONS);
        drawTexturedModalRectWithMouseHighlight(screen, stack, x, y, 8 * 18, 2 * 18, 18, 18, mouseX, mouseY);
        myTooltip(screen, stack, x, y, 18, 18, mouseX, mouseY, new TranslatableComponent("easierchests.matchup"));
    }

    public static void drawTexturedModalRectWithMouseHighlight(AbstractContainerScreen screen, PoseStack stack, int screenx, int screeny, int textx, int texty, int sizex, int sizey, int mousex, int mousey) {
        if (mousex >= screenx && mousex < screenx + sizex && mousey >= screeny && mousey < screeny + sizey) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            screen.blit(stack, screenx, screeny, textx, texty, sizex, sizey);
        } else {
            if (ConfigurationHandler.getInstance().transparent()) {
                RenderSystem.enableBlend();
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.3f);
            }
            if (ConfigurationHandler.getInstance().halfSizeButtons()) {
                PoseStack stack2 = RenderSystem.getModelViewStack();
                stack2.pushPose();
                stack2.scale(0.5f, 0.5f, 0.5f);
                RenderSystem.applyModelViewMatrix();
                screen.blit(stack, screenx * 2 + sizex / 2, screeny * 2 + sizey / 2, textx, texty, sizex, sizey);
                stack2.popPose();
                RenderSystem.applyModelViewMatrix();
            } else {
                screen.blit(stack, screenx, screeny, textx, texty, sizex, sizey);
            }
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    private static void myTooltip(AbstractContainerScreen screen, PoseStack stack, int screenx, int screeny, int sizex, int sizey, int mousex, int mousey, Component tooltip) {
        if (tooltip != null && mousex >= screenx && mousex <= screenx + sizex && mousey >= screeny && mousey <= screeny + sizey) {
            screen.renderTooltip(stack, tooltip, mousex, mousey);
        }
    }

    public static void sortInventory(SlotClicker screen, boolean isChest, IItemHandler inv) {

        int size = isChest ? inv.getSlots() : 36;     // player's Inventory has 41 items which includes armor and left hand, but we don't want these.
        if (size > 9 * 6 && !ConfigurationHandler.getInstance().largeChests())
            size = 9 * 6;
        for (int toSlot = 0; toSlot < size; toSlot++) {
            ItemStack toStack = inv.getStackInSlot(toSlot);
            String targetItemName = toStack.getDescriptionId();
            if (toStack.getItem() == Items.AIR) {
                if (!isChest && toSlot < 9)
                    continue;                   // Don't move stuff into empty player hotbar slots
                targetItemName = "§§§";           // make sure it is highest so gets sorted last
            }

            // First, find an item that fits better into the current slot, but
            // don't remove hotbar things and don't
            // pull things from frozen slots unless Shift is pressed
            if (isChest || toSlot >= 9 && (hasShiftDown() || !FrozenSlotDatabase.isSlotFrozen(toSlot))) {
                for (int fromSlot = toSlot + 1; fromSlot < size; fromSlot++) {
                    if (!isChest && !hasShiftDown() && FrozenSlotDatabase.isSlotFrozen(fromSlot))
                        continue;
                    ItemStack slotStack = inv.getStackInSlot(fromSlot);
                    if (slotStack.getItem() == Items.AIR)
                        continue;
                    String slotItem = inv.getStackInSlot(fromSlot).getDescriptionId();
                    if (slotItem.compareToIgnoreCase(targetItemName) < 0) {
                        targetItemName = slotItem;
                    }
                }
            } else {
                // Hotbar slots: allow filling them up but not replacing armor/weapon
                if (toStack.getCount() >= toStack.getMaxStackSize()) {
                    continue;
                }
            }

            // Next, check for items that we can merge into the current item,
            // or that have the same name but are lower in some respect
            // ( display name, number of enchantments, name of first enchantment, damage ...)

            for (int fromSlot = toSlot + 1; fromSlot < size; fromSlot++) {
                if (!isChest && !hasShiftDown()) {
                    if (FrozenSlotDatabase.isSlotFrozen(fromSlot)) {
                        continue;
                    }
                }
                toStack = inv.getStackInSlot(toSlot);
                ItemStack fromStack = inv.getStackInSlot(fromSlot);
                if (fromStack.getDescriptionId().equals(targetItemName)
                        && (!toStack.getDescriptionId().equals(targetItemName)
                        || stackShouldGoBefore(fromStack, toStack))) {
                    screen.EasierChests$onMouseClick(null, isChest ? fromSlot : screen.EasierChests$slotIndexfromPlayerInventoryIndex(fromSlot), 0, ClickType.PICKUP);
                    screen.EasierChests$onMouseClick(null, isChest ? toSlot : screen.EasierChests$slotIndexfromPlayerInventoryIndex(toSlot), 0, ClickType.PICKUP);
                    screen.EasierChests$onMouseClick(null, isChest ? fromSlot : screen.EasierChests$slotIndexfromPlayerInventoryIndex(fromSlot), 0, ClickType.PICKUP);
                }
            }
        }
    }

    private static boolean stackShouldGoBefore(ItemStack replacement, ItemStack original) {
        String replacementName = replacement.getHoverName().getString();
        String originalName = original.getHoverName().getString();
        // alphabetically by display name

        if (replacementName.compareToIgnoreCase(originalName) > 0) {
            return false;
        }
        // if both damageable (same item name ...) then less damage before more damage
        if (replacement.isDamageableItem() && original.isDamageableItem()
                && replacement.getDamageValue() > original.getDamageValue()) {
            return false;
        }
        // less enchantments before more enchantments
        ListTag originalEnchantments = (original.getItem() == Items.ENCHANTED_BOOK) ? EnchantedBookItem.getEnchantments(original) : original.getEnchantmentTags();
        ListTag replacementEnchantments = (replacement.getItem() == Items.ENCHANTED_BOOK) ? EnchantedBookItem.getEnchantments(replacement) : replacement.getEnchantmentTags();
        if (replacementEnchantments == null || replacementEnchantments.isEmpty()) {
            if (originalEnchantments == null || originalEnchantments.isEmpty()) {
                // Items are equal - same item type, same display name, no enchantments.
                // Try to merge them, but only if the original ItemStack isn't full.
                return original.getCount() != original.getMaxStackSize();
            }
            return true;
        }
        if (originalEnchantments == null || originalEnchantments.isEmpty()) {
            return false;
        }
        if (replacementEnchantments.size() < originalEnchantments.size()) {
            return true;
        } else if (replacementEnchantments.size() == originalEnchantments.size()) {
            for (int i = 0; i < replacementEnchantments.size(); i++) {
                String originalId = ((CompoundTag) originalEnchantments.get(i)).getString("id");
                String replacementId = ((CompoundTag) replacementEnchantments.get(i)).getString("id");
                int compared = originalId.compareTo(replacementId);

                if (compared < 0) {
                    return false;
                } else if (compared > 0) {
                    return true;
                }
                int originalLevel = ((CompoundTag) originalEnchantments.get(i)).getInt("lvl");
                int replacementLevel = ((CompoundTag) replacementEnchantments.get(i)).getInt("lvl");
                if (originalLevel == replacementLevel) {
                    continue;
                }
                return replacementLevel < originalLevel;
            }
            return false;           // all enchantments identical
        } else {
            return false;
        }
    }

    public static void moveMatchingItems(AbstractContainerScreen screen, boolean isChestToPlayer) {
        // System.out.println("move matching from "+(isChest ? "chest" : "player"));
        IItemHandler from, to; //forge has a wrapper called InvWrapper, allowing us to use iitemhandler everywhere for modcompat and reject Container
        int fromSize, toSize;
        Minecraft minecraft = Minecraft.getInstance();
        Slot potentialSlot = screen.getMenu().getSlot(0);
        IItemHandler containerInventory = potentialSlot instanceof SlotItemHandler handler ? handler.getItemHandler() : new InvWrapper(potentialSlot.container);

        // use 36 for player inventory size so we won't use armor/2h slots
        if (isChestToPlayer) {
            from = containerInventory;
            to = new InvWrapper(minecraft.player.getInventory());
            fromSize = from.getSlots();
            toSize = 36;
        } else {
            from = new InvWrapper(minecraft.player.getInventory());
            to = containerInventory;
            fromSize = 36;
            toSize = to.getSlots();
        }
        if (!ConfigurationHandler.getInstance().largeChests()) {
            if (fromSize > 9 * 6) fromSize = 9 * 6;
            if (toSize > 9 * 6) toSize = 9 * 6;
        }
        for (int i = 0; i < fromSize; i++) {
            if (!isChestToPlayer && !hasShiftDown() && FrozenSlotDatabase.isSlotFrozen(i))
                continue;
            ItemStack fromStack = from.getStackInSlot(i);
            int slot;
            if (isChestToPlayer) {
                slot = i;
            } else {
                slot = ((SlotClicker) screen).EasierChests$slotIndexfromPlayerInventoryIndex(i);
            }
            for (int j = 0; j < toSize; j++) {
                ItemStack toStack = to.getStackInSlot(j);
                if (fromStack.sameItemStackIgnoreDurability(toStack)
                        && ItemStack.tagMatches(fromStack, toStack)) {
                    // System.out.println("  from["+i+"] is same as to["+j+"] ("+toStack.getDisplayName()+"), clicking "+slot);
                    ((SlotClicker) screen).EasierChests$onMouseClick(null, slot, 0, ClickType.QUICK_MOVE);
                }
            }
        }
    }


}