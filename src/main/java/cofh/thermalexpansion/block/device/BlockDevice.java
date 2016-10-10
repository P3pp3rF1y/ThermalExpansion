package cofh.thermalexpansion.block.device;

import cofh.core.util.RegistryHelper;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.block.BlockTEBase;

import java.util.List;

import cofh.thermalexpansion.item.ItemAugment;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockDevice extends BlockTEBase {

	public static final PropertyEnum<BlockDevice.Type> VARIANT = PropertyEnum.<BlockDevice.Type> create("type", BlockDevice.Type.class);

	public BlockDevice() {

		super(Material.IRON);

		setUnlocalizedName("device");

		setHardness(15.0F);
		setResistance(25.0F);
	}

	@Override
	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, new IProperty[] { VARIANT });
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {

		for (int i = 0; i < BlockDevice.Type.METADATA_LOOKUP.length; i++) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {

		return this.getDefaultState().withProperty(VARIANT, BlockDevice.Type.byMetadata(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {

		return state.getValue(VARIANT).getMetadata();
	}

	@Override
	public int damageDropped(IBlockState state) {

		return state.getValue(VARIANT).getMetadata();
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {

		switch (state.getValue(VARIANT)) {
			case ACTIVATOR:
				return null;
			default:
				return null;
		}
	}

	/* IInitializer */
	@Override
	public boolean preInit() {

		RegistryHelper.registerBlockAndItem(this, new ResourceLocation(modName, "device"), ItemBlockDevice::new);

		TileDeviceBase.configure();

		TileActivator.initialize();
		TileBuffer.initialize();
		TileCollector.initialize();
		TileNullifier.initialize();

		//TODO READD
		/*
		TileWorkbenchFalse.initialize();
		TileBreaker.initialize();
		TileExtender.initialize();
*/
		if (defaultRedstoneControl) {
			defaultAugments[0] = ItemHelper.cloneStack(ItemAugment.generalRedstoneControl);
		}
		if (defaultReconfigSides) {
			defaultAugments[1] = ItemHelper.cloneStack(ItemAugment.generalReconfigSides);
		}
		deviceActivator = ItemBlockDevice.setDefaultTag(new ItemStack(this, 1, Type.ACTIVATOR.ordinal()));
		deviceBreaker = ItemBlockDevice.setDefaultTag(new ItemStack(this, 1, Type.BREAKER.ordinal()));
		deviceCollector = ItemBlockDevice.setDefaultTag(new ItemStack(this, 1, Type.COLLECTOR.ordinal()));
		deviceNullifier = ItemBlockDevice.setDefaultTag(new ItemStack(this, 1, Type.NULLIFIER.ordinal()));
		deviceBuffer = ItemBlockDevice.setDefaultTag(new ItemStack(this, 1, Type.BUFFER.ordinal()));
		// extender = ItemBlockDevice.setDefaultTag(new ItemStack(this, 1, Types.EXTENDER.ordinal()));

		return true;
	}

	@Override
	public boolean initialize() {

		return true;
	}

	@Override
	public boolean postInit() {

		return true;
	}

	/* TYPE */
	public static enum Type implements IStringSerializable {

		// @formatter:off
		ACTIVATOR(0, "activator", deviceActivator),
		BREAKER(1, "breaker", deviceBreaker),
		COLLECTOR(2, "collector", deviceCollector),
		NULLIFIER(3, "nullifier", deviceNullifier),
		BUFFER(4, "buffer", deviceBuffer);
		// @formatter:on

		private static final BlockDevice.Type[] METADATA_LOOKUP = new BlockDevice.Type[values().length];
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

	public static boolean[] enable = new boolean[Type.values().length];
	public static ItemStack[] defaultAugments = new ItemStack[4];

	public static boolean defaultRedstoneControl = true;
	public static boolean defaultReconfigSides = true;

	static {
		String category = "Device.";

		for (int i = 0; i < Type.values().length; i++) {
			enable[i] = ThermalExpansion.CONFIG.get(category + StringHelper.titleCase(Type.byMetadata(i).getName()), "Recipe.Enable", true);
		}
		//TODO READD THESE???
		/*
		enable[Type.WORKBENCH_FALSE.ordinal()] = false;
		enable[Type.PUMP.ordinal()] = false;
		enable[Type.EXTENDER.ordinal()] = false;
		ThermalExpansion.config.removeCategory(category + StringHelper.titleCase(NAMES[Types.WORKBENCH_FALSE.ordinal()]));
		ThermalExpansion.config.removeCategory(category + StringHelper.titleCase(NAMES[Types.PUMP.ordinal()]));
		ThermalExpansion.config.removeCategory(category + StringHelper.titleCase(NAMES[Types.EXTENDER.ordinal()]));
*/
	}


	/* REFERENCES */
	public static ItemStack deviceActivator;
	public static ItemStack deviceBreaker;
	public static ItemStack deviceCollector;
	public static ItemStack deviceNullifier;
	public static ItemStack deviceBuffer;
	public static ItemStack deviceExtender;

}
