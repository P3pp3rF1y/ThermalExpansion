package cofh.thermalexpansion.model;

import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.block.simple.BlockFrame;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ModelFrame implements IModel {

	public static final ResourceLocation BASE_MODEL_LOCATION = new ResourceLocation(ThermalExpansion.modId, "frame");

	private static final Map<BlockFrame.Type, ModelFrame> MODELS;

	static {
		ImmutableMap.Builder<BlockFrame.Type, ModelFrame> builder = ImmutableMap.builder();

		for (BlockFrame.Type type : BlockFrame.Type.values()) {
			builder.put(type, new ModelFrame(type));
		}

		MODELS = builder.build();
	}

	private BlockFrame.Type type;

	private ModelFrame(BlockFrame.Type type) {

		this.type = type;
	}

	@Override
	public Collection<ResourceLocation> getDependencies() {

		return Collections.emptySet();
	}

	@Override
	public Collection<ResourceLocation> getTextures() {

		return Collections.emptySet();
	}

	@Override
	public IBakedModel bake(IModelState state, VertexFormat format,
			Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {

		return new BakedModelFrame(type, state, format, bakedTextureGetter);
	}

	@Override
	public IModelState getDefaultState() {

		return null;
	}

	public static IModel getModel(ResourceLocation modelLocation) {

		String variant = ((ModelResourceLocation) modelLocation).getVariant();

		for (BlockFrame.Type type : MODELS.keySet()) {
			//TODO optimize the String handling
			if (variant.endsWith(type.getName())) {
				return MODELS.get(type);
			}
		}

		return MODELS.get(BlockFrame.Type.CELL_BASIC);
	}
}
