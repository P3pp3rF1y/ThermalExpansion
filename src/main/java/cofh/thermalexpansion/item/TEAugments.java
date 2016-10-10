package cofh.thermalexpansion.item;

import net.minecraft.item.ItemStack;

public class TEAugments {

	private TEAugments() {

	}

	//TODO cleanup and use enum(s) instead / may need a base augment item and then sub ones separately for different machines
	public static byte NUM_DYNAMO_EFFICIENCY = 3;
	public static byte NUM_DYNAMO_OUTPUT = 3;

	public static byte NUM_ENERGY_STORAGE = 3;

	public static byte NUM_MACHINE_SECONDARY = 3;
	public static byte NUM_MACHINE_SPEED = 3;

	public static byte NUM_MACHINE_EXTRUDER = 3;
	public static byte NUM_MACHINE_CHARGER = 3;

	public static final int[] DYNAMO_EFFICIENCY_MOD = { 0, 10, 25, 50 };
	public static final int[] DYNAMO_EFFICIENCY_MOD_SUM = { 0, 10, 35, 85 };
	public static final int[] DYNAMO_OUTPUT_MOD = { 1, 2, 4, 8 };
	public static final int[] DYNAMO_OUTPUT_EFFICIENCY_MOD = { 0, 15, 10, 5 };
	public static final int[] DYNAMO_OUTPUT_EFFICIENCY_SUM = { 0, 15, 25, 30 };

	public static final int[] ENERGY_STORAGE_MOD = { 1, 2, 4, 8 };

	public static final int[] MACHINE_SPEED_PROCESS_MOD = { 1, 2, 4, 8 };
	public static final int[] MACHINE_SPEED_ENERGY_MOD = { 1, 3, 8, 20 };
	public static final int[] MACHINE_SPEED_ENERGY_MOD_TOOLTIP = { 1, 50, 100, 150 };
	// public static final int[] MACHINE_SPEED_SECONDARY_MOD = { 0, 5, 10, 15 }; TODO: May bring this back; not sure.
	// public static final int[] MACHINE_SPEED_SECONDARY_MOD_TOOLTIP = { 0, 5, 15, 25 };
	public static final int[] MACHINE_SECONDARY_MOD = { 0, 10, 15, 20 };
	public static final int[] MACHINE_SECONDARY_MOD_TOOLTIP = { 0, 11, 33, 81 };

	public static final int[][] MACHINE_EXTRUDER_PROCESS_MOD = { { 1, 16, 32, 64 }, { 1, 8, 16, 32 }, { 1, 4, 8, 16 } };
	public static final int[] MACHINE_EXTRUDER_WATER_MOD = { 1000, 500, 250, 125 };

	//	public static ItemAugment itemAugment;
	//
	public static ItemStack dynamoCoilDuct;
	public static ItemStack[] dynamoEfficiency = new ItemStack[NUM_DYNAMO_EFFICIENCY];
	public static ItemStack[] dynamoOutput = new ItemStack[NUM_DYNAMO_OUTPUT];
	public static ItemStack dynamoThrottle;

	public static ItemStack enderEnergy;
	public static ItemStack enderFluid;
	public static ItemStack enderItem;

	public static ItemStack[] machineSecondary = new ItemStack[NUM_MACHINE_SECONDARY];
	public static ItemStack[] machineSpeed = new ItemStack[NUM_MACHINE_SPEED];

	public static ItemStack[] machineExtruderBoost = new ItemStack[NUM_MACHINE_EXTRUDER];
	public static ItemStack[] machineChargerBoost = new ItemStack[NUM_MACHINE_CHARGER];

	/* Augment Helpers */
	public static String DYNAMO_COIL_DUCT = "dynamoCoilDuct";
	public static String DYNAMO_EFFICIENCY = "dynamoEfficiency";
	public static String DYNAMO_OUTPUT = "dynamoOutput";
	public static String DYNAMO_THROTTLE = "dynamoThrottle";

	//TODO add these augments in the future
	public static String ENDER_ENERGY = "enderEnergy";
	public static String ENDER_FLUID = "enderFluid";
	public static String ENDER_ITEM = "enderItem";

	public static String ENERGY_STORAGE = "energyStorage";

	public static String GENERAL_AUTO_OUTPUT = "generalAutoOutput";
	public static String GENERAL_AUTO_INPUT = "generalAutoInput";
	public static String GENERAL_RECONFIG_SIDES = "generalReconfigSides";
	public static String GENERAL_REDSTONE_CONTROL = "generalRedstoneControl";

	public static String MACHINE_SECONDARY = "machineSecondary";
	public static String MACHINE_SPEED = "machineSpeed";

	public static String MACHINE_NULL = "machineNull";

	public static String MACHINE_FURNACE_FOOD = "machineFurnaceFood";
	public static String MACHINE_EXTRUDER_BOOST = "machineExtruderBoost";
	public static String MACHINE_CHARGER_BOOST = "machineChargerBoost";

}
