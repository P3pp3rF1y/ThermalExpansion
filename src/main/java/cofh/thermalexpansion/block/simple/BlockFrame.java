package cofh.thermalexpansion.block.simple;

import cofh.api.block.IDismantleable;
import cofh.api.core.IInitializer;
import cofh.api.core.IModelRegister;
import cofh.core.util.CoreUtils;
import cofh.core.util.RegistryHelper;
import cofh.lib.util.helpers.ItemHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.model.ModelFrame;
import cofh.thermalexpansion.util.crafting.PulverizerManager;
import cofh.thermalexpansion.util.crafting.TransposerManager;
import cofh.thermalfoundation.fluid.TFFluids;
import cofh.thermalfoundation.item.ItemMaterial;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;

import static cofh.lib.util.helpers.ItemHelper.ShapedRecipe;

public class BlockFrame extends Block implements IDismantleable, IInitializer, IModelRegister {

	public static final PropertyEnum<Type> TYPE = PropertyEnum.create("type", Type.class);
	public static int renderPass = 0;

	public static boolean hasCenter(int metadata) {

		return metadata < Type.ILLUMINATOR.getMetadata();
	}

	public static boolean hasFrame(int metadata) {

		return metadata < Type.ILLUMINATOR.getMetadata();
	}

	public BlockFrame() {

		super(Material.IRON);
		setHardness(15.0F);
		setResistance(25.0F);
		setSoundType(SoundType.METAL);
		setCreativeTab(ThermalExpansion.tabBlocks);
		setUnlocalizedName("thermalexpansion.frame");
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {

		for (int i = 0; i < Type.values().length; i++) {
			if (enable[i]) {
				list.add(new ItemStack(item, 1, i));
			}
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, new IProperty[] { TYPE });
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {

		return super.getStateFromMeta(meta).withProperty(TYPE, Type.byMetadata(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {

		return state.getValue(TYPE).getMetadata();
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
	public int damageDropped(IBlockState state) {

		return state.getValue(TYPE).getMetadata();
	}

	@Override
	public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, EntityLiving.SpawnPlacementType type) {

		return false;
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
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {

		return true;
	}

	/* IDismantleable */
	@Override
	public ArrayList<ItemStack> dismantleBlock(World world, BlockPos pos, IBlockState state, EntityPlayer player,
			boolean returnDrops) {

		Type type = state.getValue(TYPE);
		ItemStack dropBlock = new ItemStack(this, 1, type.getMetadata());
		world.setBlockToAir(pos);

		if (!returnDrops) {
			float f = 0.3F;
			double x2 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
			double y2 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
			double z2 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
			EntityItem entity = new EntityItem(world, pos.getX() + x2, pos.getY() + y2, pos.getZ() + z2, dropBlock);
			entity.setPickupDelay(10);
			world.spawnEntityInWorld(entity);

			CoreUtils.dismantleLog(player.getName(), pos, state);
		}
		ArrayList<ItemStack> ret = new ArrayList<>();
		ret.add(dropBlock);
		return ret;
	}

	@Override
	public boolean canDismantle(World world, BlockPos pos, IBlockState state, EntityPlayer player) {

		return true;
	}

	/* IModelRegister */
	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels() {

		StateMapperBase mapper = new StateMapperBase() {

			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState state) {

				return new ModelResourceLocation(ModelFrame.BASE_MODEL_LOCATION.toString(),
						"type=" + state.getValue(TYPE).getName());
			}
		};
		ModelLoader.setCustomStateMapper(this, mapper);

		for (Type type : Type.values()) {
			ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation(
					ModelFrame.BASE_MODEL_LOCATION.toString(), "type=" + type.getName());
			ModelLoader
					.setCustomModelResourceLocation(Item.getItemFromBlock(this), type.getMetadata(), itemModelResourceLocation);
		}

	}

	/* IInitializer */
	@Override
	public boolean preInit() {

		RegistryHelper.registerBlockAndItem(this, new ResourceLocation(ThermalExpansion.modId, "frame"), ItemBlockFrame::new);

		return true;
	}

	@Override
	public boolean initialize() {

		frameMachineBasic = new ItemStack(this, 1, Type.MACHINE_BASIC.getMetadata());
		frameMachineHardened = new ItemStack(this, 1, Type.MACHINE_HARDENED.getMetadata());
		frameMachineReinforced = new ItemStack(this, 1, Type.MACHINE_REINFORCED.getMetadata());
		frameMachineResonant = new ItemStack(this, 1, Type.MACHINE_RESONANT.getMetadata());
		frameCellBasic = new ItemStack(this, 1, Type.CELL_BASIC.getMetadata());
		frameCellHardened = new ItemStack(this, 1, Type.CELL_HARDENED.getMetadata());
		frameCellReinforcedEmpty = new ItemStack(this, 1, Type.CELL_REINFORCED_EMPTY.getMetadata());
		frameCellReinforcedFull = new ItemStack(this, 1, Type.CELL_REINFORCED_FULL.getMetadata());
		frameCellResonantEmpty = new ItemStack(this, 1, Type.CELL_RESONANT_EMPTY.getMetadata());
		frameCellResonantFull = new ItemStack(this, 1, Type.CELL_RESONANT_FULL.getMetadata());
		frameTesseractEmpty = new ItemStack(this, 1, Type.TESSERACT_EMPTY.getMetadata());
		frameTesseractFull = new ItemStack(this, 1, Type.TESSERACT_FULL.getMetadata());
		frameIlluminator = new ItemStack(this, 1, Type.ILLUMINATOR.getMetadata());

		OreDictionary.registerOre("thermalexpansion:machineFrame", frameMachineBasic);
		OreDictionary.registerOre("thermalexpansion:machineFrame", frameMachineHardened);
		OreDictionary.registerOre("thermalexpansion:machineFrame", frameMachineReinforced);
		OreDictionary.registerOre("thermalexpansion:machineFrame", frameMachineResonant);

		return true;
	}

	@Override
	public boolean postInit() {

		GameRegistry.addRecipe(ShapedRecipe(frameMachineBasic,
				"IGI", "GXG", "IGI", 'I', "ingotIron", 'G', "blockGlass", 'X', "gearTin"));

		/* Direct Recipes */
		// GameRegistry.addRecipe(ShapedRecipe(frameMachineHardened, new Object[] { "IGI", "GXG", "IGI", 'I', "ingotInvar", 'G', "blockGlass", 'X',
		// "gearElectrum" }));
		// GameRegistry.addRecipe(ShapedRecipe(frameMachineReinforced, new Object[] { "IGI", "GXG", "IGI", 'I', "ingotInvar", 'G', "blockGlassHardened",
		// 'X', "gearSignalum" }));
		// GameRegistry.addRecipe(ShapedRecipe(frameMachineResonant, new Object[] { "IGI", "GXG", "IGI", 'I', "ingotInvar", 'G', "blockGlassHardened",
		// 'X',
		// "gearEnderium" }));

		/* Tiered Recipes */
		GameRegistry.addRecipe(
				ShapedRecipe(frameMachineHardened, "IGI", " X ", "I I", 'I', "ingotInvar", 'G', "gearElectrum", 'X',
						frameMachineBasic));
		GameRegistry.addRecipe(
				ShapedRecipe(frameMachineReinforced, "IGI", " X ", "I I", 'I', "blockGlassHardened", 'G', "gearSignalum", 'X',
						frameMachineHardened));
		GameRegistry
				.addRecipe(ShapedRecipe(frameMachineResonant, "IGI", " X ", "I I", 'I', "ingotSilver", 'G', "gearEnderium", 'X',
						frameMachineReinforced));

		GameRegistry.addRecipe(ShapedRecipe(frameCellBasic, "IGI", "GXG", "IGI", 'I', "ingotLead", 'G', "blockGlass", 'X',
				Blocks.REDSTONE_BLOCK));
		PulverizerManager.addRecipe(4000, frameCellBasic, ItemHelper.cloneStack(Items.REDSTONE, 8),
				ItemHelper.cloneStack(ItemMaterial.ingotLead, 3));

		GameRegistry.addRecipe(
				ShapedRecipe(frameCellHardened, " I ", "IXI", " I ", 'I', "ingotInvar", 'X', frameCellBasic));
		PulverizerManager.addRecipe(8000, frameCellHardened, ItemHelper.cloneStack(Items.REDSTONE, 8),
				ItemHelper.cloneStack(ItemMaterial.ingotInvar, 3));

		GameRegistry.addRecipe(
				ShapedRecipe(frameCellReinforcedEmpty, "IGI", "GXG", "IGI", 'I', "ingotElectrum", 'G', "blockGlassHardened", 'X',
						"gemDiamond"));
		TransposerManager.addTEFillRecipe(16000, frameCellReinforcedEmpty, frameCellReinforcedFull,
				new FluidStack(TFFluids.fluidRedstone, 4000), false);

		GameRegistry.addRecipe(
				ShapedRecipe(frameCellResonantEmpty, " I ", "IXI", " I ", 'I', "ingotEnderium", 'X', frameCellReinforcedEmpty));
		GameRegistry.addRecipe(
				ShapedRecipe(frameCellResonantFull, " I ", "IXI", " I ", 'I', "ingotEnderium", 'X', frameCellReinforcedFull));
		TransposerManager.addTEFillRecipe(16000, frameCellResonantEmpty, frameCellResonantFull,
				new FluidStack(TFFluids.fluidRedstone, 4000), false);

		if (recipe[Type.TESSERACT_EMPTY.getMetadata()]) {
			GameRegistry.addRecipe(
					ShapedRecipe(frameTesseractEmpty, "IGI", "GXG", "IGI", 'I', "ingotEnderium", 'G', "blockGlassHardened", 'X',
							"gemDiamond"));
		}
		if (recipe[Type.TESSERACT_FULL.getMetadata()]) {
			TransposerManager
					.addTEFillRecipe(16000, frameTesseractEmpty, frameTesseractFull, new FluidStack(TFFluids.fluidEnder, 1000),
							false);
		}
		GameRegistry.addRecipe(
				ShapedRecipe(ItemHelper.cloneStack(frameIlluminator, 2), " Q ", "G G", " S ", 'G', "blockGlassHardened", 'Q',
						"gemQuartz", 'S', "ingotSignalum"));

		return true;
	}

	public enum Type implements IStringSerializable {

		MACHINE_BASIC(0, "machineBasic", 5.0F, 15),
		MACHINE_HARDENED(1, "machineHardened", 15.0F, 90),
		MACHINE_REINFORCED(2, "machineReinforced", 20.0F, 120),
		MACHINE_RESONANT(3, "machineResonant", 20.0F, 120),
		CELL_BASIC(4, "cellBasic", 5.0F, 15),
		CELL_HARDENED(5, "cellHardened", 15.0F, 90),
		CELL_REINFORCED_EMPTY(6, "cellReinforcedEmpty", 20.0F, 120),
		CELL_REINFORCED_FULL(7, "cellReinforcedFull", 20.0F, 120),
		CELL_RESONANT_EMPTY(8, "cellResonantEmpty", 20.0F, 120),
		CELL_RESONANT_FULL(9, "cellResonantFull", 20.0F, 120),
		TESSERACT_EMPTY(10, "tesseractEmpty", 15.0F, 2000),
		TESSERACT_FULL(11, "tesseractFull", 15.0F, 2000),
		ILLUMINATOR(12, "illuminator", 3.0F, 150);

		private static final Type[] METADATA_LOOKUP = new Type[values().length];
		private int metadata;
		private String name;
		private float hardness;
		private int resistance;

		Type(int metadata, String name, float hardness, int resistance) {

			this.metadata = metadata;
			this.name = name;
			this.hardness = hardness;
			this.resistance = resistance;
		}

		@Override
		public String getName() {

			return name.toLowerCase();
		}

		public int getMetadata() {

			return metadata;
		}

		public float getHardness() {

			return hardness;
		}

		public int getResistance() {

			return resistance;
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

		public String getUnlocalizedName() {

			return name;
		}
	}

	public static boolean[] enable = new boolean[Type.values().length];
	public static boolean[] recipe = new boolean[Type.values().length];

	static {
		for (int i = 0; i < Type.values().length; i++) {
			enable[i] = true;
			recipe[i] = true;
		}
	}

	public static ItemStack frameMachineBasic;
	public static ItemStack frameMachineHardened;
	public static ItemStack frameMachineReinforced;
	public static ItemStack frameMachineResonant;
	public static ItemStack frameCellBasic;
	public static ItemStack frameCellHardened;
	public static ItemStack frameCellReinforcedEmpty;
	public static ItemStack frameCellReinforcedFull;
	public static ItemStack frameCellResonantEmpty;
	public static ItemStack frameCellResonantFull;
	public static ItemStack frameTesseractEmpty;
	public static ItemStack frameTesseractFull;
	public static ItemStack frameIlluminator;

}
