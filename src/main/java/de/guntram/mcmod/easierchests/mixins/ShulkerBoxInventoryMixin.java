package de.guntram.mcmod.easierchests.mixins;


import de.guntram.mcmod.easierchests.InventoryExporter;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ShulkerBoxMenu.class)
public class ShulkerBoxInventoryMixin implements InventoryExporter {

    @Shadow @Final private Container container;
    
    @Override
    public Container getInventory() {
        return container;
    }
}
