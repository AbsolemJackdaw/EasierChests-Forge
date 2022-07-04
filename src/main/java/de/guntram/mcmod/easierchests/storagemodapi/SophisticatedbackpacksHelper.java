package de.guntram.mcmod.easierchests.storagemodapi;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.client.gui.BackpackScreen;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.InventoryScrollPanel;

public class SophisticatedbackpacksHelper implements ChestGuiInfo {
    @Override
    public int getRows(AbstractContainerScreen<?> screen) {
        BackpackScreen gui = ((BackpackScreen) screen);
        return Math.min((screen.getYSize() - 114) / 18, gui.getMenu().getNumberOfRows());
    }

    @Override
    public int getColumns(AbstractContainerScreen<?> screen) {

        return ((BackpackScreen) screen).getSlotsOnLine();
    }

    @Override
    public int getRowsAll(AbstractContainerScreen<?> handler) {
        Slot slot = handler.getMenu().getSlot(0);
        IItemHandler chestInventory;
        if (slot instanceof SlotItemHandler slotItemHandler)
            chestInventory = slotItemHandler.getItemHandler();
        else
            chestInventory = new InvWrapper(slot.container);
        return chestInventory.getSlots() / getColumns(handler);
    }

    public int getArrowOffset() {
        return 26;
    }
}
