package cofh.thermalexpansion.model;

import cofh.thermalexpansion.block.BlockTEBase;
import cofh.thermalexpansion.block.machine.BlockMachine;
import cofh.thermalexpansion.core.TEProps;
import com.google.common.base.Function;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BakedModelMachine extends BakedModelBase {

	private BlockMachine.Type type;
	private PreBakedModel modelFrame;
	private PreBakedModel itemOverlay;
	private PreBakedModel modelOverlay;
	private PreBakedModel fluidOverlay;

	public BakedModelMachine(BlockMachine.Type type, IModelState state, VertexFormat format,
			Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {

		super(state, format, bakedTextureGetter);
		this.type = type;

		initPreBakedModels();
	}

	private void initPreBakedModels() {

		modelFrame = new PreBakedModel(format);
		PreBakedModel.CubeElement cube = modelFrame.addCube();
		cube.setTexture(TextureLocations.Machine.SIDE);
		cube.getChild(EnumFacing.UP.ordinal()).setTexture(TextureLocations.Machine.TOP);
		cube.getChild(EnumFacing.DOWN.ordinal()).setTexture(TextureLocations.Machine.BOTTOM);
		modelFrame.preBake();

		modelOverlay = new PreBakedModel(format);
		modelOverlay.addCube(true);
		modelOverlay.preBake();

		itemOverlay = new PreBakedModel(format);
		itemOverlay.addFace(EnumFacing.NORTH).setTexture(TextureLocations.Machine.FACE_MAP.get(type));
		itemOverlay.preBake();

		if (type == BlockMachine.Type.CRUCIBLE || type == BlockMachine.Type.TRANSPOSER) {
			fluidOverlay = new PreBakedModel(format);
			fluidOverlay.addCube(true);
			fluidOverlay.preBake();
		}
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {

		if (side != null) {
			return Collections.emptyList();
		}

		List<BakedQuad> quads = new ArrayList<>();

		quads.addAll(modelFrame.getBakedQuads());

		if (state == null) {
			quads.addAll(itemOverlay.getBakedQuads());
		} else {
			IExtendedBlockState extState = (IExtendedBlockState) state;
			EnumFacing frontFacing = extState.getValue(TEProps.FACING);
			boolean active = extState.getValue(TEProps.ACTIVE);
			String fluidName = extState.getValue(TEProps.FLUID);

			BlockTEBase.EnumSideConfig[] configs = new BlockTEBase.EnumSideConfig[6];
			for (EnumFacing confFacing : EnumFacing.VALUES) {
				configs[confFacing.getIndex()] = extState.getValue(TEProps.SIDE_CONFIG[confFacing.getIndex()]);
			}

			PreBakedModel.CompositeElement overlayCube = ((PreBakedModel.CompositeElement) modelOverlay.getElement(0));
			for (EnumFacing facing : EnumFacing.VALUES) {
				PreBakedModel.BaseElement face = overlayCube.getChild(facing.ordinal());
				if (frontFacing == facing) {
					if (active && fluidName != null && !fluidName.isEmpty()) {
						updateFluidOverlay(fluidName, facing);
						quads.addAll(fluidOverlay.getBakedQuads());
					}
					face.setVisible(true);
					face.setTexture(getFaceTexture(active));
				} else {
					if (configs[facing.getIndex()] != BlockTEBase.EnumSideConfig.NONE) {
						face.setVisible(true);
						face.setTexture(getConfigTexture(configs[facing.getIndex()]));
					} else {
						face.setVisible(false);
					}
				}
			}
			quads.addAll(modelOverlay.getBakedQuads());
		}

		return quads;
	}

	private void updateFluidOverlay(String fluidName, EnumFacing facing) {

		PreBakedModel.CompositeElement fluidCube = ((PreBakedModel.CompositeElement) fluidOverlay.getElement(0));
		fluidCube.setVisible(false);
		PreBakedModel.BaseElement fluidFace = fluidCube.getChild(facing.ordinal());
		fluidFace.setVisible(true);
		fluidFace.setTexture(fluidName);
	}

	private ResourceLocation getConfigTexture(BlockTEBase.EnumSideConfig config) {

		return TextureLocations.Config.CONFIG_MAP.get(config);
	}

	private ResourceLocation getFaceTexture(boolean active) {

		return active ? TextureLocations.Machine.ACTIVE_FACE_MAP.get(type) : TextureLocations.Machine.FACE_MAP.get(type);
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {

		return RenderHelper.getSpriteFromLocation(TextureLocations.Machine.SIDE_MAP.get(EnumFacing.UP));
	}

}
