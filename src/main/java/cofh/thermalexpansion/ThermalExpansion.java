package cofh.thermalexpansion;

import cofh.core.CoFHProps;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketCore;
import cofh.core.network.PacketCore.PacketTypes;
import cofh.core.util.ConfigHandler;
import cofh.thermalexpansion.block.TEBlocks;
import cofh.thermalexpansion.core.Proxy;
import cofh.thermalexpansion.core.TEProps;
import cofh.thermalexpansion.gui.CreativeTabTE;
import cofh.thermalexpansion.gui.CreativeTabTEFlorbs;
import cofh.thermalexpansion.gui.GuiHandler;
import cofh.thermalexpansion.network.PacketTEBase;
import cofh.thermalexpansion.util.FuelManager;
import cofh.thermalexpansion.util.IMCHandler;
import cofh.thermalexpansion.util.crafting.ChargerManager;
import cofh.thermalexpansion.util.crafting.CrucibleManager;
import cofh.thermalexpansion.util.crafting.FurnaceManager;
import cofh.thermalexpansion.util.crafting.InsolatorManager;
import cofh.thermalexpansion.util.crafting.PulverizerManager;
import cofh.thermalexpansion.util.crafting.SawmillManager;
import cofh.thermalexpansion.util.crafting.SmelterManager;
import cofh.thermalexpansion.util.crafting.TransposerManager;
import cofh.thermalfoundation.ThermalFoundation;

import java.io.File;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.CustomProperty;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ThermalExpansion.modId, name = ThermalExpansion.modName, version = ThermalExpansion.version, dependencies = ThermalExpansion.dependencies,
		guiFactory = ThermalExpansion.modGuiFactory, canBeDeactivated = false, customProperties = @CustomProperty(k = "cofhversion", v = "true"))
public class ThermalExpansion {

	public static final String modId = "thermalexpansion";
	public static final String modName = "Thermal Expansion";
	public static final String version = "1.10.2A0.0.1";
	public static final String version_max = "1.10.2A0.0.1";
	public static final String dependencies = ThermalFoundation.version_group;
	public static final String modGuiFactory = "cofh.thermalexpansion.gui.GuiConfigTEFactory";

	public static final String version_group = "required-after:" + modId + "@[" + version + "," + version_max + ");";
	public static final String releaseURL = "https://raw.github.com/CoFH/VERSION/master/" + modId;

	@Instance(modId)
	public static ThermalExpansion instance;

	@SidedProxy(clientSide = "cofh.thermalexpansion.core.ProxyClient", serverSide = "cofh.thermalexpansion.core.Proxy")
	public static Proxy proxy;

	public static final Logger LOG = LogManager.getLogger(modId);
	public static final ConfigHandler CONFIG = new ConfigHandler(version);
	public static final ConfigHandler CONFIG_CLIENT = new ConfigHandler(version);
	public static final GuiHandler GUI_HANDLER = new GuiHandler();

	public static CreativeTabs tabCommon = new CreativeTabTE();
	public static CreativeTabs tabBlocks = tabCommon;
	public static CreativeTabs tabItems = tabCommon;
	public static CreativeTabs tabTools = tabCommon;
	public static CreativeTabs tabFlorbs = tabCommon;

	/* INIT SEQUENCE */
	public ThermalExpansion() {

		//super(log);
	}

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {

		CONFIG.setConfiguration(new Configuration(new File(CoFHProps.configDir, "/cofh/thermalexpansion/common.cfg"), true));
		CONFIG_CLIENT.setConfiguration(new Configuration(new File(CoFHProps.configDir, "cofh/thermalexpansion/client.cfg"), true));

		TEBlocks.preInit();

		proxy.preInit(event);
	}

	@EventHandler
	public void initialize(FMLInitializationEvent event) {

		TEBlocks.initialize();

		NetworkRegistry.INSTANCE.registerGuiHandler(instance, GUI_HANDLER);

		PacketTEBase.initialize();

		proxy.initialize(event);
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {

		TEBlocks.postInit();

		proxy.postInit(event);
	}

	@EventHandler
	public void loadComplete(FMLLoadCompleteEvent event) {

		IMCHandler.instance.handleIMC(FMLInterModComms.fetchRuntimeMessages(this));

		FurnaceManager.loadRecipes();
		PulverizerManager.loadRecipes();
		SawmillManager.loadRecipes();
		SmelterManager.loadRecipes();
		InsolatorManager.loadRecipes();
		ChargerManager.loadRecipes();
		CrucibleManager.loadRecipes();
		TransposerManager.loadRecipes();

		FuelManager.parseFuels();

		CONFIG.cleanUp(false, true);
		CONFIG_CLIENT.cleanUp(false, true);

		LOG.info(modName + ": Load Complete.");
	}

	@EventHandler
	public void serverStarting(FMLServerStartingEvent event) {

	}

	/* IMC */
	@EventHandler
	public void handleIMC(IMCEvent event) {

	}

	/* SYNC */
	public PacketCoFHBase getConfigSync() {

		PacketCoFHBase payload = PacketCore.getPacket(PacketTypes.CONFIG_SYNC);

		return payload;
	}

	public void handleConfigSync(PacketCoFHBase payload) {

	}

	/* HELPERS */
	void configOptions() {

		String category;
		String comment;

		/* GRAPHICS */
		if (CoFHProps.enableColorBlindTextures) {
			TEProps.textureGuiCommon = TEProps.PATH_COMMON_CB;
			TEProps.textureGuiAssembler = TEProps.PATH_ASSEMBLER_CB;
			TEProps.textureSelection = TEProps.TEXTURE_CB;
			//BlockCell.textureSelection = BlockCell.TEXTURE_CB;
		}
		TEProps.useAlternateStarfieldShader = ThermalExpansion.CONFIG_CLIENT.get("Render", "UseAlternateShader", true,
				"Set to TRUE for Tesseracts to use an alternate starfield shader.");

		/* INTERFACE */
		category = "Interface.CreativeTab";
		boolean blockTab = false;
		boolean itemTab = false;
		boolean toolTab = false;
		boolean florbTab = false;

		comment = "Set to TRUE to put Thermal Expansion Blocks under a general \"Thermal Expansion\" Creative Tab.";
		blockTab = CONFIG_CLIENT.get(category, "BlocksInCommonTab", blockTab);

		comment = "Set to TRUE to put Thermal Expansion Items under a general \"Thermal Expansion\" Creative Tab.";
		itemTab = CONFIG_CLIENT.get(category, "ItemsInCommonTab", itemTab);

		comment = "Set to TRUE to put Thermal Expansion Tools under a general \"Thermal Expansion\" Creative Tab.";
		toolTab = CONFIG_CLIENT.get(category, "ToolsInCommonTab", toolTab);

		comment = "Set to TRUE to put Thermal Expansion Florbs under a general \"Thermal Expansion\" Creative Tab.";
		florbTab = CONFIG_CLIENT.get(category, "FlorbsInCommonTab", florbTab);

		if (blockTab || itemTab || toolTab || florbTab) {
			tabCommon = new CreativeTabTE();
		}
		tabBlocks = blockTab ? tabCommon : new CreativeTabTE("Blocks") {

			//	@Override
			//	protected ItemStack getStack() {
			//
			//		return BlockFrame.frameCellReinforcedFull;
			//	}
		};
		tabItems = itemTab ? tabCommon : new CreativeTabTE("Items") {

			@Override
			protected ItemStack getStack() {

				return null;
				//return TEItems.powerCoilElectrum;
			}
		};
		tabTools = toolTab ? tabCommon : new CreativeTabTE("Tools") {

			@Override
			protected ItemStack getStack() {

				return null;
				//return TEItems.toolWrench;
			}
		};
		tabFlorbs = florbTab ? tabCommon : new CreativeTabTEFlorbs();
		// TEProps.enableDebugOutput = config.get(category, "EnableDebugOutput", TEProps.enableDebugOutput);
		// TEProps.enableAchievements = config.get(category, "EnableAchievements", TEProps.enableAchievements);
	}

	public synchronized void handleIdMapping() {

		FurnaceManager.refreshRecipes();
		PulverizerManager.refreshRecipes();
		SawmillManager.refreshRecipes();
		SmelterManager.refreshRecipes();
		InsolatorManager.refreshRecipes();
		ChargerManager.refreshRecipes();
		CrucibleManager.refreshRecipes();
		TransposerManager.refreshRecipes();
	}

}
