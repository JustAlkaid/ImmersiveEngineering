/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IInitialMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockFace;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities.ItemHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DroppingMultiblockOutput
{
	private final MultiblockFace relativeDropPos;
	private final Supplier<@Nullable IItemHandler> output;

	public DroppingMultiblockOutput(
			MultiblockFace relativeDropPos, IInitialMultiblockContext<?> ctx
	)
	{
		this.relativeDropPos = relativeDropPos;
		this.output = ctx.getCapabilityAt(ItemHandler.BLOCK, relativeDropPos);
	}

	public void insertOrDrop(ItemStack toDrop, IMultiblockLevel level)
	{
		toDrop = Utils.insertStackIntoInventory(this.output, toDrop, false);
		if(!toDrop.isEmpty())
			Utils.dropStackAtPos(
					level.getRawLevel(),
					level.toAbsolute(relativeDropPos.posInMultiblock()),
					toDrop,
					relativeDropPos.face().forFront(level.getOrientation()).getOpposite()
			);
	}
}
