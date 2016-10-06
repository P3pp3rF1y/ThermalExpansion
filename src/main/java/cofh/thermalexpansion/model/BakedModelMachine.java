package cofh.thermalexpansion.model;

import cofh.thermalexpansion.block.BlockTEBase;
import cofh.thermalexpansion.block.machine.BlockMachine;
import cofh.thermalexpansion.core.TEProps;
import com.google.common.base.Function;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BakedModelMachine implements IBakedModel{

	private TextureAtlasSprite spriteSide;
	private TextureAtlasSprite spriteTop;
	private TextureAtlasSprite spriteBottom;
	private TextureAtlasSprite spriteFurnace;
	private TextureAtlasSprite spriteFurnaceActive;
	private TextureAtlasSprite spritePulverizer;
	private TextureAtlasSprite spritePulverizerActive;
	private TextureAtlasSprite spriteFrameTop;
	private TextureAtlasSprite spriteFrameBottom;
	private TextureAtlasSprite spriteFrameSide;

	private TextureAtlasSprite spriteConfigBlue;
	private TextureAtlasSprite spriteConfigGreen;
	private TextureAtlasSprite spriteConfigOpen;
	private TextureAtlasSprite spriteConfigOrange;
	private TextureAtlasSprite spriteConfigPurple;
	private TextureAtlasSprite spriteConfigRed;
	private TextureAtlasSprite spriteConfigYellow;


	private VertexFormat format;

	public BakedModelMachine(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {

		spriteSide = bakedTextureGetter.apply(ModelMachine.TEXTURE_LOCATION_SIDE);
		spriteTop = bakedTextureGetter.apply(ModelMachine.TEXTURE_LOCATION_TOP);
		spriteBottom = bakedTextureGetter.apply(ModelMachine.TEXTURE_LOCATION_BOTTOM);
		spriteFurnace = bakedTextureGetter.apply(ModelMachine.TEXTURE_LOCATION_FURNACE);
		spriteFurnaceActive = bakedTextureGetter.apply(ModelMachine.TEXTURE_LOCATION_FURNACE_ACTIVE);
		spritePulverizer = bakedTextureGetter.apply(ModelMachine.TEXTURE_LOCATION_PULVERIZER);
		spritePulverizerActive = bakedTextureGetter.apply(ModelMachine.TEXTURE_LOCATION_PULVERIZER_ACTIVE);
		spriteFrameTop = bakedTextureGetter.apply(ModelMachine.TEXTURE_LOCATION_FRAME_TOP);
		spriteFrameBottom = bakedTextureGetter.apply(ModelMachine.TEXTURE_LOCATION_FRAME_BOTTOM);
		spriteFrameSide = bakedTextureGetter.apply(ModelMachine.TEXTURE_LOCATION_FRAME_SIDE);

		spriteConfigBlue = bakedTextureGetter.apply(ModelMachine.TEXTURE_LOCATION_CONFIG_BLUE);
		spriteConfigGreen = bakedTextureGetter.apply(ModelMachine.TEXTURE_LOCATION_CONFIG_GREEN);
		spriteConfigOpen = bakedTextureGetter.apply(ModelMachine.TEXTURE_LOCATION_CONFIG_OPEN);
		spriteConfigOrange = bakedTextureGetter.apply(ModelMachine.TEXTURE_LOCATION_CONFIG_ORANGE);
		spriteConfigPurple = bakedTextureGetter.apply(ModelMachine.TEXTURE_LOCATION_CONFIG_PURPLE);
		spriteConfigRed = bakedTextureGetter.apply(ModelMachine.TEXTURE_LOCATION_CONFIG_RED);
		spriteConfigYellow = bakedTextureGetter.apply(ModelMachine.TEXTURE_LOCATION_CONFIG_YELLOW);

		this.format = format;
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

		BlockTEBase.EnumSideConfig[] configs = new BlockTEBase.EnumSideConfig[6];
		for (EnumFacing confFacing: EnumFacing.VALUES) {
			configs[confFacing.getIndex()] = extState.getValue(TEProps.SIDE_CONFIG[confFacing.getIndex()]);
		}

		for (EnumFacing facing : EnumFacing.VALUES) {
			if (frontFacing == facing) {
				quads.add(createFullFaceQuad(facing, getFaceTexture(type, active)));
			} else if (configs[facing.getIndex()] == BlockTEBase.EnumSideConfig.NONE){
				quads.add(createFullFaceQuad(facing, getSideTexture(facing)));
			} else {
				quads.add(createFullFaceQuad(facing, getFrameSideTexture(facing)));
				quads.add(createFullFaceQuad(facing, getConfigTexture(configs[facing.getIndex()])));
			}
		}

		return quads;
	}

	private TextureAtlasSprite getConfigTexture(BlockTEBase.EnumSideConfig config) {
		switch (config) {
			case BLUE:
				return spriteConfigBlue;
			case GREEN:
				return spriteConfigGreen;
			case OPEN:
				return spriteConfigOpen;
			case ORANGE:
				return spriteConfigOrange;
			case PURPLE:
				return spriteConfigPurple;
			case RED:
				return spriteConfigRed;
			case YELLOW:
				return spriteConfigYellow;
		}
		return null;
	}

	private TextureAtlasSprite getFrameSideTexture(EnumFacing facing) {

		if (facing == EnumFacing.UP)
			return spriteFrameTop;
		if (facing == EnumFacing.DOWN)
			return spriteFrameBottom;
		return spriteFrameSide;
	}

	private TextureAtlasSprite getSideTexture(EnumFacing facing) {

		if (facing == EnumFacing.UP)
			return spriteTop;
		if (facing == EnumFacing.DOWN)
				return spriteBottom;
		return spriteSide;
	}

	private TextureAtlasSprite getFaceTexture(BlockMachine.Type type, boolean active) {
		switch (type) {
			case FURNACE:
				return active ? spriteFurnaceActive : spriteFurnace;
			case PULVERIZER:
				return active ? spritePulverizerActive : spritePulverizer;
		}

		return null;
	}

	private BakedQuad createFullFaceQuad(EnumFacing facing, TextureAtlasSprite sprite) {
		Vec3d vec1, vec2, vec3, vec4;

		switch (facing) {
			case UP:
				vec1 = new Vec3d(1, 0, 0);
				vec2 = new Vec3d(0, 0, 0);
				vec3 = new Vec3d(0, 0, 1);
				vec4 = new Vec3d(1, 0, 1);
				break;
			case DOWN:
				vec1 = new Vec3d(1, 1, 1);
				vec2 = new Vec3d(0, 1, 1);
				vec3 = new Vec3d(0, 1, 0);
				vec4 = new Vec3d(1, 1, 0);
				break;
			case EAST:
				vec1 = new Vec3d(0, 1, 1);
				vec2 = new Vec3d(0, 0, 1);
				vec3 = new Vec3d(0, 0, 0);
				vec4 = new Vec3d(0, 1, 0);
				break;
			case WEST:
				vec1 = new Vec3d(1, 1, 0);
				vec2 = new Vec3d(1, 0, 0);
				vec3 = new Vec3d(1, 0, 1);
				vec4 = new Vec3d(1, 1, 1);
				break;
			case NORTH:
				vec1 = new Vec3d(1, 0, 1);
				vec2 = new Vec3d(0, 0, 1);
				vec3 = new Vec3d(0, 1, 1);
				vec4 = new Vec3d(1, 1, 1);
				break;
			default:
				vec1 = new Vec3d(1, 1, 0);
				vec2 = new Vec3d(0, 1, 0);
				vec3 = new Vec3d(0, 0, 0);
				vec4 = new Vec3d(1, 0, 0);
		}

		return createQuad(vec1, vec2, vec3, vec4, sprite);
	}

	private BakedQuad createQuad(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, TextureAtlasSprite sprite) {
		Vec3d normal = v1.subtract(v2).crossProduct(v3.subtract(v2));

		UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
		builder.setTexture(sprite);
		putVertex(builder, normal, v1.xCoord, v1.yCoord, v1.zCoord, sprite, 0, 0);
		putVertex(builder, normal, v2.xCoord, v2.yCoord, v2.zCoord, sprite, 0, 16);
		putVertex(builder, normal, v3.xCoord, v3.yCoord, v3.zCoord, sprite, 16, 16);
		putVertex(builder, normal, v4.xCoord, v4.yCoord, v4.zCoord, sprite, 16, 0);
		return builder.build();
	}

	private void putVertex(UnpackedBakedQuad.Builder builder, Vec3d normal, double x, double y, double z, TextureAtlasSprite sprite, float u, float v) {
		for (int e = 0; e < format.getElementCount(); e++) {
			switch (format.getElement(e).getUsage()) {
				case POSITION:
					builder.put(e, (float)x, (float)y, (float)z, 1.0f);
					break;
				case COLOR:
					builder.put(e, 1.0f, 1.0f, 1.0f, 1.0f);
					break;
				case UV:
					if (format.getElement(e).getIndex() == 0) {
						u = sprite.getInterpolatedU(u);
						v = sprite.getInterpolatedV(v);
						builder.put(e, u, v, 0f, 1f);
						break;
					}
				case NORMAL:
					builder.put(e, (float) normal.xCoord, (float) normal.yCoord, (float) normal.zCoord, 0f);
					break;
				default:
					builder.put(e);
					break;
			}
		}
	}

	@Override
	public boolean isAmbientOcclusion() {
		return false;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return spriteTop;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public ItemOverrideList getOverrides() {
		return null;
	}
}
