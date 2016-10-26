package cofh.thermalexpansion.block.simple;

import cofh.core.item.ItemBlockCoFHBase;
import cofh.lib.util.helpers.ItemHelper;
import net.minecraft.block.Block;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;

public class ItemBlockFrame extends ItemBlockCoFHBase {

	public ItemBlockFrame(Block block) {

		super(block);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {

		return "tile.thermalexpansion.frame." + BlockFrame.Type.byMetadata(ItemHelper.getItemDamage(stack)).getName();
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {

		switch (BlockFrame.Type.byMetadata(ItemHelper.getItemDamage(stack))) {
		case MACHINE_RESONANT:
		case CELL_RESONANT_FULL:
		case TESSERACT_FULL:
			return EnumRarity.RARE;
		case MACHINE_REINFORCED:
		case CELL_REINFORCED_FULL:
			return EnumRarity.UNCOMMON;
		default:
			return EnumRarity.COMMON;
		}
	}

}
