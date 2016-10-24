package cofh.thermalexpansion.block.cell;

import static cofh.lib.util.helpers.ItemHelper.ShapedRecipe;
import cofh.core.util.CoreUtils;
import cofh.core.util.RegistryHelper;
import cofh.core.util.crafting.RecipeUpgradeOverride;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.MathHelper;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.block.BlockTEBase;
import cofh.thermalexpansion.block.simple.BlockFrame;
import cofh.thermalexpansion.core.TEProps;
import cofh.thermalexpansion.item.TEItems;
import cofh.thermalexpansion.util.ReconfigurableHelper;
import cofh.thermalexpansion.util.crafting.PulverizerManager;
import cofh.thermalexpansion.util.crafting.TECraftingHandler;
import cofh.thermalfoundation.item.ItemMaterial;
import cofh.thermalfoundation.item.TFItems;
import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockCell extends BlockTEBase {

	public static final PropertyEnum<Type> TYPE = PropertyEnum.create("type", Type.class);

	public BlockCell() {

		super(Material.IRON);
		setHardness(20.0F);
		setResistance(120.0F);
		setUnlocalizedName("thermalexpansion.cell");
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
	public IIcon getIcon(int side, int metadata) {

		return IconRegistry.getIcon("Cell" + 2 * metadata);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir) {

		for (int i = 0; i < 9; i++) {
			IconRegistry.addIcon("CellMeter" + i, "thermalexpansion:cell/Cell_Meter_" + i, ir);
		}
		IconRegistry.addIcon("CellMeterCreative", "thermalexpansion:cell/Cell_Meter_Creative", ir);
		IconRegistry.addIcon("Cell" + 0, "thermalexpansion:cell/Cell_Creative", ir);
		IconRegistry.addIcon("Cell" + 1, "thermalexpansion:cell/Cell_Creative_Inner", ir);
		IconRegistry.addIcon("Cell" + 2, "thermalexpansion:cell/Cell_Basic", ir);
		IconRegistry.addIcon("Cell" + 3, "thermalexpansion:cell/Cell_Basic_Inner", ir);
		IconRegistry.addIcon("Cell" + 4, "thermalexpansion:cell/Cell_Hardened", ir);
		IconRegistry.addIcon("Cell" + 5, "thermalexpansion:cell/Cell_Hardened_Inner", ir);
		IconRegistry.addIcon("Cell" + 6, "thermalexpansion:cell/Cell_Reinforced", ir);
		IconRegistry.addIcon("Cell" + 7, "thermalexpansion:cell/Cell_Reinforced_Inner", ir);
		IconRegistry.addIcon("Cell" + 8, "thermalexpansion:cell/Cell_Resonant", ir);
		IconRegistry.addIcon("Cell" + 9, "thermalexpansion:cell/Cell_Resonant_Inner", ir);

		IconRegistry.addIcon(TEXTURE_DEFAULT + 0, "thermalexpansion:config/Config_None", ir);
		IconRegistry.addIcon(TEXTURE_DEFAULT + 1, "thermalexpansion:cell/Cell_Config_Orange", ir);
		IconRegistry.addIcon(TEXTURE_DEFAULT + 2, "thermalexpansion:cell/Cell_Config_Blue", ir);

		IconRegistry.addIcon(TEXTURE_CB + 0, "thermalexpansion:config/Config_None", ir);
		IconRegistry.addIcon(TEXTURE_CB + 1, "thermalexpansion:cell/Cell_Config_Orange_CB", ir);
		IconRegistry.addIcon(TEXTURE_CB + 2, "thermalexpansion:cell/Cell_Config_Blue_CB", ir);

		IconRegistry.addIcon("StorageRedstone", "thermalexpansion:cell/Cell_Center_Solid", ir);
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

		if (enable[Type.BASIC.ordinal()]) {
			GameRegistry.addRecipe(ShapedRecipe(cellBasic, " I ", "IXI", " P ", 'I', "ingotCopper", 'X', BlockFrame.frameCellBasic, 'P',
					ItemMaterial.powerCoilElectrum));
			PulverizerManager.addRecipe(4000, cellBasic, ItemHelper.cloneStack(Items.REDSTONE, 8), ItemHelper.cloneStack(ItemMaterial.ingotLead, 3));
		}
		if (enable[Type.HARDENED.ordinal()]) {
			GameRegistry.addRecipe(ShapedRecipe(cellHardened, " I ", "IXI", " P ", 'I', "ingotCopper", 'X', BlockFrame.frameCellHardened, 'P',
					ItemMaterial.powerCoilElectrum));
			GameRegistry.addRecipe(new RecipeUpgradeOverride(cellHardened, new Object[] { " I ", "IXI", " I ", 'I', "ingotInvar", 'X', cellBasic }).addInteger(
					"Send", Type.BASIC.getMaxSend(), Type.HARDENED.getMaxSend()).addInteger("Recv", Type.BASIC.getMaxReceive(), Type.HARDENED.getMaxReceive()));
			GameRegistry.addRecipe(ShapedRecipe(cellHardened, "IYI", "YXY", "IPI", 'I', "ingotInvar", 'X', BlockFrame.frameCellBasic, 'Y', "ingotCopper", 'P',
					ItemMaterial.powerCoilElectrum));
			PulverizerManager.addRecipe(4000, cellHardened, ItemHelper.cloneStack(Items.REDSTONE, 8), ItemHelper.cloneStack(ItemMaterial.ingotInvar, 3));
		}
		if (enable[Type.REINFORCED.ordinal()]) {
			GameRegistry.addRecipe(ShapedRecipe(cellReinforced, " X ", "YCY", "IPI", 'C', BlockFrame.frameCellReinforcedFull, 'I', "ingotLead", 'P',
					ItemMaterial.powerCoilElectrum, 'X', "ingotElectrum", 'Y', "ingotElectrum"));
		}
		if (enable[Type.RESONANT.ordinal()]) {
			GameRegistry.addRecipe(ShapedRecipe(cellResonant, " X ", "YCY", "IPI", 'C', BlockFrame.frameCellResonantFull, 'I', "ingotLead", 'P',
					ItemMaterial.powerCoilElectrum, 'X', "ingotElectrum", 'Y', "ingotElectrum"));
			GameRegistry.addRecipe(new RecipeUpgradeOverride(cellResonant, new Object[] { " I ", "IXI", " I ", 'I', "ingotEnderium", 'X', cellReinforced })
					.addInteger("Send", Type.REINFORCED.getMaxSend(), Type.RESONANT.getMaxSend()).addInteger("Recv", Type.REINFORCED.getMaxReceive(),
							Type.RESONANT.getMaxReceive()));
		}
		TECraftingHandler.addSecureRecipe(cellCreative);
		TECraftingHandler.addSecureRecipe(cellBasic);
		TECraftingHandler.addSecureRecipe(cellHardened);
		TECraftingHandler.addSecureRecipe(cellReinforced);
		TECraftingHandler.addSecureRecipe(cellResonant);

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

		Type(int metadata, String name, ItemStack stack, float hardness, int resistance, int maxSend, int maxReceive, int capacity) {

			this.metadata = metadata;
			this.name = name;
			this.stack = stack;
			this.hardness = hardness;
			this.resistance = resistance;

			String category = "Cell." + StringHelper.titleCase(name);
			if (metadata == 0) {
				this.maxSend = MathHelper.clamp(ThermalExpansion.CONFIG.get(category, "MaxValue", maxSend), maxSend / 10, maxSend * 1000);
				this.maxReceive = this.maxSend;
				this.capacity = -1;
			} else {
				this.maxSend = MathHelper.clamp(ThermalExpansion.CONFIG.get(category, "MaxSend", maxSend), maxSend / 10, maxSend * 1000);
				this.maxReceive = MathHelper.clamp(ThermalExpansion.CONFIG.get(category, "MaxReceive", maxReceive), maxReceive / 10, maxReceive * 1000);;
				this.capacity = MathHelper.clamp(ThermalExpansion.CONFIG.get(category, "Capacity", capacity), capacity / 10, metadata == 4 ? 1000000 * 1000 : capacity);
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
			enable[i] = ThermalExpansion.CONFIG.get(category + StringHelper.titleCase(Type.byMetadata(i).getName()), "Recipe.Enable", true);
		}
	}

	public static final String TEXTURE_DEFAULT = "CellConfig_";
	public static final String TEXTURE_CB = "CellConfig_CB_";

	public static String textureSelection = TEXTURE_DEFAULT;

	public static ItemStack cellCreative;
	public static ItemStack cellBasic;
	public static ItemStack cellHardened;
	public static ItemStack cellReinforced;
	public static ItemStack cellResonant;

}
