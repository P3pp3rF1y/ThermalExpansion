package cofh.thermalexpansion.block.cell;

import cofh.lib.util.helpers.ServerHelper;
import cofh.thermalexpansion.model.TextureLocations;
import cofh.thermalfoundation.fluid.TFFluids;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TileCellCreative extends TileCell {

	public static void initialize() {

		GameRegistry.registerTileEntity(TileCellCreative.class, "thermalexpansion.CellCreative");
	}

	public static final byte[] DEFAULT_SIDES = { 1, 1, 1, 1, 1, 1 };

	public TileCellCreative() {

		energyStorage.setEnergyStored(-1);
	}

	public TileCellCreative(int metadata) {

		super(metadata);
		energyStorage.setEnergyStored(-1);
	}

	@Override
	public byte[] getDefaultSides() {

		return DEFAULT_SIDES.clone();
	}

	@Override
	public void update() {

		if (ServerHelper.isClientWorld(worldObj)) {
			return;
		}
		if (!cached) {
			onNeighborBlockChange();
		}
		if (redstoneControlOrDisable()) {
			for (int i = 0; i < 6; i++) {
				transferEnergy(i);
			}
		}
	}

	@Override
	protected void transferEnergy(int bSide) {

		if (sideCache[bSide] != 1) {
			return;
		}
		if (adjacentHandlers[bSide] == null) {
			return;
		}
		adjacentHandlers[bSide].receiveEnergy(EnumFacing.values()[bSide].getOpposite(), energySend, false);
	}

	/* NBT METHODS */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		energyStorage.setEnergyStored(energyStorage.getMaxEnergyStored());
	}

	/* IEnergyHandler */
	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {

		if (from == null || sideCache[from.ordinal()] == 2) {
			return Math.min(maxReceive, energyReceive);
		}
		return 0;
	}

	@Override
	public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {

		if (from == null || sideCache[from.ordinal()] == 1) {
			return Math.min(maxExtract, energySend);
		}
		return 0;
	}

	/* ISidedTexture */
	@Override
	public ResourceLocation getTexture(EnumFacing side, int pass) {

		if (pass == 0) {
			return TFFluids.fluidRedstone.getStill();
		} else if (pass == 1) {
			return TextureLocations.Cell.FACE_MAP.get(type);
		} else if (pass == 2) {
			return TextureLocations.Cell.CONFIG_MAP.get(getSideConfig(side));
		}
		return side.getIndex() != facing ?
				TextureLocations.Cell.CONFIG_MAP.get(getSideConfig(side)) :
				TextureLocations.Cell.METER_CREATIVE;
	}
}
