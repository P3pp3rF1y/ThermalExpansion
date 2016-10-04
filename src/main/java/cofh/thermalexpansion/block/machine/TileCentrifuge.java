package cofh.thermalexpansion.block.machine;

import cofh.core.util.CoreUtils;
import cofh.core.util.fluid.FluidTankAdv;
import cofh.lib.util.helpers.MathHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.core.TEProps;
import cofh.thermalexpansion.gui.client.machine.GuiCentrifuge;
import cofh.thermalexpansion.gui.container.machine.ContainerCentrifuge;
import cofh.thermalexpansion.util.crafting.CentrifugeManager;
import cofh.thermalexpansion.util.crafting.CentrifugeManager.RecipeCentrifuge;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TileCentrifuge extends TileMachineBase {

	public static void initialize() {

		int type = BlockMachine.Type.CENTRIFUGE.ordinal();

		DEFAULT_SIDE_CONFIG[type] = new SideConfig();
		DEFAULT_SIDE_CONFIG[type].numConfig = 6;
		DEFAULT_SIDE_CONFIG[type].slotGroups = new int[][] { {}, { 0 }, { 1, 2, 3, 4, 5, 6 }, {}, { 1, 2, 3, 4, 5, 6 }, { 0, 1, 2, 3, 4, 5, 6 } };
		DEFAULT_SIDE_CONFIG[type].allowInsertionSide = new boolean[] { false, true, false, false, false, true };
		DEFAULT_SIDE_CONFIG[type].allowExtractionSide = new boolean[] { false, true, true, true, true, true };
		DEFAULT_SIDE_CONFIG[type].allowInsertionSlot = new boolean[] { true, false, false, false, false };
		DEFAULT_SIDE_CONFIG[type].allowExtractionSlot = new boolean[] { true, true, true, true, false };
		DEFAULT_SIDE_CONFIG[type].sideTex = new int[] { 0, 1, 2, 3, 4, 7 };
		DEFAULT_SIDE_CONFIG[type].defaultSides = new byte[] { 3, 1, 2, 2, 2, 2 };

		String category = "Machine.Centrifuge";
		int basePower = MathHelper.clamp(ThermalExpansion.CONFIG.get(category, "BasePower", 40), 10, 500);
		ThermalExpansion.CONFIG.set(category, "BasePower", basePower);
		DEFAULT_ENERGY_CONFIG[type] = new EnergyConfig();
		DEFAULT_ENERGY_CONFIG[type].setParamsPower(basePower);

		SOUNDS[type] = CoreUtils.getSoundEvent(ThermalExpansion.modId, "blockMachineCentrifuge");

		GameRegistry.register(SOUNDS[type].setRegistryName(ThermalExpansion.modId, "blockMachineCentrifuge"));
		GameRegistry.registerTileEntity(TileCentrifuge.class, "thermalexpansion.machineCentrifuge");
	}

	int inputTracker;
	int outputTrackerPrimary;
	int outputTrackerSecondary;

	FluidTankAdv tank = new FluidTankAdv(TEProps.MAX_FLUID_LARGE);

	public TileCentrifuge() {

		super(BlockMachine.Type.CENTRIFUGE);
		inventory = new ItemStack[1 + 6 + 1];
	}

	@Override
	protected boolean canStart() {

		return false;
	}

	@Override
	protected boolean hasValidInput() {

		RecipeCentrifuge recipe = CentrifugeManager.getRecipe(inventory[0]);
		return recipe == null ? false : recipe.getInput().stackSize <= inventory[0].stackSize;
	}

	@Override
	protected void transferInput() {

		if (!augmentAutoInput) {
			return;
		}
		int side;
		for (int i = inputTracker + 1; i <= inputTracker + 6; i++) {
			side = i % 6;
			if (sideCache[side] == 1) {
				if (extractItem(0, AUTO_TRANSFER[level], EnumFacing.VALUES[side])) {
					inputTracker = side;
					break;
				}
			}
		}
	}

	@Override
	protected void transferOutput() {

		if (!augmentAutoOutput) {
			return;
		}
	}

	/* GUI METHODS */
	@Override
	public Object getGuiClient(InventoryPlayer inventory) {

		return new GuiCentrifuge(inventory, this);
	}

	@Override
	public Object getGuiServer(InventoryPlayer inventory) {

		return new ContainerCentrifuge(inventory, this);
	}

	@Override
	public FluidTankAdv getTank() {

		return tank;
	}

	@Override
	public FluidStack getTankFluid() {

		return tank.getFluid();
	}

	/* NBT METHODS */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		inputTracker = nbt.getInteger("TrackIn");
		outputTrackerPrimary = nbt.getInteger("TrackOut1");
		outputTrackerSecondary = nbt.getInteger("TrackOut2");

		tank.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		nbt.setInteger("TrackIn", inputTracker);
		nbt.setInteger("TrackOut1", outputTrackerPrimary);
		nbt.setInteger("TrackOut2", outputTrackerSecondary);
		tank.writeToNBT(nbt);

		return nbt;
	}

	/* IInventory */
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {

		return slot == 0 ? CentrifugeManager.recipeExists(stack) : true;
	}

}
