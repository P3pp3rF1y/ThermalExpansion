package cofh.thermalexpansion.model;

import cofh.thermalexpansion.block.cell.BlockCell;
import cofh.thermalexpansion.block.simple.BlockFrame;
import cofh.thermalfoundation.fluid.TFFluids;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.model.IModelState;

import javax.annotation.Nullable;
import javax.vecmath.Color4f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BakedModelFrame extends BakedModelBase {

	private BlockFrame.Type type;
	private PreBakedModel frameModel;
	private PreBakedModel insetFrameModel;
	private PreBakedModel centerModel;

	BakedModelFrame(BlockFrame.Type type, IModelState state, VertexFormat format,
			Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {

		super(state, format, bakedTextureGetter);
		this.type = type;

		prebakeModels();
	}

	private static final Color4f TRANSLUCENT_COLOR = new Color4f(1.0f, 1.0f, 1.0f, 0.3f);

	private void prebakeModels() {

		frameModel = new PreBakedModel(format);
		PreBakedModel.CubeElement frame = frameModel.addCube();
		frame.setTexture(getFrameTexture());
		switch (type) {
		case MACHINE_BASIC:
		case MACHINE_HARDENED:
		case MACHINE_REINFORCED:
		case MACHINE_RESONANT:
			frame.getChild(EnumFacing.UP.ordinal()).setTexture(TextureLocations.Machine.FRAME_TOP);
			frame.getChild(EnumFacing.DOWN.ordinal()).setTexture(TextureLocations.Machine.FRAME_BOTTOM);
			break;
		default:
		}
		frameModel.preBake();

		if (type != BlockFrame.Type.ILLUMINATOR) {
			insetFrameModel = new PreBakedModel(format);
			PreBakedModel.CompositeElement insetFrame = insetFrameModel.addCube().inset(0.8125);
			insetFrame.setTexture(getInsetTexture());
			insetFrameModel.preBake();
		}

		if (type != BlockFrame.Type.CELL_REINFORCED_EMPTY && type != BlockFrame.Type.CELL_RESONANT_EMPTY && type !=
				BlockFrame.Type.TESSERACT_EMPTY && type != BlockFrame.Type.ILLUMINATOR) {
			centerModel = new PreBakedModel(format);
			PreBakedModel.CompositeElement center = centerModel.addCube().setSize(0.7, true);
			center.setTexture(getCenterTexture());
			if (hasTranslucentCenter()) {
				center.setColor(TRANSLUCENT_COLOR);
			}
			centerModel.preBake();
		}
	}

	private ResourceLocation getCenterTexture() {

		switch (type) {
		case MACHINE_BASIC:
			return TextureLocations.Block.TIN;
		case MACHINE_HARDENED:
			return TextureLocations.Block.ELECTRUM;
		case MACHINE_REINFORCED:
			return TextureLocations.Block.SIGNALUM;
		case MACHINE_RESONANT:
			return TextureLocations.Block.ENDERIUM;
		case CELL_BASIC:
		case CELL_HARDENED:
			return TextureLocations.Cell.CENTER_SOLID;
		case CELL_REINFORCED_FULL:
		case CELL_RESONANT_FULL:
			return TFFluids.fluidRedstone.getStill();
		case TESSERACT_FULL:
			return TFFluids.fluidEnder.getStill();
		}

		return null;
	}

	private ResourceLocation getInsetTexture() {

		switch (type) {
		case MACHINE_BASIC:
		case MACHINE_HARDENED:
		case MACHINE_REINFORCED:
		case MACHINE_RESONANT:
			return TextureLocations.Machine.FRAME_INNER;
		case CELL_BASIC:
			return TextureLocations.Cell.BASIC_INNER;
		case CELL_HARDENED:
			return TextureLocations.Cell.HARDENED_INNER;
		case CELL_REINFORCED_EMPTY:
		case CELL_REINFORCED_FULL:
			return TextureLocations.Cell.REINFORCED_INNER;
		case CELL_RESONANT_EMPTY:
		case CELL_RESONANT_FULL:
			return TextureLocations.Cell.RESONANT_INNER;
		case TESSERACT_EMPTY:
		case TESSERACT_FULL:
			return TextureLocations.Tesseract.FRAME_INNER;
		}

		return null;
	}

	private ResourceLocation getFrameTexture() {

		switch (type) {
		case MACHINE_BASIC:
		case MACHINE_HARDENED:
		case MACHINE_REINFORCED:
		case MACHINE_RESONANT:
			return TextureLocations.Machine.FRAME_SIDE;
		case CELL_BASIC:
			return TextureLocations.Cell.BASIC;
		case CELL_HARDENED:
			return TextureLocations.Cell.HARDENED;
		case CELL_REINFORCED_EMPTY:
		case CELL_REINFORCED_FULL:
			return TextureLocations.Cell.REINFORCED;
		case CELL_RESONANT_EMPTY:
		case CELL_RESONANT_FULL:
			return TextureLocations.Cell.RESONANT;
		case TESSERACT_EMPTY:
		case TESSERACT_FULL:
			return TextureLocations.Tesseract.FRAME;
		case ILLUMINATOR:
			return TextureLocations.Illuminator.FRAME;
		}

		return null;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {

		BlockRenderLayer renderLayer = MinecraftForgeClient.getRenderLayer();

		if (renderLayer == null || renderLayer == BlockRenderLayer.CUTOUT) {
			List<BakedQuad> quads = frameModel.getBakedQuads();

			if (insetFrameModel != null) {
				quads.addAll(insetFrameModel.getBakedQuads());
			}

			if (centerModel != null && (renderLayer == null || !hasTranslucentCenter())) {
				quads.addAll(centerModel.getBakedQuads());
			}

			return quads;
		} else if (renderLayer == BlockRenderLayer.TRANSLUCENT && hasTranslucentCenter()) {
			return centerModel.getBakedQuads();
		}

		return Collections.emptyList();
	}

	private boolean hasTranslucentCenter() {

		switch(type) {
		case CELL_REINFORCED_FULL:
		case CELL_RESONANT_FULL:
		case TESSERACT_FULL:
			return true;
		}

		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {

		return RenderHelper.getSpriteFromLocation(getFrameTexture());
	}
}
