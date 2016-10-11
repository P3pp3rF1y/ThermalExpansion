package cofh.thermalexpansion.block.machine;

import cofh.api.core.IInitializer;
import cofh.api.core.IModelRegister;
import cofh.core.util.RegistryHelper;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.block.BlockTEBase;
import cofh.thermalexpansion.core.TEProps;

import java.util.List;

import cofh.thermalexpansion.item.ItemAugment;
import cofh.thermalexpansion.model.ModelMachine;
import cofh.thermalexpansion.util.ReconfigurableHelper;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
				new IProperty[] {TYPE},
				new IUnlistedProperty[] {TEProps.ACTIVE, TEProps.FACING, TEProps.SIDE_CONFIG[0], TEProps.SIDE_CONFIG[1],
					TEProps.SIDE_CONFIG[2], TEProps.SIDE_CONFIG[3], TEProps.SIDE_CONFIG[4], TEProps.SIDE_CONFIG[5], TEProps.FLUID}
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
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		TileEntity tile = world.getTileEntity(pos);

		//TODO ADD EXTRUDER AND PRECIPITATOR
/*
		if (tile instanceof TileExtruder || tile instanceof TilePrecipitator) {
			if (FluidHelper.fillHandlerWithContainer(world, (IFluidHandler) tile, player)) {
				return true;
			}
		}
*/

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

		StateMapperBase ignoreState = new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState iBlockState) {
				return new ModelResourceLocation(ModelMachine.MODEL_LOCATION, "normal");
			}
		};
		ModelLoader.setCustomStateMapper(this, ignoreState);

		for (int i = 0; i < Type.values().length; i++) {
			ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation(new ResourceLocation(ThermalExpansion.modId, "machine_" + Type.byMetadata(i).getName()), "inventory" );
			ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), i, itemModelResourceLocation);
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
//TODO add
		/*
		precipitator = ItemBlockMachine.setDefaultTag(new ItemStack(this, 1, Type.PRECIPITATOR.ordinal()));
		extruder = ItemBlockMachine.setDefaultTag(new ItemStack(this, 1, Type.EXTRUDER.ordinal()));
*/
		machineCharger = ItemBlockMachine.setDefaultTag(new ItemStack(this, 1, Type.CHARGER.ordinal()));
		machineInsolator = ItemBlockMachine.setDefaultTag(new ItemStack(this, 1, Type.INSOLATOR.ordinal()));
		machineAccumulator = ItemBlockMachine.setDefaultTag(new ItemStack(this, 1, Type.ACCUMULATOR.ordinal()));
		machineAssembler = ItemBlockMachine.setDefaultTag(new ItemStack(this, 1, Type.ASSEMBLER.ordinal()));

		return true;
	}

	@Override
	public boolean postInit() {

		String machineFrame = "thermalexpansion:machineFrame";
		String copperPart = "thermalexpansion:machineCopper";
		String invarPart = "thermalexpansion:machineInvar";

		//TODO REPLACE WITH JEI
		// @formatter:off
/*
		if (enable[Types.FURNACE.ordinal()]) {
			NEIRecipeWrapper.addMachineRecipe(new RecipeMachine(furnace, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', copperPart,
					'P', TEItems.powerCoilGold,
					'X', "dustRedstone",
					'Y', Blocks.brick_block
			}));
		}
		if (enable[Types.PULVERIZER.ordinal()]) {
			String category = "Machine.Pulverizer";
			String comment = "If enabled, the Pulverizer will require Diamonds instead of Flint.";
			Item component = ThermalExpansion.config.get(category, "RequireDiamonds", false, comment) ? Items.diamond : Items.flint;
			NEIRecipeWrapper.addMachineRecipe(new RecipeMachine(pulverizer, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', copperPart,
					'P', TEItems.powerCoilGold,
					'X', Blocks.piston,
					'Y', component
			}));
		}
		if (enable[Types.SAWMILL.ordinal()]) {
			NEIRecipeWrapper.addMachineRecipe(new RecipeMachine(sawmill, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', copperPart,
					'P', TEItems.powerCoilGold,
					'X', Items.iron_axe,
					'Y', "plankWood"
			}));
		}
		if (enable[Types.SMELTER.ordinal()]) {
			NEIRecipeWrapper.addMachineRecipe(new RecipeMachine(smelter, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', invarPart,
					'P', TEItems.powerCoilGold,
					'X', Items.bucket,
					'Y', "ingotInvar"
			}));
		}
		if (enable[Types.CRUCIBLE.ordinal()]) {
			NEIRecipeWrapper.addMachineRecipe(new RecipeMachine(crucible, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', invarPart,
					'P', TEItems.powerCoilGold,
					'X', BlockFrame.frameCellBasic,
					'Y', Blocks.nether_brick
			}));
		}
		if (enable[Types.TRANSPOSER.ordinal()]) {
			NEIRecipeWrapper.addMachineRecipe(new RecipeMachine(transposer, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', copperPart,
					'P', TEItems.powerCoilGold,
					'X', Items.bucket,
					'Y', "blockGlass"
			}));
		}
		if (enable[Types.PRECIPITATOR.ordinal()]) {
			NEIRecipeWrapper.addMachineRecipe(new RecipeMachine(precipitator, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', copperPart,
					'P', TEItems.powerCoilGold,
					'X', Blocks.piston,
					'Y', "ingotInvar"
			}));
		}
		if (enable[Types.EXTRUDER.ordinal()]) {
			NEIRecipeWrapper.addMachineRecipe(new RecipeMachine(extruder, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', copperPart,
					'P', TEItems.pneumaticServo,
					'X', Blocks.piston,
					'Y', "blockGlass"
			}));
		}
		if (enable[Types.ACCUMULATOR.ordinal()]) {
			NEIRecipeWrapper.addMachineRecipe(new RecipeMachine(accumulator, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', copperPart,
					'P', TEItems.pneumaticServo,
					'X', Items.bucket,
					'Y', "blockGlass"
			}));
		}
		if (enable[Types.ASSEMBLER.ordinal()]) {
			NEIRecipeWrapper.addMachineRecipe(new RecipeMachine(assembler, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', copperPart,
					'P', TEItems.powerCoilGold,
					'X', Blocks.chest,
					'Y', "gearTin"
			}));
		}
		if (enable[Types.CHARGER.ordinal()]) {
			NEIRecipeWrapper.addMachineRecipe(new RecipeMachine(charger, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', copperPart,
					'P', TEItems.powerCoilGold,
					'X', BlockFrame.frameCellBasic,
					'Y', TEItems.powerCoilSilver
			}));
		}
		if (enable[Types.INSOLATOR.ordinal()]) {
			NEIRecipeWrapper.addMachineRecipe(new RecipeMachine(insolator, defaultAugments, new Object[] {
					" X ",
					"YCY",
					"IPI",
					'C', machineFrame,
					'I', copperPart,
					'P', TEItems.powerCoilGold,
					'X', "gearLumium",
					'Y', Blocks.dirt
			}));
		}
*/
		// @formatter:on
//TODO READD WITH JEI
/*
		TECraftingHandler.addMachineUpgradeRecipes(machineFurnace);
		TECraftingHandler.addMachineUpgradeRecipes(machinePulverizer);
		TECraftingHandler.addMachineUpgradeRecipes(sawmill);
		TECraftingHandler.addMachineUpgradeRecipes(smelter);
		TECraftingHandler.addMachineUpgradeRecipes(crucible);
		TECraftingHandler.addMachineUpgradeRecipes(transposer);
		TECraftingHandler.addMachineUpgradeRecipes(precipitator);
		TECraftingHandler.addMachineUpgradeRecipes(extruder);
		TECraftingHandler.addMachineUpgradeRecipes(accumulator);
		TECraftingHandler.addMachineUpgradeRecipes(assembler);
		TECraftingHandler.addMachineUpgradeRecipes(charger);
		TECraftingHandler.addMachineUpgradeRecipes(insolator);

		TECraftingHandler.addSecureRecipe(furnace);
		TECraftingHandler.addSecureRecipe(pulverizer);
		TECraftingHandler.addSecureRecipe(sawmill);
		TECraftingHandler.addSecureRecipe(smelter);
		TECraftingHandler.addSecureRecipe(crucible);
		TECraftingHandler.addSecureRecipe(transposer);
		TECraftingHandler.addSecureRecipe(precipitator);
		TECraftingHandler.addSecureRecipe(extruder);
		TECraftingHandler.addSecureRecipe(accumulator);
		TECraftingHandler.addSecureRecipe(assembler);
		TECraftingHandler.addSecureRecipe(charger);
		TECraftingHandler.addSecureRecipe(insolator);
*/

		return true;
	}

	public static void refreshItemStacks() {

		machineFurnace = ItemBlockMachine.setDefaultTag(machineFurnace);
		machinePulverizer = ItemBlockMachine.setDefaultTag(machinePulverizer);
		machineSawmill = ItemBlockMachine.setDefaultTag(machineSawmill);
		machineSmelter = ItemBlockMachine.setDefaultTag(machineSmelter);
		machineCrucible = ItemBlockMachine.setDefaultTag(machineCrucible);
		machineTransposer = ItemBlockMachine.setDefaultTag(machineTransposer);
		//TODO READD
/*
		machinePrecipitator = ItemBlockMachine.setDefaultTag(machinePrecipitator);
		machineExtruder = ItemBlockMachine.setDefaultTag(machineExtruder);
		machineAssembler = ItemBlockMachine.setDefaultTag(machineAssembler);
*/
		machineCharger = ItemBlockMachine.setDefaultTag(machineCharger);
		machineInsolator = ItemBlockMachine.setDefaultTag(machineInsolator);
		machineAccumulator = ItemBlockMachine.setDefaultTag(machineAccumulator);
		machineAssembler = ItemBlockMachine.setDefaultTag(machineAssembler);
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
		ASSEMBLER(9, "assembler", machineAssembler);


		//TODO add additional machine types (some of them need more info)
		//CENTRIFUGE(8, "centrifuge", machineCentrifuge);
		// TRANSCAPSULATOR
		// CRAFTER
		// BREWER
		// ENCHANTER
		// PRECIPITATOR
		// EXTRUDER
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
			enable[i] = ThermalExpansion.CONFIG.get(category + StringHelper.titleCase(Type.byMetadata(i).getName()), "Recipe.Enable", true);
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

}
