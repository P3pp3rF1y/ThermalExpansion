package cofh.thermalexpansion.render;

import cofh.thermalexpansion.block.BlockTEBase;
import cofh.thermalexpansion.block.TileTEBase;
import cofh.thermalexpansion.block.machine.BlockMachine;
import cofh.thermalexpansion.core.TEProps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.List;

public class MachineModel extends BasePerspectiveAwareModel {

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {

		if (state instanceof IExtendedBlockState) {
			IExtendedBlockState extState = (IExtendedBlockState) state;
		
			BlockMachine.Type type = extState.getValue(BlockMachine.VARIANT);
			boolean active = extState.getValue(TEProps.ACTIVE);
			EnumFacing facing = extState.getValue(TEProps.FACING);
			BlockTEBase.EnumSideConfig[] sideConfigs = new BlockTEBase.EnumSideConfig[6];
			for (int i = 0; i < 6; i++) {
				sideConfigs[i] = extState.getValue(TEProps.SIDE_CONFIG[i]);
			}


		}

		return null;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public ItemOverrideList getOverrides() {
		return ItemOverrideList.NONE;
	}
}
