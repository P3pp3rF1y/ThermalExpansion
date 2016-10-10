package cofh.thermalexpansion.block.machine;

/**
 * Implement this interface on Machine Tile Entities which display fluid texture on their face when active
 *
 * @author P3pp3rF1y
 *
 */
public interface IFluidFace {

	/**
	 * Returns the fluid texture to use for rendering.
	 *
	 * @return The icon to use.
	 */
	String getFluidTextureName();
}
