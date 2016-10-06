package cofh.thermalexpansion.core;

import cofh.api.core.IModelRegister;

import java.util.ArrayList;

import cofh.thermalexpansion.model.BakedModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ProxyClient extends Proxy {

	/* INIT */
	@Override
	public void preInit(FMLPreInitializationEvent event) {

		super.preInit(event);

		ModelLoaderRegistry.registerLoader(new BakedModelLoader());

		for (int i = 0; i < modelList.size(); i++) {
			modelList.get(i).registerModels();
		}

	}

	@Override
	public void initialize(FMLInitializationEvent event) {

		super.initialize(event);
	}

	@Override
	public void postInit(FMLPostInitializationEvent event) {

		super.postInit(event);
	}

	/* REGISTRATION */

	/* HELPERS */

	public static ArrayList<IModelRegister> modelList = new ArrayList<IModelRegister>();

}
