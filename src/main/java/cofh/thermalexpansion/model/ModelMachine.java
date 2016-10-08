package cofh.thermalexpansion.model;

import cofh.thermalexpansion.ThermalExpansion;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
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
import java.util.HashSet;
import java.util.Set;

public class ModelMachine implements IModel {

	public static final ResourceLocation MODEL_LOCATION = new ResourceLocation(ThermalExpansion.modId, "machine");

	private static final Set<ResourceLocation> TEXTURES = ImmutableSet.<ResourceLocation>builder()
			.addAll(TextureLocations.Machine.ALL)
			.addAll(TextureLocations.Config.ALL)
			.add(TextureLocations.MISSING).build();

	@Override
	public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		return new BakedModelMachine(state, format, bakedTextureGetter);
	}

	@Override
	public Collection<ResourceLocation> getDependencies() {
		return Collections.emptySet();
	}

	@Override
	public Collection<ResourceLocation> getTextures() {

		return TEXTURES;
	}

	@Override
	public IModelState getDefaultState() {
		return TRSRTransformation.identity();
	}}
