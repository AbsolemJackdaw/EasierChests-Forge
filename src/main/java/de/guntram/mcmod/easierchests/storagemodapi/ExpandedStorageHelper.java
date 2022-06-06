package de.guntram.mcmod.easierchests.storagemodapi;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import ninjaphenix.container_library.api.client.gui.AbstractScreen;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Field;

public class ExpandedStorageHelper implements ChestGuiInfo {

    private boolean warnedOutdated = false;

    @Override
    public int getRows(AbstractContainerScreen<?> screen) {
        try {
            Field myField = ((AbstractScreen) screen).getClass().getDeclaredField("inventoryHeight");
            myField.setAccessible(true);
            //((AbstractScreen) screen).inventoryHeight
            return myField.getInt(((AbstractScreen) screen));
        } catch (NoSuchMethodError | NoSuchFieldException | IllegalAccessException ex) {
            warnOutdated("Reinforced");
            return -1;
        }
    }

    @Override
    public int getColumns(AbstractContainerScreen<?> screen) {
        try {
            Field myField = ((AbstractScreen) screen).getClass().getDeclaredField("inventoryWidth");
            myField.setAccessible(true);
            //((AbstractScreen) screen).inventoryHeight
            return myField.getInt(((AbstractScreen) screen));
        } catch (NoSuchMethodError | NoSuchFieldException | IllegalAccessException ex) {
            warnOutdated("Reinforced");
            return -1;
        }
    }

    private void warnOutdated(String what) {
        if (!warnedOutdated) {
            LogManager.getLogger(this.getClass()).warn("You need a current version of " + what + ", trying to fall back");
            warnedOutdated = true;
        }
    }
}
