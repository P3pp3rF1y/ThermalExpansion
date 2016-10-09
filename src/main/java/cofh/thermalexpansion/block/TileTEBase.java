package cofh.thermalexpansion.block;

import cofh.api.tileentity.IPortableData;
import cofh.core.block.TileCoFHBase;
import cofh.core.network.ITileInfoPacketHandler;
import cofh.core.network.ITilePacketHandler;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.gui.GuiHandler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraftforge.fml.relauncher.Side;

public abstract class TileTEBase extends TileCoFHBase implements ITilePacketHandler, ITileInfoPacketHandler, IPortableData {

	protected String tileName = "";

	public boolean setName(String name) {

		if (name.isEmpty()) {
			return false;
		}
		tileName = name;
		return true;
	}

	/* HELPERS */
	protected boolean readPortableTagInternal(EntityPlayer player, NBTTagCompound tag) {

		return false;
	}

	protected boolean writePortableTagInternal(EntityPlayer player, NBTTagCompound tag) {

		return false;
	}

	/* GUI METHODS */
	@Override
	public int getInvSlotCount() {

		return 0;
	}

	@Override
	public boolean openGui(EntityPlayer player) {

		if (hasGui()) {
			player.openGui(ThermalExpansion.instance, GuiHandler.TILE_ID, worldObj, pos.getX(), pos.getY(), pos.getZ());
			return true;
		}
		return false;
	}

	@Override
	public void sendGuiNetworkData(Container container, IContainerListener listener) {

		if (listener instanceof EntityPlayer) {
			PacketCoFHBase guiPacket = getGuiPacket();
			if (guiPacket != null) {
				PacketHandler.sendTo(guiPacket, (EntityPlayer) listener);
			}
		}
	}

	/* NBT METHODS */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		if (nbt.hasKey("Name")) {
			tileName = nbt.getString("Name");
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);
		nbt.setString("Version", ThermalExpansion.version);

		if (!tileName.isEmpty()) {
			nbt.setString("Name", tileName);
		}

		return nbt;
	}

	/* NETWORK METHODS */
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {

		SPacketUpdateTileEntity packet = super.getUpdatePacket();

		packet.getNbtCompound().setString("tileName", tileName);

		return packet;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {

		super.onDataPacket(net, pkt);

		if (ServerHelper.isClientWorld(worldObj)) {
			tileName = pkt.getNbtCompound().getString("tileName");
		}
	}

	@Override
	public PacketCoFHBase getPacket() {

		PacketCoFHBase payload = super.getPacket();
		payload.addString(tileName);
		return payload;
	}

	/* ITilePacketHandler */
	@Override
	public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {

		if (ServerHelper.isClientWorld(worldObj)) {
			tileName = payload.getString();
		} else {
			payload.getString();
		}
	}

	/* ITileInfoPacketHandler */
	@Override
	public void handleTileInfoPacket(PacketCoFHBase payload, boolean isServer, EntityPlayer player) {

		switch (TilePacketID.values()[payload.getByte()]) {
		case GUI:
			handleGuiPacket(payload);
			return;
		case FLUID:
			handleFluidPacket(payload);
			return;
		case MODE:
			handleModePacket(payload);
			return;
		default:
		}
	}

	/* IPortableData */
	@Override
	public String getDataType() {

		return getName();
	}

	@Override
	public void readPortableData(EntityPlayer player, NBTTagCompound tag) {

		if (!canPlayerAccess(player)) {
			return;
		}
		if (readPortableTagInternal(player, tag)) {
			markDirty();
			sendUpdatePacket(Side.CLIENT);
		}
	}

	@Override
	public void writePortableData(EntityPlayer player, NBTTagCompound tag) {

		if (!canPlayerAccess(player)) {
			return;
		}
		if (writePortableTagInternal(player, tag)) {

		}

	}

	/* Energy Config Class */
	public static class EnergyConfig {

		public int minPower = 8;
		public int maxPower = 80;
		public int maxEnergy = 40000;
		public int minPowerLevel = 1 * maxEnergy / 10;
		public int maxPowerLevel = 9 * maxEnergy / 10;
		public int energyRamp = maxPowerLevel / maxPower;

		public EnergyConfig() {

		}

		public EnergyConfig(EnergyConfig config) {

			this.minPower = config.minPower;
			this.maxPower = config.maxPower;
			this.maxEnergy = config.maxEnergy;
			this.minPowerLevel = config.minPowerLevel;
			this.maxPowerLevel = config.maxPowerLevel;
			this.energyRamp = config.energyRamp;
		}

		public EnergyConfig copy() {

			return new EnergyConfig(this);
		}

		public boolean setParams(int minPower, int maxPower, int maxEnergy) {

			this.minPower = minPower;
			this.maxPower = maxPower;
			this.maxEnergy = maxEnergy;
			this.maxPowerLevel = maxEnergy * 8 / 10;
			this.energyRamp = maxPower > 0 ? maxPowerLevel / maxPower : 0;
			this.minPowerLevel = minPower * energyRamp;

			return true;
		}

		public boolean setParamsPower(int maxPower) {

			return setParams(maxPower / 4, maxPower, maxPower * 1200);
		}

		public boolean setParamsPower(int maxPower, int scale) {

			return setParams(maxPower / 4, maxPower, maxPower * 1200 * scale);
		}

		public boolean setParamsEnergy(int maxEnergy) {

			return setParams(maxEnergy / 4800, maxEnergy / 1200, maxEnergy);
		}

		public boolean setParamsEnergy(int maxEnergy, int scale) {

			maxEnergy *= scale;
			return setParams(maxEnergy / 4800, maxEnergy / 1200, maxEnergy);
		}

		public boolean setParamsDefault(int maxPower) {

			this.maxPower = maxPower;
			minPower = maxPower / 10;
			maxEnergy = maxPower * 500;
			minPowerLevel = 1 * maxEnergy / 10;
			maxPowerLevel = 9 * maxEnergy / 10;
			energyRamp = maxPowerLevel / maxPower;

			return true;
		}

	}

	/* Side Config Class */
	// TODO - this is raw yet super efficient.
	// Is it worth changing?
	public static class SideConfig {

		/* Number of Side Configs */
		public int numConfig;

		/* Slot Groups accessibble per Config */
		public int[][] slotGroups;

		/* Whether or not the SIDE allows insertion */
		public boolean[] allowInsertionSide;

		/* Whether or not the SIDE allows extraction */
		public boolean[] allowExtractionSide;

		/* Whether or not the SLOT allows input */
		public boolean[] allowInsertionSlot;

		/* Whether or not the SLOT allows extraction */
		public boolean[] allowExtractionSlot;

		/* Config Textures to use on Sides */
		public int[] sideTex;

		/* Default Side configuration for freshly placed block */
		public byte[] defaultSides;
	}

}
