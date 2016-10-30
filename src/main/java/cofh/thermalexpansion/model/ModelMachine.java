package cofh.thermalexpansion.model;

import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.block.machine.BlockMachine;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ModelMachine implements IModel {

	public static final ResourceLocation BASE_MODEL_LOCATION = new ResourceLocation(ThermalExpansion.modId, "machine");

	private static final Map<BlockMachine.Type, ModelMachine> MODELS;

	static {
		ImmutableMap.Builder<BlockMachine.Type, ModelMachine> builder = ImmutableMap.builder();

		for (BlockMachine.Type type : BlockMachine.Type.values()) {
			builder.put(type, new ModelMachine(type));
		}

		MODELS = builder.build();
	}

	private BlockMachine.Type type;

	private ModelMachine(BlockMachine.Type type) {

		this.type = type;
	}

	@Override
	public IBakedModel bake(IModelState state, VertexFormat format,
			Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {

		return new BakedModelMachine(type, state, format, bakedTextureGetter);
	}

	public static IModel getModel(ResourceLocation modelLocation) {

		String variant = ((ModelResourceLocation) modelLocation).getVariant();

		for (BlockMachine.Type type : MODELS.keySet()) {
			//TODO optimize the String handling
			if (variant.endsWith(type.getName())) {
				return MODELS.get(type);
			}
		}

		return MODELS.get(BlockMachine.Type.FURNACE);
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
	public IModelState getDefaultState() {

		return TRSRTransformation.identity();
	}
}
