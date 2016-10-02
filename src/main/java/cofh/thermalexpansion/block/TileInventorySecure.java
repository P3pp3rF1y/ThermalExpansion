package cofh.thermalexpansion.block;

import cofh.api.tileentity.ISecurable;
import cofh.core.CoFHProps;
import cofh.core.network.PacketCoFHBase;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.SecurityHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.lib.util.helpers.StringHelper;
import cofh.lib.util.helpers.TransferHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.gui.GuiHandler;
import com.google.common.base.Strings;
import com.mojang.authlib.GameProfile;

import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PreYggdrasilConverter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public abstract class TileInventorySecure extends TileTEBase implements IInventory, ISecurable {

	protected GameProfile owner = CoFHProps.DEFAULT_OWNER;
	protected AccessMode access = AccessMode.PUBLIC;
	protected boolean canAccess = true;

	public ItemStack[] inventory = new ItemStack[0];

	public boolean enableSecurity() {

		return true;
	}

	public boolean isSecured() {

		return !SecurityHelper.isDefaultUUID(owner.getId());
	}

	/* ITEM TRANSFER */
	public boolean extractItem(int index, int count, EnumFacing side) {

		if (index > inventory.length) {
			return false;
		}
		ItemStack stack = inventory[index];

		if (stack != null) {
			count = Math.min(count, stack.getMaxStackSize() - stack.stackSize);
			stack = inventory[index].copy();
		}
		int initialCount = count;
		TileEntity tile = BlockHelper.getAdjacentTileEntity(this, side);

		if (TransferHelper.isAccessibleInput(tile, side)) {
			if (tile instanceof ISidedInventory) {
				ISidedInventory sidedInv = (ISidedInventory) tile;
				int slots[] = sidedInv.getSlotsForFace(side.getOpposite());

				if (slots == null) {
					return false;
				}
				for (int i = 0; i < slots.length && count > 0; i++) {
					ItemStack queryStack = sidedInv.getStackInSlot(slots[i]);
					if (queryStack == null) {
						continue;
					}
					if (sidedInv.canExtractItem(slots[i], queryStack, side.getOpposite())) {
						if (stack == null) {
							if (isItemValidForSlot(index, queryStack)) {
								int toExtract = Math.min(count, queryStack.stackSize);
								stack = ItemHelper.cloneStack(queryStack, toExtract);
								queryStack.stackSize -= toExtract;

								if (queryStack.stackSize <= 0) {
									sidedInv.setInventorySlotContents(slots[i], null);
								} else {
									sidedInv.setInventorySlotContents(slots[i], queryStack);
								}
								count -= toExtract;
							}
						} else if (ItemHelper.itemsIdentical(stack, queryStack)) {
							int toExtract = Math.min(stack.getMaxStackSize() - stack.stackSize, Math.min(count, queryStack.stackSize));
							stack.stackSize += toExtract;
							queryStack.stackSize -= toExtract;

							if (queryStack.stackSize <= 0) {
								sidedInv.setInventorySlotContents(slots[i], null);
							} else {
								sidedInv.setInventorySlotContents(slots[i], queryStack);
							}
							count -= toExtract;
						}
					}
				}
			} else {
				IInventory inv = (IInventory) tile;
				for (int i = 0; i < inv.getSizeInventory() && count > 0; i++) {
					ItemStack queryStack = inv.getStackInSlot(i);
					if (queryStack == null) {
						continue;
					}
					if (stack == null) {
						if (isItemValidForSlot(index, queryStack)) {
							int toExtract = Math.min(count, queryStack.stackSize);
							stack = ItemHelper.cloneStack(queryStack, toExtract);
							queryStack.stackSize -= toExtract;

							if (queryStack.stackSize <= 0) {
								inv.setInventorySlotContents(i, null);
							} else {
								inv.setInventorySlotContents(i, queryStack);
							}
							count -= toExtract;
						}
					} else if (ItemHelper.itemsEqualWithMetadata(stack, queryStack)) {
						int toExtract = Math.min(stack.getMaxStackSize() - stack.stackSize, Math.min(count, queryStack.stackSize));
						stack.stackSize += toExtract;
						queryStack.stackSize -= toExtract;

						if (queryStack.stackSize <= 0) {
							inv.setInventorySlotContents(i, null);
						} else {
							inv.setInventorySlotContents(i, queryStack);
						}
						count -= toExtract;
					}
				}
			}
			if (initialCount != count) {
				inventory[index] = stack;
				tile.markDirty();
				return true;
			}
		}
		return false;
	}

	public boolean transferItem(int index, int count, EnumFacing side) {

		if (inventory[index] == null || index > inventory.length) {
			return false;
		}
		ItemStack stack = inventory[index].copy();
		count = Math.min(count, stack.stackSize);
		stack.stackSize = count;
		int added = 0;

		TileEntity tile = BlockHelper.getAdjacentTileEntity(this, side);
		/* Add to Adjacent Inventory */
		if (TransferHelper.isAccessibleOutput(tile, side)) {
			added = TransferHelper.addToInsertion(tile, stack, side);
			if (added >= count) {
				return false;
			}
			inventory[index].stackSize -= count - added;
			if (inventory[index].stackSize <= 0) {
				inventory[index] = null;
			}
			return true;
		}
		added = 0;
		// TODO: BC Pipes
		/* Add to Adjacent Pipe */
		//		if (Utils.isPipeTile(curTile)) {
		//			added = Utils.addToPipeTile(tile, side, stack);
		//			if (added <= 0) {
		//				return false;
		//			}
		//			inventory[index].stackSize -= added;
		//			if (inventory[index].stackSize <= 0) {
		//				inventory[index] = null;
		//			}
		//			return true;
		//		}
		return false;
	}

	/* GUI METHODS */
	@Override
	public int getInvSlotCount() {

		return inventory.length;
	}

	public boolean canAccess() {

		return canAccess;
	}

	@Override
	public boolean hasGui() {

		return true;
	}

	@Override
	public boolean openGui(EntityPlayer player) {

		if (canPlayerAccess(player)) {
			if (hasGui()) {
				player.openGui(ThermalExpansion.instance, GuiHandler.TILE_ID, worldObj, pos.getX(), pos.getY(), pos.getZ());
			}
			return hasGui();
		}
		if (ServerHelper.isServerWorld(worldObj)) {
			player.addChatMessage(new TextComponentTranslation("chat.cofh.secure", getOwnerName()));
		}
		return false;
	}

	@Override
	public void receiveGuiNetworkData(int id, int data) {

		if (data == 0) {
			canAccess = false;
		} else {
			canAccess = true;
		}
	}

	@Override
	public void sendGuiNetworkData(Container container, IContainerListener listener) {

		super.sendGuiNetworkData(container, listener);

		listener.sendProgressBarUpdate(container, 0, canPlayerAccess(((EntityPlayer) listener)) ? 1 : 0);
	}

	/* NBT METHODS */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		owner = CoFHProps.DEFAULT_OWNER;
		access = AccessMode.values()[nbt.getByte("Access")];

		String uuid = nbt.getString("OwnerUUID");
		String name = nbt.getString("Owner");
		if (!Strings.isNullOrEmpty(uuid)) {
			setOwner(new GameProfile(UUID.fromString(uuid), name));
		} else {
			setOwnerName(name);
		}

		if (!enableSecurity()) {
			access = AccessMode.PUBLIC;
		}
		readInventoryFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		nbt.setByte("Access", (byte) access.ordinal());
		nbt.setString("OwnerUUID", owner.getId().toString());
		nbt.setString("Owner", owner.getName());

		writeInventoryToNBT(nbt);

		return nbt;
	}

	public void readInventoryFromNBT(NBTTagCompound nbt) {

		NBTTagList list = nbt.getTagList("Inventory", 10);
		inventory = new ItemStack[inventory.length];
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			int slot = tag.getInteger("Slot");
			if (slot >= 0 && slot < inventory.length) {
				inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
			}
		}
	}

	public void writeInventoryToNBT(NBTTagCompound nbt) {

		if (inventory.length <= 0) {
			return;
		}
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < inventory.length; i++) {
			if (inventory[i] != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setInteger("Slot", i);
				inventory[i].writeToNBT(tag);
				list.appendTag(tag);
			}
		}
		nbt.setTag("Inventory", list);
	}

	/* NETWORK METHODS */
	@Override
	public PacketCoFHBase getPacket() {

		PacketCoFHBase payload = super.getPacket();

		payload.addByte((byte) access.ordinal());
		payload.addUUID(owner.getId());
		payload.addString(owner.getName());

		return payload;
	}

	/* ITilePacketHandler */
	@Override
	public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {

		super.handleTilePacket(payload, isServer);

		access = ISecurable.AccessMode.values()[payload.getByte()];

		if (!isServer) {
			owner = CoFHProps.DEFAULT_OWNER;
			setOwner(new GameProfile(payload.getUUID(), payload.getString()));
		} else {
			payload.getUUID();
			payload.getString();
		}
	}

	/* IInventory */
	@Override
	public int getSizeInventory() {

		return inventory.length;
	}

	@Override
	public ItemStack getStackInSlot(int index) {

		return inventory[index];
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {

		if (inventory[index] == null) {
			return null;
		}
		if (inventory[index].stackSize <= count) {
			count = inventory[index].stackSize;
		}
		ItemStack stack = inventory[index].splitStack(count);

		if (inventory[index].stackSize <= 0) {
			inventory[index] = null;
		}
		return stack;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {

		if (inventory[index] == null) {
			return null;
		}
		ItemStack stack = inventory[index];
		inventory[index] = null;
		return stack;
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {

		inventory[index] = stack;

		if (stack != null && stack.stackSize > getInventoryStackLimit()) {
			stack.stackSize = getInventoryStackLimit();
		}
		if (inWorld) {
			markChunkDirty();
		}
	}

	@Override
	public int getInventoryStackLimit() {

		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {

		return isUsable(player);
	}

	@Override
	public void openInventory(EntityPlayer player) {

	}

	@Override
	public void closeInventory(EntityPlayer player) {

	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {

		return true;
	}

	@Override
	public int getField(int id) {

		return 0;
	}

	@Override
	public void setField(int id, int value) {

	}

	@Override
	public int getFieldCount() {

		return 0;
	}

	@Override
	public void clear() {

		inventory = new ItemStack[inventory.length];
	}

	/* IWorldNameable */
	@Override
	public ITextComponent getDisplayName() {

		return tileName.isEmpty() ? new TextComponentString(getName()) : new TextComponentString(tileName);
	}

	@Override
	public boolean hasCustomName() {

		return !tileName.isEmpty();
	}

	/* ISecurable */
	@Override
	public boolean setAccess(AccessMode access) {

		this.access = access;
		sendUpdatePacket(Side.SERVER);
		return true;
	}

	@Override
	public boolean setOwnerName(String name) {

		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();

		if (server == null) {
			return false;
		}
		if (Strings.isNullOrEmpty(name) || CoFHProps.DEFAULT_OWNER.getName().equalsIgnoreCase(name)) {
			return false;
		}
		String uuid = PreYggdrasilConverter.convertMobOwnerIfNeeded(server, name);
		if (Strings.isNullOrEmpty(uuid)) {
			return false;
		}
		return setOwner(new GameProfile(UUID.fromString(uuid), name));
	}

	@Override
	public boolean setOwner(GameProfile profile) {

		if (SecurityHelper.isDefaultUUID(owner.getId())) {
			owner = profile;
			if (!SecurityHelper.isDefaultUUID(owner.getId())) {
				if (FMLCommonHandler.instance().getMinecraftServerInstance() != null) {
					new Thread("CoFH User Loader") {

						@Override
						public void run() {

							owner = SecurityHelper.getProfile(owner.getId(), owner.getName());
						}
					}.start();
				}
				if (inWorld) {
					markChunkDirty();
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public AccessMode getAccess() {

		return access;
	}

	@Override
	public String getOwnerName() {

		String name = owner.getName();
		if (name == null) {
			return StringHelper.localize("info.cofh.anotherplayer");
		}
		return name;
	}

	@Override
	public GameProfile getOwner() {

		return owner;
	}

}
