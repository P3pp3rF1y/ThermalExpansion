package cofh.thermalexpansion.model;

import cofh.thermalexpansion.ThermalExpansion;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.ResourceLocation;

import java.util.Set;

public class TextureLocations {

	public static final ResourceLocation MISSING = new ResourceLocation("missingno");

	public static class Config {

		public static final ResourceLocation BLUE = new ResourceLocation(ThermalExpansion.modId, "blocks/config/Config_Blue");
		public static final ResourceLocation GREEN = new ResourceLocation(ThermalExpansion.modId, "blocks/config/Config_Green");
		public static final ResourceLocation OPEN = new ResourceLocation(ThermalExpansion.modId, "blocks/config/Config_Open");
		public static final ResourceLocation ORANGE = new ResourceLocation(ThermalExpansion.modId, "blocks/config/Config_Orange");
		public static final ResourceLocation PURPLE = new ResourceLocation(ThermalExpansion.modId, "blocks/config/Config_Purple");
		public static final ResourceLocation RED = new ResourceLocation(ThermalExpansion.modId, "blocks/config/Config_Red");
		public static final ResourceLocation YELLOW = new ResourceLocation(ThermalExpansion.modId, "blocks/config/Config_Yellow");

		public static final Set<ResourceLocation> ALL = ImmutableSet.of(BLUE, GREEN, OPEN, ORANGE, PURPLE, RED, YELLOW);

		public static class CB {

		}
	}

	public static class Machine {

		public static final ResourceLocation PULVERIZER = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/Machine_Face_Pulverizer");
		public static final ResourceLocation PULVERIZER_ACTIVE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/Machine_Active_Pulverizer");
		public static final ResourceLocation FURNACE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/Machine_Face_Furnace");
		public static final ResourceLocation FURNACE_ACTIVE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/Machine_Active_Furnace");
		public static final ResourceLocation SIDE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/Machine_Side");
		public static final ResourceLocation TOP = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/Machine_Top");
		public static final ResourceLocation BOTTOM = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/Machine_Bottom");
		public static final ResourceLocation FRAME_TOP = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/Machine_Frame_Top");
		public static final ResourceLocation FRAME_BOTTOM = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/Machine_Frame_Bottom");
		public static final ResourceLocation FRAME_SIDE = new ResourceLocation(ThermalExpansion.modId, "blocks/machine/Machine_Frame_Side");

		public static final Set<ResourceLocation> ALL = ImmutableSet.of(PULVERIZER, PULVERIZER_ACTIVE, FURNACE, FURNACE_ACTIVE, SIDE, TOP, BOTTOM, FRAME_TOP, FRAME_BOTTOM, FRAME_SIDE);
	}
}
