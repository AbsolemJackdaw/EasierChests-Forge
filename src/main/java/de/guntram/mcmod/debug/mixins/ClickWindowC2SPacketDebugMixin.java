package de.guntram.mcmod.debug.mixins;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerboundContainerClickPacket.class)
public class ClickWindowC2SPacketDebugMixin {
    @Inject(method="<init>(IIIILnet/minecraft/world/inventory/ClickType;Lnet/minecraft/world/item/ItemStack;Lit/unimi/dsi/fastutil/ints/Int2ObjectMap;)V", at=@At("RETURN"))
    private void dumpC2SNewInfo(int syncid, int revision, int slot, int button, ClickType actionType, ItemStack stack,
            Int2ObjectMap<ItemStack> modifiedStacks, CallbackInfo ci) {
        System.out.println("new ClickWindow C2S: syncid="+syncid+", revision="+revision+", slot="+slot+", button="+button+
                ", action="+actionType.toString()+", item="+stack.getCount()+" of "+stack.getHoverName().getString()+
                "");
    }
}
