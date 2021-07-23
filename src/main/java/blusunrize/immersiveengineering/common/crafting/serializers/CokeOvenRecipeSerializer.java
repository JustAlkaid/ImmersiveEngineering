/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class CokeOvenRecipeSerializer extends IERecipeSerializer<CokeOvenRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Multiblocks.cokeOven);
	}

	@Override
	public CokeOvenRecipe readFromJson(ResourceLocation recipeId, JsonObject json)
	{
		ItemStack output = readOutput(json.get("result"));
		IngredientWithSize input = IngredientWithSize.deserialize(json.get("input"));
		int time = GsonHelper.getAsInt(json, "time");
		int oil = GsonHelper.getAsInt(json, "creosote");
		return new CokeOvenRecipe(recipeId, output, input, time, oil);
	}

	@Nullable
	@Override
	public CokeOvenRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
	{
		ItemStack output = buffer.readItem();
		IngredientWithSize input = IngredientWithSize.read(buffer);
		int time = buffer.readInt();
		int oil = buffer.readInt();
		return new CokeOvenRecipe(recipeId, output, input, time, oil);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, CokeOvenRecipe recipe)
	{
		buffer.writeItem(recipe.output);
		recipe.input.write(buffer);
		buffer.writeInt(recipe.time);
		buffer.writeInt(recipe.creosoteOutput);
	}
}
