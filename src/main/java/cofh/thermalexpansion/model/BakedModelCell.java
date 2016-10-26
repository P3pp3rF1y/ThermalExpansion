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

		BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
		List<BakedQuad> quads = new ArrayList<>();
		IExtendedBlockState exState = (IExtendedBlockState) state;

		//TODO clean this so that it's more readable, get caching in place
		if (layer == null || layer == BlockRenderLayer.CUTOUT) {
			for (EnumFacing facing : EnumFacing.VALUES) {
				quads.add(createInsetFullFaceQuad(facing, 0.8125, getInnerTexture()));
				quads.add(createFullFaceQuad(facing, getFaceTexture()));

				if (exState != null) {
					if (facing == exState.getValue(TEProps.FACING)) {
						if (type == BlockCell.Type.CREATIVE) {
							quads.add(createFullFaceQuad(facing, getCreativeMeterTexture()));
						} else {
							quads.add(createFullFaceQuad(facing, getMeterTexture(exState.getValue(BlockCell.METER))));
						}
					} else {
						quads.add(createFullFaceQuad(facing, getConfigTexture(exState.getValue(TEProps.SIDE_CONFIG[facing.ordinal()]))));
					}
				}
			}

			if (type == BlockCell.Type.BASIC || type == BlockCell.Type.HARDENED) {
				quads.addAll(createCenteredCube(0.7, getCenterTexture(), 1.0f, 1.0f));
			}
		}

		if ((layer == null || layer == BlockRenderLayer.TRANSLUCENT) &&
				!(type == BlockCell.Type.BASIC || type == BlockCell.Type.HARDENED)) {
			float red = 1.0f;

			if (exState != null) {
				red = (165f + (exState.getValue(BlockCell.METER) * 10)) / 255f;
			}

			quads.addAll(createCenteredCube(0.7, getCenterTexture(), layer == null ? 1.0f : 0.4f, red));
		}

		return quads;
	}

	private TextureAtlasSprite getConfigTexture(BlockTEBase.EnumSideConfig config) {

		return getSpriteFromLocation(TextureLocations.Cell.CONFIG_MAP.get(config));
	}

	private TextureAtlasSprite getMeterTexture(int meterTracker) {

		return getSpriteFromLocation(TextureLocations.Cell.METER_MAP.get(meterTracker));
	}

	private TextureAtlasSprite getCreativeMeterTexture() {

		return getSpriteFromLocation(TextureLocations.Cell.METER_CREATIVE);
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
	public boolean isGui3d() {

		return true;
	}

	@Override
	public boolean isAmbientOcclusion() {

		return true;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {

		return getFaceTexture();
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
