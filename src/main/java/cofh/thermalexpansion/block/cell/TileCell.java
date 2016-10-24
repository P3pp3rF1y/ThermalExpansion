package cofh.thermalexpansion.block.cell;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import cofh.api.tileentity.ISidedTexture;
import cofh.core.network.PacketCoFHBase;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.helpers.EnergyHelper;
import cofh.lib.util.helpers.MathHelper;
import cofh.lib.util.helpers.RedstoneControlHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.block.TileReconfigurable;
import cofh.thermalexpansion.gui.client.GuiCell;
import cofh.thermalexpansion.gui.container.ContainerTEBase;

import cofh.thermalexpansion.util.ReconfigurableHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class TileCell extends TileReconfigurable implements IEnergyProvider, ITickable, ISidedTexture {

	public static void initialize() {

		GameRegistry.registerTileEntity(TileCell.class, "thermalexpansion.Cell");
		configure();
	}

	public static void configure() {

		String comment = "Enable this to allow for Energy Cells to be securable.";
		enableSecurity = ThermalExpansion.CONFIG.get("Security", "Cell.All.Securable", enableSecurity, comment);
	}

	public static boolean enableSecurity = true;

	public static final byte[] DEFAULT_SIDES = { 1, 2, 2, 2, 2, 2 };

	int compareTracker;
	byte meterTracker;
	byte outputTracker;

	boolean cached = false;
	IEnergyReceiver[] adjacentHandlers = new IEnergyReceiver[6];

	public int energyReceive;
	public int energySend;
	public BlockCell.Type type = BlockCell.Type.BASIC;

	public TileCell() {

		energyStorage = new EnergyStorage(BlockCell.Type.BASIC.getCapacity(), BlockCell.Type.BASIC.getMaxReceive());
	}

	public TileCell(int metadata) {

		type = BlockCell.Type.byMetadata(metadata);
		energyStorage = new EnergyStorage(type.getCapacity(), type.getMaxReceive());
	}

	@Override
	public String getName() {

		return "tile.thermalexpansion.cell." + type.getName() + ".name";
	}


	@Override
	public int getComparatorInputOverride() {

		return compareTracker;
	}

	@Override
	public int getLightValue() {

		return Math.min(8, getScaledEnergyStored(9));
	}

	@Override
	public byte[] getDefaultSides() {

		return DEFAULT_SIDES.clone();
	}

	@Override
	public boolean enableSecurity() {

		return enableSecurity;
	}

	@Override
	public void onNeighborBlockChange() {

		super.onNeighborBlockChange();
		updateAdjacentHandlers();
	}

	@Override
	public void onNeighborTileChange(BlockPos pos) {

		super.onNeighborTileChange(pos);
		updateAdjacentHandler(pos);
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
			for (int i = outputTracker; i < 6 && energyStorage.getEnergyStored() > 0; i++) {
				transferEnergy(i);
			}
			for (int i = 0; i < outputTracker && energyStorage.getEnergyStored() > 0; i++) {
				transferEnergy(i);
			}
			++outputTracker;
			outputTracker %= 6;
		}
		if (timeCheck()) {
			int curScale = getScaledEnergyStored(15);

			if (compareTracker != curScale) {
				compareTracker = curScale;
				callNeighborTileChange();
			}
			curScale = getLightValue();

			if (meterTracker != curScale) {
				meterTracker = (byte) curScale;
				updateLighting();
				sendUpdatePacket(Side.CLIENT);
			}
		}
	}

	@Override
	public void invalidate() {

		cached = false;
		super.invalidate();
	}

	protected void transferEnergy(int bSide) {

		if (sideCache[bSide] != 1) {
			return;
		}
		if (adjacentHandlers[bSide] == null) {
			return;
		}
		energyStorage.modifyEnergyStored(-adjacentHandlers[bSide].receiveEnergy(EnumFacing.values()[bSide].getOpposite(),
				Math.min(energySend, energyStorage.getEnergyStored()), false));
	}

	protected void updateAdjacentHandlers() {

		if (ServerHelper.isClientWorld(worldObj)) {
			return;
		}
		for (EnumFacing facing : EnumFacing.VALUES) {
			TileEntity tile = BlockHelper.getAdjacentTileEntity(this, facing);

			if (EnergyHelper.isEnergyReceiverOnSide(tile, facing.getOpposite())) {
				adjacentHandlers[facing.getIndex()] = (IEnergyReceiver) tile;
			} else {
				adjacentHandlers[facing.getIndex()] = null;
			}
		}
		cached = true;
	}

	protected void updateAdjacentHandler(BlockPos adjacentPos) {

		if (ServerHelper.isClientWorld(worldObj)) {
			return;
		}
		EnumFacing side = BlockHelper.determineAdjacentSide(this, adjacentPos);

		TileEntity tile = worldObj.getTileEntity(adjacentPos);

		if (EnergyHelper.isEnergyReceiverOnSide(tile,  side.getOpposite())) {
			adjacentHandlers[side.ordinal()] = (IEnergyReceiver) tile;
		} else {
			adjacentHandlers[side.ordinal()] = null;
		}
	}

	@Override
	protected boolean readPortableTagInternal(EntityPlayer player, NBTTagCompound tag) {

		rsMode = RedstoneControlHelper.getControlFromNBT(tag);
		int storedFacing = ReconfigurableHelper.getFacingFromNBT(tag);
		byte[] storedSideCache = ReconfigurableHelper.getSideCacheFromNBT(tag, getDefaultSides());

		sideCache[0] = storedSideCache[0];
		sideCache[1] = storedSideCache[1];
		sideCache[facing] = storedSideCache[storedFacing];
		sideCache[BlockHelper.getLeftSide(facing)] = storedSideCache[BlockHelper.getLeftSide(storedFacing)];
		sideCache[BlockHelper.getRightSide(facing)] = storedSideCache[BlockHelper.getRightSide(storedFacing)];
		sideCache[BlockHelper.getOppositeSide(facing)] = storedSideCache[BlockHelper.getOppositeSide(storedFacing)];

		for (int i = 0; i < 6; i++) {
			if (sideCache[i] >= getNumConfig(EnumFacing.VALUES[i])) {
				sideCache[i] = 0;
			}
		}
		energySend = (tag.getInteger("Send") * type.getMaxSend()) / 1000;
		energyReceive = (tag.getInteger("Recv") * type.getMaxReceive()) / 1000;

		return true;
	}

	@Override
	protected boolean writePortableTagInternal(EntityPlayer player, NBTTagCompound tag) {

		RedstoneControlHelper.setItemStackTagRS(tag, this);
		ReconfigurableHelper.setItemStackTagReconfig(tag, this);

		tag.setInteger("Send", (energySend * 1000) / type.getMaxSend());
		tag.setInteger("Recv", (energyReceive * 1000) / type.getMaxReceive());

		return true;
	}

	/* GUI METHODS */
	@Override
	public Object getGuiClient(InventoryPlayer inventory) {

		return new GuiCell(inventory, this);
	}

	@Override
	public Object getGuiServer(InventoryPlayer inventory) {

		return new ContainerTEBase(inventory, this);
	}

	/* NBT METHODS */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		type = BlockCell.Type.byMetadata(nbt.getByte("Type"));
		outputTracker = nbt.getByte("Tracker");
		energySend = MathHelper.clamp(nbt.getInteger("Send"), 0, type.getMaxSend());
		energyReceive = MathHelper.clamp(nbt.getInteger("Recv"), 0, type.getMaxReceive());

		energyStorage = new EnergyStorage(type.getCapacity(), type.getMaxReceive());
		energyStorage.readFromNBT(nbt);
		meterTracker = (byte) Math.min(8, getScaledEnergyStored(9));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		nbt.setByte("Type", (byte) type.getMetadata());
		nbt.setByte("Tracker", outputTracker);
		nbt.setInteger("Send", energySend);
		nbt.setInteger("Recv", energyReceive);

		return nbt;
	}

	/* NETWORK METHODS */

	packets !!!!!!

	@Override
	public PacketCoFHBase getPacket() {

		PacketCoFHBase payload = super.getPacket();

		payload.addInt(energySend);
		payload.addInt(energyReceive);
		payload.addInt(energyStorage.getEnergyStored());

		return payload;
	}

	@Override
	public PacketCoFHBase getGuiPacket() {

		PacketCoFHBase payload = super.getGuiPacket();

		payload.addInt(energySend);
		payload.addInt(energyReceive);
		payload.addInt(energyStorage.getEnergyStored());

		return payload;
	}

	@Override
	public PacketCoFHBase getModePacket() {

		PacketCoFHBase payload = super.getModePacket();

		payload.addInt(MathHelper.clamp(energySend, 0, type.getMaxSend()));
		payload.addInt(MathHelper.clamp(energyReceive, 0, type.getMaxReceive()));

		return payload;
	}

	@Override
	protected void handleGuiPacket(PacketCoFHBase payload) {

		super.handleGuiPacket(payload);

		energySend = payload.getInt();
		energyReceive = payload.getInt();
		energyStorage.setEnergyStored(payload.getInt());
	}

	@Override
	protected void handleModePacket(PacketCoFHBase payload) {

		super.handleModePacket(payload);

		energySend = payload.getInt();
		energyReceive = payload.getInt();
	}

	/* ITilePacketHandler */
	@Override
	public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {

		super.handleTilePacket(payload, isServer);

		if (!isServer) {
			energySend = payload.getInt();
			energyReceive = payload.getInt();
		} else {
			payload.getInt();
			payload.getInt();
		}
	}

	/* IEnergyHandler */
	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {

		if (from == null || sideCache[from.ordinal()] == 2) {
			return energyStorage.receiveEnergy(Math.min(maxReceive, energyReceive), simulate);
		}
		return 0;
	}

	@Override
	public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {

		if (from == null || sideCache[from.ordinal()] == 1) {
			return energyStorage.extractEnergy(Math.min(maxExtract, energySend), simulate);
		}
		return 0;
	}

	@Override
	public boolean canConnectEnergy(EnumFacing from) {

		if (from == null) {
			return false;
		}
		return sideCache[from.ordinal()] > 0;
	}

	/* IPortableData */
	@Override
	public String getDataType() {

		return "tile.thermalexpansion.cell";
	}

	/* IReconfigurableFacing */
	@Override
	public boolean allowYAxisFacing() {

		return false;
	}

	@Override
	public boolean rotateBlock(EnumFacing facing) {

		super.rotateBlock(facing);
		updateAdjacentHandlers();
		return true;
	}

	/* IReconfigurableSides */
	@Override
	public final boolean decrSide(EnumFacing side) {

		int sideInt = side.ordinal();

		sideCache[sideInt] += getNumConfig(side) - 1;
		sideCache[sideInt] %= getNumConfig(side);
		sendUpdatePacket(Side.SERVER);
		return true;
	}

	@Override
	public final boolean incrSide(EnumFacing side) {

		int sideInt = side.ordinal();

		sideCache[sideInt] += 1;
		sideCache[sideInt] %= getNumConfig(side);
		sendUpdatePacket(Side.SERVER);
		return true;
	}

	@Override
	public boolean setSide(EnumFacing side, int config) {

		int sideInt = side.ordinal();

		if (sideCache[sideInt] == config || config >= getNumConfig(side)) {
			return false;
		}
		sideCache[sideInt] = (byte) config;
		sendUpdatePacket(Side.SERVER);
		return true;
	}

	@Override
	public int getNumConfig(EnumFacing side) {

		return 3;
	}

	/* ISidedTexture */
	@Override
	public ResourceLocation getTexture(EnumFacing side, int pass) {

		if (pass == 0) {
			return type.getMetadata() < 2 ? IconRegistry.getIcon("StorageRedstone") : IconRegistry.getIcon("FluidRedstone");
		} else if (pass == 1) {
			return IconRegistry.getIcon("Cell", type * 2);
		} else if (pass == 2) {
			return IconRegistry.getIcon(BlockCell.textureSelection, sideCache[side]);
		}
		if (side != facing) {
			return IconRegistry.getIcon(BlockCell.textureSelection, 0);
		}
		int stored = Math.min(8, getScaledEnergyStored(9));
		return IconRegistry.getIcon("CellMeter", stored);
	}

}
