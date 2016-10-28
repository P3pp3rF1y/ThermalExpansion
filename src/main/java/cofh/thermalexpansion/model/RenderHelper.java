package cofh.thermalexpansion.model;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

public class RenderHelper {

	private static final Vec3d[] NORTH_FACE = new Vec3d[] { new Vec3d(1, 1, 0), new Vec3d(1, 0, 0), new Vec3d(0, 0, 0),
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
	private static final Map<EnumFacing, Vec3d[]> FACE_VECTORS = ImmutableMap.<EnumFacing, Vec3d[]>builder()
			.put(EnumFacing.UP, UP_FACE)
			.put(EnumFacing.DOWN, DOWN_FACE)
			.put(EnumFacing.NORTH, NORTH_FACE)
			.put(EnumFacing.SOUTH, SOUTH_FACE)
			.put(EnumFacing.EAST, EAST_FACE)
			.put(EnumFacing.WEST, WEST_FACE).build();

	public static Vec3d[] getFaceVectors(EnumFacing facing) {

		Vec3d[] vectors = FACE_VECTORS.get(facing);

		Vec3d[] copy = new Vec3d[4];

		for (int i = 0; i < 4; i++) {
			copy[i] = new Vec3d(vectors[i].xCoord, vectors[i].yCoord, vectors[i].zCoord);
		}

		return copy;
	}

	public static TextureAtlasSprite getSpriteFromTextureName(String name) {

		return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(name);
	}

	public static TextureAtlasSprite getSpriteFromLocation(ResourceLocation location) {

		return getSpriteFromTextureName(location.toString());
	}
}
