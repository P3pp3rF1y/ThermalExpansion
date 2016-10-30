package cofh.thermalexpansion.model;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

public class BakedModelLoader implements ICustomModelLoader {

	@Override
	public boolean accepts(ResourceLocation modelLocation) {

		return isSubModelOf(modelLocation, ModelMachine.BASE_MODEL_LOCATION)
				|| isSubModelOf(modelLocation, ModelCell.BASE_MODEL_LOCATION)
				|| isSubModelOf(modelLocation, ModelFrame.BASE_MODEL_LOCATION);
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws Exception {

		if (isSubModelOf(modelLocation, ModelCell.BASE_MODEL_LOCATION)) {
			return ModelCell.getModel(modelLocation);
		}
		if (isSubModelOf(modelLocation, ModelFrame.BASE_MODEL_LOCATION)) {
			return ModelFrame.getModel(modelLocation);
		}

		return ModelMachine.getModel(modelLocation);
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {

	}

	private boolean isSubModelOf(ResourceLocation modelLocation, ResourceLocation baseModel) {

		if (!modelLocation.getResourceDomain().equals(baseModel.getResourceDomain()))
			return false;

		return modelLocation.getResourcePath().startsWith(baseModel.getResourcePath());
	}
}
