package cofh.thermalexpansion.render;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.HashMap;

/*
	Base implementation of perspective aware model.
	Mostly based on code found in chisels and bits mod. Thanks AlgorithmX2!
 */
public abstract class BasePerspectiveAwareModel implements IPerspectiveAwareModel {

	private static final HashMap<ItemCameraTransforms.TransformType, Matrix4f> TRANSFORMATIONS = new HashMap<>();

	static
	{
		{
			final Vector3f translation = new Vector3f( 0, 0, 0 );
			final Vector3f scale = new Vector3f( 0.625f, 0.625f, 0.625f );
			final Quat4f rotation = TRSRTransformation.quatFromXYZDegrees( new Vector3f( 30, 225, 0 ) );

			final TRSRTransformation transform = new TRSRTransformation( translation, rotation, scale, null );
			TRANSFORMATIONS.put(ItemCameraTransforms.TransformType.GUI, transform.getMatrix());
		}

		{
			final Vector3f translation = new Vector3f( 0, 0, 0 );
			final Vector3f scale = new Vector3f( 0.25f, 0.25f, 0.25f );
			final Quat4f rotation = TRSRTransformation.quatFromXYZDegrees( new Vector3f( 0, 0, 0 ) );

			final TRSRTransformation transform = new TRSRTransformation( translation, rotation, scale, null );
			TRANSFORMATIONS.put(ItemCameraTransforms.TransformType.GROUND, transform.getMatrix());
		}

		{
			final Vector3f translation = new Vector3f( 0, 0, 0 );
			final Vector3f scale = new Vector3f( 0.5f, 0.5f, 0.5f );
			final Quat4f rotation = TRSRTransformation.quatFromXYZDegrees( new Vector3f( 0, 0, 0 ) );

			final TRSRTransformation transform = new TRSRTransformation( translation, rotation, scale, null );
			TRANSFORMATIONS.put(ItemCameraTransforms.TransformType.FIXED, transform.getMatrix());
		}

		{
			final Vector3f translation = new Vector3f( 0, 0, 0 );
			final Vector3f scale = new Vector3f( 0.375f, 0.375f, 0.375f );
			final Quat4f rotation = TRSRTransformation.quatFromXYZDegrees( new Vector3f( 75, 45, 0 ) );

			final TRSRTransformation transform = new TRSRTransformation( translation, rotation, scale, null );
			Matrix4f matrix = transform.getMatrix();
			TRANSFORMATIONS.put(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, matrix);
			TRANSFORMATIONS.put(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, matrix);
		}

		{
			final Vector3f translation = new Vector3f( 0, 0, 0 );
			final Vector3f scale = new Vector3f( 0.40f, 0.40f, 0.40f );
			final Quat4f rotation = TRSRTransformation.quatFromXYZDegrees( new Vector3f( 0, 45, 0 ) );

			final TRSRTransformation transform = new TRSRTransformation( translation, rotation, scale, null );
			TRANSFORMATIONS.put(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, transform.getMatrix());
		}

		{
			final Vector3f translation = new Vector3f( 0, 0, 0 );
			final Vector3f scale = new Vector3f( 0.40f, 0.40f, 0.40f );
			final Quat4f rotation = TRSRTransformation.quatFromXYZDegrees( new Vector3f( 0, 225, 0 ) );

			final TRSRTransformation transform = new TRSRTransformation( translation, rotation, scale, null );
			TRANSFORMATIONS.put(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, transform.getMatrix());
		}
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(
			final ItemCameraTransforms.TransformType cameraTransformType )
	{

		return new ImmutablePair<>(this, TRANSFORMATIONS.getOrDefault(cameraTransformType, TRANSFORMATIONS.get(ItemCameraTransforms.TransformType.FIXED)));
	}
}
