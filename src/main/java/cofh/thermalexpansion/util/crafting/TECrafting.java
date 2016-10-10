package cofh.thermalexpansion.util.crafting;

public class TECrafting {

	public static void preInit() {

	}

	public static void initialize() {

	}

	public static void postInit() {

		FurnaceManager.addDefaultRecipes();
		PulverizerManager.addDefaultRecipes();
		SawmillManager.addDefaultRecipes();
		SmelterManager.addDefaultRecipes();
		CrucibleManager.addDefaultRecipes();
		TransposerManager.addDefaultRecipes();
		//TODO READD
/*
		PrecipitatorManager.addDefaultRecipes();
		ExtruderManager.addDefaultRecipes();
*/
		ChargerManager.addDefaultRecipes();
		InsolatorManager.addDefaultRecipes();
	}

	public static void loadComplete() {

		FurnaceManager.loadRecipes();
		PulverizerManager.loadRecipes();
		SawmillManager.loadRecipes();
		SmelterManager.loadRecipes();
		InsolatorManager.loadRecipes();
		ChargerManager.loadRecipes();
		CrucibleManager.loadRecipes();
		TransposerManager.loadRecipes();
		//TODO READD
/*
		PrecipitatorManager.loadRecipes();
		ExtruderManager.loadRecipes();
*/
	}
}
