package cofh.thermalexpansion.block.machine;

import cofh.api.tileentity.IRedstoneControl.ControlMode;
import cofh.core.item.ItemBlockCoFHBase;
import cofh.lib.util.helpers.AugmentHelper;
import cofh.lib.util.helpers.EnergyHelper;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.RedstoneControlHelper;
import cofh.thermalexpansion.util.ReconfigurableHelper;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public class ItemBlockMachine extends ItemBlockCoFHBase {

	public ItemBlockMachine(Block block) {

		super(block);
		setHasSubtypes(true);
		setMaxDamage(0);
		setNoRepair();
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {

		return "tile.thermalexpansion.machine." + BlockMachine.Type.byMetadata(ItemHelper.getItemDamage(stack)).getName() + ".name";
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

	public static final String[] NAMES = { "basic", "hardened", "reinforced", "resonant" };
}
