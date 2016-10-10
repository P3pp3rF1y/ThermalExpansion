package cofh.thermalexpansion.item;

import cofh.api.core.IInitializer;
import cofh.thermalexpansion.ThermalExpansion;

import java.util.ArrayList;

public class TEItems {

	private TEItems() {

	}

	public static void preInit() {

		itemAugment = new ItemAugment();

		initList.add(itemAugment);

		ThermalExpansion.proxy.addModelRegister(itemAugment);

		for (int i = 0; i < initList.size(); i++) {
			initList.get(i).preInit();
		}
	}

	public static void initialize() {

		for (int i = 0; i < initList.size(); i++) {
			initList.get(i).initialize();
		}
	}

	public static void postInit() {

		for (int i = 0; i < initList.size(); i++) {
			initList.get(i).postInit();
		}
	}

	static ArrayList<IInitializer> initList = new ArrayList<IInitializer>();

	/* REFERENCES */

	private static ItemAugment itemAugment;

}
