package cofh.thermalexpansion.block;

import cofh.api.tileentity.IReconfigurableFacing;
import cofh.api.tileentity.IReconfigurableSides;
import cofh.core.network.PacketCoFHBase;
import cofh.lib.util.helpers.BlockHelper;
import cofh.thermalexpansion.util.ReconfigurableHelper;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;

public abstract class TileReconfigurable extends TilePowered implements IReconfigurableFacing, IReconfigurableSides {

	protected byte facing = 3;
	public byte[] sideCache = { 0, 0, 0, 0, 0, 0 };

	@Override
	public boolean onWrench(EntityPlayer player, EnumFacing side) {

		return rotateBlock(side);
	}

	public byte[] getDefaultSides() {

		return new byte[] { 0, 0, 0, 0, 0, 0 };
	}

	public void setDefaultSides() {

		sideCache = getDefaultSides();
	}

	/* GUI METHODS */
	public final boolean hasSide(int side) {

		for (int i = 0; i < 6; i++) {
			if (sideCache[i] == side) {
				return true;
			}
		}
		return false;
	}

	/* NBT METHODS */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		facing = ReconfigurableHelper.getFacingFromNBT(nbt);
		sideCache = ReconfigurableHelper.getSideCacheFromNBT(nbt, getDefaultSides());
		for (int i = 0; i < 6; i++) {
			if (sideCache[i] >= getNumConfig(EnumFacing.VALUES[i])) {
				sideCache[i] = 0;
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		nbt.setByte("Facing", facing);
		nbt.setByteArray("SideCache", sideCache);

		return nbt;
	}

	/* NETWORK METHODS */
	@Override
	public PacketCoFHBase getPacket() {

		PacketCoFHBase payload = super.getPacket();

		payload.addByteArray(sideCache);
		payload.addByte(facing);

		return payload;
	}

	/* ITilePacketHandler */
	@Override
	public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {

		super.handleTilePacket(payload, isServer);

		payload.getByteArray(sideCache);

		for (int i = 0; i < 6; i++) {
			if (sideCache[i] >= getNumConfig(EnumFacing.VALUES[i])) {
				sideCache[i] = 0;
			}
		}
		if (!isServer) {
			facing = payload.getByte();
		} else {
			payload.getByte();
		}
		callNeighborTileChange();
	}

	/* IReconfigurableFacing */
	@Override
	public final int getFacing() {

		return facing;
	}

	@Override
	public boolean allowYAxisFacing() {

		return false;
	}

	@Override
	public boolean rotateBlock(EnumFacing side) {

		if (allowYAxisFacing()) {
			byte[] tempCache = new byte[6];

			switch (facing) {
			case 0:
				for (int i = 0; i < 6; i++) {
					tempCache[i] = sideCache[BlockHelper.INVERT_AROUND_X[i]];
				}
				break;
			case 1:
				for (int i = 0; i < 6; i++) {
					tempCache[i] = sideCache[BlockHelper.ROTATE_CLOCK_X[i]];
				}
				break;
			case 2:
				for (int i = 0; i < 6; i++) {
					tempCache[i] = sideCache[BlockHelper.INVERT_AROUND_Y[i]];
				}
				break;
			case 3:
				for (int i = 0; i < 6; i++) {
					tempCache[i] = sideCache[BlockHelper.ROTATE_CLOCK_Y[i]];
				}
				break;
			case 4:
				for (int i = 0; i < 6; i++) {
					tempCache[i] = sideCache[BlockHelper.INVERT_AROUND_Z[i]];
				}
				break;
			case 5:
				for (int i = 0; i < 6; i++) {
					tempCache[i] = sideCache[BlockHelper.ROTATE_CLOCK_Z[i]];
				}
				break;
			}
			sideCache = tempCache.clone();
			facing++;
			facing %= 6;
			markDirty();
			sendUpdatePacket(Side.CLIENT);
			return true;
		}
		if (isActive) {
			return false;
		}
		byte[] tempCache = new byte[6];
		for (int i = 0; i < 6; i++) {
			tempCache[i] = sideCache[BlockHelper.ROTATE_CLOCK_Y[i]];
		}
		sideCache = tempCache.clone();
		facing = BlockHelper.SIDE_LEFT[facing];
		markDirty();
		sendUpdatePacket(Side.CLIENT);
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
		markDirty();
		sendUpdatePacket(Side.CLIENT);
		return true;
	}

	/* IReconfigurableSides */
	@Override
	public boolean decrSide(EnumFacing side) {

		int sideInt = side.ordinal();

		if (sideInt == facing) {
			return false;
		}
		sideCache[sideInt] += getNumConfig(side) - 1;
		sideCache[sideInt] %= getNumConfig(side);
		sendUpdatePacket(Side.SERVER);
		return true;
	}

	@Override
	public boolean incrSide(EnumFacing side) {

		int sideInt = side.ordinal();

		if (sideInt == facing) {
			return false;
		}
		sideCache[sideInt] += 1;
		sideCache[sideInt] %= getNumConfig(side);
		sendUpdatePacket(Side.SERVER);
		return true;
	}

	@Override
	public boolean setSide(EnumFacing side, int config) {

		int sideInt = side.ordinal();

		if (sideInt == facing || sideCache[sideInt] == config || config >= getNumConfig(side)) {
			return false;
		}
		sideCache[sideInt] = (byte) config;
		sendUpdatePacket(Side.SERVER);
		return true;
	}

	@Override
	public boolean resetSides() {

		boolean update = false;
		for (int i = 0; i < 6; i++) {
			if (sideCache[i] > 0) {
				sideCache[i] = 0;
				update = true;
			}
		}
		if (update) {
			sendUpdatePacket(Side.SERVER);
		}
		return update;
	}

	@Override
	public abstract int getNumConfig(EnumFacing side);

}
