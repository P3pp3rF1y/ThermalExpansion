package cofh.thermalexpansion.block.machine;

import cofh.api.core.IInitializer;
import cofh.api.core.IModelRegister;
import cofh.core.util.RegistryHelper;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.helpers.FluidHelper;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.block.BlockTEBase;
import cofh.thermalexpansion.block.simple.BlockFrame;
import cofh.thermalexpansion.core.TEProps;
import cofh.thermalexpansion.item.ItemAugment;
import cofh.thermalexpansion.model.ModelMachine;
import cofh.thermalexpansion.util.ReconfigurableHelper;
import cofh.thermalexpansion.util.crafting.RecipeMachine;
import cofh.thermalexpansion.util.crafting.TECraftingHandler;
import cofh.thermalfoundation.item.ItemMaterial;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class BlockMachine extends BlockTEBase implements IInitializer, IModelRegister {

	public static final PropertyEnum<BlockMachine.Type> TYPE = PropertyEnum.create("type", BlockMachine.Type.class);

	public BlockMachine() {

		super(Material.IRON);

		setUnlocalizedName("machine");

		setHardness(15.0F);
		setResistance(25.0F);
	}

	@Override
	protected BlockStateContainer createBlockState() {

		return new ExtendedBlockState(this,
				new IProperty[] { TYPE },
				new IUnlistedProperty[] { TEProps.ACTIVE, TEProps.FACING, TEProps.SIDE_CONFIG[0], TEProps.SIDE_CONFIG[1],
						TEProps.SIDE_CONFIG[2], TEProps.SIDE_CONFIG[3], TEProps.SIDE_CONFIG[4], TEProps.SIDE_CONFIG[5],
						TEProps.FLUID }
		);
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {

		TileMachineBase tile = (TileMachineBase) world.getTileEntity(pos);

		return tile.getExtendedState(state, world, pos);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {

		for (int i = 0; i < BlockMachine.Type.METADATA_LOOKUP.length; i++) {
			for (int j = 0; j < 4; j++) {
				if (creativeTiers[j]) {
					list.add(ItemBlockMachine.setDefaultTag(new ItemStack(item, 1, i), (byte) j));
				}
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {

		if (stack.getTagCompound() != null) {
			TileMachineBase tile = (TileMachineBase) world.getTileEntity(pos);

			tile.readAugmentsFromNBT(stack.getTagCompound());
			tile.installAugments();
			tile.setEnergyStored(stack.getTagCompound().getInteger("Energy"));

			int facing = BlockHelper.determineXZPlaceFacing(placer);
			int storedFacing = ReconfigurableHelper.getFacing(stack);
			byte[] sideCache = ReconfigurableHelper.getSideCache(stack, tile.getDefaultSides());

			tile.sideCache[0] = sideCache[0];
			tile.sideCache[1] = sideCache[1];
			tile.sideCache[facing] = 0;
			tile.sideCache[BlockHelper.getLeftSide(facing)] = sideCache[BlockHelper.getLeftSide(storedFacing)];
			tile.sideCache[BlockHelper.getRightSide(facing)] = sideCache[BlockHelper.getRightSide(storedFacing)];
			tile.sideCache[BlockHelper.getOppositeSide(facing)] = sideCache[BlockHelper.getOppositeSide(storedFacing)];
		}
		super.onBlockPlacedBy(world, pos, state, placer, stack);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
			ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {

		TileEntity tile = world.getTileEntity(pos);

		//TODO change to fluid caps (or just warp that inside the fluid helper
		if (tile instanceof TileExtruder || tile instanceof TilePrecipitator) {
			if (FluidHelper.fillHandlerWithContainer(world, (IFluidHandler) tile, player)) {
				return true;
			}
		}

		return super.onBlockActivated(world, pos, state, player, hand, heldItem, side, hitX, hitY, hitZ);
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {

		return false;
	}

	@Override
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {

		return true;
	}

	@Override
	public BlockRenderLayer getBlockLayer() {

		return BlockRenderLayer.CUTOUT_MIPPED;
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {

		return this.getDefaultState().withProperty(TYPE, BlockMachine.Type.byMetadata(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {

		return state.getValue(TYPE).getMetadata();
	}

	@Override
	public int damageDropped(IBlockState state) {

		return state.getValue(TYPE).getMetadata();
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {

		switch (state.getValue(TYPE)) {
		case FURNACE:
			return new TileFurnace();
		case PULVERIZER:
			return new TilePulverizer();
		case SAWMILL:
			return new TileSawmill();
		case SMELTER:
			return new TileSmelter();
		case INSOLATOR:
			return new TileInsolator();
		case CHARGER:
			return new TileCharger();
		case CRUCIBLE:
			return new TileCrucible();
		case TRANSPOSER:
			return new TileTransposer();
		case ACCUMULATOR:
			return new TileAccumulator();
		case ASSEMBLER:
			return new TileAssembler();
		case EXTRUDER:
			return new TileExtruder();
		case PRECIPITATOR:
			return new TilePrecipitator();
		default:
			return null;
		}
	}

	@Override
	public NBTTagCompound getItemStackTag(IBlockAccess world, BlockPos pos) {

		NBTTagCompound tag = super.getItemStackTag(world, pos);
		TileMachineBase tile = (TileMachineBase) world.getTileEntity(pos);

		if (tile != null) {
			if (tag == null) {
				tag = new NBTTagCompound();
			}
			ReconfigurableHelper.setItemStackTagReconfig(tag, tile);
			tag.setInteger("Energy", tile.getEnergyStored(EnumFacing.DOWN));
			tile.writeAugmentsToNBT(tag);
		}
		return tag;
	}

	/* IModelRegister */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels() {

		StateMapperBase mapper = new StateMapperBase() {

			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {

				return new ModelResourceLocation(ModelMachine.BASE_MODEL_LOCATION.toString(),
						"type=" + state.getValue(TYPE).getName());
			}
		};
		ModelLoader.setCustomStateMapper(this, mapper);

		for (BlockMachine.Type type : BlockMachine.Type.values()) {
			ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation(
					ModelMachine.BASE_MODEL_LOCATION.toString(), "type=" + type.getName());
			ModelLoader
					.setCustomModelResourceLocation(Item.getItemFromBlock(this), type.getMetadata(), itemModelResourceLocation);
		}

		for (Type type : Type.values()) {
			ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation(
					ModelMachine.BASE_MODEL_LOCATION.toString(), "type=" + type.getName());
			ModelLoader
					.setCustomModelResourceLocation(Item.getItemFromBlock(this), type.getMetadata(), itemModelResourceLocation);
		}

	}

	/* IInitializer */
	@Override
	public boolean preInit() {

		RegistryHelper.registerBlockAndItem(this, new ResourceLocation(ThermalExpansion.modId, "machine"), ItemBlockMachine::new);

		return true;
	}

	@Override
	public boolean initialize() {

		TileMachineBase.configure();

		TileFurnace.initialize();
		TilePulverizer.initialize();
		TileSawmill.initialize();
		TileSmelter.initialize();
		TileInsolator.initialize();
		TileCharger.initialize();
		TileCrucible.initialize();
		TileTransposer.initialize();
		TileAccumulator.initialize();
		TileAssembler.initialize();
		TileExtruder.initialize();
		TilePrecipitator.initialize();

		if (defaultAutoTransfer) {
			defaultAugments[0] = ItemHelper.cloneStack(ItemAugment.generalAutoOutput);
		}
		if (defaultRedstoneControl) {
			defaultAugments[1] = ItemHelper.cloneStack(ItemAugment.generalRedstoneControl);
		}
		if (defaultReconfigSides) {
			defaultAugments[2] = ItemHelper.cloneStack(ItemAugment.generalReconfigSides);
		}

		machineFurnace = ItemBlockMachine.setDefaultTag(new ItemStack(this, 1, Type.FURNACE.ordinal()));
		machinePulverizer = ItemBlockMachine.setDefaultTag(new ItemStack(this, 1, Type.PULVERIZER.ordinal()));
		machineSawmill = ItemBlockMachine.setDefaultTag(new ItemStack(this, 1, Type.SAWMILL.ordinal()));
		machineSmelter = ItemBlockMachine.setDefaultTag(new ItemStack(this, 1, Type.SMELTER.ordinal()));
		machineCrucible = ItemBlockMachine.setDefaultTag(new ItemStack(this, 1, Type.CRUCIBLE.ordinal()));
		machineTransposer = ItemBlockMachine.setDefaultTag(new ItemStack(this, 1, Type.TRANSPOSER.ordinal()));
		machineCharger = ItemBlockMachine.setDefaultTag(new ItemStack(this, 1, Type.CHARGER.ordinal()));
		machineInsolator = ItemBlockMachine.setDefaultTag(new ItemStack(this, 1, Type.INSOLATOR.ordinal()));
		machineAccumulator = ItemBlockMachine.setDefaultTag(new ItemStack(this, 1, Type.ACCUMULATOR.ordinal()));
		machineAssembler = ItemBlockMachine.setDefaultTag(new ItemStack(this, 1, Type.ASSEMBLER.ordinal()));
		machineExtruder = ItemBlockMachine.setDefaultTag(new ItemStack(this, 1, Type.EXTRUDER.ordinal()));
		machinePrecipitator = ItemBlockMachine.setDefaultTag(new ItemStack(this, 1, Type.PRECIPITATOR.ordinal()));

		return true;
	}

	@Override
	public boolean postInit() {

		String machineFrame = "thermalexpansion:machineFrame";
		String copperPart = "thermalexpansion:machineCopper";
		String invarPart = "thermalexpansion:machineInvar";

		//TODO consider readding wrapper implementation with JEI

		// @formatter:off
		if (enable[Type.FURNACE.getMetadata()]) {
			GameRegistry.addRecipe(new RecipeMachine(machineFurnace, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', copperPart,
					'P', ItemMaterial.powerCoilGold,
					'X', "dustRedstone",
					'Y', Blocks.BRICK_BLOCK
			}));
		}
		if (enable[Type.PULVERIZER.getMetadata()]) {
			String category = "Machine.Pulverizer";
			String comment = "If enabled, the Pulverizer will require Diamonds instead of Flint.";
			Item component = ThermalExpansion.CONFIG.get(category, "RequireDiamonds", false, comment) ? Items.DIAMOND : Items.FLINT;
			GameRegistry.addRecipe(new RecipeMachine(machinePulverizer, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', copperPart,
					'P', ItemMaterial.powerCoilGold,
					'X', Blocks.PISTON,
					'Y', component
			}));
		}
		if (enable[Type.SAWMILL.getMetadata()]) {
			GameRegistry.addRecipe(new RecipeMachine(machineSawmill, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', copperPart,
					'P', ItemMaterial.powerCoilGold,
					'X', Items.IRON_AXE,
					'Y', "plankWood"
			}));
		}
		if (enable[Type.SMELTER.getMetadata()]) {
			GameRegistry.addRecipe(new RecipeMachine(machineSmelter, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', invarPart,
					'P', ItemMaterial.powerCoilGold,
					'X', Items.BUCKET,
					'Y', "ingotInvar"
			}));
		}
		if (enable[Type.CRUCIBLE.getMetadata()]) {
			GameRegistry.addRecipe(new RecipeMachine(machineCrucible, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', invarPart,
					'P', ItemMaterial.powerCoilGold,
					'X', BlockFrame.frameCellBasic,
					'Y', Blocks.NETHER_BRICK
			}));
		}
		if (enable[Type.TRANSPOSER.getMetadata()]) {
			GameRegistry.addRecipe(new RecipeMachine(machineTransposer, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', copperPart,
					'P', ItemMaterial.powerCoilGold,
					'X', Items.BUCKET,
					'Y', "blockGlass"
			}));
		}
		if (enable[Type.PRECIPITATOR.getMetadata()]) {
			GameRegistry.addRecipe(new RecipeMachine(machinePrecipitator, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', copperPart,
					'P', ItemMaterial.powerCoilGold,
					'X', Blocks.PISTON,
					'Y', "ingotInvar"
			}));
		}
		if (enable[Type.EXTRUDER.getMetadata()]) {
			GameRegistry.addRecipe(new RecipeMachine(machineExtruder, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', copperPart,
					'P', ItemMaterial.pneumaticServo,
					'X', Blocks.PISTON,
					'Y', "blockGlass"
			}));
		}
		if (enable[Type.ACCUMULATOR.getMetadata()]) {
			GameRegistry.addRecipe(new RecipeMachine(machineAccumulator, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', copperPart,
					'P', ItemMaterial.pneumaticServo,
					'X', Items.BUCKET,
					'Y', "blockGlass"
			}));
		}
		if (enable[Type.ASSEMBLER.getMetadata()]) {
			GameRegistry.addRecipe(new RecipeMachine(machineAssembler, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', copperPart,
					'P', ItemMaterial.powerCoilGold,
					'X', Blocks.CHEST,
					'Y', "gearTin"
			}));
		}
		if (enable[Type.CHARGER.getMetadata()]) {
			GameRegistry.addRecipe(new RecipeMachine(machineCharger, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', copperPart,
					'P', ItemMaterial.powerCoilGold,
					'X', BlockFrame.frameCellBasic,
					'Y', ItemMaterial.powerCoilSilver
			}));
		}
		if (enable[Type.INSOLATOR.getMetadata()]) {
			GameRegistry.addRecipe(new RecipeMachine(machineInsolator, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', copperPart,
					'P', ItemMaterial.powerCoilGold,
					'X', "gearLumium",
					'Y', Blocks.DIRT
			}));
		}
		// @formatter:on
		TECraftingHandler.addMachineUpgradeRecipes(machineFurnace);
		TECraftingHandler.addMachineUpgradeRecipes(machinePulverizer);
		TECraftingHandler.addMachineUpgradeRecipes(machineSawmill);
		TECraftingHandler.addMachineUpgradeRecipes(machineSmelter);
		TECraftingHandler.addMachineUpgradeRecipes(machineCrucible);
		TECraftingHandler.addMachineUpgradeRecipes(machineTransposer);
		TECraftingHandler.addMachineUpgradeRecipes(machinePrecipitator);
		TECraftingHandler.addMachineUpgradeRecipes(machineExtruder);
		TECraftingHandler.addMachineUpgradeRecipes(machineAccumulator);
		TECraftingHandler.addMachineUpgradeRecipes(machineAssembler);
		TECraftingHandler.addMachineUpgradeRecipes(machineCharger);
		TECraftingHandler.addMachineUpgradeRecipes(machineInsolator);

		TECraftingHandler.addSecureRecipe(machineFurnace);
		TECraftingHandler.addSecureRecipe(machinePulverizer);
		TECraftingHandler.addSecureRecipe(machineSawmill);
		TECraftingHandler.addSecureRecipe(machineSmelter);
		TECraftingHandler.addSecureRecipe(machineCrucible);
		TECraftingHandler.addSecureRecipe(machineTransposer);
		TECraftingHandler.addSecureRecipe(machinePrecipitator);
		TECraftingHandler.addSecureRecipe(machineExtruder);
		TECraftingHandler.addSecureRecipe(machineAccumulator);
		TECraftingHandler.addSecureRecipe(machineAssembler);
		TECraftingHandler.addSecureRecipe(machineCharger);
		TECraftingHandler.addSecureRecipe(machineInsolator);

		return true;
	}

	public static void refreshItemStacks() {

		machineFurnace = ItemBlockMachine.setDefaultTag(machineFurnace);
		machinePulverizer = ItemBlockMachine.setDefaultTag(machinePulverizer);
		machineSawmill = ItemBlockMachine.setDefaultTag(machineSawmill);
		machineSmelter = ItemBlockMachine.setDefaultTag(machineSmelter);
		machineCrucible = ItemBlockMachine.setDefaultTag(machineCrucible);
		machineTransposer = ItemBlockMachine.setDefaultTag(machineTransposer);
		machineCharger = ItemBlockMachine.setDefaultTag(machineCharger);
		machineInsolator = ItemBlockMachine.setDefaultTag(machineInsolator);
		machineAccumulator = ItemBlockMachine.setDefaultTag(machineAccumulator);
		machineAssembler = ItemBlockMachine.setDefaultTag(machineAssembler);
		machineExtruder = ItemBlockMachine.setDefaultTag(machineExtruder);
		machinePrecipitator = ItemBlockMachine.setDefaultTag(machinePrecipitator);
	}

	/* TYPE */
	public static enum Type implements IStringSerializable {

		// @formatter:off
		FURNACE(0, "furnace", machineFurnace),
		PULVERIZER(1, "pulverizer", machinePulverizer),
		SAWMILL(2, "sawmill", machineSawmill),
		SMELTER(3, "smelter", machineSmelter),
		INSOLATOR(4, "insolator", machineInsolator),
		CHARGER(5, "charger", machineCharger),
		CRUCIBLE(6, "crucible", machineCrucible),
		TRANSPOSER(7, "transposer", machineTransposer),
		ACCUMULATOR(8, "accumulator", machineAccumulator),
		ASSEMBLER(9, "assembler", machineAssembler),
		EXTRUDER(10, "extruder", machineExtruder),
		PRECIPITATOR(11, "precipitator", machinePrecipitator);

		//TODO add additional machine types (some of them need more info)
		//CENTRIFUGE(8, "centrifuge", machineCentrifuge);
		// TRANSCAPSULATOR
		// CRAFTER
		// BREWER
		// ENCHANTER
		// @formatter:on

		private static final BlockMachine.Type[] METADATA_LOOKUP = new BlockMachine.Type[values().length];
		private final int metadata;
		private final String name;
		private final ItemStack stack;

		private final int light;

		private Type(int metadata, String name, ItemStack stack, int light) {

			this.metadata = metadata;
			this.name = name;
			this.stack = stack;

			this.light = light;
		}

		private Type(int metadata, String name, ItemStack stack) {

			this(metadata, name, stack, 0);
		}

		public int getMetadata() {

			return this.metadata;
		}

		@Override
		public String getName() {

			return this.name;
		}

		public ItemStack getStack() {

			return this.stack;
		}

		public int getLight() {

			return light;
		}

		public static Type byMetadata(int metadata) {

			if (metadata < 0 || metadata >= METADATA_LOOKUP.length) {
				metadata = 0;
			}
			return METADATA_LOOKUP[metadata];
		}

		static {
			for (Type type : values()) {
				METADATA_LOOKUP[type.getMetadata()] = type;
			}
		}
	}

	public static boolean defaultAutoTransfer = true;
	public static boolean defaultRedstoneControl = true;
	public static boolean defaultReconfigSides = true;

	public static boolean[] enable = new boolean[Type.values().length];
	public static boolean[] creativeTiers = new boolean[4];
	public static ItemStack[] defaultAugments = new ItemStack[3];

	static {
		String category = "Machine.";

		for (int i = 0; i < Type.values().length; i++) {
			enable[i] = ThermalExpansion.CONFIG
					.get(category + StringHelper.titleCase(Type.byMetadata(i).getName()), "Recipe.Enable", true);
		}
		category = "Machine.All";

		creativeTiers[0] = ThermalExpansion.CONFIG.get(category, "CreativeTab.Basic", false);
		creativeTiers[1] = ThermalExpansion.CONFIG.get(category, "CreativeTab.Hardened", false);
		creativeTiers[2] = ThermalExpansion.CONFIG.get(category, "CreativeTab.Reinforced", false);
		creativeTiers[3] = ThermalExpansion.CONFIG.get(category, "CreativeTab.Resonant", true);

		category += ".Augments";

		defaultAutoTransfer = ThermalExpansion.CONFIG.get(category, "Default.AutoTransfer", true);
		defaultRedstoneControl = ThermalExpansion.CONFIG.get(category, "Default.RedstoneControl", true);
		defaultReconfigSides = ThermalExpansion.CONFIG.get(category, "Default.ReconfigurableSides", true);

	}

	/* REFERENCES */
	public static ItemStack machineFurnace;
	public static ItemStack machinePulverizer;
	public static ItemStack machineSawmill;
	public static ItemStack machineSmelter;
	public static ItemStack machineInsolator;
	public static ItemStack machineCharger;
	public static ItemStack machineCrucible;
	public static ItemStack machineTransposer;
	public static ItemStack machineCentrifuge;
	public static ItemStack machineAccumulator;
	public static ItemStack machineAssembler;
	public static ItemStack machineExtruder;
	public static ItemStack machinePrecipitator;

}
