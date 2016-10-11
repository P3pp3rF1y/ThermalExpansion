package cofh.thermalexpansion.core;

import cofh.thermalexpansion.block.BlockTEBase;
import cofh.thermalexpansion.block.BlockTEBase.EnumSideConfig;

import com.google.common.base.Optional;
import net.minecraft.block.properties.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fluids.FluidContainerRegistry;

import java.util.Collection;

public class TEProps {

	private TEProps() {

	}

	/* General */
	public static final int MAX_FLUID_SMALL = FluidContainerRegistry.BUCKET_VOLUME * 4;
	public static final int MAX_FLUID_LARGE = FluidContainerRegistry.BUCKET_VOLUME * 10;
	public static final int MAGMATIC_TEMPERATURE = 1000;

	public static boolean enableAchievements = false;

	/* Graphics */
	public static final String PATH_GFX = "thermalexpansion:textures/";
	public static final String PATH_ARMOR = PATH_GFX + "armor/";
	public static final String PATH_GUI = PATH_GFX + "gui/";
	public static final String PATH_ENTITY = PATH_GFX + "entity/";
	public static final String PATH_RENDER = PATH_GFX + "blocks/";
	public static final String PATH_ELEMENTS = PATH_GUI + "elements/";
	public static final String PATH_ICONS = PATH_GUI + "icons/";;

	public static final String PATH_GUI_DEVICE = PATH_GUI + "device/";
	public static final String PATH_GUI_DYNAMO = PATH_GUI + "dynamo/";
	public static final String PATH_GUI_MACHINE = PATH_GUI + "machine/";
	public static final String PATH_GUI_WORKBENCH = PATH_GUI + "workbench/";

	public static final ResourceLocation PATH_COMMON = new ResourceLocation(PATH_ELEMENTS + "slots.png");
	public static final ResourceLocation PATH_COMMON_CB = new ResourceLocation(PATH_ELEMENTS + "slots_cb.png");
	public static final ResourceLocation PATH_ASSEMBLER = new ResourceLocation(PATH_ELEMENTS + "slots_assembler.png");
	public static final ResourceLocation PATH_ASSEMBLER_CB = new ResourceLocation(PATH_ELEMENTS + "slots_assembler_cb.png");
	public static final String PATH_ICON = PATH_GUI + "icons/";

	public static ResourceLocation textureGuiCommon = PATH_COMMON;
	public static ResourceLocation textureGuiAssembler = PATH_ASSEMBLER;
	public static boolean useAlternateStarfieldShader = false;

	/* Render Ids */
	public static int renderIdCell = -1;
	public static int renderIdDynamo = -1;
	public static int renderIdFrame = -1;
	public static int renderIdLight = -1;
	public static int renderIdMachine = -1;
	public static int renderIdPlate = -1;
	public static int renderIdTank = -1;
	public static int renderIdEnder = -1;

	/* Common Block Properties */
	public static final IUnlistedProperty<Boolean> ACTIVE = Properties.toUnlisted(PropertyBool.create("active"));
	public static final IUnlistedProperty<EnumFacing> FACING = Properties.toUnlisted(PropertyDirection.create("facing"));
	public static final IUnlistedProperty<String> FLUID = new IUnlistedProperty<String>() {
		@Override public String getName() { return "fluid_rl"; }
		@Override public boolean isValid(String value) { return true; }
		@Override public Class<String> getType() { return String.class; }
		@Override public String valueToString(String value) { return value; }
	};
	public static final IUnlistedProperty<EnumSideConfig>[] SIDE_CONFIG = new IUnlistedProperty[6];

	static {
		for (int i = 0; i < 6; i++) {
			TEProps.SIDE_CONFIG[i] = Properties.toUnlisted(PropertyEnum.create("config_" + EnumFacing.VALUES[i].name(), EnumSideConfig.class));
		}
	}

}
