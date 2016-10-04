package cofh.thermalexpansion.gui.container;

import cofh.api.tileentity.IAugmentable;
import cofh.core.block.TileCoFHBase;
import cofh.lib.gui.container.ContainerBase;
import cofh.lib.gui.container.IAugmentableContainer;
import cofh.lib.gui.slot.SlotAugment;
import cofh.lib.util.helpers.AugmentHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermalexpansion.network.PacketTEBase;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class ContainerTEBase extends ContainerBase implements IAugmentableContainer {

	public final TileCoFHBase baseTile;

	protected Slot[] augmentSlots = new Slot[0];
	protected boolean[] augmentStatus = new boolean[0];

	protected boolean augmentLock = true;

	protected boolean hasAugSlots = true;
	protected boolean hasPlayerInvSlots = true;

	public ContainerTEBase() {

		baseTile = null;
	}

	public ContainerTEBase(TileEntity tile) {

		baseTile = (TileCoFHBase) tile;
	}

	public ContainerTEBase(InventoryPlayer inventory, TileEntity tile) {

		this(inventory, tile, true, true);
	}

	public ContainerTEBase(InventoryPlayer inventory, TileEntity tile, boolean augSlots, boolean playerInvSlots) {

		if (tile instanceof TileCoFHBase) {
			baseTile = (TileCoFHBase) tile;
		} else {
			baseTile = null;
		}
		hasAugSlots = augSlots;
		hasPlayerInvSlots = playerInvSlots;

		/* Augment Slots */
		if (hasAugSlots) {
			addAugmentSlots();
		}

		/* Player Inventory */
		if (hasPlayerInvSlots) {
			bindPlayerInventory(inventory);
		}
	}

	@Override
	protected int getPlayerInventoryVerticalOffset() {

		return 84;
	}

	@Override
	protected int getSizeInventory() {

		if (baseTile instanceof IInventory) {
			return ((IInventory) baseTile).getSizeInventory();
		}
		return 0;
	}

	protected void addAugmentSlots() {

		if (baseTile instanceof IAugmentable) {
			augmentSlots = new Slot[((IAugmentable) baseTile).getAugmentSlots().length];
			for (int i = 0; i < augmentSlots.length; i++) {
				augmentSlots[i] = addSlotToContainer(new SlotAugment((IAugmentable) baseTile, null, i, 0, 0));
			}
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {

		return baseTile == null ? true : baseTile.isUsable(player);
	}

	@Override
	public void detectAndSendChanges() {

		super.detectAndSendChanges();

		if (baseTile == null) {
			return;
		}
		for (int i = 0; i < listeners.size(); i++) {
			baseTile.sendGuiNetworkData(this, listeners.get(i));
		}
	}

	@Override
	public void updateProgressBar(int i, int j) {

		if (baseTile == null) {
			return;
		}
		baseTile.receiveGuiNetworkData(i, j);
	}

	@Override
	protected boolean performMerge(int slotIndex, ItemStack stack) {

		int invAugment = augmentSlots.length;
		int invPlayer = invAugment + 27;
		int invFull = invPlayer + 9;
		int invTile = invFull + (baseTile == null ? 0 : baseTile.getInvSlotCount());

		if (slotIndex < invAugment) {
			return mergeItemStack(stack, invAugment, invFull, true);
		} else if (slotIndex < invFull) {
			if (!augmentLock && invAugment > 0 && AugmentHelper.isAugmentItem(stack)) {
				return mergeItemStack(stack, 0, invAugment, false);
			}
			return mergeItemStack(stack, invFull, invTile, false);
		}
		return mergeItemStack(stack, invAugment, invFull, true);
	}

	/* IAugmentableContainer */
	@Override
	public void setAugmentLock(boolean lock) {

		augmentLock = lock;

		if (ServerHelper.isClientWorld(baseTile.getWorld())) {
			PacketTEBase.sendTabAugmentPacketToServer(lock);
		}
	}

	@Override
	public Slot[] getAugmentSlots() {

		return augmentSlots;
	}

}
