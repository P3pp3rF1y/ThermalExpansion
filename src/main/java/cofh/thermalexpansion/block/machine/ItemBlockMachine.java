package cofh.thermalexpansion.block.machine;

import cofh.api.tileentity.IRedstoneControl.ControlMode;
import cofh.core.item.ItemBlockCoFHBase;
import cofh.lib.util.helpers.*;
import cofh.thermalexpansion.util.ReconfigurableHelper;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.translation.I18n;

import java.util.List;

public class ItemBlockMachine extends ItemBlockCoFHBase {

	public ItemBlockMachine(Block block) {

		super(block);
		setHasSubtypes(true);
		setMaxDamage(0);
		setNoRepair();
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {

		return "tile.thermalexpansion.machine." + BlockMachine.Type.byMetadata(ItemHelper.getItemDamage(stack)).getName();
	}

	/* HELPERS */
	public static ItemStack setDefaultTag(ItemStack container) {

		return setDefaultTag(container, (byte) 0);
	}

	public static ItemStack setDefaultTag(ItemStack container, byte level) {

		ReconfigurableHelper.setFacing(container, 3);
		ReconfigurableHelper.setSideCache(container, TileMachineBase.DEFAULT_SIDE_CONFIG[container.getItemDamage()].defaultSides);
		RedstoneControlHelper.setControl(container, ControlMode.DISABLED);
		EnergyHelper.setDefaultEnergyTag(container, 0);
		container.getTagCompound().setByte("Level", level);
		AugmentHelper.writeAugments(container, BlockMachine.defaultAugments);

		return container;
	}

	public static byte getLevel(ItemStack container) {

		if (!container.hasTagCompound()) {
			setDefaultTag(container);
		}
		return container.getTagCompound().getByte("Level");
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {

		String unloc = getUnlocalizedNameInefficiently(stack);
		String unloc2 = '.' + NAMES[getLevel(stack)];

		if (I18n.canTranslate(unloc + unloc2 + ".name")) {
			return StringHelper.localize(unloc + unloc2 + ".name");
		}

		return StringHelper.localize(unloc + ".name") + " (" + StringHelper.localize("info.thermalexpansion" + unloc2) + ")";
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {

		switch (getLevel(stack)) {
			case 3:
				return EnumRarity.RARE;
			case 2:
				return EnumRarity.UNCOMMON;
			default:
				return EnumRarity.COMMON;
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {

		SecurityHelper.addOwnerInformation(stack, tooltip);
		if (StringHelper.displayShiftForDetail && !StringHelper.isShiftKeyDown()) {
			tooltip.add(StringHelper.shiftForDetails());
		}
		if (!StringHelper.isShiftKeyDown()) {
			return;
		}
		SecurityHelper.addAccessInformation(stack, tooltip);

		tooltip.add(StringHelper.getInfoText("info.thermalexpansion.machine." + BlockMachine.Type.byMetadata(ItemHelper.getItemDamage(stack)).getName()));

		RedstoneControlHelper.addRSControlInformation(stack, tooltip);
	}

	public static final String[] NAMES = { "basic", "hardened", "reinforced", "resonant" };
}
