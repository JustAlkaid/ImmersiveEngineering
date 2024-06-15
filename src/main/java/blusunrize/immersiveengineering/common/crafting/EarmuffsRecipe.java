/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.items.components.AttachedItem;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.Lists;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.List;

public class EarmuffsRecipe implements CraftingRecipe
{
	public EarmuffsRecipe()
	{
	}

	@Override
	public boolean isSpecial()
	{
		return true;
	}

	@Override
	public boolean matches(CraftingInput inv, @Nonnull Level worldIn)
	{
		ItemStack earmuffs = ItemStack.EMPTY;
		ItemStack armor = ItemStack.EMPTY;
		List<ItemStack> list = Lists.newArrayList();
		for(int i = 0; i < inv.size(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
			if(!stackInSlot.isEmpty())
			{
				final boolean isEarmuffs = stackInSlot.is(Misc.EARMUFFS.asItem());
				if(earmuffs.isEmpty()&&isEarmuffs)
					earmuffs = stackInSlot;
				else if(armor.isEmpty()&&stackInSlot.getItem() instanceof ArmorItem armorItem&&
						armorItem.getEquipmentSlot()==EquipmentSlot.HEAD&&
						!isEarmuffs)
					armor = stackInSlot;
				else if(Utils.isDye(stackInSlot))
					list.add(stackInSlot);
				else
					return false;
			}
		}
		if(!earmuffs.isEmpty()&&(!armor.isEmpty()||!list.isEmpty()))
			return true;
		else return !armor.isEmpty()&&armor.has(IEDataComponents.CONTAINED_EARMUFF)&&earmuffs.isEmpty()&&list.isEmpty();
	}

	@Nonnull
	@Override
	public ItemStack assemble(CraftingInput inv, Provider access)
	{
		ItemStack earmuffs = ItemStack.EMPTY;
		ItemStack armor = ItemStack.EMPTY;
		int[] colourArray = new int[3];
		int j = 0;
		int totalColourSets = 0;
		for(int i = 0; i < inv.size(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
			if(!stackInSlot.isEmpty())
			{
				final boolean isEarmuffs = stackInSlot.is(Misc.EARMUFFS.asItem());
				if(earmuffs.isEmpty()&&isEarmuffs)
				{
					earmuffs = stackInSlot;
					int colour = ((IColouredItem)earmuffs.getItem()).getColourForIEItem(earmuffs, 0);
					float r = (float)(colour >> 16&255)/255.0F;
					float g = (float)(colour >> 8&255)/255.0F;
					float b = (float)(colour&255)/255.0F;
					j = (int)((float)j+Math.max(r, Math.max(g, b))*255.0F);
					colourArray[0] = (int)((float)colourArray[0]+r*255.0F);
					colourArray[1] = (int)((float)colourArray[1]+g*255.0F);
					colourArray[2] = (int)((float)colourArray[2]+b*255.0F);
					++totalColourSets;
				}
				else if(Utils.isDye(stackInSlot))
				{
					int color = Utils.getDye(stackInSlot).getTextureDiffuseColor();
					int r = (color>>16)&255;
					int g = (color>>8)&255;
					int b = color&255;
					j += Math.max(r, Math.max(g, b));
					colourArray[0] += r;
					colourArray[1] += g;
					colourArray[2] += b;
					++totalColourSets;
				}
				else if(armor.isEmpty()&&stackInSlot.getItem() instanceof ArmorItem&&
						((ArmorItem)stackInSlot.getItem()).getEquipmentSlot()==EquipmentSlot.HEAD&&
						!isEarmuffs)
					armor = stackInSlot;
			}
		}

		if(!earmuffs.isEmpty())
		{
			if(totalColourSets > 1)
			{
				int r = colourArray[0]/totalColourSets;
				int g = colourArray[1]/totalColourSets;
				int b = colourArray[2]/totalColourSets;
				float colourMod = (float)j/(float)totalColourSets;
				float highestColour = (float)Math.max(r, Math.max(g, b));
				r = (int)((float)r*colourMod/highestColour);
				g = (int)((float)g*colourMod/highestColour);
				b = (int)((float)b*colourMod/highestColour);
				earmuffs.set(IEDataComponents.COLOR, new Color4(r, g, b, 1));
			}
			ItemStack output;
			if(!armor.isEmpty())
			{
				output = armor.copy();
				output.set(IEDataComponents.CONTAINED_EARMUFF, new AttachedItem(earmuffs));
			}
			else
				output = earmuffs.copy();
			return output;
		}
		else if(!armor.isEmpty()&&armor.has(IEDataComponents.CONTAINED_EARMUFF))
		{
			ItemStack output = armor.copy();
			output.remove(IEDataComponents.CONTAINED_EARMUFF);
			return output;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height)
	{
		return width >= 2&&height >= 2;
	}

	@Nonnull
	@Override
	public ItemStack getResultItem(Provider access)
	{
		return new ItemStack(Misc.EARMUFFS, 1);
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput inv)
	{
		NonNullList<ItemStack> remaining = CraftingRecipe.super.getRemainingItems(inv);
		for(int i = 0; i < remaining.size(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
			final var earmuffs = stackInSlot.get(IEDataComponents.CONTAINED_EARMUFF);
			if(earmuffs!=null)
				remaining.set(i, earmuffs.attached());
		}
		return remaining;
	}

	@Nonnull
	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.EARMUFF_SERIALIZER.get();
	}

	@Override
	public NonNullList<Ingredient> getIngredients()
	{
		return NonNullList.withSize(1, Ingredient.of(Misc.EARMUFFS));
	}

	@Override
	public CraftingBookCategory category()
	{
		return CraftingBookCategory.MISC;
	}
}
