/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import com.google.common.collect.ImmutableSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import java.util.Set;
import java.util.function.Predicate;

public class RockcutterItem extends SawbladeItem
{
	private static Set<Material> silktouchMaterials = ImmutableSet.of(
			Material.PLANT, Material.REPLACEABLE_PLANT, Material.GRASS,
			Material.STONE, Material.GLASS, Material.ICE, Material.ICE_SOLID
	);
	private static ListTag enchants = new ListTag();
	public static ResourceLocation texture = new ResourceLocation("immersiveengineering:item/rockcutter_blade");

	static
	{
		CompoundTag tag = new CompoundTag();
		tag.putString("id", "silk_touch");
		tag.putInt("lvl", 1);
		enchants.add(tag);
	}

	public RockcutterItem(int maxDamage, float sawbladeSpeed, float sawbladeDamage)
	{
		super(maxDamage, sawbladeSpeed, sawbladeDamage);
	}

	@Override
	public boolean canSawbladeFellTree()
	{
		return false;
	}

	@Override
	public ListTag getSawbladeEnchants()
	{
		return enchants.copy();
	}

	@Override
	public Predicate<BlockState> getSawbladeMaterials()
	{
		return s -> silktouchMaterials.contains(s.getMaterial());
	}

	@Override
	public ResourceLocation getSawbladeTexture()
	{
		return texture;
	}
}