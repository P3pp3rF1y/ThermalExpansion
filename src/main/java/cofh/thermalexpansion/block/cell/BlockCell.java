package cofh.thermalexpansion.block.cell;

import cofh.api.core.IModelRegister;
import cofh.core.util.CoreUtils;
import cofh.core.util.RegistryHelper;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.helpers.MathHelper;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.block.BlockTEBase;
import cofh.thermalexpansion.core.TEProps;
import cofh.thermalexpansion.model.ModelCell;
import cofh.thermalexpansion.util.ReconfigurableHelper;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class BlockCell extends BlockTEBase implements IModelRegister {

	public static final PropertyEnum<Type> TYPE = PropertyEnum.create("type", Type.class);
	public static final IUnlistedProperty<Integer> METER = Properties.toUnlisted(PropertyInteger.create("meter", 0, 8));

	public BlockCell() {

		super(Material.IRON);
		setHardness(20.0F);
		setResistance(120.0F);
		setUnlocalizedName("thermalexpansion.cell");
	}

	@Override
	protected BlockStateContainer createBlockState() {

		return new ExtendedBlockState(this,
				new IProperty[] { TYPE },
				new IUnlistedProperty[] { TEProps.FACING, TEProps.SIDE_CONFIG[0], TEProps.SIDE_CONFIG[1],
						TEProps.SIDE_CONFIG[2], TEProps.SIDE_CONFIG[3], TEProps.SIDE_CONFIG[4], TEProps.SIDE_CONFIG[5], METER }
		);
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {

		TileCell tile = (TileCell) world.getTileEntity(pos);

		return tile.getExtendedState(state, world, pos);
	}

	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {

		return layer == BlockRenderLayer.CUTOUT || layer == BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {

		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {

		return false;
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {

		if (enable[0]) {
			list.add(ItemBlockCell.setDefaultTag(new ItemStack(item, 1, 0), -1));
		}
		for (int i = 1; i < Type.values().length; i++) {
			list.add(ItemBlockCell.setDefaultTag(new ItemStack(item, 1, i), 0));
			list.add(ItemBlockCell.setDefaultTag(new ItemStack(item, 1, i), Type.byMetadata(i).getCapacity()));
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {

		return this.getDefaultState().withProperty(TYPE, BlockCell.Type.byMetadata(meta));
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
	public boolean hasTileEntity(IBlockState state) {

		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {

		Type type = state.getValue(TYPE);
		if (type == Type.CREATIVE) {
			if (!enable[Type.CREATIVE.ordinal()]) {
				return null;
			}
			return new TileCellCreative(type.metadata);
		}
		return new TileCell(type.metadata);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase living, ItemStack stack) {

		if (state.getValue(TYPE) == Type.CREATIVE && !enable[0]) {
			world.setBlockToAir(pos);
			return;
		}
		if (stack.getTagCompound() != null) {
			TileCell tile = (TileCell) world.getTileEntity(pos);

			tile.setEnergyStored(stack.getTagCompound().getInteger("Energy"));
			tile.energySend = stack.getTagCompound().getInteger("Send");
			tile.energyReceive = stack.getTagCompound().getInteger("Recv");

			int facing = BlockHelper.determineXZPlaceFacing(living);
			int storedFacing = ReconfigurableHelper.getFacing(stack);
			byte[] sideCache = ReconfigurableHelper.getSideCache(stack, tile.getDefaultSides());

			tile.sideCache[0] = sideCache[0];
			tile.sideCache[1] = sideCache[1];
			tile.sideCache[facing] = sideCache[storedFacing];
			tile.sideCache[BlockHelper.getLeftSide(facing)] = sideCache[BlockHelper.getLeftSide(storedFacing)];
			tile.sideCache[BlockHelper.getRightSide(facing)] = sideCache[BlockHelper.getRightSide(storedFacing)];
			tile.sideCache[BlockHelper.getOppositeSide(facing)] = sideCache[BlockHelper.getOppositeSide(storedFacing)];
		}
		super.onBlockPlacedBy(world, pos, state, living, stack);
	}

	@Override
	public float getBlockHardness(IBlockState state, World world, BlockPos pos) {

		return state.getValue(TYPE).getHardness();
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {

		return world.getBlockState(pos).getValue(TYPE).getResistance();
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {

		return true;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {

		return true;
	}

	@Override
	public NBTTagCompound getItemStackTag(IBlockAccess world, BlockPos pos) {

		NBTTagCompound tag = super.getItemStackTag(world, pos);
		TileCell tile = (TileCell) world.getTileEntity(pos);

		if (tile != null) {
			if (tag == null) {
				tag = new NBTTagCompound();
			}
			ReconfigurableHelper.setItemStackTagReconfig(tag, tile);

			tag.setInteger("Energy", tile.getEnergyStored(EnumFacing.DOWN));
			tag.setInteger("Send", tile.energySend);
			tag.setInteger("Recv", tile.energyReceive);
		}
		return tag;
	}

	@Override
	public boolean canDismantle(World world, BlockPos pos, IBlockState state, EntityPlayer player) {

		if (state.getValue(TYPE) == Type.CREATIVE && !CoreUtils.isOp(player)) {
			return false;
		}
		return super.canDismantle(world, pos, state, player);
	}

	/* IModelRegister */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels() {

		StateMapperBase mapper = new StateMapperBase() {

			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {

				return new ModelResourceLocation(ModelCell.BASE_MODEL_LOCATION.toString(),
						"type=" + state.getValue(TYPE).getName());
			}
		};
		ModelLoader.setCustomStateMapper(this, mapper);

		for (Type type : Type.values()) {
			ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation(
					ModelCell.BASE_MODEL_LOCATION.toString(), "type=" + type.getName());
			ModelLoader
					.setCustomModelResourceLocation(Item.getItemFromBlock(this), type.getMetadata(), itemModelResourceLocation);
		}

	}

	/* IInitializer */
	@Override
	public boolean preInit() {

		RegistryHelper.registerBlockAndItem(this, new ResourceLocation(ThermalExpansion.modId, "cell"), ItemBlockCell::new);

		return true;
	}

	@Override
	public boolean initialize() {

		TileCell.initialize();
		TileCellCreative.initialize();

		cellCreative = new ItemStack(this, 1, Type.CREATIVE.ordinal());
		cellBasic = new ItemStack(this, 1, Type.BASIC.ordinal());
		cellHardened = new ItemStack(this, 1, Type.HARDENED.ordinal());
		cellReinforced = new ItemStack(this, 1, Type.REINFORCED.ordinal());
		cellResonant = new ItemStack(this, 1, Type.RESONANT.ordinal());

		ItemBlockCell.setDefaultTag(cellCreative, 0);
		ItemBlockCell.setDefaultTag(cellBasic, 0);
		ItemBlockCell.setDefaultTag(cellHardened, 0);
		ItemBlockCell.setDefaultTag(cellReinforced, 0);
		ItemBlockCell.setDefaultTag(cellResonant, 0);

		return true;
	}

	@Override
	public boolean postInit() {

		//TODO fix recipes
/*		if (enable[Type.BASIC.ordinal()]) {
			GameRegistry.addRecipe(
					ShapedRecipe(cellBasic, " I ", "IXI", " P ", 'I', "ingotCopper", 'X', BlockFrame.frameCellBasic, 'P',
							ItemMaterial.powerCoilElectrum));
			PulverizerManager.addRecipe(4000, cellBasic, ItemHelper.cloneStack(Items.REDSTONE, 8),
					ItemHelper.cloneStack(ItemMaterial.ingotLead, 3));
		}
		if (enable[Type.HARDENED.ordinal()]) {
			GameRegistry.addRecipe(
					ShapedRecipe(cellHardened, " I ", "IXI", " P ", 'I', "ingotCopper", 'X', BlockFrame.frameCellHardened, 'P',
							ItemMaterial.powerCoilElectrum));
			GameRegistry.addRecipe(new RecipeUpgradeOverride(cellHardened,
					new Object[] { " I ", "IXI", " I ", 'I', "ingotInvar", 'X', cellBasic }).addInteger(
					"Send", Type.BASIC.getMaxSend(), Type.HARDENED.getMaxSend())
					.addInteger("Recv", Type.BASIC.getMaxReceive(), Type.HARDENED.getMaxReceive()));
			GameRegistry.addRecipe(
					ShapedRecipe(cellHardened, "IYI", "YXY", "IPI", 'I', "ingotInvar", 'X', BlockFrame.frameCellBasic, 'Y',
							"ingotCopper", 'P',
							ItemMaterial.powerCoilElectrum));
			PulverizerManager.addRecipe(4000, cellHardened, ItemHelper.cloneStack(Items.REDSTONE, 8),
					ItemHelper.cloneStack(ItemMaterial.ingotInvar, 3));
		}
		if (enable[Type.REINFORCED.ordinal()]) {
			GameRegistry.addRecipe(
					ShapedRecipe(cellReinforced, " X ", "YCY", "IPI", 'C', BlockFrame.frameCellReinforcedFull, 'I', "ingotLead",
							'P',
							ItemMaterial.powerCoilElectrum, 'X', "ingotElectrum", 'Y', "ingotElectrum"));
		}
		if (enable[Type.RESONANT.ordinal()]) {
			GameRegistry.addRecipe(
					ShapedRecipe(cellResonant, " X ", "YCY", "IPI", 'C', BlockFrame.frameCellResonantFull, 'I', "ingotLead", 'P',
							ItemMaterial.powerCoilElectrum, 'X', "ingotElectrum", 'Y', "ingotElectrum"));
			GameRegistry.addRecipe(new RecipeUpgradeOverride(cellResonant,
					new Object[] { " I ", "IXI", " I ", 'I', "ingotEnderium", 'X', cellReinforced })
					.addInteger("Send", Type.REINFORCED.getMaxSend(), Type.RESONANT.getMaxSend())
					.addInteger("Recv", Type.REINFORCED.getMaxReceive(),
							Type.RESONANT.getMaxReceive()));
		}
		TECraftingHandler.addSecureRecipe(cellCreative);
		TECraftingHandler.addSecureRecipe(cellBasic);
		TECraftingHandler.addSecureRecipe(cellHardened);
		TECraftingHandler.addSecureRecipe(cellReinforced);
		TECraftingHandler.addSecureRecipe(cellResonant);*/

		return true;
	}

	public enum Type implements IStringSerializable {

		CREATIVE(0, "creative", cellCreative, -1.0F, 1200, 100000, 100000, 100000),
		BASIC(1, "basic", cellBasic, 5.0F, 15, 200, 200, 400000),
		HARDENED(2, "hardened", cellHardened, 15.0F, 90, 800, 800, 2000000),
		REINFORCED(3, "reinforced", cellReinforced, 20.0F, 120, 8000, 8000, 20000000),
		RESONANT(4, "resonant", cellResonant, 20.0F, 120, 32000, 32000, 80000000);

		private static final Type[] METADATA_LOOKUP = new Type[values().length];
		private final int metadata;
		private final String name;
		private final ItemStack stack;
		private final float hardness;
		private final int resistance;
		private final int maxSend;
		private final int maxReceive;
		private final int capacity;

		Type(int metadata, String name, ItemStack stack, float hardness, int resistance, int maxSend, int maxReceive,
				int capacity) {

			this.metadata = metadata;
			this.name = name;
			this.stack = stack;
			this.hardness = hardness;
			this.resistance = resistance;

			String category = "Cell." + StringHelper.titleCase(name);
			if (metadata == 0) {
				this.maxSend = MathHelper
						.clamp(ThermalExpansion.CONFIG.get(category, "MaxValue", maxSend), maxSend / 10, maxSend * 1000);
				this.maxReceive = this.maxSend;
				this.capacity = -1;
			} else {
				this.maxSend = MathHelper
						.clamp(ThermalExpansion.CONFIG.get(category, "MaxSend", maxSend), maxSend / 10, maxSend * 1000);
				this.maxReceive = MathHelper
						.clamp(ThermalExpansion.CONFIG.get(category, "MaxReceive", maxReceive), maxReceive / 10,
								maxReceive * 1000);
				;
				this.capacity = MathHelper.clamp(ThermalExpansion.CONFIG.get(category, "Capacity", capacity), capacity / 10,
						metadata == 4 ? 1000000 * 1000 : capacity);
			}
		}

		@Override
		public String getName() {

			return name;
		}

		public int getMetadata() {

			return this.metadata;
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

		public float getHardness() {

			return hardness;
		}

		public int getResistance() {

			return resistance;
		}

		public int getMaxSend() {

			return maxSend;
		}

		public int getMaxReceive() {

			return maxReceive;
		}

		public int getCapacity() {

			return capacity;
		}
	}

	public static boolean[] enable = new boolean[Type.values().length];

	static {
		String category = "Cell.";

		enable[0] = ThermalExpansion.CONFIG.get(category + StringHelper.titleCase(Type.CREATIVE.getName()), "Enable", true);
		for (int i = 1; i < Type.values().length; i++) {
			enable[i] = ThermalExpansion.CONFIG
					.get(category + StringHelper.titleCase(Type.byMetadata(i).getName()), "Recipe.Enable", true);
		}
	}

	public static ItemStack cellCreative;
	public static ItemStack cellBasic;
	public static ItemStack cellHardened;
	public static ItemStack cellReinforced;
	public static ItemStack cellResonant;

}
