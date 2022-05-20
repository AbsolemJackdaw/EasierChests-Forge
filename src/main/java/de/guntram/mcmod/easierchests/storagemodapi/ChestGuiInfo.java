/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.easierchests.storagemodapi;

import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 *
 * @author gbl
 */
public interface ChestGuiInfo {
    public int getRows(AbstractContainerMenu handler);
    public int getColumns(AbstractContainerMenu handler);
}
