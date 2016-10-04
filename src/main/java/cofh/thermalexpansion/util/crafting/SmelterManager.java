package cofh.thermalexpansion.util.crafting;

import cofh.lib.inventory.ComparableItemStack;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.MathHelper;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.api.crafting.recipes.ISmelterRecipe;
import cofh.thermalfoundation.block.BlockGlass;
import cofh.thermalfoundation.item.ItemMaterial;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class SmelterManager {

	public static ItemStack blockSand = new ItemStack(Blocks.SAND);
	public static ItemStack blockSoulSand = new ItemStack(Blocks.SOUL_SAND);

	private static Map<List<ComparableItemStackSmelter>, RecipeSmelter> recipeMap = new THashMap<List<ComparableItemStackSmelter>, RecipeSmelter>();
	private static Set<ComparableItemStackSmelter> validationSet = new THashSet<ComparableItemStackSmelter>();
	private static Set<ComparableItemStackSmelter> lockSet = new THashSet<ComparableItemStackSmelter>();
	private static boolean allowOverwrite = false;
	public static final int DEFAULT_ENERGY = 3200;

	private static int oreMultiplier = 2;
	private static int oreMultiplierSpecial = 3;

	private static ArrayList<String> blastList = new ArrayList<String>();

	static {
		allowOverwrite = ThermalExpansion.CONFIG.get("RecipeManagers.Smelter", "AllowRecipeOverwrite", false);

		String category = "RecipeManagers.Smelter.Ore";
		String comment = "This sets the default rate for Ore->Ingot conversion. This number is used in all automatically generated recipes.";
		oreMultiplier = MathHelper.clamp(ThermalExpansion.CONFIG.get(category, "DefaultMultiplier", oreMultiplier, comment), 1, 64);

		comment = "This sets the boosted rate for Ore->Ingot conversion - when Rich Slag or Cinnabar Crystals are used. This number is used in all automatically generated recipes.";
		oreMultiplierSpecial = MathHelper.clamp(ThermalExpansion.CONFIG.get(category, "SpecialMultiplier", oreMultiplierSpecial, comment), 1, 64);

		blastList.add("mithril");
		blastList.add("enderium");

		blastList.add("aluminum");
		blastList.add("ardite");
		blastList.add("cobalt");
	}

	private SmelterManager() {

	}

	public static boolean isRecipeReversed(ItemStack primaryInput, ItemStack secondaryInput) {

		if (primaryInput == null || secondaryInput == null) {
			return false;
		}
		ComparableItemStackSmelter query = new ComparableItemStackSmelter(primaryInput);
		ComparableItemStackSmelter querySecondary = new ComparableItemStackSmelter(secondaryInput);

		RecipeSmelter recipe = recipeMap.get(Arrays.asList(query, querySecondary));
		return recipe != null ? false : recipeMap.get(Arrays.asList(querySecondary, query)) != null;
	}

	public static RecipeSmelter getRecipe(ItemStack primaryInput, ItemStack secondaryInput) {

		if (primaryInput == null || secondaryInput == null) {
			return null;
		}
		ComparableItemStackSmelter query = new ComparableItemStackSmelter(primaryInput);
		ComparableItemStackSmelter querySecondary = new ComparableItemStackSmelter(secondaryInput);

		RecipeSmelter recipe = recipeMap.get(Arrays.asList(query, querySecondary));

		if (recipe == null) {
			recipe = recipeMap.get(Arrays.asList(querySecondary, query));
		}
		if (recipe == null) {
			return null;
		}
		return recipe;
	}

	public static boolean recipeExists(ItemStack primaryInput, ItemStack secondaryInput) {

		return getRecipe(primaryInput, secondaryInput) != null;
	}

	public static RecipeSmelter[] getRecipeList() {

		return recipeMap.values().toArray(new RecipeSmelter[0]);
	}

	public static boolean isItemValid(ItemStack input) {

		return input == null ? false : validationSet.contains(new ComparableItemStackSmelter(input));
	}

	public static boolean isItemFlux(ItemStack input) {

		return input == null ? false : lockSet.contains(new ComparableItemStackSmelter(input));
	}

	public static boolean isStandardOre(String oreName) {

		return ItemHelper.oreNameExists(oreName) && FurnaceManager.recipeExists(OreDictionary.getOres(oreName).get(0));
	}

	public static void addDefaultRecipes() {

		addFlux(blockSand);
		addFlux(blockSoulSand);
		addFlux(ItemMaterial.crystalSlag);
		addFlux(ItemMaterial.crystalCinnabar);
		addFlux(ItemMaterial.dustPyrotheum);

		addTERecipe(4000, new ItemStack(Blocks.COBBLESTONE, 2), blockSand, new ItemStack(Blocks.STONEBRICK, 1), ItemMaterial.crystalSlag, 100);
		addTERecipe(4000, new ItemStack(Blocks.REDSTONE_ORE), blockSand, new ItemStack(Blocks.REDSTONE_BLOCK), ItemMaterial.crystalSlagRich, 50);
		addTERecipe(4000, new ItemStack(Blocks.NETHERRACK, 4), blockSoulSand, new ItemStack(Blocks.NETHER_BRICK, 2), ItemMaterial.dustSulfur, 25);

		ItemStack blockGlass = ItemHelper.cloneStack(BlockGlass.glassLead, 2);
		addAlloyRecipe(4000, "dustLead", 1, "dustObsidian", 4, blockGlass);
		addAlloyRecipe(4000, "ingotLead", 1, "dustObsidian", 4, blockGlass);

		blockGlass = ItemHelper.cloneStack(BlockGlass.glassLumium, 2);
		addAlloyRecipe(4000, "dustLumium", 1, "dustObsidian", 4, blockGlass);
		addAlloyRecipe(4000, "ingotLumium", 1, "dustObsidian", 4, blockGlass);

		addDefaultOreDictionaryRecipe("oreIron", "dustIron", ItemMaterial.ingotIron, ItemMaterial.ingotNickel);
		addDefaultOreDictionaryRecipe("oreGold", "dustGold", ItemMaterial.ingotGold, null, 20, 75, 25);
		addDefaultOreDictionaryRecipe("oreCopper", "dustCopper", ItemMaterial.ingotCopper, ItemMaterial.ingotGold);
		addDefaultOreDictionaryRecipe("oreTin", "dustTin", ItemMaterial.ingotTin, ItemMaterial.ingotIron);
		addDefaultOreDictionaryRecipe("oreSilver", "dustSilver", ItemMaterial.ingotSilver, ItemMaterial.ingotLead);
		addDefaultOreDictionaryRecipe("oreLead", "dustLead", ItemMaterial.ingotLead, ItemMaterial.ingotSilver);
		addDefaultOreDictionaryRecipe("oreNickel", "dustNickel", ItemMaterial.ingotNickel, ItemMaterial.ingotPlatinum, 15, 75, 25);
		addDefaultOreDictionaryRecipe("orePlatinum", "dustPlatinum", ItemMaterial.ingotPlatinum);
		addDefaultOreDictionaryRecipe(null, "dustElectrum", ItemMaterial.ingotElectrum);
		addDefaultOreDictionaryRecipe(null, "dustInvar", ItemMaterial.ingotInvar);
		addDefaultOreDictionaryRecipe(null, "dustBronze", ItemMaterial.ingotBronze);

		/* ALLOYS */
		ItemStack stackElectrum = ItemHelper.cloneStack(ItemMaterial.ingotElectrum, 2);
		ItemStack stackInvar = ItemHelper.cloneStack(ItemMaterial.ingotInvar, 3);
		ItemStack stackBronze = ItemHelper.cloneStack(ItemMaterial.ingotBronze, 4);

		addAlloyRecipe(1600, "dustSilver", 1, "dustGold", 1, stackElectrum);
		addAlloyRecipe(2400, "ingotSilver", 1, "ingotGold", 1, stackElectrum);
		addAlloyRecipe(1600, "dustNickel", 1, "dustIron", 2, stackInvar);
		addAlloyRecipe(2400, "ingotNickel", 1, "ingotIron", 2, stackInvar);
		addAlloyRecipe(1600, "dustTin", 1, "dustCopper", 3, stackBronze);
		addAlloyRecipe(2400, "ingotTin", 1, "ingotCopper", 3, stackBronze);
	}

	public static void loadRecipes() {

		String category = "RecipeManagers.Smelter.Recipes";

		boolean steelRecipe = ThermalExpansion.CONFIG.get(category, "Steel", true);

		/* STEEL */
		if (ItemHelper.oreNameExists("ingotSteel") && steelRecipe) {
			ItemStack ingotSteel = ItemHelper.cloneStack(OreDictionary.getOres("ingotSteel").get(0), 1);

			addAlloyRecipe(8000, "dustCoal", 2, "dustSteel", 1, ingotSteel);
			addAlloyRecipe(8000, "dustCoal", 2, "dustIron", 1, ingotSteel);
			addAlloyRecipe(8000, "dustCoal", 2, "ingotIron", 1, ingotSteel);
			addAlloyRecipe(8000, "dustCharcoal", 4, "dustSteel", 1, ingotSteel);
			addAlloyRecipe(8000, "dustCharcoal", 4, "dustIron", 1, ingotSteel);
			addAlloyRecipe(8000, "dustCharcoal", 4, "ingotIron", 1, ingotSteel);

			addDefaultOreDictionaryRecipe(null, "dustSteel", ingotSteel);
		}
		/* BRASS */
		if (ItemHelper.oreNameExists("ingotBrass")) {
			ItemStack ingotBrass = ItemHelper.cloneStack(OreDictionary.getOres("ingotBrass").get(0), 4);

			addAlloyRecipe(1600, "dustCopper", 3, "dustZinc", 1, ingotBrass);
			addAlloyRecipe(2400, "ingotCopper", 3, "ingotZinc", 1, ingotBrass);
		}
		String[] oreNameList = OreDictionary.getOreNames();
		String oreName = "";

		for (int i = 0; i < oreNameList.length; i++) {
			if (oreNameList[i].startsWith("ore")) {
				oreName = oreNameList[i].substring(3, oreNameList[i].length());

				if (isStandardOre(oreNameList[i])) {
					addDefaultOreDictionaryRecipe(oreName);
				}
			} else if (oreNameList[i].startsWith("dust")) {
				oreName = oreNameList[i].substring(4, oreNameList[i].length());

				if (isStandardOre(oreNameList[i])) {
					addDefaultOreDictionaryRecipe(oreName);
				}
			}
		}
		for (int i = 0; i < blastList.size(); i++) {
			addBlastOreRecipe(blastList.get(i));
		}
	}

	public static void refreshRecipes() {

		Map<List<ComparableItemStackSmelter>, RecipeSmelter> tempMap = new THashMap<List<ComparableItemStackSmelter>, RecipeSmelter>(recipeMap.size());
		Set<ComparableItemStackSmelter> tempSet = new THashSet<ComparableItemStackSmelter>();
		RecipeSmelter tempRecipe;

		for (Entry<List<ComparableItemStackSmelter>, RecipeSmelter> entry : recipeMap.entrySet()) {
			tempRecipe = entry.getValue();
			ComparableItemStackSmelter primary = new ComparableItemStackSmelter(tempRecipe.primaryInput);
			ComparableItemStackSmelter secondary = new ComparableItemStackSmelter(tempRecipe.secondaryInput);

			tempMap.put(Arrays.asList(primary, secondary), tempRecipe);
			tempSet.add(primary);
			tempSet.add(secondary);
		}
		recipeMap.clear();
		recipeMap = tempMap;
		validationSet.clear();
		validationSet = tempSet;

		Set<ComparableItemStackSmelter> tempSet2 = new THashSet<ComparableItemStackSmelter>();
		for (ComparableItemStackSmelter entry : lockSet) {
			ComparableItemStackSmelter lock = new ComparableItemStackSmelter(new ItemStack(entry.item, entry.stackSize, entry.metadata));
			tempSet2.add(lock);
		}
		lockSet.clear();
		lockSet = tempSet2;
	}

	/* ADD RECIPES */
	protected static boolean addTERecipe(int energy, ItemStack primaryInput, ItemStack secondaryInput, ItemStack primaryOutput, ItemStack secondaryOutput,
			int secondaryChance) {

		if (primaryInput == null || secondaryInput == null || energy <= 0) {
			return false;
		}
		RecipeSmelter recipe = new RecipeSmelter(primaryInput, secondaryInput, primaryOutput, secondaryOutput, secondaryChance, energy);
		recipeMap.put(Arrays.asList(new ComparableItemStackSmelter(primaryInput), new ComparableItemStackSmelter(secondaryInput)), recipe);
		validationSet.add(new ComparableItemStackSmelter(primaryInput));
		validationSet.add(new ComparableItemStackSmelter(secondaryInput));
		return true;
	}

	public static boolean addRecipe(int energy, ItemStack primaryInput, ItemStack secondaryInput, ItemStack primaryOutput, ItemStack secondaryOutput,
			int secondaryChance, boolean overwrite) {

		if (primaryInput == null || secondaryInput == null || energy <= 0 || !(allowOverwrite & overwrite) && recipeExists(primaryInput, secondaryInput)) {
			return false;
		}
		RecipeSmelter recipe = new RecipeSmelter(primaryInput, secondaryInput, primaryOutput, secondaryOutput, secondaryChance, energy);
		recipeMap.put(Arrays.asList(new ComparableItemStackSmelter(primaryInput), new ComparableItemStackSmelter(secondaryInput)), recipe);
		validationSet.add(new ComparableItemStackSmelter(primaryInput));
		validationSet.add(new ComparableItemStackSmelter(secondaryInput));
		return true;
	}

	/* REMOVE RECIPES */
	public static boolean removeRecipe(ItemStack primaryInput, ItemStack secondaryInput) {

		return recipeMap.remove(Arrays.asList(new ComparableItemStackSmelter(primaryInput), new ComparableItemStackSmelter(secondaryInput))) != null;
	}

	/* HELPER FUNCTIONS */
	private static void addFlux(ItemStack flux) {

		lockSet.add(new ComparableItemStackSmelter(flux));
	}

	public static void addDefaultOreDictionaryRecipe(String oreName, String dustName, ItemStack ingot, ItemStack ingotRelated, int richSlagChance,
			int slagOreChance, int slagDustChance) {

		if (ingot == null) {
			return;
		}
		if (oreName != null) {
			addOreToIngotRecipe(oreName, ItemHelper.cloneStack(ingot, oreMultiplier), ItemHelper.cloneStack(ingot, oreMultiplierSpecial),
					ItemHelper.cloneStack(ingotRelated, 1), richSlagChance, slagOreChance);
		}
		if (dustName != null) {
			addDustToIngotRecipe(dustName, ItemHelper.cloneStack(ingot, 2), slagDustChance);
		}
	}

	public static void addDefaultOreDictionaryRecipe(String oreType) {

		addDefaultOreDictionaryRecipe(oreType, "");
	}

	public static void addDefaultOreDictionaryRecipe(String oreType, String relatedType) {

		if (oreType.length() <= 0) {
			return;
		}
		String oreName = "ore" + StringHelper.titleCase(oreType);
		String dustName = "dust" + StringHelper.titleCase(oreType);
		String ingotName = "ingot" + StringHelper.titleCase(oreType);

		List<ItemStack> registeredOre = OreDictionary.getOres(oreName);
		List<ItemStack> registeredDust = OreDictionary.getOres(dustName);
		List<ItemStack> registeredIngot = OreDictionary.getOres(ingotName);
		List<ItemStack> registeredRelated = new ArrayList<ItemStack>();

		if (relatedType != "") {
			String relatedName = "ingot" + StringHelper.titleCase(relatedType);
			registeredRelated = OreDictionary.getOres(relatedName);
		}
		if (registeredIngot.isEmpty()) {
			return;
		}
		if (registeredOre.isEmpty()) {
			oreName = null;
		}
		if (registeredDust.isEmpty()) {
			dustName = null;
		}
		if (!registeredRelated.isEmpty() && registeredRelated.get(0) != null) {
			addDefaultOreDictionaryRecipe(oreName, dustName, registeredIngot.get(0), registeredRelated.get(0), 5, 75, 25);
		} else {
			addDefaultOreDictionaryRecipe(oreName, dustName, registeredIngot.get(0), null, 5, 75, 25);
		}
	}

	public static void addDefaultOreDictionaryRecipe(String oreName, String dustName, ItemStack ingot) {

		addDefaultOreDictionaryRecipe(oreName, dustName, ingot, null, 5, 75, 25);
	}

	public static void addDefaultOreDictionaryRecipe(String oreName, String dustName, ItemStack ingot, ItemStack ingotRelated) {

		addDefaultOreDictionaryRecipe(oreName, dustName, ingot, ingotRelated, 5, 75, 25);
	}

	public static void addOreToIngotRecipe(String oreName, ItemStack ingot2, ItemStack ingot3, ItemStack ingotSecondary, int richSlagChance, int slagOreChance) {

		List<ItemStack> registeredOres = OreDictionary.getOres(oreName);

		if (registeredOres.size() > 0) {
			ItemStack ore = registeredOres.get(0);
			addRecipe(3200, ore, blockSand, ingot2, ItemMaterial.crystalSlagRich, richSlagChance);
			addRecipe(4000, ore, ItemMaterial.crystalSlagRich, ingot3, ItemMaterial.crystalSlag, slagOreChance);
			addRecipe(4000, ore, ItemMaterial.dustPyrotheum, ingot2, ItemMaterial.crystalSlagRich, Math.min(60, richSlagChance * 3));

			if (ingotSecondary != null) {
				addRecipe(4000, ore, ItemMaterial.crystalCinnabar, ingot3, ingotSecondary, 100);
			} else {
				addRecipe(4000, ore, ItemMaterial.crystalCinnabar, ingot3, ItemMaterial.crystalSlagRich, 75);
			}
		}
	}

	public static void addDustToIngotRecipe(String dustName, ItemStack ingot2, int slagDustChance) {

		List<ItemStack> registeredOres = OreDictionary.getOres(dustName);

		if (registeredOres.size() > 0) {
			addRecipe(800, ItemHelper.cloneStack(registeredOres.get(0), 2), blockSand, ingot2, ItemMaterial.crystalSlag, slagDustChance);
		}
	}

	public static void addAlloyRecipe(int energy, String primaryOreName, int primaryAmount, String secondaryOreName, int secondaryAmount,
			ItemStack primaryOutput) {

		List<ItemStack> primaryOreList = OreDictionary.getOres(primaryOreName);
		List<ItemStack> secondaryOreList = OreDictionary.getOres(secondaryOreName);

		if (primaryOreList.size() > 0 && secondaryOreList.size() > 0) {
			addAlloyRecipe(energy, ItemHelper.cloneStack(primaryOreList.get(0), primaryAmount),
					ItemHelper.cloneStack(secondaryOreList.get(0), secondaryAmount), primaryOutput);
		}
	}

	public static void addAlloyRecipe(int energy, ItemStack primaryInput, ItemStack secondaryInput, ItemStack primaryOutput) {

		addTERecipe(energy, primaryInput, secondaryInput, primaryOutput, null, 0);
	}

	public static void addBlastOreRecipe(String oreType) {

		String oreName = "ore" + StringHelper.titleCase(oreType);
		String dustName = "dust" + StringHelper.titleCase(oreType);
		String ingotName = "ingot" + StringHelper.titleCase(oreType);

		List<ItemStack> registeredOre = OreDictionary.getOres(oreName);
		List<ItemStack> registeredDust = OreDictionary.getOres(dustName);
		List<ItemStack> registeredIngot = OreDictionary.getOres(ingotName);

		if (registeredIngot.isEmpty()) {
			return;
		}
		if (!registeredOre.isEmpty()) {
			addRecipe(12000, ItemHelper.cloneStack(registeredOre.get(0), 1), ItemMaterial.dustPyrotheum, ItemHelper.cloneStack(registeredIngot.get(0), 2));
		}
		if (!registeredDust.isEmpty()) {
			addRecipe(8000, ItemHelper.cloneStack(registeredDust.get(0), 2), ItemMaterial.dustPyrotheum, ItemHelper.cloneStack(registeredIngot.get(0), 2));
		}
	}

	public static void addBlastOreName(String oreName) {

		blastList.add(StringHelper.camelCase(oreName));
	}

	public static boolean addRecipe(int energy, ItemStack primaryInput, ItemStack secondaryInput, ItemStack primaryOutput) {

		return addRecipe(energy, primaryInput, secondaryInput, primaryOutput, false);
	}

	public static boolean addRecipe(int energy, ItemStack primaryInput, ItemStack secondaryInput, ItemStack primaryOutput, boolean overwrite) {

		return addRecipe(energy, primaryInput, secondaryInput, primaryOutput, null, 0, overwrite);
	}

	public static boolean addRecipe(int energy, ItemStack primaryInput, ItemStack secondaryInput, ItemStack primaryOutput, ItemStack secondaryOutput) {

		return addRecipe(energy, primaryInput, secondaryInput, primaryOutput, secondaryOutput, false);
	}

	public static boolean addRecipe(int energy, ItemStack primaryInput, ItemStack secondaryInput, ItemStack primaryOutput, ItemStack secondaryOutput,
			boolean overwrite) {

		return addRecipe(energy, primaryInput, secondaryInput, primaryOutput, secondaryOutput, 100, overwrite);
	}

	public static boolean addRecipe(int energy, ItemStack primaryInput, ItemStack secondaryInput, ItemStack primaryOutput, ItemStack secondaryOutput,
			int secondaryChance) {

		return addRecipe(energy, primaryInput, secondaryInput, primaryOutput, secondaryOutput, secondaryChance, false);
	}

	/* RECIPE CLASS */
	public static class RecipeSmelter implements ISmelterRecipe {

		final ItemStack primaryInput;
		final ItemStack secondaryInput;
		final ItemStack primaryOutput;
		final ItemStack secondaryOutput;
		final int secondaryChance;
		final int energy;

		RecipeSmelter(ItemStack primaryInput, ItemStack secondaryInput, ItemStack primaryOutput, ItemStack secondaryOutput, int secondaryChance, int energy) {

			this.primaryInput = primaryInput;
			this.secondaryInput = secondaryInput;
			this.primaryOutput = primaryOutput;
			this.secondaryOutput = secondaryOutput;
			this.secondaryChance = secondaryChance;
			this.energy = energy;

			if (primaryInput.stackSize <= 0) {
				primaryInput.stackSize = 1;
			}
			if (secondaryInput.stackSize <= 0) {
				secondaryInput.stackSize = 1;
			}
			if (primaryOutput.stackSize <= 0) {
				primaryOutput.stackSize = 1;
			}
			if (secondaryOutput != null && secondaryOutput.stackSize <= 0) {
				secondaryOutput.stackSize = 1;
			}
		}

		@Override
		public ItemStack getPrimaryInput() {

			return primaryInput;
		}

		@Override
		public ItemStack getSecondaryInput() {

			return secondaryInput;
		}

		@Override
		public ItemStack getPrimaryOutput() {

			return primaryOutput;
		}

		@Override
		public ItemStack getSecondaryOutput() {

			if (secondaryOutput == null) {
				return null;
			}
			return secondaryOutput;
		}

		@Override
		public int getSecondaryOutputChance() {

			return secondaryChance;
		}

		@Override
		public int getEnergy() {

			return energy;
		}
	}

	/* ITEMSTACK CLASS */
	public static class ComparableItemStackSmelter extends ComparableItemStack {

		static final String BLOCK = "block";
		static final String ORE = "ore";
		static final String DUST = "dust";
		static final String INGOT = "ingot";
		static final String NUGGET = "nugget";
		static final String SAND = "sand";

		public static boolean safeOreType(String oreName) {

			return oreName.startsWith(BLOCK) || oreName.startsWith(ORE) || oreName.startsWith(DUST) || oreName.startsWith(INGOT) || oreName.startsWith(NUGGET)
					|| oreName.equals("sand");
		}

		public static int getOreID(ItemStack stack) {

			int id = ItemHelper.oreProxy.getPrimaryOreID(stack);

			if (id == -1 || !safeOreType(ItemHelper.oreProxy.getOreName(id))) {
				return -1;
			}
			return id;
		}

		public static int getOreID(String oreName) {

			if (!safeOreType(oreName)) {
				return -1;
			}
			return ItemHelper.oreProxy.getOreID(oreName);
		}

		public ComparableItemStackSmelter(ItemStack stack) {

			super(stack);
			oreID = getOreID(stack);
		}

		public ComparableItemStackSmelter(Item item, int damage, int stackSize) {

			super(item, damage, stackSize);
			this.oreID = getOreID(this.toItemStack());
		}

		@Override
		public ComparableItemStackSmelter set(ItemStack stack) {

			super.set(stack);
			oreID = getOreID(stack);

			return this;
		}
	}

}
