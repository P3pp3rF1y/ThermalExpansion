package cofh.thermalexpansion.block.dynamo;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import cofh.api.energy.IEnergyStorage;
import cofh.api.item.IAugmentItem;
import cofh.api.tileentity.IAugmentable;
import cofh.api.tileentity.IEnergyInfo;
import cofh.api.tileentity.IReconfigurableFacing;
import cofh.core.CoFHProps;
import cofh.core.network.PacketCoFHBase;
import cofh.core.util.fluid.FluidTankAdv;
import cofh.lib.util.TimeTracker;
import cofh.lib.util.helpers.AugmentHelper;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.helpers.EnergyHelper;
import cofh.lib.util.helpers.MathHelper;
import cofh.lib.util.helpers.RedstoneControlHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermalexpansion.block.TileRSControl;
import cofh.thermalexpansion.core.TEProps;
import cofh.thermalexpansion.item.TEAugments;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fml.relauncher.Side;

public abstract class TileDynamoBase extends TileRSControl implements IEnergyProvider, IAugmentable, IEnergyInfo, IReconfigurableFacing, ISidedInventory,
ITickable {

	protected static final EnergyConfig[] DEFAULT_ENERGY_CONFIG = new EnergyConfig[BlockDynamo.Type.values().length];
	public static final boolean[] SECURITY = new boolean[BlockDynamo.Type.values().length];

	protected static final int FUEL_MOD = 100;
	protected static final int MAX_FLUID = FluidContainerRegistry.BUCKET_VOLUME * 4;
	protected static final int SLOTS[] = { 0 };

	int compareTracker;
	int fuelRF;
	boolean wasActive;
	boolean cached;
	IEnergyReceiver adjacentHandler = null;

	protected final byte type;
	protected EnergyConfig energyConfig;
	protected TimeTracker tracker = new TimeTracker();

	protected byte facing = 1;
	protected EnergyStorage energyStorage = new EnergyStorage(0);

	int energyMod = 1;
	int fuelMod = FUEL_MOD;

	/* Augment Variables */
	ItemStack[] augments = new ItemStack[4];
	boolean[] augmentStatus = new boolean[4];

	public boolean augmentRedstoneControl;
	public boolean augmentThrottle;
	public boolean augmentCoilDuct;

	public TileDynamoBase() {

		this(BlockDynamo.Type.STEAM);
		if (getClass() != TileDynamoBase.class) {
			throw new IllegalArgumentException();
		}
	}

	public TileDynamoBase(BlockDynamo.Type type) {

		this.type = (byte) type.ordinal();

		energyConfig = DEFAULT_ENERGY_CONFIG[this.type].copy();
		energyStorage = new EnergyStorage(energyConfig.maxEnergy, energyConfig.maxPower * 2);

	}

	@Override
	public String getName() {

		return "tile.thermalexpansion.dynamo." + BlockDynamo.Type.byMetadata(type).getName() + ".name";
	}

	@Override
	public int getComparatorInputOverride() {

		return compareTracker;
	}

	@Override
	public int getLightValue() {

		return isActive ? BlockDynamo.Type.values()[type].getLight() : 0;
	}

	@Override
	public boolean enableSecurity() {

		return SECURITY[type];
	}

	@Override
	public boolean onWrench(EntityPlayer player, EnumFacing side) {

		rotateBlock(side);
		return true;
	}

	@Override
	public void blockPlaced() {

		byte oldFacing = facing;
		for (int i = facing + 1, e = facing + 6; i < e; i++) {
			if (EnergyHelper.isAdjacentEnergyReceiverFromSide(this, EnumFacing.VALUES[i % 6])) {
				facing = (byte) (i % 6);
				if (facing != oldFacing) {
					updateAdjacentHandlers();
					markDirty();
					sendUpdatePacket(Side.CLIENT);
				}
			}
		}
	}

	@Override
	public void invalidate() {

		cached = false;
		super.invalidate();
	}

	@Override
	public void onNeighborBlockChange() {

		super.onNeighborBlockChange();
		updateAdjacentHandlers();
	}

	@Override
	public void onNeighborTileChange(BlockPos pos) {

		super.onNeighborTileChange(pos);
		updateAdjacentHandlers();
	}

	public final void setEnergyStored(int quantity) {

		energyStorage.setEnergyStored(quantity);
	}

	protected int calcEnergy() {

		if (!isActive) {
			return 0;
		}
		if (augmentThrottle) {
			return calcEnergyAugment();
		}
		if (energyStorage.getEnergyStored() < energyConfig.minPowerLevel) {
			return energyConfig.maxPower;
		}
		if (energyStorage.getEnergyStored() > energyConfig.maxPowerLevel) {
			return energyConfig.minPower;
		}
		return (energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored()) / energyConfig.energyRamp;
	}

	protected int calcEnergyAugment() {

		if (energyStorage.getEnergyStored() >= energyStorage.getMaxEnergyStored()) {
			return 0;
		}
		if (energyStorage.getEnergyStored() < energyConfig.minPowerLevel) {
			return energyConfig.maxPower;
		}
		if (energyStorage.getEnergyStored() >= energyStorage.getMaxEnergyStored() - energyConfig.energyRamp) {
			return 1;
		}
		return (energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored()) / energyConfig.energyRamp;
	}

	protected abstract boolean canGenerate();

	protected boolean hasEnergy(int energy) {

		return energyStorage.getEnergyStored() >= energy;
	}

	protected void attenuate() {

		if (timeCheck() && fuelRF > 0) {
			fuelRF -= 10;

			if (fuelRF < 0) {
				fuelRF = 0;
			}
		}
	}

	protected abstract void generate();

	protected void transferEnergy(EnumFacing side) {

		if (adjacentHandler == null) {
			return;
		}
		energyStorage.modifyEnergyStored(-adjacentHandler.receiveEnergy(side.getOpposite(),
				Math.min(energyStorage.getMaxExtract(), energyStorage.getEnergyStored()), false));
	}

	protected void updateAdjacentHandlers() {

		if (ServerHelper.isClientWorld(worldObj)) {
			return;
		}
		TileEntity tile = BlockHelper.getAdjacentTileEntity(this, EnumFacing.VALUES[facing]);

		if (EnergyHelper.isAdjacentEnergyReceiverFromSide(tile, EnumFacing.VALUES[facing ^ 1])) {
			adjacentHandler = (IEnergyReceiver) tile;
		} else {
			adjacentHandler = null;
		}
		cached = true;
	}

	protected void updateIfChanged(boolean curActive) {

		if (curActive != isActive && !wasActive) {
			updateLighting();
			sendUpdatePacket(Side.CLIENT);
		} else if (wasActive && tracker.hasDelayPassed(worldObj, 100)) {
			wasActive = false;
			updateLighting();
			sendUpdatePacket(Side.CLIENT);
		}
	}

	@Override
	protected boolean readPortableTagInternal(EntityPlayer player, NBTTagCompound tag) {

		if (augmentRedstoneControl) {
			rsMode = RedstoneControlHelper.getControlFromNBT(tag);
		}
		return true;
	}

	@Override
	protected boolean writePortableTagInternal(EntityPlayer player, NBTTagCompound tag) {

		RedstoneControlHelper.setItemStackTagRS(tag, this);

		return true;
	}

	/* ITickable */
	@Override
	public void update() {

		if (ServerHelper.isClientWorld(worldObj)) {
			return;
		}
		if (!cached) {
			onNeighborBlockChange();
		}
		boolean curActive = isActive;

		if (isActive) {
			if (redstoneControlOrDisable() && canGenerate()) {
				generate();
				transferEnergy(EnumFacing.VALUES[facing]);
			} else {
				isActive = false;
				wasActive = true;
				tracker.markTime(worldObj);
			}
		} else if (redstoneControlOrDisable() && canGenerate()) {
			isActive = true;
			generate();
			transferEnergy(EnumFacing.VALUES[facing]);
		} else {
			attenuate();
		}
		if (timeCheck()) {
			int curScale = getScaledEnergyStored(15);
			if (curScale != compareTracker) {
				compareTracker = curScale;
				callNeighborTileChange();
			}
		}
		updateIfChanged(curActive);
	}

	/* BLOCK STATE */
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {

		IExtendedBlockState exState = (IExtendedBlockState) state;

		return exState.withProperty(TEProps.ACTIVE, isActive).withProperty(TEProps.FACING, EnumFacing.VALUES[facing]);
	}

	/* GUI METHODS */
	public IEnergyStorage getEnergyStorage() {

		return energyStorage;
	}

	public int getScaledEnergyStored(int scale) {

		return MathHelper.round((long) energyStorage.getEnergyStored() * scale / energyStorage.getMaxEnergyStored());
	}

	public FluidTankAdv getTank(int tankIndex) {

		return null;
	}

	public int getScaledDuration(int scale) {

		return 0;
	}

	/* NBT METHODS */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		readAugmentsFromNBT(nbt);
		installAugments();
		energyStorage.readFromNBT(nbt);

		facing = (byte) (nbt.getByte("Facing") % 6);
		isActive = nbt.getBoolean("Active");
		fuelRF = nbt.getInteger("Fuel");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		writeAugmentsToNBT(nbt);
		energyStorage.writeToNBT(nbt);

		nbt.setByte("Facing", facing);
		nbt.setBoolean("Active", isActive);
		nbt.setInteger("Fuel", fuelRF);

		return nbt;
	}

	public void readAugmentsFromNBT(NBTTagCompound nbt) {

		NBTTagList list = nbt.getTagList("Augments", 10);

		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			int slot = tag.getInteger("Slot");
			if (slot >= 0 && slot < augments.length) {
				augments[slot] = ItemStack.loadItemStackFromNBT(tag);
			}
		}
	}

	public void writeAugmentsToNBT(NBTTagCompound nbt) {

		if (augments.length <= 0) {
			return;
		}
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < augments.length; i++) {
			if (augments[i] != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setInteger("Slot", i);
				augments[i].writeToNBT(tag);
				list.appendTag(tag);
			}
		}
		nbt.setTag("Augments", list);
	}

	/* NETWORK METHODS */
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {

		SPacketUpdateTileEntity packet = super.getUpdatePacket();
		NBTTagCompound nbt = packet.getNbtCompound();

		nbt.setByte("facing", facing);
		nbt.setBoolean("augmentRedstoneControl", augmentRedstoneControl);

		return packet;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {

		super.onDataPacket(net, pkt);

		if (net.getDirection() == EnumPacketDirection.CLIENTBOUND) {
			NBTTagCompound nbt = pkt.getNbtCompound();

			facing = nbt.getByte("facing");
			augmentRedstoneControl = nbt.getBoolean("augmentRedstoneControl");
		}
	}

	@Override
	public PacketCoFHBase getPacket() {

		PacketCoFHBase payload = super.getPacket();

		payload.addByte(facing);
		payload.addBool(augmentRedstoneControl);

		return payload;
	}

	@Override
	public PacketCoFHBase getGuiPacket() {

		PacketCoFHBase payload = super.getGuiPacket();

		payload.addInt(energyStorage.getMaxEnergyStored());
		payload.addInt(energyStorage.getEnergyStored());
		payload.addInt(fuelRF);

		payload.addBool(augmentRedstoneControl);

		return payload;
	}

	@Override
	protected void handleGuiPacket(PacketCoFHBase payload) {

		super.handleGuiPacket(payload);

		energyStorage.setCapacity(payload.getInt());
		energyStorage.setEnergyStored(payload.getInt());
		fuelRF = payload.getInt();

		boolean prevControl = augmentRedstoneControl;
		augmentRedstoneControl = payload.getBool();

		if (augmentRedstoneControl != prevControl) {
			onInstalled();
			sendUpdatePacket(Side.SERVER);
		}
	}

	/* ITilePacketHandler */
	@Override
	public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {

		super.handleTilePacket(payload, isServer);

		if (!isServer) {
			facing = payload.getByte();
			augmentRedstoneControl = payload.getBool();
		} else {
			payload.getByte();
			payload.getBool();
		}
	}

	/* IAugmentable */
	@Override
	public ItemStack[] getAugmentSlots() {

		return augments;
	}

	@Override
	public boolean[] getAugmentStatus() {

		return augmentStatus;
	}

	@Override
	public void installAugments() {

		resetAugments();
		for (int i = 0; i < augments.length; i++) {
			augmentStatus[i] = false;
			if (AugmentHelper.isAugmentItem(augments[i])) {
				augmentStatus[i] = installAugment(i);
			}
		}
		if (worldObj != null && ServerHelper.isServerWorld(worldObj)) {
			onInstalled();
			sendUpdatePacket(Side.CLIENT);
		}
	}

	/* AUGMENT HELPERS */
	protected boolean hasAugment(String type, int augLevel) {

		for (int i = 0; i < augments.length; i++) {
			if (AugmentHelper.isAugmentItem(augments[i]) && ((IAugmentItem) augments[i].getItem()).getAugmentLevel(augments[i], type) == augLevel) {
				return true;
			}
		}
		return false;
	}

	protected boolean hasDuplicateAugment(String type, int augLevel, int slot) {

		for (int i = 0; i < augments.length; i++) {
			if (i != slot && AugmentHelper.isAugmentItem(augments[i]) && ((IAugmentItem) augments[i].getItem()).getAugmentLevel(augments[i], type) == augLevel) {
				return true;
			}
		}
		return false;
	}

	protected boolean hasAugmentChain(String type, int augLevel) {

		boolean preReq = true;
		for (int i = 1; i < augLevel; i++) {
			preReq = preReq && hasAugment(type, i);
		}
		return preReq;
	}

	protected boolean installAugment(int slot) {

		IAugmentItem augmentItem = (IAugmentItem) augments[slot].getItem();
		boolean installed = false;

		if (augmentItem.getAugmentLevel(augments[slot], TEAugments.DYNAMO_EFFICIENCY) > 0) {
			if (augmentItem.getAugmentLevel(augments[slot], TEAugments.DYNAMO_OUTPUT) > 0) {
				return false;
			}
			int augLevel = Math.min(TEAugments.NUM_DYNAMO_EFFICIENCY, augmentItem.getAugmentLevel(augments[slot], TEAugments.DYNAMO_EFFICIENCY));
			if (hasDuplicateAugment(TEAugments.DYNAMO_EFFICIENCY, augLevel, slot)) {
				return false;
			}
			if (hasAugmentChain(TEAugments.DYNAMO_EFFICIENCY, augLevel)) {
				fuelMod += TEAugments.DYNAMO_EFFICIENCY_MOD[augLevel];
				installed = true;
			} else {
				return false;
			}
		}
		if (augmentItem.getAugmentLevel(augments[slot], TEAugments.DYNAMO_OUTPUT) > 0) {
			int augLevel = augmentItem.getAugmentLevel(augments[slot], TEAugments.DYNAMO_OUTPUT);
			if (hasDuplicateAugment(TEAugments.DYNAMO_OUTPUT, augLevel, slot)) {
				return false;
			}
			if (hasAugmentChain(TEAugments.DYNAMO_OUTPUT, augLevel)) {
				energyMod = Math.max(energyMod, TEAugments.DYNAMO_OUTPUT_MOD[augLevel]);
				energyStorage.setMaxTransfer(Math.max(energyStorage.getMaxExtract(), energyConfig.maxPower * 2 * TEAugments.DYNAMO_OUTPUT_MOD[augLevel]));
				fuelMod -= TEAugments.DYNAMO_OUTPUT_EFFICIENCY_MOD[augLevel];
				installed = true;
			} else {
				return false;
			}
		}
		if (augmentItem.getAugmentLevel(augments[slot], TEAugments.DYNAMO_COIL_DUCT) > 0) {
			augmentCoilDuct = true;
			installed = true;
		}
		if (augmentItem.getAugmentLevel(augments[slot], TEAugments.DYNAMO_THROTTLE) > 0) {
			if (hasAugment(TEAugments.GENERAL_REDSTONE_CONTROL, 0)) {
				augmentThrottle = true;
				installed = true;
			} else {
				return false;
			}
		}
		if (augmentItem.getAugmentLevel(augments[slot], TEAugments.ENDER_ENERGY) > 0) {

		}
		if (augmentItem.getAugmentLevel(augments[slot], TEAugments.GENERAL_REDSTONE_CONTROL) > 0) {
			augmentRedstoneControl = true;
			installed = true;
		}
		return installed;
	}

	protected void onInstalled() {

		if (!augmentRedstoneControl) {
			this.rsMode = ControlMode.DISABLED;
		}
	}

	protected void resetAugments() {

		energyMod = 1;
		fuelMod = FUEL_MOD;
		energyStorage.setMaxTransfer(energyConfig.maxPower * 2);

		augmentRedstoneControl = false;
		augmentThrottle = false;
		augmentCoilDuct = false;
	}

	/* IEnergyProvider */
	@Override
	public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {

		return from == null ? 0 : from.ordinal() != facing ? 0 : energyStorage.extractEnergy(Math.min(energyConfig.maxPower * 2, maxExtract), simulate);
	}

	@Override
	public int getEnergyStored(EnumFacing from) {

		return energyStorage.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(EnumFacing from) {

		return energyStorage.getMaxEnergyStored();
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from) {

		return from == null ? false : from.ordinal() == facing;
	}

	/* IEnergyInfo */
	@Override
	public int getInfoEnergyPerTick() {

		return calcEnergy() * energyMod;
	}

	@Override
	public int getInfoMaxEnergyPerTick() {

		return energyConfig.maxPower * energyMod;
	}

	@Override
	public int getInfoEnergyStored() {

		return energyStorage.getEnergyStored();
	}

	@Override
	public int getInfoMaxEnergyStored() {

		return energyConfig.maxEnergy;
	}

	/* IFluidHandler */
	public boolean canFill(EnumFacing from, Fluid fluid) {

		return augmentCoilDuct || from == null || from.ordinal() != facing;
	}

	public boolean canDrain(EnumFacing from, Fluid fluid) {

		return augmentCoilDuct || from == null || from.ordinal() != facing;
	}

	/* IPortableData */
	@Override
	public String getDataType() {

		return "tile.thermalexpansion.dynamo";
	}

	/* IReconfigurableFacing */
	@Override
	public int getFacing() {

		return facing;
	}

	@Override
	public boolean allowYAxisFacing() {

		return true;
	}

	@Override
	public boolean rotateBlock(EnumFacing side) {

		if (ServerHelper.isClientWorld(worldObj)) {
			return false;
		}
		if (adjacentHandler != null) {
			byte oldFacing = facing;
			for (int i = facing + 1, e = facing + 6; i < e; i++) {
				if (EnergyHelper.isAdjacentEnergyReceiverFromSide(this, EnumFacing.VALUES[i % 6])) {
					facing = (byte) (i % 6);
					if (facing != oldFacing) {
						updateAdjacentHandlers();
						markDirty();
						sendUpdatePacket(Side.CLIENT);
					}
					return true;
				}
			}
			return false;
		}
		facing = (byte) ((facing + 1) % 6);
		updateAdjacentHandlers();
		markDirty();
		sendUpdatePacket(Side.CLIENT);
		return true;
	}

	@Override
	public boolean setFacing(EnumFacing side) {

		return false;
	}

	/* ISidedInventory */
	@Override
	public int[] getSlotsForFace(EnumFacing side) {

		return CoFHProps.EMPTY_INVENTORY;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, EnumFacing side) {

		return augmentCoilDuct || side.ordinal() != facing ? isItemValidForSlot(slot, stack) : false;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, EnumFacing side) {

		return augmentCoilDuct || side.ordinal() != facing;
	}

}
