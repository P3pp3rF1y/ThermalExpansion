package cofh.thermalexpansion.item;

import cofh.api.core.IInitializer;
import cofh.api.core.IModelRegister;
import cofh.api.item.IAugmentItem;
import cofh.core.item.ItemCoFHBase;
import cofh.core.util.StateMapper;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.block.machine.BlockMachine;
import cofh.thermalexpansion.block.machine.ItemBlockMachine;
import cofh.thermalexpansion.model.ModelMachine;
import cofh.thermalfoundation.item.ItemMaterial;
import gnu.trove.map.TMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static cofh.lib.util.helpers.ItemHelper.ShapedRecipe;
import static cofh.thermalexpansion.item.TEAugments.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ItemAugment extends ItemCoFHBase implements IInitializer, IAugmentItem, IModelRegister {

	public class AugmentEntry {

		public String primaryType = "";
		public int primaryLevel = 0;
		public int numInfo = 1;
		public TObjectIntHashMap<String> augmentTypeInfo = new TObjectIntHashMap<String>();
	}

	TIntObjectHashMap<AugmentEntry> augmentMap = new TIntObjectHashMap<AugmentEntry>();

	public ItemAugment() {

		super("thermalexpansion");

		setUnlocalizedName("augment");
		setCreativeTab(ThermalExpansion.tabItems);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {

		return StringHelper.localize("info.thermalexpansion.augment") + ": " + StringHelper.localize(getUnlocalizedName(stack) + ".name");
	}

	@Override
	public boolean isFull3D() {
		return true;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {

		if (StringHelper.displayShiftForDetail && !StringHelper.isShiftKeyDown()) {
			tooltip.add(StringHelper.shiftForDetails());
		}
		if (!StringHelper.isShiftKeyDown()) {
			return;
		}
		boolean augmentChain = true;
		String type = getPrimaryType(stack);
		tooltip.add(StringHelper.localize("info.thermalexpansion.augment." + type));

		int level = getPrimaryLevel(stack);
		tooltip.add(StringHelper.WHITE + StringHelper.localize("info.cofh.level") + " " + StringHelper.ROMAN_NUMERAL[level] + StringHelper.END);

		int numInfo = getNumInfo(stack);
		for (int i = 0; i < numInfo; i++) {
			tooltip.add(StringHelper.BRIGHT_GREEN + StringHelper.localize("info.thermalexpansion.augment." + type + "." + i) + StringHelper.END);
		}

		/* DYNAMO THROTTLE */
		if (type.equals(TEAugments.DYNAMO_THROTTLE)) {
			augmentChain = false;
			tooltip.add(StringHelper.getNoticeText("info.thermalexpansion.augment.requireRS"));
		}
		/* DYNAMO EFFICIENCY */
		else if (type.equals(TEAugments.DYNAMO_EFFICIENCY)) {
			tooltip.add(StringHelper.BRIGHT_GREEN + "+" + TEAugments.DYNAMO_EFFICIENCY_MOD_SUM[level] + "% "
					+ StringHelper.localize("info.thermalexpansion.augment.fuelEnergy") + StringHelper.END);

		}
		/* DYNAMO OUTPUT */
		else if (type.equals(TEAugments.DYNAMO_OUTPUT)) {
			tooltip.add(StringHelper.BRIGHT_GREEN + "x" + TEAugments.DYNAMO_OUTPUT_MOD[level] + " "
					+ StringHelper.localize("info.thermalexpansion.augment.energyProduced") + StringHelper.END);
			tooltip.add("x" + TEAugments.DYNAMO_OUTPUT_MOD[level] + " " + StringHelper.localize("info.thermalexpansion.augment.fuelConsumed") + StringHelper.END);
			tooltip.add(StringHelper.RED + "-" + TEAugments.DYNAMO_OUTPUT_EFFICIENCY_SUM[level] + "% "
					+ StringHelper.localize("info.thermalexpansion.augment.fuelEnergy") + StringHelper.END);

		}
		/* MACHINE SECONDARY */
		else if (type.equals(TEAugments.MACHINE_SECONDARY)) {
			tooltip.add(StringHelper.BRIGHT_GREEN + "+" + TEAugments.MACHINE_SECONDARY_MOD_TOOLTIP[level] + "% "
					+ StringHelper.localize("info.thermalexpansion.augment.secondaryChance") + StringHelper.END);
			addMachineInfo(tooltip, level);

		}
		/* MACHINE SPEED */
		else if (type.equals(TEAugments.MACHINE_SPEED)) {
			tooltip.add(StringHelper.BRIGHT_GREEN + "x" + TEAugments.MACHINE_SPEED_PROCESS_MOD[level] + " "
					+ StringHelper.localize("info.thermalexpansion.augment.speed") + StringHelper.END);
			tooltip.add(StringHelper.RED + "+" + TEAugments.MACHINE_SPEED_ENERGY_MOD_TOOLTIP[level] + "% "
					+ StringHelper.localize("info.thermalexpansion.augment.energyUsed") + StringHelper.END);
			tooltip.add(StringHelper.YELLOW + "(x" + TEAugments.MACHINE_SPEED_ENERGY_MOD[level] + " RF/t)" + StringHelper.END);
			// list.add(StringHelper.RED + "-" + TEAugments.MACHINE_SPEED_SECONDARY_MOD_TOOLTIP[level] + "% " TODO: May bring this back, not sure.
			// + StringHelper.localize("info.thermalexpansion.augment.secondaryChance") + StringHelper.END);
			addMachineInfo(tooltip, level);

		}
		/* MACHINE - FURNACE */
		else if (type.equals(TEAugments.MACHINE_FURNACE_FOOD)) {
			tooltip.add(StringHelper.BRIGHT_GREEN + "-50% " + StringHelper.localize("info.thermalexpansion.augment.energyUsed"));
			tooltip.add(StringHelper.RED + StringHelper.localize("info.thermalexpansion.augment.machineFurnaceFood.1") + StringHelper.END);

		}
		/* MACHINE - EXTRUDER */
		else if (type.equals(TEAugments.MACHINE_EXTRUDER_BOOST)) {
			tooltip.add(StringHelper.BRIGHT_GREEN + StringHelper.localize("info.thermalexpansion.augment.upTo") + " "
					+ TEAugments.MACHINE_EXTRUDER_PROCESS_MOD[0][level] + " " + Blocks.COBBLESTONE.getLocalizedName() + " "
					+ StringHelper.localize("info.thermalexpansion.augment.perOperation") + StringHelper.END);
			tooltip.add(StringHelper.BRIGHT_GREEN + StringHelper.localize("info.thermalexpansion.augment.upTo") + " "
					+ TEAugments.MACHINE_EXTRUDER_PROCESS_MOD[1][level] + " " + Blocks.STONE.getLocalizedName() + " "
					+ StringHelper.localize("info.thermalexpansion.augment.perOperation") + StringHelper.END);
			tooltip.add(StringHelper.BRIGHT_GREEN + StringHelper.localize("info.thermalexpansion.augment.upTo") + " "
					+ TEAugments.MACHINE_EXTRUDER_PROCESS_MOD[2][level] + " " + Blocks.OBSIDIAN.getLocalizedName() + " "
					+ StringHelper.localize("info.thermalexpansion.augment.perOperation") + StringHelper.END);
			tooltip.add(StringHelper.BRIGHT_GREEN + "-" + (1000 - MACHINE_EXTRUDER_WATER_MOD[level]) / 10D + "% "
					+ StringHelper.localize("info.thermalexpansion.augment.waterConsumed") + StringHelper.END);
			addMachineInfo(tooltip, level);
		}
		if (level > 1 && augmentChain) {
			tooltip.add(StringHelper.getNoticeText("info.thermalexpansion.augment.levels.0"));
			tooltip.add(StringHelper.getNoticeText("info.thermalexpansion.augment.levels.1"));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addMachineInfo(List list, int level) {

		list.add(StringHelper.localize("info.thermalexpansion.augment.machine.0") + " " + getRarity(level)
				+ StringHelper.localize("info.thermalexpansion." + ItemBlockMachine.NAMES[level]) + " " + StringHelper.LIGHT_GRAY
				+ StringHelper.localize("info.thermalexpansion.augment.machine.1"));
	}


	public void addAugmentData(int number, String augmentType, int augmentLevel) {

		addAugmentData(number, augmentType, augmentLevel, 1);
	}

	public void addAugmentData(int number, String augmentType, int augmentLevel, int numInfo) {

		int index = number;

		if (!augmentMap.containsKey(index)) {
			augmentMap.put(index, new AugmentEntry());
			augmentMap.get(index).primaryType = augmentType;
			augmentMap.get(index).primaryLevel = augmentLevel;
			augmentMap.get(index).numInfo = numInfo;
		}
		augmentMap.get(index).augmentTypeInfo.put(augmentType, augmentLevel);
	}

	private String getPrimaryType(ItemStack stack) {

		AugmentEntry entry = augmentMap.get(ItemHelper.getItemDamage(stack));
		if (entry == null) {
			return "";
		}
		return entry.primaryType;
	}

	private int getPrimaryLevel(ItemStack stack) {

		AugmentEntry entry = augmentMap.get(ItemHelper.getItemDamage(stack));
		if (entry == null) {
			return 0;
		}
		return entry.primaryLevel;
	}

	private int getNumInfo(ItemStack stack) {

		AugmentEntry entry = augmentMap.get(ItemHelper.getItemDamage(stack));
		if (entry == null) {
			return 0;
		}
		return entry.numInfo;
	}

	public String getRarity(int level) {

		switch (level) {
			case 2:
				return StringHelper.YELLOW;
			case 3:
				return StringHelper.BRIGHT_BLUE;
			default:
				return StringHelper.WHITE;
		}
	}

	/* IModelRegister */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels() {

		//TODO likely change to enum iteration when refactored
		for (TMap.Entry<Integer, ItemEntry> entry : itemMap.entrySet()) {
			ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation(new ResourceLocation(ThermalExpansion.modId, "augment"), entry.getValue().name);
			ModelLoader.setCustomModelResourceLocation(this, entry.getKey(), itemModelResourceLocation);
		}
	}

	/* IInitializer */
	@Override
	public boolean preInit() {

		GameRegistry.register(setRegistryName(new ResourceLocation(ThermalExpansion.modId, "augment")));

		generalAutoOutput = addItem(0, GENERAL_AUTO_OUTPUT);
		generalAutoInput = addItem(1, GENERAL_AUTO_INPUT);
		generalReconfigSides = addItem(16, GENERAL_RECONFIG_SIDES);
		generalRedstoneControl = addItem(32, GENERAL_REDSTONE_CONTROL);

		addAugmentData(0, GENERAL_AUTO_OUTPUT, 1);
		addAugmentData(1, GENERAL_AUTO_INPUT, 1);
		addAugmentData(16, GENERAL_RECONFIG_SIDES, 1);
		addAugmentData(32, GENERAL_REDSTONE_CONTROL, 1);

		dynamoCoilDuct = addItem(48, DYNAMO_COIL_DUCT);
		addAugmentData(48, DYNAMO_COIL_DUCT, 1);

		dynamoThrottle = addItem(49, DYNAMO_THROTTLE);
		addAugmentData(49, DYNAMO_THROTTLE, 2);

		for (int i = 0; i < NUM_DYNAMO_EFFICIENCY; i++) {
			dynamoEfficiency[i] = addItem(64 + i, DYNAMO_EFFICIENCY + i);
			addAugmentData(64 + i, DYNAMO_EFFICIENCY, 1 + i, 0);
		}
		for (int i = 0; i < NUM_DYNAMO_OUTPUT; i++) {
			dynamoOutput[i] = addItem(80 + i, DYNAMO_OUTPUT + i);
			addAugmentData(80 + i, DYNAMO_OUTPUT, 1 + i, 0);
		}

		for (int i = 0; i < NUM_MACHINE_SECONDARY; i++) {
			machineSecondary[i] = addItem(112 + i, MACHINE_SECONDARY + i);
			addAugmentData(112 + i, MACHINE_SECONDARY, 1 + i, 0);
		}
		for (int i = 0; i < NUM_MACHINE_SPEED; i++) {
			machineSpeed[i] = addItem(128 + i, MACHINE_SPEED + i);
			addAugmentData(128 + i, MACHINE_SPEED, 1 + i, 0);
		}
		machineNull = addItem(144, MACHINE_NULL);
		addAugmentData(144, MACHINE_NULL, 1);

		machineFurnaceFood = addItem(256, MACHINE_FURNACE_FOOD);
		addAugmentData(256, MACHINE_FURNACE_FOOD, 1);

		for (int i = 0; i < NUM_MACHINE_EXTRUDER; i++) {
			machineExtruderBoost[i] = addItem(312 + i, MACHINE_EXTRUDER_BOOST + i);
			addAugmentData(312 + i, MACHINE_EXTRUDER_BOOST, 1 + i, 0);
		}

		return true;
	}

	@Override
	public boolean initialize() {

		return true;
	}

	@Override
	public boolean postInit() {

		//TODO FIX RECIPES
//			/* GENERAL */
//		GameRegistry.addRecipe(ShapedRecipe(generalAutoOutput, " I ", "IXI", " I ", 'I', "nuggetTin", 'X', ItemMaterial.pneumaticServo));
//		GameRegistry.addRecipe(ShapedRecipe(generalAutoInput, " I ", "IXI", " I ", 'I', "nuggetIron", 'X', ItemMaterial.pneumaticServo));
//		GameRegistry.addRecipe(ShapedRecipe(generalReconfigSides, " I ", "IXI", " I ", 'I', "nuggetTin", 'X', "ingotGold"));
//		GameRegistry.addRecipe(ShapedRecipe(generalRedstoneControl, " I ", "IXI", " I ", 'I', "nuggetTin", 'X', "dustRedstone"));
//
//			/* DYNAMO */
//		GameRegistry.addRecipe(ShapedRecipe(dynamoCoilDuct, " I ", "IXI", " I ", 'I', "nuggetLead", 'X', "ingotCopper"));
//		GameRegistry
//				.addRecipe(ShapedRecipe(dynamoThrottle, " I ", "IXI", "YIY", 'I', "nuggetLead", 'X', "ingotElectrum", 'Y', "dustRedstone"));
//
//		GameRegistry.addRecipe(ShapedRecipe(dynamoEfficiency[0], " N ", "NXN", "YNY", 'N', "ingotLead", 'X', ItemMaterial.powerCoilSilver, 'Y', "ingotTin",
//				'Y', "dustRedstone"));
//		GameRegistry.addRecipe(ShapedRecipe(dynamoEfficiency[1], "ZIZ", "NXN", "YIY", 'N', "ingotLead", 'I', "ingotElectrum", 'X', ItemMaterial.powerCoilSilver,
//				'Y', "dustGlowstone", 'Z', "dustRedstone"));
//		GameRegistry.addRecipe(ShapedRecipe(dynamoEfficiency[2], "ZIZ", "IXI", "YIY", 'I', "ingotElectrum", 'X', ItemMaterial.powerCoilSilver,
//				'Y', "dustCryotheum", 'Z', "dustGlowstone"));
//
//		GameRegistry.addRecipe(ShapedRecipe(dynamoOutput[0], " N ", "NXN", "YNY", 'N', "ingotCopper", 'X', ItemMaterial.powerCoilSilver, 'Y', "dustRedstone"));
//		GameRegistry.addRecipe(ShapedRecipe(dynamoOutput[1], "ZIZ", "NXN", "YIY", 'N', "ingotCopper", 'I', "ingotSilver", 'X', ItemMaterial.powerCoilSilver,
//				'Y', "dustGlowstone", 'Z', "dustRedstone"));
//		GameRegistry.addRecipe(ShapedRecipe(dynamoOutput[2], "ZIZ", "IXI", "YIY", 'I', "ingotSilver", 'X', ItemMaterial.powerCoilSilver, 'Y', "dustCryotheum",
//				'Z', "dustGlowstone"));
//
//			/* ENDER */
//
//			/* ENERGY */
//
//			/* MACHINE */
//		GameRegistry
//				.addRecipe(ShapedRecipe(machineSecondary[0], " N ", "NXN", "YNY", 'N', "ingotBronze", 'X', "blockCloth", 'Y', "blockCloth"));
//		GameRegistry.addRecipe(ShapedRecipe(machineSecondary[1], "ZIZ", "NXN", "YIY", 'N', "ingotBronze", 'I', "blockGlassHardened", 'X', "blockClothRock",
//				'Y', "dustGlowstone", 'Z', "blockCloth"));
//		GameRegistry.addRecipe(ShapedRecipe(machineSecondary[2], "ZIZ", "IXI", "YIY", 'I', "blockGlassHardened", 'X', ItemMaterial.pneumaticServo,
//				'Y', "dustCryotheum", 'Z', "dustGlowstone"));
//
//		GameRegistry.addRecipe(ShapedRecipe(machineSpeed[0], " N ", "NXN", "YNY", 'N', "ingotBronze", 'X', ItemMaterial.powerCoilGold, 'Y', "dustRedstone"));
//		GameRegistry.addRecipe(ShapedRecipe(machineSpeed[1], "ZIZ", "NXN", "YIY", 'N', "ingotBronze", 'I', "ingotGold", 'X', ItemMaterial.powerCoilGold,
//				'Y', "dustPyrotheum", 'Z', "dustRedstone"));
//		GameRegistry.addRecipe(ShapedRecipe(machineSpeed[2], "ZIZ", "IXI", "YIY", 'I', "ingotGold", 'X', ItemMaterial.powerCoilGold, 'Y', Items.ENDER_PEARL,
//				'Z', "dustPyrotheum"));
//
//		GameRegistry.addRecipe(ShapedRecipe(ItemAugment.machineNull, " I ", "NXN", "YIY", 'N', "ingotInvar", 'I', "ingotSilver", 'X', Items.LAVA_BUCKET,
//				'Y', "dustRedstone"));
//
//			/* MACHINE SPECIFIC */
//		GameRegistry.addRecipe(ShapedRecipe(ItemAugment.machineFurnaceFood, " I ", "NXN", "YIY", 'N', "dustRedstone", 'I', "ingotSilver",
//				'X', ItemMaterial.powerCoilGold, 'Y', Blocks.BRICK_BLOCK));
//
//		GameRegistry.addRecipe(ShapedRecipe(machineExtruderBoost[0], " N ", "NXN", "YNY", 'N', "ingotBronze", 'X', ItemMaterial.pneumaticServo,
//				'Y', Blocks.COBBLESTONE));
//		GameRegistry.addRecipe(ShapedRecipe(machineExtruderBoost[1], "ZIZ", "NXN", "YIY", 'N', "ingotBronze", 'I', "ingotGold",
//				'X', ItemMaterial.pneumaticServo, 'Y', Blocks.STONE, 'Z', Blocks.COBBLESTONE));
//		GameRegistry.addRecipe(ShapedRecipe(machineExtruderBoost[2], "ZIZ", "IXI", "YIY", 'I', "ingotGold", 'X', ItemMaterial.pneumaticServo,
//				'Y', Blocks.OBSIDIAN, 'Z', Blocks.STONE));

		return true;
	}

	/* REFERENCES */
	public static ItemStack generalAutoOutput;
	public static ItemStack generalAutoInput;
	public static ItemStack generalReconfigSides;
	public static ItemStack generalRedstoneControl;

	public static ItemStack dynamoCoilDuct;
	public static ItemStack dynamoThrottle;

	public static ItemStack machineNull;
	public static ItemStack machineFurnaceFood;


	/* IAugmentItem */
	@Override
	public int getAugmentLevel(ItemStack stack, String type) {

		AugmentEntry entry = augmentMap.get(ItemHelper.getItemDamage(stack));
		if (!entry.augmentTypeInfo.containsKey(type)) {
			return 0;
		}
		return entry.augmentTypeInfo.get(type);	}

	@Override
	public Set<String> getAugmentTypes(ItemStack stack) {

		return augmentMap.get(ItemHelper.getItemDamage(stack)).augmentTypeInfo.keySet();
	}
}
