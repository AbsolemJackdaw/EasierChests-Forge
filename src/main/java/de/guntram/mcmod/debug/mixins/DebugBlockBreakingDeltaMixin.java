/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.debug.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 *
 * @author gbl
 */

@Mixin(BlockBehaviour.class)

public class DebugBlockBreakingDeltaMixin {
    @Inject(method="getDestroyProgress", at=@At(value="HEAD"))
    public void debugBlockBreakDelta(BlockState state, Player player, BlockGetter world, BlockPos pos, CallbackInfoReturnable ci)
    {
        float f = state.getDestroySpeed(world, pos);
        int i = player.hasCorrectToolForDrops(state) ? 30 : 100;
        float result = player.getDestroySpeed(state) / f / (float)i;
        int ticksToBreak = (int) Math.ceil(1.0f / result);
        
        System.out.println("Player "+player.getScoreboardName()
                +" is breaking a block, tool effectivity "+ i
                +" block breaking speed "+result
                +" hardness "+f
                +" final result "+player.getDestroySpeed(state) / f / (float)i
                +" expect break after "+ticksToBreak+" ticks"
        );
    }
}
