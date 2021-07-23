/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.mixin.coremods;

import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.IConnectionTemplate;
import blusunrize.immersiveengineering.common.wires.WireTemplateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(StructureTemplate.class)
public abstract class TemplateMixin implements IConnectionTemplate
{
	private final List<Connection> connections = new ArrayList<>();

	@Inject(method = "takeBlocksFromWorld", at = @At("HEAD"))
	public void takeConnectionsFromWorld(
			Level worldIn, BlockPos startPos, BlockPos size, boolean takeEntities, Block toIgnore, CallbackInfo ci
	)
	{
		WireTemplateHelper.fillConnectionsInArea(worldIn, startPos, size, this);
	}

	@Inject(method = "func_237146_a_", at = @At("RETURN"))
	public void addConnectionsToWorld(
			ServerLevelAccessor iworld,
			BlockPos startPos, BlockPos p_237146_3_, StructurePlaceSettings orientation,
			Random p_237146_5_, int p_237146_6_, CallbackInfoReturnable<Boolean> cir
	)
	{
		if(cir.getReturnValue()==Boolean.TRUE)
			WireTemplateHelper.addConnectionsFromTemplate(iworld, this, orientation, startPos);
	}

	@Inject(method = "writeToNBT", at = @At("RETURN"))
	public void writeConnectionsToNBT(CompoundTag $, CallbackInfoReturnable<CompoundTag> cir)
	{
		WireTemplateHelper.addConnectionsToNBT(this, cir.getReturnValue());
	}

	@Inject(method = "read", at = @At("RETURN"))
	public void readConnectionsFromNBT(CompoundTag compound, CallbackInfo ci)
	{
		WireTemplateHelper.readConnectionsFromNBT(compound, this);
	}

	@Override
	public List<Connection> getStoredConnections()
	{
		return connections;
	}
}
