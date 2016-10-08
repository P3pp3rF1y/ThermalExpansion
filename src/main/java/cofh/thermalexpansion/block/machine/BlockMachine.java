package cofh.thermalexpansion.block.machine;

import cofh.api.core.IInitializer;
import cofh.api.core.IModelRegister;
import cofh.core.util.RegistryHelper;
import cofh.lib.util.helpers.BlockHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.block.BlockTEBase;
import cofh.thermalexpansion.block.TileReconfigurable;
import cofh.thermalexpansion.core.TEProps;

import java.util.List;

import cofh.thermalexpansion.model.ModelMachine;
import cofh.thermalexpansion.util.ReconfigurableHelper;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
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
					TEProps.SIDE_CONFIG[2], TEProps.SIDE_CONFIG[3], TEProps.SIDE_CONFIG[4], TEProps.SIDE_CONFIG[5]}
				);
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {

		IExtendedBlockState extState = (IExtendedBlockState) state;

		TileReconfigurable tile = (TileReconfigurable) world.getTileEntity(pos);
		extState = extState.withProperty(TEProps.ACTIVE, tile.isActive);
		extState = extState.withProperty(TEProps.FACING, EnumFacing.values()[tile.getFacing()]);
		for (int side = 0; side < 6 ; side++) {
			extState = extState.withProperty(TEProps.SIDE_CONFIG[side], EnumSideConfig.values()[tile.sideCache[side]]);
		}

		return extState;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {

		for (int i = 0; i < BlockMachine.Type.METADATA_LOOKUP.length; i++) {
			list.add(new ItemStack(item, 1, i));
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
			default:
				return null;
		}
	}

	/* IModelRegister */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels() {

		StateMapperBase ignoreState = new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState iBlockState) {
				return ModelMachine.MODEL_LOCATION;
			}
		};

		ModelLoader.setCustomStateMapper(this, ignoreState);
	}

	//TODO standardize with an interface or something
	public void initItemModel() {
		// ClientProxy.init (not preInit) so that's why it is a separate method.
		Item itemBlock = Item.REGISTRY.getObject(new ResourceLocation(ThermalExpansion.modName, name));

		for (int i = 0; i < Type.values().length; i++) {
			ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation(ModelMachine.MODEL_LOCATION, "type=" + Type.byMetadata(i).getName());
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(itemBlock, i, itemModelResourceLocation);
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

		TileFurnace.initialize();
		TilePulverizer.initialize();
		TileSawmill.initialize();
		TileSmelter.initialize();
		TileInsolator.initialize();
		TileCharger.initialize();
		TileCrucible.initialize();
		TileTransposer.initialize();

		return true;
	}

	@Override
	public boolean postInit() {

		return true;
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

		CENTRIFUGE(8, "centrifuge", machineCentrifuge);

		//TODO add additional machine types (some of them need more info)
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

}
