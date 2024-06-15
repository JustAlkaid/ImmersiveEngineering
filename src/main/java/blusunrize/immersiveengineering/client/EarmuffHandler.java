/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.items.EarmuffsItem;
import blusunrize.immersiveengineering.common.items.EarmuffsItem.EarmuffData;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@EventBusSubscriber(value = Dist.CLIENT, modid = Lib.MODID, bus = Bus.GAME)
public class EarmuffHandler
{
	private static final Map<SoundSource, Float> LAST_MULTIPLIERS = makeDefaultMultipliers();
	private static Set<ResourceLocation> IGNORED_SOUNDS = Set.of();

	/**
	 * Only the volume multiplier for ticking sounds is updated tick-by-tick. For non-ticking sounds (e.g. records) we
	 * need to force the volume update when necessary.
	 */
	@SubscribeEvent
	public static void updateEarmuffMultipliers(ClientTickEvent.Pre ev)
	{
		if(ClientUtils.mc().player==null)
			return;
		Map<SoundSource, Float> newMultipliers = makeDefaultMultipliers();
		ItemStack earmuffs = EarmuffsItem.EARMUFF_GETTERS.getFrom(ClientUtils.mc().player);
		if(!earmuffs.isEmpty())
		{
			final var earmuffConfig = earmuffs.getOrDefault(IEDataComponents.EARMUFF_DATA, EarmuffData.DEFAULT);
			for(SoundSource source : SoundSource.values())
				if(EarmuffsItem.affectedSoundCategories.contains(source.getName())&&earmuffConfig.affectedCategories().contains(source.getName()))
				{
					// The max call is just a last safeguard against overly high attenuation (see documentation on
					// EarmuffsItem.MIN_MULTIPLIER). The workbench config UI should limit the attenuation by itself.
					final float newMultiplier = Math.max(EarmuffsItem.MIN_MULTIPLIER, earmuffConfig.volumeMod());
					newMultipliers.put(source, newMultiplier);
				}
		}
		for(SoundSource source : SoundSource.values())
			if(LAST_MULTIPLIERS.get(source).floatValue()!=newMultipliers.get(source))
			{
				LAST_MULTIPLIERS.put(source, newMultipliers.get(source));
				Minecraft.getInstance().getSoundManager().updateSourceVolume(
						source, Minecraft.getInstance().options.getSoundSourceVolume(source)
				);
			}
	}

	public static float getVolumeMultiplier(SoundInstance sound)
	{
		if(IGNORED_SOUNDS.contains(sound.getLocation()))
			return 1;
		else
			return LAST_MULTIPLIERS.get(sound.getSource());
	}

	public static void onConfigUpdate()
	{
		IGNORED_SOUNDS = IEClientConfig.earDefenders_SoundBlacklist.get().stream()
				.map(ResourceLocation::tryParse)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	private static Map<SoundSource, Float> makeDefaultMultipliers()
	{
		Map<SoundSource, Float> result = new EnumMap<>(SoundSource.class);
		for(SoundSource type : SoundSource.values())
			result.put(type, 1f);
		return result;
	}
}
