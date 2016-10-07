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
import java.util.Set;

public class ModelMachine implements IModel {

	public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation(ThermalExpansion.modId + ":machine");

	//TODO this doesn't look great, there must be better way to get all the textures here and in the baked model
	public static final ResourceLocation TEXTURE_LOCATION_PULVERIZER = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/Machine_Face_Pulverizer");
	public static final ResourceLocation TEXTURE_LOCATION_PULVERIZER_ACTIVE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/Machine_Active_Pulverizer");
	public static final ResourceLocation TEXTURE_LOCATION_FURNACE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/Machine_Face_Furnace");
	public static final ResourceLocation TEXTURE_LOCATION_FURNACE_ACTIVE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/Machine_Active_Furnace");
	public static final ResourceLocation TEXTURE_LOCATION_SIDE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/Machine_Side");
	public static final ResourceLocation TEXTURE_LOCATION_TOP = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/Machine_Top");
	public static final ResourceLocation TEXTURE_LOCATION_BOTTOM = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/Machine_Bottom");
	public static final ResourceLocation TEXTURE_LOCATION_FRAME_TOP = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/Machine_Frame_Top");
	public static final ResourceLocation TEXTURE_LOCATION_FRAME_BOTTOM = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/Machine_Frame_Bottom");
	public static final ResourceLocation TEXTURE_LOCATION_FRAME_SIDE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/Machine_Frame_Side");

	public static final ResourceLocation TEXTURE_LOCATION_CONFIG_BLUE = new ResourceLocation(ThermalExpansion.modId, "blocks/config/Config_Blue");
	public static final ResourceLocation TEXTURE_LOCATION_CONFIG_GREEN = new ResourceLocation(ThermalExpansion.modId, "blocks/config/Config_Green");
	public static final ResourceLocation TEXTURE_LOCATION_CONFIG_OPEN = new ResourceLocation(ThermalExpansion.modId, "blocks/config/Config_Open");
	public static final ResourceLocation TEXTURE_LOCATION_CONFIG_ORANGE = new ResourceLocation(ThermalExpansion.modId, "blocks/config/Config_Orange");
	public static final ResourceLocation TEXTURE_LOCATION_CONFIG_PURPLE = new ResourceLocation(ThermalExpansion.modId, "blocks/config/Config_Purple");
	public static final ResourceLocation TEXTURE_LOCATION_CONFIG_RED = new ResourceLocation(ThermalExpansion.modId, "blocks/config/Config_Red");
	public static final ResourceLocation TEXTURE_LOCATION_CONFIG_YELLOW = new ResourceLocation(ThermalExpansion.modId, "blocks/config/Config_Yellow");


	private static final Set<ResourceLocation> TEXTURES = ImmutableSet.of(TEXTURE_LOCATION_PULVERIZER, TEXTURE_LOCATION_PULVERIZER_ACTIVE, TEXTURE_LOCATION_FURNACE,
			TEXTURE_LOCATION_FURNACE_ACTIVE, TEXTURE_LOCATION_SIDE, TEXTURE_LOCATION_TOP, TEXTURE_LOCATION_BOTTOM, TEXTURE_LOCATION_FRAME_TOP, TEXTURE_LOCATION_FRAME_BOTTOM,
			TEXTURE_LOCATION_FRAME_SIDE, TEXTURE_LOCATION_CONFIG_BLUE, TEXTURE_LOCATION_CONFIG_GREEN, TEXTURE_LOCATION_CONFIG_OPEN, TEXTURE_LOCATION_CONFIG_ORANGE,
			TEXTURE_LOCATION_CONFIG_PURPLE, TEXTURE_LOCATION_CONFIG_RED, TEXTURE_LOCATION_CONFIG_YELLOW);

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
