package cofh.thermalexpansion.block.machine;

import cofh.api.item.IAugmentItem;
import cofh.core.util.CoreUtils;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.MathHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.gui.client.machine.GuiFurnace;
import cofh.thermalexpansion.gui.container.machine.ContainerFurnace;
import cofh.thermalexpansion.item.TEAugments;
import cofh.thermalexpansion.util.crafting.FurnaceManager;
import cofh.thermalexpansion.util.crafting.FurnaceManager.RecipeFurnace;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TileFurnace extends TileMachineBase {

	public static void initialize() {

		int type = BlockMachine.Type.FURNACE.ordinal();

		DEFAULT_SIDE_CONFIG[type] = new SideConfig();
		DEFAULT_SIDE_CONFIG[type].numConfig = 4;
		DEFAULT_SIDE_CONFIG[type].slotGroups = new int[][] { {}, { 0 }, { 1 }, { 0, 1 } };
		DEFAULT_SIDE_CONFIG[type].allowInsertionSide = new boolean[] { false, true, false, true };
		DEFAULT_SIDE_CONFIG[type].allowExtractionSide = new boolean[] { false, true, true, true };
		DEFAULT_SIDE_CONFIG[type].allowInsertionSlot = new boolean[] { true, false, false };
		DEFAULT_SIDE_CONFIG[type].allowExtractionSlot = new boolean[] { true, true, false };
		DEFAULT_SIDE_CONFIG[type].sideTex = new int[] { 0, 1, 4, 7 };
		DEFAULT_SIDE_CONFIG[type].defaultSides = new byte[] { 1, 1, 2, 2, 2, 2 };

		String category = "Machine.Furnace";
		int basePower = MathHelper.clamp(ThermalExpansion.CONFIG.get(category, "BasePower", 20), 10, 500);
		ThermalExpansion.CONFIG.set(category, "BasePower", basePower);
		DEFAULT_ENERGY_CONFIG[type] = new EnergyConfig();
		DEFAULT_ENERGY_CONFIG[type].setParamsPower(basePower);

		SOUNDS[type] = CoreUtils.getSoundEvent(ThermalExpansion.modId, "blockMachineFurnace");

		GameRegistry.register(SOUNDS[type].setRegistryName(new ResourceLocation(ThermalExpansion.modId, "blockMachineFurnace")));
		GameRegistry.registerTileEntity(TileFurnace.class, "thermalexpansion.machineFurnace");
	}

	int inputTracker;
	int outputTracker;

	public boolean foodBoost;

	public TileFurnace() {

		super(BlockMachine.Type.FURNACE);
		inventory = new ItemStack[1 + 1 + 1];
	}

	@Override
	protected boolean canStart() {

		if (inventory[0] == null) {
			return false;
		}
		if (foodBoost && !FurnaceManager.isFoodItem(inventory[0])) {
			return false;
		}
		RecipeFurnace recipe = FurnaceManager.getRecipe(inventory[0]);

		if (recipe == null || energyStorage.getEnergyStored() < recipe.getEnergy() * energyMod / processMod) {
			return false;
		}
		ItemStack output = recipe.getOutput();

		if (inventory[1] == null) {
			return true;
		}
		if (!inventory[1].isItemEqual(output)) {
			return false;
		}
		return inventory[1].stackSize + output.stackSize <= output.getMaxStackSize();
	}

	@Override
	protected boolean hasValidInput() {

		RecipeFurnace recipe = FurnaceManager.getRecipe(inventory[0]);

		if (foodBoost && !FurnaceManager.isFoodItem(inventory[0])) {
			return false;
		}
		return recipe == null ? false : recipe.getInput().stackSize <= inventory[0].stackSize;
	}

	@Override
	protected void processStart() {

		processMax = FurnaceManager.getRecipe(inventory[0]).getEnergy();

		if (foodBoost) {
			processMax /= 2;
		}
		processRem = processMax;
	}

	@Override
	protected void processFinish() {

		RecipeFurnace recipe = FurnaceManager.getRecipe(inventory[0]);

		if (recipe == null) {
			isActive = false;
			wasActive = true;
			tracker.markTime(worldObj);
			processRem = 0;
			return;
		}
		ItemStack output = ItemHelper.cloneStack(recipe.getOutput());
		if (inventory[1] == null) {
			inventory[1] = output;
		} else {
			inventory[1].stackSize += output.stackSize;
		}
		if (foodBoost && recipe.isOutputFood() && inventory[1].stackSize < inventory[1].getMaxStackSize()) {
			inventory[1].stackSize += output.stackSize;
		}
		inventory[0].stackSize -= recipe.getInput().stackSize;

		if (inventory[0].stackSize <= 0) {
			inventory[0] = null;
		}
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
		if (inventory[1] == null) {
			return;
		}
		int side;
		for (int i = outputTracker + 1; i <= outputTracker + 6; i++) {
			side = i % 6;
			if (sideCache[side] == 2) {
				if (transferItem(1, AUTO_TRANSFER[level], EnumFacing.VALUES[side])) {
					outputTracker = side;
					break;
				}
			}
		}
	}

	/* GUI METHODS */
	@Override
	public Object getGuiClient(InventoryPlayer inventory) {

		return new GuiFurnace(inventory, this);
	}

	@Override
	public Object getGuiServer(InventoryPlayer inventory) {

		return new ContainerFurnace(inventory, this);
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

	/* AUGMENT HELPERS */
	@Override
	protected boolean installAugment(int slot) {

		IAugmentItem augmentItem = (IAugmentItem) augments[slot].getItem();
		boolean installed = false;

		if (augmentItem.getAugmentLevel(augments[slot], TEAugments.MACHINE_FURNACE_FOOD) > 0) {
			foodBoost = true;
			installed = true;
		}
		return installed ? true : super.installAugment(slot);
	}

	@Override
	protected void resetAugments() {

		super.resetAugments();

		foodBoost = false;
	}

	/* IInventory */
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {

		return slot == 0 ? foodBoost ? FurnaceManager.isFoodItem(stack) : FurnaceManager.recipeExists(stack) : true;
	}

}
