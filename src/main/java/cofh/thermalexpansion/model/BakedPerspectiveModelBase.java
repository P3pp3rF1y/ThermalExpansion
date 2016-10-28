package cofh.thermalexpansion.model;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import java.util.Map;

import static net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType.*;

public abstract class BakedPerspectiveModelBase implements IPerspectiveAwareModel {

	private static final Map<TransformType, Matrix4f> TRANSFORMATIONS;

	static {

		TRANSFORMATIONS = ImmutableMap.<TransformType, Matrix4f>builder()
				.put(GUI, 						getTransformMatrix(30, 225, 0, 0, 0, 0, 0.625f, 0.625f, 0.625f))
				.put(GROUND, 					getTransformMatrix(0, 0, 0, 0, 3, 0, 0.25f, 0.25f, 0.25f))
				.put(FIXED, 					getTransformMatrix(0, 0, 0, 0, 0, 0, 0.5f, 0.5f, 0.5f))
				.put(THIRD_PERSON_LEFT_HAND, 	getTransformMatrix(75, 45, 0, 0, 2.5f, 0, 0.375f, 0.375f, 0.375f))
				.put(THIRD_PERSON_RIGHT_HAND, 	getTransformMatrix(75, 45, 0, 0, 2.5f, 0, 0.375f, 0.375f, 0.375f))
				.put(FIRST_PERSON_RIGHT_HAND, 	getTransformMatrix(0, 45, 0, 0, 0, 0, 0.40f, 0.40f, 0.40f))
				.put(FIRST_PERSON_LEFT_HAND, 	getTransformMatrix(0, 225, 0, 0, 0, 0, 0.40f, 0.40f, 0.40f)).build();
	}

	private static Matrix4f getTransformMatrix(float rotationX, float rotationY, float rotationZ, float translationX,
			float translationY, float translationZ, float scaleX, float scaleY,
			float scaleZ) {

		final javax.vecmath.Vector3f translation = new javax.vecmath.Vector3f(translationX / 16, translationY / 16,
				translationZ / 16);
		final javax.vecmath.Vector3f scale = new javax.vecmath.Vector3f(scaleX, scaleY, scaleZ);
		final Quat4f rotation = TRSRTransformation
				.quatFromXYZDegrees(new javax.vecmath.Vector3f(rotationX, rotationY, rotationZ));

		final TRSRTransformation transform = new TRSRTransformation(translation, rotation, scale, null);

		return transform.getMatrix();
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(
			final TransformType cameraTransformType) {

		Matrix4f matrix = TRANSFORMATIONS.get(cameraTransformType);

		if (matrix == null) {
			matrix = TRANSFORMATIONS.get(TransformType.FIXED);
		}

		return new ImmutablePair<IBakedModel, Matrix4f>(this, matrix);
	}
}
