package cofh.thermalexpansion.block.device;

import cofh.lib.util.TimeTracker;
import cofh.thermalexpansion.block.TileAugmentable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;

public abstract class TileDeviceBase extends TileAugmentable {

	protected static final SideConfig[] DEFAULT_SIDE_CONFIG = new SideConfig[BlockDevice.Type.values().length];
	protected static final EnergyConfig[] DEFAULT_ENERGY_CONFIG = new EnergyConfig[BlockDevice.Type.values().length];
	public static final boolean[] SECURITY = new boolean[BlockDevice.Type.values().length];

	boolean wasActive;

	protected final byte type;
	protected EnergyConfig energyConfig;
	protected TimeTracker tracker = new TimeTracker();

	public TileDeviceBase() {

		this(BlockDevice.Type.ACTIVATOR);
		if (getClass() != TileDeviceBase.class) {
			throw new IllegalArgumentException();
		}
	}

	public TileDeviceBase(BlockDevice.Type type) {

		this.type = (byte) type.ordinal();

		sideConfig = DEFAULT_SIDE_CONFIG[this.type];
		energyConfig = DEFAULT_ENERGY_CONFIG[this.type].copy();
		//energyStorage = new EnergyStorage(energyConfig.maxEnergy, energyConfig.maxPower * ENERGY_TRANSFER[level]);
		setDefaultSides();
	}

	@Override
	public String getName() {

		return "tile.thermalexpansion.device." + BlockDevice.Type.byMetadata(type).getName() + ".name";
	}

	@Override
	public int getLightValue() {

		return isActive ? BlockDevice.Type.values()[type].getLight() : 0;
	}

	@Override
	public boolean enableSecurity() {

		return SECURITY[type];
	}

	@Override
	public boolean sendRedstoneUpdates() {

		return true;
	}

	//TODO remove?
	public void onEntityCollidedWithBlock() {

	}

	/* BLOCK STATE */
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {

		// TODO: FIX
		//		state = state.withProperty(BlockMachine.ACTIVE, isActive);
		//		state = state.withProperty(BlockMachine.FACING, EnumFacing.VALUES[facing]);
		//		for (int i = 0; i < 6; i++) {
		//			state = state.withProperty(BlockMachine.SIDE_CONFIG[i], EnumSideConfig.VALUES[sideCache[i]]);
		//		}
		return state;
	}

	/* IReconfigurableFacing */
	@Override
	public boolean allowYAxisFacing() {

		return true;
	}

	@Override
	public boolean setFacing(EnumFacing side) {

		int sideInt = side.ordinal();

		if (sideInt < 0 || sideInt > 5) {
			return false;
		}
		if (!allowYAxisFacing() && sideInt < 2) {
			return false;
		}
		facing = (byte) sideInt;
		sideCache[facing] = 0;
		sideCache[facing ^ 1] = 1;
		markDirty();
		sendUpdatePacket(Side.CLIENT);
		return true;
	}

}
