/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.blocks.metal.SqueezerBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

public class SqueezerContainer extends IEBaseContainer<SqueezerBlockEntity>
{
	public SqueezerContainer(MenuType<?> type, int id, Inventory inventoryPlayer, SqueezerBlockEntity tile)
	{
		super(type, inventoryPlayer, tile, id);

		for(int i = 0; i < 8; i++)
			this.addSlot(new Slot(this.inv, i, 8+(i%4)*18, 35+(i/4)*18));
		this.addSlot(new IESlot.Output(this, this.inv, 8, 91, 53));
		this.addSlot(new IESlot.FluidContainer(this, this.inv, 9, 134, 17, 0));
		this.addSlot(new IESlot.Output(this, this.inv, 10, 134, 53));
		slotCount = 11;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 143));
	}
}