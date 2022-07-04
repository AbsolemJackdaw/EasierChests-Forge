/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.easierchests.storagemodapi;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

/**
 * @author gbl
 */
public interface ChestGuiInfo {
    public int getRows(AbstractContainerScreen<?> handler);

    public int getColumns(AbstractContainerScreen<?> handler);
    public int getRowsAll(AbstractContainerScreen<?> handler);

    public int getArrowOffset();
}
