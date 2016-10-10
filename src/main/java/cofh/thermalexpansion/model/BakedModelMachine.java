package cofh.thermalexpansion.model;

import cofh.thermalexpansion.block.BlockTEBase;
import cofh.thermalexpansion.block.machine.BlockMachine;
import cofh.thermalexpansion.core.TEProps;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.*;

public class BakedModelMachine implements IBakedModel {

	private static Map<EnumFacing, TextureAtlasSprite> sideSprites;
	private static Map<BlockMachine.Type, TextureAtlasSprite> faceSprites;
	private static Map<BlockMachine.Type, TextureAtlasSprite> activeFaceSprites;
	private static Map<BlockTEBase.EnumSideConfig, TextureAtlasSprite> configSprites;

	private static final Vec3d[] UP_FACE = new Vec3d[] {new Vec3d(1, 1, 1), new Vec3d(1, 1, 0), new Vec3d(0, 1, 0), new Vec3d(0, 1, 1)};
	private static final Vec3d[] DOWN_FACE = new Vec3d[] {new Vec3d(1, 0, 1), new Vec3d(0, 0, 1), new Vec3d(0, 0, 0), new Vec3d(1, 0, 0)};
	private static final Vec3d[] NORTH_FACE = new Vec3d[] {new Vec3d(1, 1, 0), new Vec3d(1, 0, 0), new Vec3d(0, 0, 0), new Vec3d(0, 1, 0)};
	private static final Vec3d[] SOUTH_FACE = new Vec3d[] {new Vec3d(0, 1, 1), new Vec3d(0, 0, 1), new Vec3d(1, 0, 1), new Vec3d(1, 1, 1)};
	private static final Vec3d[] EAST_FACE = new Vec3d[] {new Vec3d(1, 1, 1), new Vec3d(1, 0, 1), new Vec3d(1, 0, 0), new Vec3d(1, 1, 0)};
	private static final Vec3d[] WEST_FACE = new Vec3d[] {new Vec3d(0, 1, 0), new Vec3d(0, 0, 0), new Vec3d(0, 0, 1), new Vec3d(0, 1, 1)};

	private static final Map<EnumFacing, Vec3d[]> FACE_VECTORS = ImmutableMap.<EnumFacing, Vec3d[]>builder()
			.put(EnumFacing.UP, UP_FACE)
			.put(EnumFacing.DOWN, DOWN_FACE)
			.put(EnumFacing.NORTH, NORTH_FACE)
			.put(EnumFacing.SOUTH, SOUTH_FACE)
			.put(EnumFacing.EAST, EAST_FACE)
			.put(EnumFacing.WEST, WEST_FACE).build();

	private VertexFormat format;

	public BakedModelMachine(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {

		sideSprites = new HashMap<>();
		for(EnumFacing facing : EnumFacing.VALUES) {
			sideSprites.put(facing, bakedTextureGetter.apply(TextureLocations.Machine.SIDE_MAP.get(facing)));
		}

		faceSprites = new HashMap<>();
		activeFaceSprites = new HashMap<>();
		for(BlockMachine.Type type : TextureLocations.Machine.FACE_MAP.keySet()) {
			faceSprites.put(type, bakedTextureGetter.apply(TextureLocations.Machine.FACE_MAP.get(type)));
			activeFaceSprites.put(type, bakedTextureGetter.apply(TextureLocations.Machine.ACTIVE_FACE_MAP.get(type)));
		}

		configSprites = new HashMap<>();
		for(BlockTEBase.EnumSideConfig config : TextureLocations.Config.CONFIG_MAP.keySet()) {
			configSprites.put(config, bakedTextureGetter.apply(TextureLocations.Config.CONFIG_MAP.get(config)));
		}

		this.format = format;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {

		if(side != null) {
			return Collections.emptyList();
		}

		List<BakedQuad> quads = new ArrayList<>();

		IExtendedBlockState extState = (IExtendedBlockState) state;
		BlockMachine.Type type = extState.getValue(BlockMachine.TYPE);
		EnumFacing frontFacing = extState.getValue(TEProps.FACING);
		boolean active = extState.getValue(TEProps.ACTIVE);
		String fluidName = extState.getValue(TEProps.FLUID);

		BlockTEBase.EnumSideConfig[] configs = new BlockTEBase.EnumSideConfig[6];
		for(EnumFacing confFacing : EnumFacing.VALUES) {
			configs[confFacing.getIndex()] = extState.getValue(TEProps.SIDE_CONFIG[confFacing.getIndex()]);
		}

		for(EnumFacing facing : EnumFacing.VALUES) {
			if(frontFacing == facing) {
				if (active && fluidName != null && !fluidName.isEmpty()) {
					quads.add(createFullFaceQuad(facing, getFluidTexture(fluidName)));
				}
				quads.add(createFullFaceQuad(facing, getFaceTexture(type, active)));
			} else {
				quads.add(createFullFaceQuad(facing, getSideTexture(facing)));
				if(configs[facing.getIndex()] != BlockTEBase.EnumSideConfig.NONE) {
					quads.add(createFullFaceQuad(facing, getConfigTexture(configs[facing.getIndex()])));
				}
			}
		}

		return quads;
	}

	private TextureAtlasSprite getFluidTexture(String fluidName) {
		return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluidName);
	}

	private TextureAtlasSprite getConfigTexture(BlockTEBase.EnumSideConfig config) {

		return configSprites.get(config);
	}

	private TextureAtlasSprite getSideTexture(EnumFacing facing) {

		return sideSprites.get(facing);
	}

	private TextureAtlasSprite getFaceTexture(BlockMachine.Type type, boolean active) {

		return active ?  activeFaceSprites.get(type) : faceSprites.get(type);
	}

	private BakedQuad createFullFaceQuad(EnumFacing facing, TextureAtlasSprite sprite) {

		Vec3d[] vectors = FACE_VECTORS.get(facing);

		return createQuad(vectors[0], vectors[1], vectors[2], vectors[3], facing, sprite);
	}

	private BakedQuad createQuad(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, EnumFacing facing, TextureAtlasSprite sprite) {
		Vec3d normal = new Vec3d(facing.getDirectionVec());

		UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
		builder.setTexture(sprite);
		putVertex(builder, normal, v1.xCoord, v1.yCoord, v1.zCoord, sprite, 0, 0 /*sprite.getMinU(), sprite.getMinV()*/);
		putVertex(builder, normal, v2.xCoord, v2.yCoord, v2.zCoord, sprite, 0, 16 /*sprite.getminu(), sprite.getmaxv()*/);
		putVertex(builder, normal, v3.xCoord, v3.yCoord, v3.zCoord, sprite, 16, 16 /*sprite.getMaxU(), sprite.getMaxV()*/);
		putVertex(builder, normal, v4.xCoord, v4.yCoord, v4.zCoord, sprite, 16, 0 /*sprite.getMaxU(), sprite.getMaxV()*/);
		return builder.build();
	}

	private void putVertex(UnpackedBakedQuad.Builder builder, Vec3d normal, double x, double y, double z, TextureAtlasSprite sprite, float u, float v) {
		for(int e = 0; e < format.getElementCount(); e++) {
			switch(format.getElement(e).getUsage()) {
				case POSITION:
					builder.put(e, (float) x, (float) y, (float) z, 1.0f);
					break;
				case COLOR:
					builder.put(e, 1.0f, 1.0f, 1.0f, 1.0f);
					break;
				case UV:
					if(format.getElement(e).getIndex() == 0) {
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
		return sideSprites.get(EnumFacing.UP);
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
