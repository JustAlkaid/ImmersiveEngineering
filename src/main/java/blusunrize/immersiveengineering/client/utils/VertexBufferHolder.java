/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.api.client.IVertexBufferHolder;
import blusunrize.immersiveengineering.api.utils.ResettableLazy;
import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.common.util.NonNullSupplier;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.Map.Entry;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.*;

public class VertexBufferHolder implements IVertexBufferHolder
{
	public static final VertexFormat BUFFER_FORMAT = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder()
			.put("Position", ELEMENT_POSITION)
			.put("Color", ELEMENT_COLOR)
			.put("UV0", ELEMENT_UV0)
			.put("Normal", ELEMENT_NORMAL)
			.put("Padding", ELEMENT_PADDING)
			.build());
	private static final Lazy<Boolean> HAS_OPTIFINE = Lazy.of(() -> {
		try
		{
			Class.forName("net.optifine.Config");
			IELogger.logger.warn(
					"OptiFine detected! Automatically disabling VBOs, this will make windmills and some"+
							" other objects render much less efficiently"
			);
			return true;
		} catch(Exception x)
		{
			return false;
		}
	});
	//TODO also sort by buffer to get rid of bindBuffer calls?
	private static final Map<RenderType, List<BufferedJob>> JOBS = new IdentityHashMap<>();
	private final ResettableLazy<VertexBuffer> buffer;
	private final ResettableLazy<List<BakedQuad>> quads;

	private VertexBufferHolder(NonNullSupplier<List<BakedQuad>> quads)
	{
		this.quads = new ResettableLazy<>(quads);
		this.buffer = new ResettableLazy<>(
				() -> {
					VertexBuffer vb = new VertexBuffer();
					RenderSystem.setShader(IEGLShaders::getVboShader);
					Tesselator tes = Tesselator.getInstance();
					BufferBuilder bb = tes.getBuilder();
					bb.begin(Mode.QUADS, BUFFER_FORMAT);
					renderToBuilder(bb, new PoseStack(), 0, 0, false);
					bb.end();
					vb.upload(bb);
					return vb;
				},
				VertexBuffer::close
		);
	}

	public static void addToAPI()
	{
		IVertexBufferHolder.CREATE.setValue(VertexBufferHolder::new);
	}

	@Override
	public void render(RenderType type, int light, int overlay, MultiBufferSource directOut, PoseStack transform, boolean inverted)
	{
		if(IEClientConfig.enableVBOs.get()&&!HAS_OPTIFINE.get())
			JOBS.computeIfAbsent(type, t -> new ArrayList<>())
					.add(new BufferedJob(this, light, overlay, transform, inverted));
		else
			renderToBuilder(directOut.getBuffer(type), transform, light, overlay, inverted);
	}

	@Override
	public void reset()
	{
		buffer.reset();
		quads.reset();
	}

	private void renderToBuilder(VertexConsumer builder, PoseStack transform, int light, int overlay, boolean inverted)
	{
		if(inverted)
			builder = new InvertingVertexBuffer(4, builder);
		for(BakedQuad quad : quads.get())
			builder.putBulkData(transform.last(), quad, 1, 1, 1, light, overlay);
	}

	//Called from aftertesr.js
	public static void afterTERRendering()
	{
		if(!JOBS.isEmpty())
		{
			for(Entry<RenderType, List<BufferedJob>> typeEntry : JOBS.entrySet())
			{
				RenderType type = typeEntry.getKey();
				type.setupRenderState();
				boolean inverted = false;
				for(BufferedJob job : typeEntry.getValue())
				{
					if(job.inverted&&!inverted)
						GL11.glCullFace(GL11.GL_FRONT);
					else if(!job.inverted&&inverted)
						GL11.glCullFace(GL11.GL_BACK);
					inverted = job.inverted;
					VertexBuffer buffer = job.buffer.buffer.get();
					buffer.bind();
					ShaderInstance shader = IEGLShaders.getVboShader();
					RenderSystem.setShader(() -> shader);
					Objects.requireNonNull(shader.getUniform("LightUV"))
							.set(job.light&0xffff, (job.light >> 16)&0xffff);
					Objects.requireNonNull(shader.getUniform("OverlayUV"))
							.set(job.overlay&0xffff, (job.overlay >> 16)&0xffff);
					buffer.drawWithShader(job.transform, RenderSystem.getProjectionMatrix(), shader);
				}
				if(inverted)
					GL11.glCullFace(GL11.GL_BACK);
				type.clearRenderState();
			}
			VertexBuffer.unbind();
			JOBS.clear();
		}
	}

	private static class BufferedJob
	{
		private final VertexBufferHolder buffer;
		private final int light;
		private final int overlay;
		private final Matrix4f transform;
		private final boolean inverted;

		private BufferedJob(VertexBufferHolder buffer, int light, int overlay, PoseStack transform, boolean inverted)
		{
			this.buffer = buffer;
			this.light = light;
			this.overlay = overlay;
			this.transform = transform.last().pose();
			this.inverted = inverted;
		}
	}
}
