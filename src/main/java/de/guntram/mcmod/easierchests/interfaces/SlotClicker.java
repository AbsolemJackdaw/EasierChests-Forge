/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.easierchests.interfaces;

import de.guntram.mcmod.easierchests.ArrowButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

import java.util.List;


/**
 * @author gbl
 */
public interface SlotClicker {
    public void EasierChests$onMouseClick(Slot slot, int invSlot, int button, ClickType slotActionType);

    public int EasierChests$getPlayerInventoryStartIndex();

    public int EasierChests$playerInventoryIndexFromSlotIndex(int slot);

    public int EasierChests$slotIndexfromPlayerInventoryIndex(int slot);

    /**
     * get the count of the visual columns.
     */
    public int getSlotColumnCount();

    /**
     * get the count of all columns, including none visible ones
     */
    public int getSlotRowCountAll();

    public int getSlotRowCount();

    public boolean isSupportedScreenHandler(AbstractContainerMenu menu);

    public EditBox getWidget();

    public int getArrowOffset();

    public List<ArrowButton> getButtons();
}
