package cofh.thermalexpansion.model;

import cofh.thermalexpansion.block.BlockTEBase;
import cofh.thermalexpansion.block.cell.BlockCell;
import cofh.thermalexpansion.core.TEProps;
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
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import javax.vecmath.Color4f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BakedModelCell extends BakedModelBase {

	private BlockCell.Type type;
	private PreBakedModel frameModel;
	private PreBakedModel centerModel;
	private PreBakedModel overlayModel;

	BakedModelCell(BlockCell.Type type, IModelState state, VertexFormat format,
			Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {

		super(state, format, bakedTextureGetter);
		this.type = type;

		initPreBakedModels();
	}

	private void initPreBakedModels() {

		frameModel = new PreBakedModel(format);
		frameModel.addCube().setTexture(getFaceTexture());
		frameModel.addCube().inset(0.8125).setTexture(getInnerTexture());
		frameModel.preBake();

		centerModel = new PreBakedModel(format);
		centerModel.addCube(!(type == BlockCell.Type.BASIC || type == BlockCell.Type.HARDENED)).setSize(0.7, true)
				.setTexture(getCenterTexture());
		centerModel.preBake();

		overlayModel = new PreBakedModel(format);
		overlayModel.addCube(true);
		overlayModel.addCube(true);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {

		BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
		List<BakedQuad> quads = new ArrayList<>();
		IExtendedBlockState exState = (IExtendedBlockState) state;

		if (layer == null || layer == BlockRenderLayer.CUTOUT) {
			quads.addAll(frameModel.getBakedQuads());
			if (type == BlockCell.Type.BASIC || type == BlockCell.Type.HARDENED) {
				quads.addAll(centerModel.getBakedQuads());
			}

			if (exState != null) {
				updateMeterAndConfigs(exState);
				quads.addAll(overlayModel.getBakedQuads());
			}
		}

		if ((layer == null || layer == BlockRenderLayer.TRANSLUCENT) &&
				!(type == BlockCell.Type.BASIC || type == BlockCell.Type.HARDENED))

		{
			float red = 1.0f;

			if (exState != null) {
				red = (165f + (exState.getValue(BlockCell.METER) * 10)) / 255f;
			}
			centerModel.getElement(0).setColor(new Color4f(red, 1.0f, 1.0f, 0.3f));
			quads.addAll(centerModel.getBakedQuads());
		}

		return quads;
	}

	private void updateMeterAndConfigs(IExtendedBlockState exState) {

		PreBakedModel.CubeElement meterOverlayCube = (PreBakedModel.CubeElement) overlayModel.getElement(0);
		PreBakedModel.CubeElement configOverlayCube = (PreBakedModel.CubeElement) overlayModel.getElement(1);
		for (EnumFacing facing : EnumFacing.VALUES) {
			PreBakedModel.BaseElement meterElement = meterOverlayCube.getChild(facing.ordinal());
			PreBakedModel.BaseElement configElement = configOverlayCube.getChild(facing.ordinal());

			if (facing == exState.getValue(TEProps.FACING)) {
				meterElement.setVisible(true).setTexture(getMeterTexture(exState.getValue(BlockCell.METER)));
			} else {
				meterElement.setVisible(false);
			}

			BlockTEBase.EnumSideConfig config = exState.getValue(TEProps.SIDE_CONFIG[facing.ordinal()]);
			if (config != BlockTEBase.EnumSideConfig.NONE) {
				configElement.setVisible(true).setTexture(getConfigTexture(config));
			} else {
				configElement.setVisible(false);
			}
		}
	}

	private ResourceLocation getConfigTexture(BlockTEBase.EnumSideConfig config) {

		return TextureLocations.Cell.CONFIG_MAP.get(config);
	}

	private ResourceLocation getMeterTexture(int meterTracker) {

		return type == BlockCell.Type.CREATIVE ?
				TextureLocations.Cell.METER_CREATIVE :
				TextureLocations.Cell.METER_MAP.get(meterTracker);
	}

	private ResourceLocation getCenterTexture() {

		return type == BlockCell.Type.BASIC || type == BlockCell.Type.HARDENED ?
				TextureLocations.Cell.CENTER_SOLID : TFFluids.fluidRedstone.getStill();
	}

	private ResourceLocation getInnerTexture() {

		return TextureLocations.Cell.INNER_MAP.get(type);
	}

	private ResourceLocation getFaceTexture() {

		return TextureLocations.Cell.FACE_MAP.get(type);
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {

		return RenderHelper.getSpriteFromLocation(getFaceTexture());
	}

	@Override
	public ItemOverrideList getOverrides() {

		return new ItemOverrideList(ImmutableList.of()) {

			@Override
			public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {

				BlockCell.Type type = BlockCell.Type.byMetadata(stack.getMetadata());

				return ModelCell.bakedModels.get(type);
			}
		};
	}

}
