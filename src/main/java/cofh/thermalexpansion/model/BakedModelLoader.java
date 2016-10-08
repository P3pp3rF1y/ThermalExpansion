package cofh.thermalexpansion.model;

import cofh.thermalexpansion.ThermalExpansion;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

public class BakedModelLoader implements ICustomModelLoader {

	public static final ModelMachine MACHINE_MODEL = new ModelMachine();

	@Override
	public boolean accepts(ResourceLocation modelLocation) {
		return ModelMachine.MODEL_LOCATION.equals(modelLocation);
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws Exception {
		return MACHINE_MODEL;
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {

	}
}
