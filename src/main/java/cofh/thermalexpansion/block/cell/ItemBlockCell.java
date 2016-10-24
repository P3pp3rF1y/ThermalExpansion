package cofh.thermalexpansion.block.cell;

import cofh.api.energy.IEnergyContainerItem;
import cofh.api.tileentity.IRedstoneControl.ControlMode;
import cofh.core.item.ItemBlockCoFHBase;
import cofh.lib.util.helpers.EnergyHelper;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.RedstoneControlHelper;
import cofh.lib.util.helpers.SecurityHelper;
import cofh.lib.util.helpers.StringHelper;
import java.util.List;

import cofh.thermalexpansion.util.ReconfigurableHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;

public class ItemBlockCell extends ItemBlockCoFHBase implements IEnergyContainerItem {

	public static ItemStack setDefaultTag(ItemStack container, int energy) {

		ReconfigurableHelper.setFacing(container, 3);
		ReconfigurableHelper.setSideCache(container, ItemHelper.getItemDamage(container) == BlockCell.Type.CREATIVE.ordinal() ? TileCellCreative.DEFAULT_SIDES
				: TileCell.DEFAULT_SIDES);
		RedstoneControlHelper.setControl(container, ControlMode.LOW);
		EnergyHelper.setDefaultEnergyTag(container, energy);
		container.getTagCompound().setInteger("Send", BlockCell.Type.byMetadata(container.getItemDamage()).getMaxSend());
		container.getTagCompound().setInteger("Recv", BlockCell.Type.byMetadata(container.getItemDamage()).getMaxReceive());

		return container;
	}

	public ItemBlockCell(Block block) {

		super(block);
		setHasSubtypes(true);
		setMaxDamage(1);
		setMaxStackSize(1);
		setNoRepair();
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {

		return "tile.thermalexpansion.cell." + BlockCell.Type.byMetadata(ItemHelper.getItemDamage(stack)).getName() + ".name";
	}

	@Override
	public int getDamage(ItemStack stack) {

		if (stack.getTagCompound() == null) {
			return BlockCell.Type.byMetadata(ItemHelper.getItemDamage(stack)).getCapacity();
		}
		return BlockCell.Type.byMetadata(ItemHelper.getItemDamage(stack)).getCapacity() - stack.getTagCompound().getInteger("Energy");
	}

	@Override
	public int getMaxDamage(ItemStack stack) {

		return BlockCell.Type.byMetadata(ItemHelper.getItemDamage(stack)).getCapacity();
	}

	@Override
	public boolean isDamaged(ItemStack stack) {

		return ItemHelper.getItemDamage(stack) != BlockCell.Type.CREATIVE.ordinal();
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {

		switch (BlockCell.Type.values()[ItemHelper.getItemDamage(stack)]) {
		case CREATIVE:
			return EnumRarity.EPIC;
		case RESONANT:
			return EnumRarity.RARE;
		case REINFORCED:
			return EnumRarity.UNCOMMON;
		default:
			return EnumRarity.COMMON;
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean check) {

		if (stack.getTagCompound() == null) {
			setDefaultTag(stack, 0);
		}
		SecurityHelper.addOwnerInformation(stack, list);
		if (StringHelper.displayShiftForDetail && !StringHelper.isShiftKeyDown()) {
			list.add(StringHelper.shiftForDetails());
		}
		if (!StringHelper.isShiftKeyDown()) {
			return;
		}
		SecurityHelper.addAccessInformation(stack, list);

		if (ItemHelper.getItemDamage(stack) == BlockCell.Type.CREATIVE.ordinal()) {
			list.add(StringHelper.localize("info.cofh.charge") + ": 1.21G RF");
		} else {
			list.add(StringHelper.localize("info.cofh.charge") + ": " + StringHelper.getScaledNumber(stack.getTagCompound().getInteger("Energy")) + " / "
					+ StringHelper.getScaledNumber(BlockCell.Type.byMetadata(ItemHelper.getItemDamage(stack)).getCapacity()) + " RF");
		}
		list.add(StringHelper.localize("info.cofh.send") + "/" + StringHelper.localize("info.cofh.receive") + ": " + stack.getTagCompound().getInteger("Send")
				+ "/" + stack.getTagCompound().getInteger("Recv") + " RF/t");

		RedstoneControlHelper.addRSControlInformation(stack, list);
	}

	/* IEnergyContainerItem */
	@Override
	public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {

		if (container.getTagCompound() == null) {
			setDefaultTag(container, 0);
		}
		int metadata = ItemHelper.getItemDamage(container);

		if (metadata == BlockCell.Type.CREATIVE.ordinal()) {
			return 0;
		}
		int stored = container.getTagCompound().getInteger("Energy");
		BlockCell.Type type = BlockCell.Type.byMetadata(metadata);
		int receive = Math.min(maxReceive, Math.min(type.getCapacity() - stored, type.getMaxReceive()));

		if (!simulate) {
			stored += receive;
			container.getTagCompound().setInteger("Energy", stored);
		}
		return receive;
	}

	@Override
	public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {

		if (container.getTagCompound() == null) {
			setDefaultTag(container, 0);
		}
		int metadata = ItemHelper.getItemDamage(container);

		if (metadata == BlockCell.Type.CREATIVE.ordinal()) {
			return maxExtract;
		}
		int stored = container.getTagCompound().getInteger("Energy");
		int extract = Math.min(maxExtract, Math.min(stored, BlockCell.Type.byMetadata(metadata).getMaxSend()));

		if (!simulate) {
			stored -= extract;
			container.getTagCompound().setInteger("Energy", stored);
		}
		return extract;
	}

	@Override
	public int getEnergyStored(ItemStack container) {

		if (container.getTagCompound() == null) {
			setDefaultTag(container, 0);
		}
		return container.getTagCompound().getInteger("Energy");
	}

	@Override
	public int getMaxEnergyStored(ItemStack container) {

		return BlockCell.Type.byMetadata(ItemHelper.getItemDamage(container)).getCapacity();
	}

}
