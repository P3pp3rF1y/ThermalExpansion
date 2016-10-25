package cofh.thermalexpansion.model;

import cofh.thermalexpansion.block.cell.BlockCell;
import cofh.thermalfoundation.fluid.TFFluids;
import com.google.common.base.Function;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.model.IModelState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BakedModelCell extends BakedModelBase {

	private BlockCell.Type type;

	BakedModelCell(BlockCell.Type type, IModelState state, VertexFormat format,
			Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {

		super(state, format, bakedTextureGetter);
		this.type = type;
	}

	BakedModelCell(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {

		this(null, state, format, bakedTextureGetter);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {

		List<BakedQuad> quads = new ArrayList<>();

/*		for (EnumFacing facing : EnumFacing.VALUES) {
			quads.add(createInsetFullFaceQuad(facing, 0.8125, getInnerTexture()));
			quads.add(createFullFaceQuad(facing, getFaceTexture()));
		}*/

		quads.addAll(createCenteredCube(0.7, getCenterTexture()));

		return quads;
	}

	private TextureAtlasSprite getCenterTexture() {

		//TODO add caching
		return getSpriteFromLocation(type == BlockCell.Type.BASIC || type == BlockCell.Type.HARDENED ?
				TextureLocations.Cell.CENTER_SOLID : TFFluids.fluidRedstone.getStill());
	}
	private TextureAtlasSprite getInnerTexture() {

		//TODO add caching
		return getSpriteFromLocation(TextureLocations.Cell.INNER_MAP.get(type));
	}

	private TextureAtlasSprite getFaceTexture() {

		//TODO add caching
		return getSpriteFromLocation(TextureLocations.Cell.FACE_MAP.get(type));
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {

		return getFaceTexture();
	}

	@Override
	public ItemOverrideList getOverrides() {

		return ItemOverrideList.NONE;
	}


}
