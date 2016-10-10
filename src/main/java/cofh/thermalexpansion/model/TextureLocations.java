package cofh.thermalexpansion.model;

import cofh.core.CoFHProps;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.block.BlockTEBase;
import cofh.thermalexpansion.block.TileTEBase;
import cofh.thermalexpansion.block.machine.BlockMachine;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TextureLocations {

	private static Map<ResourceLocation, ResourceLocation> txCache = new HashMap<>();
	private static final String CB_SUFFIX = CoFHProps.enableColorBlindTextures ? "_cb" : "";

	public static ResourceLocation addTxFolder(ResourceLocation rl) {
		if (!txCache.keySet().contains(rl)) {
			txCache.put(rl, new ResourceLocation(rl.getResourceDomain(), "textures/" + rl.getResourcePath() + ".png"));
		}
		return txCache.get(rl);
	}

	public static final ResourceLocation MISSING = new ResourceLocation("missingno");

	public static class Config {

		public static final ResourceLocation NONE = new ResourceLocation(ThermalExpansion.modId, "blocks/config/config_none");
		public static final ResourceLocation BLUE = new ResourceLocation(ThermalExpansion.modId, "blocks/config/config_blue" + CB_SUFFIX);
		public static final ResourceLocation GREEN = new ResourceLocation(ThermalExpansion.modId, "blocks/config/config_green" + CB_SUFFIX);
		public static final ResourceLocation OPEN = new ResourceLocation(ThermalExpansion.modId, "blocks/config/config_open");
		public static final ResourceLocation ORANGE = new ResourceLocation(ThermalExpansion.modId, "blocks/config/config_orange" + CB_SUFFIX);
		public static final ResourceLocation PURPLE = new ResourceLocation(ThermalExpansion.modId, "blocks/config/config_purple" + CB_SUFFIX);
		public static final ResourceLocation RED = new ResourceLocation(ThermalExpansion.modId, "blocks/config/config_red" + CB_SUFFIX);
		public static final ResourceLocation YELLOW = new ResourceLocation(ThermalExpansion.modId, "blocks/config/config_yellow" + CB_SUFFIX);
		public static final Map<BlockTEBase.EnumSideConfig, ResourceLocation> CONFIG_MAP = ImmutableMap.<BlockTEBase.EnumSideConfig, ResourceLocation>builder()
				.put(BlockTEBase.EnumSideConfig.NONE, NONE)
				.put(BlockTEBase.EnumSideConfig.BLUE, BLUE)
				.put(BlockTEBase.EnumSideConfig.GREEN, GREEN)
				.put(BlockTEBase.EnumSideConfig.OPEN, OPEN)
				.put(BlockTEBase.EnumSideConfig.ORANGE, ORANGE)
				.put(BlockTEBase.EnumSideConfig.PURPLE, PURPLE)
				.put(BlockTEBase.EnumSideConfig.RED, RED)
				.put(BlockTEBase.EnumSideConfig.YELLOW, YELLOW).build();

		public static final Set<ResourceLocation> ALL = ImmutableSet.copyOf(CONFIG_MAP.values());
	}

	public static class Machine {

		public static final ResourceLocation SIDE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/machine_side");
		public static final ResourceLocation TOP = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/machine_top");
		public static final ResourceLocation BOTTOM = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/machine_bottom");
		public static final Map<EnumFacing, ResourceLocation> SIDE_MAP = ImmutableMap.<EnumFacing, ResourceLocation>builder()
			.put(EnumFacing.DOWN, BOTTOM)
			.put(EnumFacing.UP, TOP)
			.put(EnumFacing.NORTH, SIDE)
			.put(EnumFacing.SOUTH, SIDE)
			.put(EnumFacing.EAST, SIDE)
			.put(EnumFacing.WEST, SIDE).build();

		public static final ResourceLocation PULVERIZER = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/machine_face_pulverizer");
		public static final ResourceLocation FURNACE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/machine_face_furnace");
		public static final ResourceLocation SAWMILL = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/machine_face_sawmill");
		public static final ResourceLocation SMELTER = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/machine_face_smelter");
		public static final ResourceLocation INSOLATOR = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/machine_face_insolator");
		public static final ResourceLocation CHARGER = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/machine_face_charger");
		public static final ResourceLocation CRUCIBLE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/machine_face_crucible");
		public static final ResourceLocation TRANSPOSER = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/machine_face_transposer");
		public static final Map<BlockMachine.Type, ResourceLocation> FACE_MAP = ImmutableMap.<BlockMachine.Type, ResourceLocation>builder()
				.put(BlockMachine.Type.FURNACE, FURNACE)
				.put(BlockMachine.Type.PULVERIZER, PULVERIZER)
				.put(BlockMachine.Type.SAWMILL, SAWMILL)
				.put(BlockMachine.Type.SMELTER, SMELTER)
				.put(BlockMachine.Type.INSOLATOR, INSOLATOR)
				.put(BlockMachine.Type.CHARGER, CHARGER)
				.put(BlockMachine.Type.CRUCIBLE, CRUCIBLE)
				.put(BlockMachine.Type.TRANSPOSER, TRANSPOSER).build();

		public static final ResourceLocation PULVERIZER_ACTIVE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/machine_active_pulverizer");
		public static final ResourceLocation FURNACE_ACTIVE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/machine_active_furnace");
		public static final ResourceLocation SAWMILL_ACTIVE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/machine_active_sawmill");
		public static final ResourceLocation SMELTER_ACTIVE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/machine_active_smelter");
		public static final ResourceLocation INSOLATOR_ACTIVE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/machine_active_insolator");
		public static final ResourceLocation CHARGER_ACTIVE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/machine_active_charger");
		public static final ResourceLocation CRUCIBLE_ACTIVE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/machine_active_crucible");
		public static final ResourceLocation TRANSPOSER_ACTIVE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/machine_active_transposer");
		public static final ResourceLocation CENTRIFUGE_ACTIVE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/machine_active_centrifuge");
		public static final Map<BlockMachine.Type, ResourceLocation> ACTIVE_FACE_MAP = ImmutableMap.<BlockMachine.Type, ResourceLocation>builder()
				.put(BlockMachine.Type.FURNACE, FURNACE_ACTIVE)
				.put(BlockMachine.Type.PULVERIZER, PULVERIZER_ACTIVE)
				.put(BlockMachine.Type.SAWMILL, SAWMILL_ACTIVE)
				.put(BlockMachine.Type.SMELTER, SMELTER_ACTIVE)
				.put(BlockMachine.Type.INSOLATOR, INSOLATOR_ACTIVE)
				.put(BlockMachine.Type.CHARGER, CHARGER_ACTIVE)
				.put(BlockMachine.Type.CRUCIBLE, CRUCIBLE_ACTIVE)
				.put(BlockMachine.Type.TRANSPOSER, TRANSPOSER_ACTIVE).build();

		public static final Set<ResourceLocation> ALL = ImmutableSet.<ResourceLocation>builder()
				.addAll(SIDE_MAP.values()).addAll(FACE_MAP.values()).addAll(ACTIVE_FACE_MAP.values()).build();
	}
}
