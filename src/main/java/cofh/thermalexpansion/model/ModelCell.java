package cofh.thermalexpansion.model;

import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.block.cell.BlockCell;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;

import java.util.*;

public class ModelCell implements IModel {

	public static final ResourceLocation BASE_MODEL_LOCATION = new ResourceLocation(ThermalExpansion.modId, "cell");

	private static final Map<BlockCell.Type, ModelCell> MODELS = ImmutableMap.<BlockCell.Type, ModelCell>builder()
			.put(BlockCell.Type.CREATIVE, new ModelCell(BlockCell.Type.CREATIVE))
			.put(BlockCell.Type.BASIC, new ModelCell(BlockCell.Type.BASIC))
			.put(BlockCell.Type.HARDENED, new ModelCell(BlockCell.Type.HARDENED))
			.put(BlockCell.Type.REINFORCED, new ModelCell(BlockCell.Type.REINFORCED))
			.put(BlockCell.Type.RESONANT, new ModelCell(BlockCell.Type.RESONANT)).build();

	static Map<BlockCell.Type, BakedModelCell> bakedModels = new HashMap<>();

	private BlockCell.Type type;

	private ModelCell(BlockCell.Type type) {

		this.type = type;
	}

	@Override
	public Collection<ResourceLocation> getDependencies() {

		return Collections.emptySet();
	}

	@Override
	public Collection<ResourceLocation> getTextures() {

		return Collections.emptySet();
	}

	@Override
	public IBakedModel bake(IModelState state, VertexFormat format,
			Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {

		BakedModelCell bakedModel = new BakedModelCell(type, state, format, bakedTextureGetter);
		bakedModels.put(type, bakedModel);

		return bakedModel;
	}

	@Override
	public IModelState getDefaultState() {

		return null;
	}

	public static IModel getModel(ResourceLocation modelLocation) {

		String variant = ((ModelResourceLocation) modelLocation).getVariant();

		for (BlockCell.Type type : MODELS.keySet()) {
			//TODO optimize the String handling
			if (variant.endsWith(type.getName())) {
				return MODELS.get(type);
			}
		}

		return MODELS.get(BlockCell.Type.BASIC);
	}
}
