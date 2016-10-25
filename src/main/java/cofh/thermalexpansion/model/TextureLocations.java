package cofh.thermalexpansion.model;

import cofh.core.CoFHProps;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.block.BlockTEBase;
import cofh.thermalexpansion.block.cell.BlockCell;
import cofh.thermalexpansion.block.machine.BlockMachine;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;
import java.util.Set;

public class TextureLocations {

	private static final String CB_SUFFIX = CoFHProps.enableColorBlindTextures ? "_cb" : "";

	public static final ResourceLocation MISSING = new ResourceLocation("missingno");

	public static class Config {

		public static final ResourceLocation NONE = new ResourceLocation(ThermalExpansion.modId, "blocks/config/config_none");
		public static final ResourceLocation BLUE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/config/config_blue" + CB_SUFFIX);
		public static final ResourceLocation GREEN = new ResourceLocation(ThermalExpansion.modId,
				"blocks/config/config_green" + CB_SUFFIX);
		public static final ResourceLocation OPEN = new ResourceLocation(ThermalExpansion.modId, "blocks/config/config_open");
		public static final ResourceLocation ORANGE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/config/config_orange" + CB_SUFFIX);
		public static final ResourceLocation PURPLE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/config/config_purple" + CB_SUFFIX);
		public static final ResourceLocation RED = new ResourceLocation(ThermalExpansion.modId,
				"blocks/config/config_red" + CB_SUFFIX);
		public static final ResourceLocation YELLOW = new ResourceLocation(ThermalExpansion.modId,
				"blocks/config/config_yellow" + CB_SUFFIX);
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
		public static final ResourceLocation BOTTOM = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_bottom");
		public static final Map<EnumFacing, ResourceLocation> SIDE_MAP = ImmutableMap.<EnumFacing, ResourceLocation>builder()
				.put(EnumFacing.DOWN, BOTTOM)
				.put(EnumFacing.UP, TOP)
				.put(EnumFacing.NORTH, SIDE)
				.put(EnumFacing.SOUTH, SIDE)
				.put(EnumFacing.EAST, SIDE)
				.put(EnumFacing.WEST, SIDE).build();

		public static final ResourceLocation PULVERIZER = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_face_pulverizer");
		public static final ResourceLocation FURNACE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_face_furnace");
		public static final ResourceLocation SAWMILL = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_face_sawmill");
		public static final ResourceLocation SMELTER = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_face_smelter");
		public static final ResourceLocation INSOLATOR = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_face_insolator");
		public static final ResourceLocation CHARGER = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_face_charger");
		public static final ResourceLocation CRUCIBLE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_face_crucible");
		public static final ResourceLocation TRANSPOSER = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_face_transposer");
		public static final ResourceLocation ACCUMULATOR = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_face_accumulator");
		public static final ResourceLocation ASSEMBLER = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_face_assembler");
		public static final ResourceLocation EXTRUDER = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_face_extruder");
		public static final ResourceLocation PRECIPITATOR = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_face_precipitator");
		public static final Map<BlockMachine.Type, ResourceLocation> FACE_MAP = ImmutableMap.<BlockMachine.Type, ResourceLocation>builder()
				.put(BlockMachine.Type.FURNACE, FURNACE)
				.put(BlockMachine.Type.PULVERIZER, PULVERIZER)
				.put(BlockMachine.Type.SAWMILL, SAWMILL)
				.put(BlockMachine.Type.SMELTER, SMELTER)
				.put(BlockMachine.Type.INSOLATOR, INSOLATOR)
				.put(BlockMachine.Type.CHARGER, CHARGER)
				.put(BlockMachine.Type.CRUCIBLE, CRUCIBLE)
				.put(BlockMachine.Type.TRANSPOSER, TRANSPOSER)
				.put(BlockMachine.Type.ACCUMULATOR, ACCUMULATOR)
				.put(BlockMachine.Type.ASSEMBLER, ASSEMBLER)
				.put(BlockMachine.Type.EXTRUDER, EXTRUDER)
				.put(BlockMachine.Type.PRECIPITATOR, PRECIPITATOR).build();

		public static final ResourceLocation PULVERIZER_ACTIVE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_active_pulverizer");
		public static final ResourceLocation FURNACE_ACTIVE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_active_furnace");
		public static final ResourceLocation SAWMILL_ACTIVE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_active_sawmill");
		public static final ResourceLocation SMELTER_ACTIVE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_active_smelter");
		public static final ResourceLocation INSOLATOR_ACTIVE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_active_insolator");
		public static final ResourceLocation CHARGER_ACTIVE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_active_charger");
		public static final ResourceLocation CRUCIBLE_ACTIVE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_active_crucible");
		public static final ResourceLocation TRANSPOSER_ACTIVE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_active_transposer");
		public static final ResourceLocation ACCUMULATOR_ACTIVE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_active_accumulator");
		public static final ResourceLocation ASSEMBLER_ACTIVE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_active_assembler");
		public static final ResourceLocation EXTRUDER_ACTIVE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_active_extruder");
		public static final ResourceLocation PRECIPITATOR_ACTIVE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_active_precipitator");
		public static final ResourceLocation CENTRIFUGE_ACTIVE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/machine/machine_active_centrifuge");
		public static final Map<BlockMachine.Type, ResourceLocation> ACTIVE_FACE_MAP = ImmutableMap.<BlockMachine.Type, ResourceLocation>builder()
				.put(BlockMachine.Type.FURNACE, FURNACE_ACTIVE)
				.put(BlockMachine.Type.PULVERIZER, PULVERIZER_ACTIVE)
				.put(BlockMachine.Type.SAWMILL, SAWMILL_ACTIVE)
				.put(BlockMachine.Type.SMELTER, SMELTER_ACTIVE)
				.put(BlockMachine.Type.INSOLATOR, INSOLATOR_ACTIVE)
				.put(BlockMachine.Type.CHARGER, CHARGER_ACTIVE)
				.put(BlockMachine.Type.CRUCIBLE, CRUCIBLE_ACTIVE)
				.put(BlockMachine.Type.TRANSPOSER, TRANSPOSER_ACTIVE)
				.put(BlockMachine.Type.ACCUMULATOR, ACCUMULATOR_ACTIVE)
				.put(BlockMachine.Type.ASSEMBLER, ASSEMBLER_ACTIVE)
				.put(BlockMachine.Type.EXTRUDER, EXTRUDER_ACTIVE)
				.put(BlockMachine.Type.PRECIPITATOR, PRECIPITATOR_ACTIVE).build();

		public static final Set<ResourceLocation> ALL = ImmutableSet.<ResourceLocation>builder()
				.addAll(SIDE_MAP.values()).addAll(FACE_MAP.values()).addAll(ACTIVE_FACE_MAP.values()).build();
	}

	public static class Cell {

		public static final ResourceLocation CREATIVE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/cell/cell_creative");
		public static final ResourceLocation BASIC = new ResourceLocation(ThermalExpansion.modId,
				"blocks/cell/cell_basic");
		public static final ResourceLocation HARDENED = new ResourceLocation(ThermalExpansion.modId,
				"blocks/cell/cell_hardened");
		public static final ResourceLocation REINFORCED = new ResourceLocation(ThermalExpansion.modId,
				"blocks/cell/cell_reinforced");
		public static final ResourceLocation RESONANT = new ResourceLocation(ThermalExpansion.modId,
				"blocks/cell/cell_resonant");
		public static final Map<BlockCell.Type, ResourceLocation> FACE_MAP = ImmutableMap.<BlockCell.Type, ResourceLocation>builder()
				.put(BlockCell.Type.CREATIVE, CREATIVE)
				.put(BlockCell.Type.BASIC, BASIC)
				.put(BlockCell.Type.HARDENED, HARDENED)
				.put(BlockCell.Type.REINFORCED, REINFORCED)
				.put(BlockCell.Type.RESONANT, RESONANT).build();

		public static final ResourceLocation CREATIVE_INNER = new ResourceLocation(ThermalExpansion.modId,
				"blocks/cell/cell_creative_inner");
		public static final ResourceLocation BASIC_INNER = new ResourceLocation(ThermalExpansion.modId,
				"blocks/cell/cell_basic_inner");
		public static final ResourceLocation HARDENED_INNER = new ResourceLocation(ThermalExpansion.modId,
				"blocks/cell/cell_hardened_inner");
		public static final ResourceLocation REINFORCED_INNER = new ResourceLocation(ThermalExpansion.modId,
				"blocks/cell/cell_reinforced_inner");
		public static final ResourceLocation RESONANT_INNER = new ResourceLocation(ThermalExpansion.modId,
				"blocks/cell/cell_resonant_inner");
		public static final Map<BlockCell.Type, ResourceLocation> INNER_MAP = ImmutableMap.<BlockCell.Type, ResourceLocation>builder()
				.put(BlockCell.Type.CREATIVE, CREATIVE_INNER)
				.put(BlockCell.Type.BASIC, BASIC_INNER)
				.put(BlockCell.Type.HARDENED, HARDENED_INNER)
				.put(BlockCell.Type.REINFORCED, REINFORCED_INNER)
				.put(BlockCell.Type.RESONANT, RESONANT_INNER).build();

		public static final ResourceLocation BLUE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/cell/cell_config_blue" + CB_SUFFIX);
		public static final ResourceLocation ORANGE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/cell/cell_config_blue" + CB_SUFFIX);
		public static final Map<BlockTEBase.EnumSideConfig, ResourceLocation> CONFIG_MAP = ImmutableMap
				.of(BlockTEBase.EnumSideConfig.NONE, Config.NONE, BlockTEBase.EnumSideConfig.BLUE, BLUE,
						BlockTEBase.EnumSideConfig.ORANGE, ORANGE);

		public static final Map<Integer, ResourceLocation> METER_MAP;

		static {
			ImmutableMap.Builder<Integer, ResourceLocation> builder = ImmutableMap.builder();

			for (int i = 0; i < 9; i++) {
				builder.put(i, new ResourceLocation(ThermalExpansion.modId, "blocks/cell/cell_meter_" + i));
			}

			METER_MAP = builder.build();
		}

		public static final ResourceLocation METER_CREATIVE = new ResourceLocation(ThermalExpansion.modId,
				"blocks/cell/cell_meter_creative.png");

		public static final ResourceLocation CENTER_SOLID = new ResourceLocation(ThermalExpansion.modId,
				"blocks/cell/cell_center_solid");

		public static final Set<ResourceLocation> ALL = ImmutableSet.<ResourceLocation>builder()
				.addAll(FACE_MAP.values()).addAll(INNER_MAP.values()).add(BLUE).add(ORANGE).add(METER_CREATIVE).add(CENTER_SOLID)
				.build();
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onTextureStitch(TextureStitchEvent.Pre event) {

		TextureMap textureMap = event.getMap();
		for (ResourceLocation rl : Cell.ALL) {
			textureMap.registerSprite(rl);
		}
	}
}
