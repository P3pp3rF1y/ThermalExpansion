package cofh.thermalexpansion.block.device;

import cofh.api.tileentity.IRedstoneControl.ControlMode;
import cofh.core.item.ItemBlockCoFHBase;
import cofh.lib.util.helpers.AugmentHelper;
import cofh.lib.util.helpers.EnergyHelper;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.RedstoneControlHelper;
import cofh.thermalexpansion.util.ReconfigurableHelper;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class ItemBlockDevice extends ItemBlockCoFHBase {

	public ItemBlockDevice(Block block) {

		super(block);
		setHasSubtypes(true);
		setMaxDamage(0);
		setNoRepair();
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {

		return "tile.thermalexpansion.device." + BlockDevice.Type.byMetadata(ItemHelper.getItemDamage(stack)).getName() + ".name";
	}

	/* HELPERS */
	public static ItemStack setDefaultTag(ItemStack container) {

		ReconfigurableHelper.setFacing(container, 3);
		ReconfigurableHelper.setSideCache(container, TileDeviceBase.DEFAULT_SIDE_CONFIG[container.getItemDamage()].defaultSides);
		RedstoneControlHelper.setControl(container, ControlMode.DISABLED);
		EnergyHelper.setDefaultEnergyTag(container, 0);
		// TODO: FIX
		// AugmentHelper.writeAugments(container, BlockDevice.defaultAugments);

		return container;
	}

}
