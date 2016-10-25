package cofh.thermalexpansion.model;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

abstract class BakedModelBase implements IBakedModel {

	static final Vec3d[] NORTH_FACE = new Vec3d[] { new Vec3d(1, 1, 0), new Vec3d(1, 0, 0), new Vec3d(0, 0, 0),
			new Vec3d(0, 1, 0) };
	private static final Vec3d[] UP_FACE = new Vec3d[] { new Vec3d(1, 1, 1), new Vec3d(1, 1, 0), new Vec3d(0, 1, 0),
			new Vec3d(0, 1, 1) };
	private static final Vec3d[] DOWN_FACE = new Vec3d[] { new Vec3d(1, 0, 1), new Vec3d(0, 0, 1), new Vec3d(0, 0, 0),
			new Vec3d(1, 0, 0) };
	private static final Vec3d[] SOUTH_FACE = new Vec3d[] { new Vec3d(0, 1, 1), new Vec3d(0, 0, 1), new Vec3d(1, 0, 1),
			new Vec3d(1, 1, 1) };
	private static final Vec3d[] EAST_FACE = new Vec3d[] { new Vec3d(1, 1, 1), new Vec3d(1, 0, 1), new Vec3d(1, 0, 0),
			new Vec3d(1, 1, 0) };
	private static final Vec3d[] WEST_FACE = new Vec3d[] { new Vec3d(0, 1, 0), new Vec3d(0, 0, 0), new Vec3d(0, 0, 1),
			new Vec3d(0, 1, 1) };
	static final Map<EnumFacing, Vec3d[]> FACE_VECTORS = ImmutableMap.<EnumFacing, Vec3d[]>builder()
			.put(EnumFacing.UP, UP_FACE)
			.put(EnumFacing.DOWN, DOWN_FACE)
			.put(EnumFacing.NORTH, NORTH_FACE)
			.put(EnumFacing.SOUTH, SOUTH_FACE)
			.put(EnumFacing.EAST, EAST_FACE)
			.put(EnumFacing.WEST, WEST_FACE).build();

	private VertexFormat format;

	BakedModelBase(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {

		this.format = format;
	}

	BakedQuad createInsetFullFaceQuad(EnumFacing facing, double inset, TextureAtlasSprite sprite) {

		Vec3d[] vectors = FACE_VECTORS.get(facing);
		Vec3d[] modifiedVectors = new Vec3d[vectors.length];

		for (int i = 0; i < vectors.length; i++) {
			modifiedVectors[i] = vectors[i].add((new Vec3d(facing.getOpposite().getDirectionVec())).scale(inset));
		}

		//TODO add caching
		return createQuad(modifiedVectors[0], modifiedVectors[1], modifiedVectors[2], modifiedVectors[3], facing, sprite);
	}

	BakedQuad createFullFaceQuad(EnumFacing facing, TextureAtlasSprite sprite) {

		Vec3d[] vectors = FACE_VECTORS.get(facing);

		return createQuad(vectors[0], vectors[1], vectors[2], vectors[3], facing, sprite);
	}

	protected List<BakedQuad> createCenteredCube(double size, TextureAtlasSprite sprite) {

		List<BakedQuad> quads = new ArrayList<>();

		double padding = (1.0 - size) / 2;
		float minU, minV;
		minU = minV = (float) (padding * 16f);
		float maxU, maxV;
		maxU = maxV = (float) ((1.0 - padding) * 16f);

		for (EnumFacing facing : EnumFacing.VALUES) {
			Vec3d[] vectors = FACE_VECTORS.get(facing);
			Vec3d[] modifiedVectors = new Vec3d[vectors.length];

			for (int i = 0; i < vectors.length; i++) {
				modifiedVectors[i] = vectors[i].add(vectors[i].scale(-padding))
						.add(vectors[i].addVector(-1, -1, -1).scale(-padding));
			}

			quads.add(createQuad(modifiedVectors[0], modifiedVectors[1], modifiedVectors[2], modifiedVectors[3], facing, sprite, minU, minV, maxU, maxV));
		}
		return quads;
	}

	private BakedQuad createQuad(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, EnumFacing facing, TextureAtlasSprite sprite) {

		return createQuad(v1, v2, v3, v4, facing, sprite, 0, 16, 0, 16);
	}

	private BakedQuad createQuad(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, EnumFacing facing, TextureAtlasSprite sprite, float minU,
			float maxU, float minV, float maxV) {

		Vec3d normal = new Vec3d(facing.getDirectionVec());

		UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
		builder.setTexture(sprite);
		putVertex(builder, normal, v1.xCoord, v1.yCoord, v1.zCoord, sprite, minU, minV);
		putVertex(builder, normal, v2.xCoord, v2.yCoord, v2.zCoord, sprite, minU, maxV);
		putVertex(builder, normal, v3.xCoord, v3.yCoord, v3.zCoord, sprite, maxU, maxV);
		putVertex(builder, normal, v4.xCoord, v4.yCoord, v4.zCoord, sprite, maxU, minV);
		return builder.build();
	}

	private void putVertex(UnpackedBakedQuad.Builder builder, Vec3d normal, double x, double y, double z,
			TextureAtlasSprite sprite, float u, float v) {

		for (int e = 0; e < format.getElementCount(); e++) {
			switch (format.getElement(e).getUsage()) {
			case POSITION:
				builder.put(e, (float) x, (float) y, (float) z, 1.0f);
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

	protected TextureAtlasSprite getSpriteFromTextureName(String name) {

		return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(name);
	}

	protected TextureAtlasSprite getSpriteFromLocation(ResourceLocation location) {

		return getSpriteFromTextureName(location.toString());
}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {

		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public ItemOverrideList getOverrides() {

		return null;
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

}
