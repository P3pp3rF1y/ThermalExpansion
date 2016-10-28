package cofh.thermalexpansion.model;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;

import javax.vecmath.Color4f;
import java.util.ArrayList;
import java.util.List;

public class PreBakedModel {

	private List<BaseElement> staticElements = new ArrayList<>();
	private List<BaseElement> dynamicElements = new ArrayList<>();

	private VertexFormat format;
	private List<BakedQuad> bakedQuads = new ArrayList<>();

	public PreBakedModel(VertexFormat format) {

		this.format = format;
	}

	public CubeElement addCube() {

		return addCube(false);
	}

	public CubeElement addCube(boolean dynamic) {

		CubeElement element = new CubeElement();
		addToCollection(element, dynamic);

		return element;
	}

	public BaseElement addFace(EnumFacing facing) {

		return addFace(facing, false);
	}

	public BaseElement addFace(EnumFacing facing, boolean dynamic) {

		FaceElement element = new FaceElement(facing);
		addToCollection(element, dynamic);

		return element;
	}

	public BaseElement getElement(int index) {

		// only returns dynamic elements as the static ones are supposed to be created at once and then left alone
		// if they can't then they probably are not static
		return dynamicElements.get(index);
	}

	private void addToCollection(BaseElement element, boolean dynamic) {

		if (dynamic) {
			dynamicElements.add(element);
		} else {
			staticElements.add(element);
		}
	}

	public void preBake() {

		//creates baked quads for the elements that are not going to change
		for (BaseElement element : staticElements) {
			List<Face> faces = element.getFaces();

			for (Face face : faces) {
				bakedQuads.add(createQuad(face.vectors[0], face.vectors[1], face.vectors[2], face.vectors[3], face.facing,
						face.sprite, face.minU, face.maxU, face.minV, face.maxV, face.color));
			}
		}
	}

	public List<BakedQuad> getBakedQuads() {

		List<BakedQuad> ret = new ArrayList<>(bakedQuads);

		for (BaseElement element : dynamicElements) {

			for (Face face : element.getFaces()) {
				ret.add(createQuad(face.vectors[0], face.vectors[1], face.vectors[2], face.vectors[3], face.facing,
						face.sprite, face.minU, face.maxU, face.minV, face.maxV, face.color));
			}
		}

		return ret;
	}

	private BakedQuad createQuad(Vec3d v1, Vec3d v2, Vec3d v3, Vec3d v4, EnumFacing facing, TextureAtlasSprite sprite, float minU,
			float maxU, float minV, float maxV, Color4f color) {

		UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(format);
		Vec3d normal = new Vec3d(facing.getDirectionVec());
		builder.setTexture(sprite);
		putVertex(builder, normal, v1.xCoord, v1.yCoord, v1.zCoord, sprite, minU, minV, color);
		putVertex(builder, normal, v2.xCoord, v2.yCoord, v2.zCoord, sprite, minU, maxV, color);
		putVertex(builder, normal, v3.xCoord, v3.yCoord, v3.zCoord, sprite, maxU, maxV, color);
		putVertex(builder, normal, v4.xCoord, v4.yCoord, v4.zCoord, sprite, maxU, minV, color);
		return builder.build();
	}

	private void putVertex(UnpackedBakedQuad.Builder builder, Vec3d normal, double x, double y, double z,
			TextureAtlasSprite sprite, float u, float v, Color4f color) {

		for (int e = 0; e < format.getElementCount(); e++) {
			switch (format.getElement(e).getUsage()) {
			case POSITION:
				builder.put(e, (float) x, (float) y, (float) z, 1.0f);
				break;
			case COLOR:
				builder.put(e, color.getX(), color.getY(), color.getZ(), color.getW());
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

	private static class Face {

		private Vec3d[] vectors = new Vec3d[4];
		private EnumFacing facing = EnumFacing.NORTH;
		private TextureAtlasSprite sprite = null;
		private float minU = 0;
		private float maxU = 16;
		private float minV = 0;
		private float maxV = 16;
		private Color4f color = new Color4f(1.0f, 1.0f, 1.0f, 1.0f);

		public void setUV(float minU, float maxU, float minV, float maxV) {

			this.minU = minU;
			this.maxU = maxU;
			this.minV = minV;
			this.maxV = maxV;
		}

		public void setSprite(TextureAtlasSprite sprite) {

			this.sprite = sprite;
		}

		public void setColor(Color4f color) {

			this.color = color;
		}
	}

	public static abstract class BaseElement {

		protected List<Face> faces = new ArrayList<>();

		protected List<Face> getFaces() {

			return faces;
		}

		public BaseElement setColor(Color4f color) {

			for (Face face : faces) {
				face.setColor(color);
			}

			return this;
		}

		public BaseElement setUV(float minU, float maxU, float minV, float maxV) {

			for (Face face : faces) {
				face.minU = minU;
				face.maxU = maxU;
				face.minV = minV;
				face.maxV = maxV;
			}

			return this;
		}

		public BaseElement setTexture(ResourceLocation location) {

			setTexture(RenderHelper.getSpriteFromLocation(location));

			return this;
		}

		public BaseElement setTexture(String textureName) {

			setTexture(RenderHelper.getSpriteFromTextureName(textureName));

			return this;
		}

		public BaseElement setTexture(TextureAtlasSprite sprite) {

			for (Face face : faces) {
				face.setSprite(sprite);
			}

			return this;
		}

		public BaseElement setSize(double size, boolean centered) {

			for (Face face : faces) {
				for (int i = 0; i < face.vectors.length; i++) {
					if (centered) {
						face.vectors[i] = resizeCentered(face.vectors[i], size);
					} else {
						face.vectors[i] = resizeSimple(face.vectors[i], size);
					}
				}

				//counts on textures being 16x16, 32x32, ... and thus needing UVs resized as well
				if (centered) {
					resizeUVsCentered(face, (float) size);
				} else {
					resizeUVsSimple(face, (float) size);
				}
			}

			return this;
		}

		private void resizeUVsSimple(Face face, float size) {

			face.setUV(0f, size * 16f, 0f, size * 16f);
		}

		private void resizeUVsCentered(Face face, float size) {

			float padding = (1.0f - size) / 2f;

			face.setUV(padding * 16f, (1.0f - padding) * 16f, padding * 16f, (1.0f - padding) * 16f);
		}

		private Vec3d resizeCentered(Vec3d vector, double size) {

			double padding = (1.0 - size) / 2;

			return vector.add(vector.scale(-padding)).add(vector.addVector(-1, -1, -1).scale(-padding));
		}

		private Vec3d resizeSimple(Vec3d vector, double size) {

			return vector.scale(size);
		}

		public BaseElement inset(double size) {

			for (Face face : faces) {
				for (int i = 0; i < face.vectors.length; i++) {
					face.vectors[i] = face.vectors[i].add((new Vec3d(face.facing.getOpposite().getDirectionVec())).scale(size));
				}
			}

			return this;
		}
	}

	public static class CompositeElement extends BaseElement {

		protected List<BaseElement> children = new ArrayList<>();

		public List<BaseElement> getChildren() {

			return children;
		}

		public BaseElement getChild(int index) {

			return children.get(index);
		}

		@Override
		protected List<Face> getFaces() {

			List<Face> ret = new ArrayList<>(faces);

			for (BaseElement child : children) {

				ret.addAll(child.getFaces());
			}

			return ret;
		}

		@Override
		public CompositeElement setColor(Color4f color) {

			for (BaseElement child : children) {
				child.setColor(color);
			}

			super.setColor(color);
			return this;
		}

		@Override
		public CompositeElement setUV(float minU, float maxU, float minV, float maxV) {

			for (BaseElement child : children) {
				child.setUV(minU, maxU, minV, maxV);
			}

			super.setUV(minU, maxU, minV, maxV);
			return this;
		}

		@Override
		public CompositeElement setTexture(TextureAtlasSprite sprite) {

			for (BaseElement child : children) {
				child.setTexture(sprite);
			}

			super.setTexture(sprite);
			return this;
		}

		@Override
		public CompositeElement setSize(double size, boolean centered) {

			for (BaseElement child : children) {
				child.setSize(size, centered);
			}

			super.setSize(size, centered);
			return this;
		}

		@Override
		public CompositeElement inset(double size) {

			for (BaseElement child : children) {
				child.inset(size);
			}

			super.inset(size);
			return this;
		}
	}

	public static class FaceElement extends BaseElement {

		private FaceElement(EnumFacing facing) {

			faces.add(new Face());
			faces.get(0).vectors = RenderHelper.getFaceVectors(facing);
			faces.get(0).facing = facing;
		}
	}

	public static class CubeElement extends CompositeElement {

		private CubeElement() {

			for (EnumFacing facing : EnumFacing.VALUES) {
				FaceElement face = new FaceElement(facing);
				children.add(face);
			}
		}
	}
}
