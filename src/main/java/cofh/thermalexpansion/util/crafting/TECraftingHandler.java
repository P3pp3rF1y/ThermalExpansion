package cofh.thermalexpansion.util.crafting;

import cofh.core.util.crafting.RecipeSecure;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermalexpansion.block.machine.BlockMachine;
import cofh.thermalexpansion.block.simple.BlockFrame;
import cofh.thermalexpansion.core.TEAchievements;
import cofh.thermalexpansion.core.TEProps;
import cofh.thermalfoundation.item.TFItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;

import static cofh.lib.util.helpers.ItemHelper.ShapelessRecipe;

public class TECraftingHandler {

	public static TECraftingHandler instance = new TECraftingHandler();

	private TECraftingHandler() {

	}

	public static void initialize() {

		MinecraftForge.EVENT_BUS.register(instance);
	}

	public static void addMachineRecipes(ItemStack stack, ItemStack augments, Object[] recipe) {

	}

	public static void addMachineUpgradeRecipes(ItemStack stack) {
		//TODO READD JEI wrapper
		GameRegistry.addRecipe(new RecipeMachineUpgrade(1, RecipeMachineUpgrade.getMachineLevel(stack, 1),
				new Object[] { "IGI", " X ", "I I", 'I',
						"ingotInvar", 'G', "gearElectrum", 'X', RecipeMachineUpgrade.getMachineLevel(stack, 0) }));
		GameRegistry.addRecipe(new RecipeMachineUpgrade(2, RecipeMachineUpgrade.getMachineLevel(stack, 2),
				new Object[] { "IGI", " X ", "I I", 'I',
						"blockGlassHardened", 'G', "gearSignalum", 'X', RecipeMachineUpgrade.getMachineLevel(stack, 1) }));
		GameRegistry.addRecipe(new RecipeMachineUpgrade(3, RecipeMachineUpgrade.getMachineLevel(stack, 3),
				new Object[] { "IGI", " X ", "I I", 'I',
						"ingotSilver", 'G', "gearEnderium", 'X', RecipeMachineUpgrade.getMachineLevel(stack, 2) }));
	}

	public static void addSecureRecipe(ItemStack stack) {

		//TODO READD JEI wrapper

		GameRegistry.addRecipe(new RecipeSecure(stack,
				new Object[] { " L ", "SXS", " S ", 'L', TFItems.itemSecurity, 'S', "nuggetSignalum", 'X', stack }));
	}

	@SubscribeEvent
	public void handleOnItemCrafted(PlayerEvent.ItemCraftedEvent event) {

		if (event.crafting == null) {
			return;
		}
		checkAchievements(event.player, event.crafting, event.craftMatrix);
	}

	private void checkAchievements(EntityPlayer player, ItemStack stack, IInventory craftMatrix) {

		if (!TEProps.enableAchievements) {
			return;
		}
		// Crafting Steps
		if (stack.isItemEqual(BlockFrame.frameMachineBasic)) {
			player.addStat(TEAchievements.machineFrame, 1);
		}
		// Machine Achievements
		else if (stack.isItemEqual(BlockMachine.machineFurnace)) {
			player.addStat(TEAchievements.furnace, 1);
		} else if (stack.isItemEqual(BlockMachine.machinePulverizer)) {
			player.addStat(TEAchievements.pulverizer, 1);
		} else if (stack.isItemEqual(BlockMachine.machineSawmill)) {
			player.addStat(TEAchievements.sawmill, 1);
		} else if (stack.isItemEqual(BlockMachine.machineSmelter)) {
			player.addStat(TEAchievements.smelter, 1);
		} else if (stack.isItemEqual(BlockMachine.machineCrucible)) {
			player.addStat(TEAchievements.crucible, 1);
		} else if (stack.isItemEqual(BlockMachine.machineTransposer)) {
			player.addStat(TEAchievements.transposer, 1);
		} else if (stack.isItemEqual(BlockMachine.machinePrecipitator)) {
			player.addStat(TEAchievements.precipitator, 1);
		} else if (stack.isItemEqual(BlockMachine.machineExtruder)) {
			player.addStat(TEAchievements.extruder, 1);
		} else if (stack.isItemEqual(BlockMachine.machineAccumulator)) {
			player.addStat(TEAchievements.accumulator, 1);
		} else if (stack.isItemEqual(BlockMachine.machineAssembler)) {
			player.addStat(TEAchievements.assembler, 1);
		} else if (stack.isItemEqual(BlockMachine.machineCharger)) {
			player.addStat(TEAchievements.charger, 1);
		} else if (stack.isItemEqual(BlockMachine.machineInsolator)) {
			player.addStat(TEAchievements.insolator, 1);
		}

		//TODO READD
		/*
		// Resonant Achievements
		else if (stack.isItemEqual(BlockCell.cellResonant)) {
			player.addStat(TEAchievements.resonantCell, 1);
		} else if (stack.isItemEqual(BlockTank.tankResonant)) {
			player.addStat(TEAchievements.resonantTank, 1);
		} else if (stack.isItemEqual(BlockCache.cacheResonant)) {
			player.addStat(TEAchievements.resonantCache, 1);
		} else if (stack.isItemEqual(BlockStrongbox.strongboxResonant)) {
			player.addStat(TEAchievements.resonantStrongbox, 1);
		}
*/
	}

	public static void loadRecipes() {

		String[] oreNameList = OreDictionary.getOreNames();
		String oreType = "";

		for (int i = 0; i < oreNameList.length; i++) {
			if (oreNameList[i].startsWith("ore")) {
				oreType = oreNameList[i].substring(3, oreNameList[i].length());

				if (oreType.isEmpty()) {
					continue;
				}
				String oreName = "ore" + StringHelper.titleCase(oreType);
				String ingotName = "ingot" + StringHelper.titleCase(oreType);

				List<ItemStack> registeredOre = OreDictionary.getOres(oreName);
				List<ItemStack> registeredIngot = OreDictionary.getOres(ingotName);

				if (registeredOre.size() <= 0 || registeredIngot.size() <= 0) {
					continue;
				}
				ItemStack ingot = ItemHelper.cloneStack(registeredIngot.get(0), 1);
				GameRegistry.addRecipe(ShapelessRecipe(ingot, new Object[] { oreName, "dustPyrotheum" }));
			}
		}
	}
}
