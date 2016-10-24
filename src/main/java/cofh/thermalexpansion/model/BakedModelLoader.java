package cofh.thermalexpansion.model;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

public class BakedModelLoader implements ICustomModelLoader {

	public static final ModelMachine MACHINE_MODEL = new ModelMachine();

	@Override
	public boolean accepts(ResourceLocation modelLocation) {

		return ModelMachine.MODEL_LOCATION.equals(modelLocation) || isSubModelOf(modelLocation, ModelCell.BASE_MODEL_LOCATION);
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws Exception {

		if (isSubModelOf(modelLocation, ModelCell.BASE_MODEL_LOCATION)) {
			return ModelCell.getModel(modelLocation);
		}

		return MACHINE_MODEL;
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {

	}

	private boolean isSubModelOf(ResourceLocation modelLocation, ResourceLocation baseModel) {

		if (modelLocation.getResourceDomain() != baseModel.getResourceDomain())
			return false;

		return modelLocation.getResourcePath().startsWith(baseModel.getResourcePath());
	}
}
