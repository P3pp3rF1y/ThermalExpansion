package cofh.thermalexpansion.core;

import cofh.api.core.IModelRegister;

import java.util.ArrayList;

import cofh.thermalexpansion.block.TEBlocks;
import cofh.thermalexpansion.model.BakedModelLoader;
import cofh.thermalexpansion.model.TextureLocations;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ProxyClient extends Proxy {

	/* INIT */
	@Override
	public void preInit(FMLPreInitializationEvent event) {

		super.preInit(event);

		MinecraftForge.EVENT_BUS.register(this);
		MinecraftForge.EVENT_BUS.register(new TextureLocations());

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

	@Override
	public void addModelRegister(IModelRegister objectToRegister) {
		modelList.add(objectToRegister);
	}

	@SubscribeEvent
	public void onTextureStitch(TextureStitchEvent event) {

/*
		for (ResourceLocation location : TextureLocations.Config.ALL) {
			event.getMap().registerSprite(location);
		}

		for (ResourceLocation location : TextureLocations.Machine.ALL) {
			event.getMap().registerSprite(location);
		}
*/
	}

	/* HELPERS */

	public static ArrayList<IModelRegister> modelList = new ArrayList<IModelRegister>();

}
