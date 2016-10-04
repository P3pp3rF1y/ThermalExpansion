package cofh.thermalexpansion.block.machine;

import cofh.api.energy.IEnergyContainerItem;
import cofh.lib.util.helpers.EnergyHelper;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.MathHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.gui.client.machine.GuiCharger;
import cofh.thermalexpansion.gui.container.machine.ContainerCharger;
import cofh.thermalexpansion.util.crafting.ChargerManager;
import cofh.thermalexpansion.util.crafting.ChargerManager.RecipeCharger;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TileCharger extends TileMachineBase {

	static int RATE[];

	public static void initialize() {

		int type = BlockMachine.Type.CHARGER.ordinal();

		DEFAULT_SIDE_CONFIG[type] = new SideConfig();
		DEFAULT_SIDE_CONFIG[type].numConfig = 4;
		DEFAULT_SIDE_CONFIG[type].slotGroups = new int[][] { {}, { 0 }, { 2 }, { 0, 2 } };
		DEFAULT_SIDE_CONFIG[type].allowInsertionSide = new boolean[] { false, true, false, true };
		DEFAULT_SIDE_CONFIG[type].allowExtractionSide = new boolean[] { false, true, true, true };
		DEFAULT_SIDE_CONFIG[type].allowInsertionSlot = new boolean[] { true, false, false, false };
		DEFAULT_SIDE_CONFIG[type].allowExtractionSlot = new boolean[] { true, false, true, false };
		DEFAULT_SIDE_CONFIG[type].sideTex = new int[] { 0, 1, 4, 7 };
		DEFAULT_SIDE_CONFIG[type].defaultSides = new byte[] { 1, 1, 2, 2, 2, 2 };

		String category = "Machine.Charger";
		int basePower = MathHelper.clamp(ThermalExpansion.CONFIG.get(category, "BasePower", 8000), 100, 20000);
		ThermalExpansion.CONFIG.set(category, "BasePower", basePower);
		DEFAULT_ENERGY_CONFIG[type] = new EnergyConfig();
		DEFAULT_ENERGY_CONFIG[type].setParams(1, basePower, Math.max(400000, basePower * 50));

		RATE = new int[4];
		RATE[0] = basePower;
		RATE[1] = basePower * 2;
		RATE[2] = basePower * 3;
		RATE[3] = basePower * 4;

		GameRegistry.registerTileEntity(TileCharger.class, "thermalexpansion.machineCharger");
	}

	int inputTracker;
	int outputTracker;

	IEnergyContainerItem containerItem = null;

	public TileCharger() {

		super(BlockMachine.Type.CHARGER);
		inventory = new ItemStack[1 + 1 + 1 + 1];
	}

	@Override
	public void update() {

		if (ServerHelper.isClientWorld(worldObj)) {
			if (inventory[1] == null) {
				processRem = 0;
				containerItem = null;
			} else if (EnergyHelper.isEnergyContainerItem(inventory[1])) {
				containerItem = (IEnergyContainerItem) inventory[1].getItem();
			}
			return;
		}
		if (containerItem == null) {
			if (EnergyHelper.isEnergyContainerItem(inventory[1])) {
				updateContainerItem();
			}
		}
		if (containerItem != null) {
			boolean curActive = isActive;
			processContainerItem();
			updateIfChanged(curActive);
			chargeEnergy();
		} else {
			super.update();
		}
	}

	@Override
	protected int calcEnergy() {

		if (!isActive) {
			return 0;
		}
		int power = 0;

		if (energyStorage.getEnergyStored() > energyConfig.maxPowerLevel) {
			power = energyConfig.maxPower;
		} else if (energyStorage.getEnergyStored() < energyConfig.energyRamp) {
			power = energyConfig.minPower;
		} else {
			power = energyStorage.getEnergyStored() / energyConfig.energyRamp;
		}
		return containerItem != null ? Math.min(power, containerItem.receiveEnergy(inventory[1], power, true)) : power;
	}

	@Override
	protected int getMaxInputSlot() {

		// This is a hack to prevent super() logic from working.
		return -1;
	}

	@Override
	protected boolean canStart() {

		if (inventory[0] == null) {
			return false;
		}
		if (EnergyHelper.isEnergyContainerItem(inventory[0])) {
			inventory[1] = ItemHelper.cloneStack(inventory[0], 1);
			inventory[0].stackSize--;

			if (inventory[0].stackSize <= 0) {
				inventory[0] = null;
			}
		}
		RecipeCharger recipe = ChargerManager.getRecipe(inventory[0]);

		if (recipe == null || energyStorage.getEnergyStored() < recipe.getEnergy()) {
			return false;
		}
		if (inventory[0].stackSize < recipe.getInput().stackSize) {
			return false;
		}
		ItemStack output = recipe.getOutput();

		if (inventory[2] == null) {
			return true;
		}
		if (!inventory[2].isItemEqual(output)) {
			return false;
		}
		return inventory[2].stackSize + output.stackSize <= output.getMaxStackSize();
	}

	@Override
	protected boolean hasValidInput() {

		if (containerItem != null) {
			return true;
		}
		RecipeCharger recipe = ChargerManager.getRecipe(inventory[1]);
		return recipe == null ? false : recipe.getInput().stackSize <= inventory[1].stackSize;
	}

	@Override
	protected void processStart() {

		RecipeCharger recipe = ChargerManager.getRecipe(inventory[0]);
		processMax = recipe.getEnergy();
		processRem = processMax;

		inventory[1] = ItemHelper.cloneStack(inventory[0], recipe.getInput().stackSize);
		inventory[0].stackSize -= recipe.getInput().stackSize;

		if (inventory[0].stackSize <= 0) {
			inventory[0] = null;
		}
	}

	@Override
	protected void processFinish() {

		RecipeCharger recipe = ChargerManager.getRecipe(inventory[1]);

		if (recipe == null) {
			isActive = false;
			wasActive = true;
			tracker.markTime(worldObj);
			processRem = 0;
			return;
		}
		ItemStack output = recipe.getOutput();
		if (inventory[2] == null) {
			inventory[2] = output;
		} else {
			inventory[2].stackSize += output.stackSize;
		}
		inventory[1] = null;
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
		if (containerItem != null) {
			if (inventory[2] == null) {
				inventory[2] = ItemHelper.cloneStack(inventory[1], 1);
				inventory[1] = null;
				containerItem = null;
			} else {
				if (inventory[1].getMaxStackSize() > 1 && ItemHelper.itemsIdentical(inventory[1], inventory[2])
						&& inventory[2].stackSize + 1 <= inventory[2].getMaxStackSize()) {
					inventory[2].stackSize++;
					inventory[1] = null;
					containerItem = null;
				}
			}
		}
		if (containerItem == null && EnergyHelper.isEnergyContainerItem(inventory[0])) {
			inventory[1] = ItemHelper.cloneStack(inventory[0], 1);
			inventory[0].stackSize--;

			if (inventory[0].stackSize <= 0) {
				inventory[0] = null;
			}
		}
		int side;
		for (int i = outputTracker + 1; i <= outputTracker + 6; i++) {
			side = i % 6;

			if (sideCache[side] == 2) {
				if (transferItem(2, AUTO_TRANSFER[level], EnumFacing.VALUES[side])) {
					outputTracker = side;
					break;
				}
			}
		}
	}

	@Override
	protected void onLevelChange() {

		super.onLevelChange();
	}

	protected void processContainerItem() {

		if (isActive) {
			updateContainerCharge();
			if (!redstoneControlOrDisable()) {
				isActive = false;
				wasActive = true;
				tracker.markTime(worldObj);
			} else {
				if (containerItem == null) {
					if (EnergyHelper.isEnergyContainerItem(inventory[1])) {
						updateContainerItem();
						isActive = true;
					} else {
						isActive = false;
						wasActive = true;
						tracker.markTime(worldObj);
					}
				}
			}
		} else if (redstoneControlOrDisable()) {
			if (timeCheck()) {
				transferOutput();
			}
			if (containerItem == null) {
				if (EnergyHelper.isEnergyContainerItem(inventory[1])) {
					updateContainerItem();
				}
			}
			if (containerItem != null) {
				isActive = true;
			}
		}
	}

	protected void updateContainerItem() {

		containerItem = (IEnergyContainerItem) inventory[1].getItem();

		if (containerItem != null) {
			processMax = containerItem.getMaxEnergyStored(inventory[1]);
			processRem = processMax - containerItem.getEnergyStored(inventory[1]);
		}
	}

	protected void updateContainerCharge() {

		int energy = Math.min(energyStorage.getEnergyStored(), calcEnergy());
		int received = energyStorage.extractEnergy(containerItem.receiveEnergy(inventory[1], energy, false), false);
		processRem -= received;

		if (processRem <= 0) {
			transferOutput();

			if (!redstoneControlOrDisable()) {
				isActive = false;
				wasActive = true;
				tracker.markTime(worldObj);
			}
		}
	}

	/* GUI METHODS */
	@Override
	public Object getGuiClient(InventoryPlayer inventory) {

		return new GuiCharger(inventory, this);
	}

	@Override
	public Object getGuiServer(InventoryPlayer inventory) {

		return new ContainerCharger(inventory, this);
	}

	/* NBT METHODS */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		inputTracker = nbt.getInteger("TrackIn");
		outputTracker = nbt.getInteger("TrackOut");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		nbt.setInteger("TrackIn", inputTracker);
		nbt.setInteger("TrackOut", outputTracker);

		return nbt;
	}

	/* IInventory */
	@Override
	public ItemStack decrStackSize(int slot, int amount) {

		ItemStack stack = super.decrStackSize(slot, amount);

		if (ServerHelper.isServerWorld(worldObj) && slot == 1) {
			if (isActive && (inventory[slot] == null || !hasValidInput())) {
				isActive = false;
				wasActive = true;
				tracker.markTime(worldObj);
				processRem = 0;
				containerItem = null;
			}
		}
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {

		if (ServerHelper.isServerWorld(worldObj) && slot == 1) {
			if (isActive && inventory[slot] != null) {
				if (stack == null || !stack.isItemEqual(inventory[slot]) || !hasValidInput()) {
					isActive = false;
					wasActive = true;
					tracker.markTime(worldObj);
					processRem = 0;
				}
			}
			containerItem = null;
		}
		inventory[slot] = stack;

		if (stack != null && stack.stackSize > getInventoryStackLimit()) {
			stack.stackSize = getInventoryStackLimit();
		}
	}

	@Override
	public void markDirty() {

		if (isActive && !hasValidInput()) {
			containerItem = null;
		}
		super.markDirty();
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {

		return slot == 0 ? EnergyHelper.isEnergyContainerItem(stack) || ChargerManager.recipeExists(stack) : true;
	}

	/* IEnergyInfo */
	@Override
	public int getInfoEnergyPerTick() {

		return calcEnergy();
	}

	@Override
	public int getInfoMaxEnergyPerTick() {

		return energyConfig.maxPower;
	}

}
