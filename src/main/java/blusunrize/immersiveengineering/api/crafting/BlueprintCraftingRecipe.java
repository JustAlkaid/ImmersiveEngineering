package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.ApiUtils;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author BluSunrize - 21.07.2015
 * <br>
 * These recipes are accessible in the Engineers Workbench, with a Engineers Blueprint item.<br>
 * For every "category" registered, a blueprint item will be added automatically.
 */
public class BlueprintCraftingRecipe extends MultiblockRecipe
{
	public static ArrayList<String> blueprintCategories = new ArrayList<String>();
	public static ArrayListMultimap<String, BlueprintCraftingRecipe> recipeList = ArrayListMultimap.create();
	public static HashMap<String, ItemStack> villagerPrices = new HashMap<String, ItemStack>();

	public String blueprintCategory;
	public ItemStack output;
	public IngredientStack[] inputs;

	public BlueprintCraftingRecipe(String blueprintCategory, ItemStack output, Object[] inputs)
	{
		this.blueprintCategory = blueprintCategory;
		this.output = output;
		this.inputs = new IngredientStack[inputs.length];
		for(int io=0; io<inputs.length; io++)
			this.inputs[io] = ApiUtils.createIngredientStack(inputs[io]);

		this.inputList = Lists.newArrayList(this.inputs);
		this.outputList = Lists.newArrayList(this.output);
	}

	public boolean matchesRecipe(ItemStack[] query)
	{
		//		ArrayList<Object> inputList = new ArrayList();
		//		for(Object i : inputs)
		//			if(i!=null)
		//				inputList.add(i instanceof ItemStack? ((ItemStack)i).copy(): i);
		//		ArrayList<ItemStack> queryList = new ArrayList();
		//		for(ItemStack q : query)
		//			if(q!=null)
		//				queryList.add(q.copy());
		//
		//		Iterator inputIt = inputList.iterator();
		//		while(inputIt.hasNext())
		//		{
		//			boolean match = false;
		//			Object o = inputIt.next();
		//			Iterator<ItemStack> queryIt = queryList.iterator();
		//			while(queryIt.hasNext())
		//			{
		//				ItemStack stack = queryIt.next();
		//				if(ApiUtils.stackMatchesObject(stack, o))
		//				{
		//					if(o instanceof ItemStack)
		//					{
		//						int taken = Math.min(stack.stackSize, ((ItemStack)o).stackSize);
		//						stack.stackSize-=taken;
		//						if(stack.stackSize<=0)
		//						{
		//							queryIt.remove();
		//						}
		//
		//						((ItemStack)o).stackSize-=taken;
		//						if(((ItemStack)o).stackSize<=0)
		//						{
		//							match = true;
		//							inputIt.remove();
		//							break;
		//						}
		//					}
		//					else
		//					{
		//						stack.stackSize--;
		//						if(stack.stackSize<=0)
		//							queryIt.remove();
		//
		//						match = true;
		//						inputIt.remove();
		//						break;
		//					}
		//
		//				}
		//			}
		//			if(!match)
		//			{
		//				return false;
		//			}
		//		}
		//		if(inputList.isEmpty())
		//			return true;
		return getMaxCrafted(query)>0;
	}
	public int getMaxCrafted(ItemStack[] query)
	{
		HashMap<ItemStack, Integer> queryAmount = new HashMap<ItemStack, Integer>();
		for(ItemStack q : query)
			if(q!=null)
			{
				boolean inc = false;
				for(ItemStack key : queryAmount.keySet())
					if(OreDictionary.itemMatches(q, key, true))
					{
						queryAmount.put(key, queryAmount.get(key)+q.stackSize);
						inc = true;
					}
				if(!inc)
					queryAmount.put(q, q.stackSize);
			}

		int maxCrafted = 0;
		ArrayList<IngredientStack> formattedInputList = getFormattedInputs();
		Iterator<IngredientStack> formInputIt = formattedInputList.iterator();
		while(formInputIt.hasNext())
		{
			IngredientStack ingr = formInputIt.next();
			int supplied = 0;
			int req = ingr.inputSize;
			Iterator<Entry<ItemStack, Integer>> queryIt = queryAmount.entrySet().iterator();
			while(queryIt.hasNext())
			{
				Entry<ItemStack, Integer> e = queryIt.next();
				ItemStack compStack = e.getKey();
				if(ingr.matchesItemStack(compStack))
				{
					int taken = e.getValue()/req;
					if(taken>0)
					{
						e.setValue(e.getValue()-taken*req);
						if(e.getValue()<=0)
							queryIt.remove();
						supplied += taken;
					}
				}
			}
			if(supplied<=0)
				return 0;
			else
				maxCrafted = maxCrafted==0?supplied:Math.min(maxCrafted, supplied);
		}
		return maxCrafted;
	}

	public void consumeInputs(ItemStack[] query, int crafted)
	{
		ArrayList<IngredientStack> inputList = new ArrayList();
		for(IngredientStack i : inputs)
			if(i!=null)
			{
				if(i.oreName!=null)
					inputList.add(new IngredientStack(i.oreName, i.inputSize));
				else if(i.stackList!=null)
					inputList.add(new IngredientStack(Lists.newArrayList(i.stackList), i.inputSize));
				else if(i.stack!=null)
					inputList.add(new IngredientStack(ApiUtils.copyStackWithAmount(i.stack, i.inputSize)));
			}

		Iterator<IngredientStack> inputIt = inputList.iterator();
		while(inputIt.hasNext())
		{
			IngredientStack ingr = inputIt.next();
			int inputSize = ingr.inputSize*crafted;

			for(int i=0; i<query.length; i++)
				if(query[i]!=null)
					if(ingr.matchesItemStack(query[i]))
					{
						int taken = Math.min(query[i].stackSize, inputSize);
						query[i].stackSize-=taken;
						if(query[i].stackSize<=0)
							query[i] = null;
						inputSize-=taken;
						if(inputSize<=0)
						{
							inputIt.remove();
							break;
						}
					}
		}
	}
	public ArrayList<IngredientStack> getFormattedInputs()
	{
		ArrayList<IngredientStack> formattedInputs = new ArrayList<IngredientStack>();  
		for(IngredientStack ingr : this.inputs)
			if(ingr!=null)
			{
				boolean isNew = true;
				for(IngredientStack formatted : formattedInputs)
				{
					if(ingr.oreName!=null && ingr.oreName.equals(formatted.oreName))
						isNew=false;
					else if(ingr.stackList!=null && formatted.stackList!=null)
					{
						for(ItemStack iStack : ingr.stackList)
							for(ItemStack iStack2 : formatted.stackList)
								if(OreDictionary.itemMatches(iStack, iStack2, false))
								{
									isNew=false;
									break;
								}
					}
					else if(ingr.stack!=null && OreDictionary.itemMatches(ingr.stack, formatted.stack, false))
						isNew=false;
					if(!isNew)
						formatted.inputSize += ingr.inputSize;
				}
				if(isNew)
				{
					if(ingr.oreName!=null)
						formattedInputs.add(new IngredientStack(ingr.oreName, ingr.inputSize));
					else if(ingr.stackList!=null)
						formattedInputs.add(new IngredientStack(Lists.newArrayList(ingr.stackList), ingr.inputSize));
					else if(ingr.stack!=null)
						formattedInputs.add(new IngredientStack(ApiUtils.copyStackWithAmount(ingr.stack, ingr.inputSize)));
				}
			}
		return formattedInputs;
	}

	public static void addRecipe(String blueprintCategory, ItemStack output, Object... inputs)
	{
		recipeList.put(blueprintCategory, new BlueprintCraftingRecipe(blueprintCategory, output, inputs));
		if(!blueprintCategories.contains(blueprintCategory))
			blueprintCategories.add(blueprintCategory);
	}
	public static BlueprintCraftingRecipe[] findRecipes(String blueprintCategory)
	{
		if(recipeList.containsKey(blueprintCategory))
		{
			List<BlueprintCraftingRecipe> list = recipeList.get(blueprintCategory);
			return list.toArray(new BlueprintCraftingRecipe[list.size()]);
		}
		return new BlueprintCraftingRecipe[0];
	}

	/**
	 * registers a type of blueprint to be up for sale at the IE villager. Stacksize of the price will be slightly randomized (+- 2)
	 */
	public static void addVillagerTrade(String blueprintCategory, ItemStack villagerPrice)
	{
		villagerPrices.put(blueprintCategory, villagerPrice);
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		NBTTagList list = new NBTTagList();
		for(IngredientStack ingr : this.inputs)
			list.appendTag(ingr.writeToNBT(new NBTTagCompound()));
		nbt.setTag("inputs", list);
		nbt.setString("blueprintCategory", this.blueprintCategory);
		return nbt;
	}
	public static BlueprintCraftingRecipe loadFromNBT(NBTTagCompound nbt)
	{
		NBTTagList list = nbt.getTagList("inputs", 10);
		IngredientStack[] inputs = new IngredientStack[list.tagCount()];
		for(int i=0; i<inputs.length; i++)
			inputs[i] = IngredientStack.readFromNBT(list.getCompoundTagAt(i));

		List<BlueprintCraftingRecipe> recipeList = BlueprintCraftingRecipe.recipeList.get("blueprintCategory");
		for(BlueprintCraftingRecipe recipe : recipeList)
		{
			boolean b = false;
			for(int i=0; i<inputs.length; i++)
			{
				for(int j=0; j<recipe.inputs.length; j++)
					if(recipe.inputs[j].matches(inputs[i]))
					{
						b = true;
						break;
					}
			}
			if(b)
				return recipe;
		}
		return null;
	}
}