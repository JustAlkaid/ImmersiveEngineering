/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.data.tags;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGeneralMultiblock;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.mixin.accessors.BETypeAccess;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.fml.config.ConfigTracker;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class BlockEntityTags extends IntrinsicHolderTagsProvider<BlockEntityType<?>>
{
	public BlockEntityTags(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(
				output, Registries.BLOCK_ENTITY_TYPE, provider,
				bet -> BuiltInRegistries.BLOCK_ENTITY_TYPE.getResourceKey(bet).orElseThrow(),
				Lib.MODID, existingFileHelper
		);
	}

	private static final List<TagKey<BlockEntityType<?>>> IMMOVABLE_TAGS = ImmutableList.of(
			tag(new ResourceLocation("forge", "relocation_not_supported")),
			tag(new ResourceLocation("forge", "immovable"))
	);

	private static TagKey<BlockEntityType<?>> tag(ResourceLocation name)
	{
		return TagKey.create(Registries.BLOCK_ENTITY_TYPE, name);
	}

	@Override
	protected void addTags(Provider p_256380_)
	{
		// Some tiles needs to config to be available in the constructor, so just load the default values
		ConfigTracker.INSTANCE.loadDefaultServerConfigs();
		IEServerConfig.refresh();
		for(Holder<BlockEntityType<?>> type : IEBlockEntities.REGISTER.getEntries())
		{
			BlockEntity instance = type.value().create(BlockPos.ZERO, ((BETypeAccess)type.value()).getValidBlocks().iterator().next().defaultBlockState());
			if(instance instanceof IImmersiveConnectable||instance instanceof IGeneralMultiblock)
				notMovable(type);
		}
	}

	private void notMovable(Holder<BlockEntityType<?>> type)
	{
		notMovable(type.value());
	}

	private void notMovable(BlockEntityType<?> type)
	{
		for(TagKey<BlockEntityType<?>> tag : IMMOVABLE_TAGS)
			tag(tag).add(type);
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "IE tile tags";
	}
}
