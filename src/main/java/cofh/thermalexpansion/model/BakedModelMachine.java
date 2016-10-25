package cofh.thermalexpansion.model;

import cofh.thermalexpansion.block.BlockTEBase;
import cofh.thermalexpansion.block.machine.BlockMachine;
import cofh.thermalexpansion.core.TEProps;
import com.google.common.base.Function;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.*;

public class BakedModelMachine extends BakedModelBase {

	private static Map<EnumFacing, TextureAtlasSprite> sideSprites;
	private static Map<BlockMachine.Type, TextureAtlasSprite> faceSprites;
	private static Map<BlockMachine.Type, TextureAtlasSprite> activeFaceSprites;
	private static Map<BlockTEBase.EnumSideConfig, TextureAtlasSprite> configSprites;

	public BakedModelMachine(IModelState state, VertexFormat format,
			Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {

		super(state, format, bakedTextureGetter);

		sideSprites = new HashMap<>();
		for (EnumFacing facing : EnumFacing.VALUES) {
			sideSprites.put(facing, bakedTextureGetter.apply(TextureLocations.Machine.SIDE_MAP.get(facing)));
		}

		faceSprites = new HashMap<>();
		activeFaceSprites = new HashMap<>();
		for (BlockMachine.Type type : TextureLocations.Machine.FACE_MAP.keySet()) {
			faceSprites.put(type, bakedTextureGetter.apply(TextureLocations.Machine.FACE_MAP.get(type)));
			activeFaceSprites.put(type, bakedTextureGetter.apply(TextureLocations.Machine.ACTIVE_FACE_MAP.get(type)));
		}

		configSprites = new HashMap<>();
		for (BlockTEBase.EnumSideConfig config : TextureLocations.Config.CONFIG_MAP.keySet()) {
			configSprites.put(config, bakedTextureGetter.apply(TextureLocations.Config.CONFIG_MAP.get(config)));
		}
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {

		if (side != null) {
			return Collections.emptyList();
		}

		List<BakedQuad> quads = new ArrayList<>();

		IExtendedBlockState extState = (IExtendedBlockState) state;
		BlockMachine.Type type = extState.getValue(BlockMachine.TYPE);
		EnumFacing frontFacing = extState.getValue(TEProps.FACING);
		boolean active = extState.getValue(TEProps.ACTIVE);
		String fluidName = extState.getValue(TEProps.FLUID);

		BlockTEBase.EnumSideConfig[] configs = new BlockTEBase.EnumSideConfig[6];
		for (EnumFacing confFacing : EnumFacing.VALUES) {
			configs[confFacing.getIndex()] = extState.getValue(TEProps.SIDE_CONFIG[confFacing.getIndex()]);
		}

		for (EnumFacing facing : EnumFacing.VALUES) {
			if (frontFacing == facing) {
				if (active && fluidName != null && !fluidName.isEmpty()) {
					quads.add(createFullFaceQuad(facing, getFluidTexture(fluidName)));
				}
				quads.add(createFullFaceQuad(facing, getFaceTexture(type, active)));
			} else {
				quads.add(createFullFaceQuad(facing, getSideTexture(facing)));
				if (configs[facing.getIndex()] != BlockTEBase.EnumSideConfig.NONE) {
					quads.add(createFullFaceQuad(facing, getConfigTexture(configs[facing.getIndex()])));
				}
			}
		}

		return quads;
	}

	private TextureAtlasSprite getFluidTexture(String fluidName) {

		return getSpriteFromTextureName(fluidName);
	}

	private TextureAtlasSprite getConfigTexture(BlockTEBase.EnumSideConfig config) {

		return configSprites.get(config);
	}

	private TextureAtlasSprite getSideTexture(EnumFacing facing) {

		return sideSprites.get(facing);
	}

	private TextureAtlasSprite getFaceTexture(BlockMachine.Type type, boolean active) {

		return active ? activeFaceSprites.get(type) : faceSprites.get(type);
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {

		return sideSprites.get(EnumFacing.UP);
	}

}
