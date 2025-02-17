/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ClocheRecipeSerializer extends IERecipeSerializer<ClocheRecipe>
{
	public static final Codec<ClocheRecipe> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			TagOutputList.CODEC.fieldOf("results").forGetter(r -> r.outputs),
			Ingredient.CODEC.fieldOf("input").forGetter(r -> r.seed),
			Ingredient.CODEC.fieldOf("soil").forGetter(r -> r.soil),
			Codec.INT.fieldOf("time").forGetter(r -> r.time),
			ClocheRenderFunction.CODEC.fieldOf("render").forGetter(r -> r.renderFunction)
	).apply(inst, ClocheRecipe::new));

	@Override
	public Codec<ClocheRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(MetalDevices.CLOCHE);
	}

	@NotNull
	@Override
	public ClocheRecipe fromNetwork(FriendlyByteBuf buffer)
	{
		int outputCount = buffer.readInt();
		List<TagOutput> outputs = new ArrayList<>(outputCount);
		for(int i = 0; i < outputCount; i++)
			outputs.add(readLazyStack(buffer));
		Ingredient seed = Ingredient.fromNetwork(buffer);
		Ingredient soil = Ingredient.fromNetwork(buffer);
		int time = buffer.readInt();
		ClocheRenderFunction renderFunction = ClocheRenderFunction.read(buffer);
		return new ClocheRecipe(new TagOutputList(outputs), seed, soil, time, renderFunction);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, ClocheRecipe recipe)
	{
		buffer.writeInt(recipe.outputs.getLazyList().size());
		for(TagOutput stack : recipe.outputs.getLazyList())
			buffer.writeItem(stack.get());
		recipe.seed.toNetwork(buffer);
		recipe.soil.toNetwork(buffer);
		buffer.writeInt(recipe.time);
		ClocheRenderFunction.write(buffer, recipe.renderFunction);
	}
}
