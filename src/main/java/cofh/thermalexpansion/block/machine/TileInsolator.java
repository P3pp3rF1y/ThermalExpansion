package cofh.thermalexpansion.block.machine;

import cofh.core.network.PacketCoFHBase;
import cofh.core.util.CoreUtils;
import cofh.core.util.fluid.FluidTankAdv;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.MathHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.core.TEProps;
import cofh.thermalexpansion.gui.client.machine.GuiInsolator;
import cofh.thermalexpansion.gui.container.machine.ContainerInsolator;
import cofh.thermalexpansion.util.crafting.InsolatorManager;
import cofh.thermalexpansion.util.crafting.InsolatorManager.RecipeInsolator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TileInsolator extends TileMachineBase implements IFluidHandler {

	public static void initialize() {

		int type = BlockMachine.Type.INSOLATOR.ordinal();

		DEFAULT_SIDE_CONFIG[type] = new SideConfig();
		DEFAULT_SIDE_CONFIG[type].numConfig = 8;
		DEFAULT_SIDE_CONFIG[type].slotGroups = new int[][] { {}, { 0, 1 }, { 2 }, { 3 }, { 2, 3 }, { 0 }, { 1 }, { 0, 1, 2, 3 } };
		DEFAULT_SIDE_CONFIG[type].allowInsertionSide = new boolean[] { false, true, false, false, false, true, true, true };
		DEFAULT_SIDE_CONFIG[type].allowExtractionSide = new boolean[] { false, true, true, true, true, false, false, true };
		DEFAULT_SIDE_CONFIG[type].allowInsertionSlot = new boolean[] { true, true, false, false, false };
		DEFAULT_SIDE_CONFIG[type].allowExtractionSlot = new boolean[] { true, true, true, true, false };
		DEFAULT_SIDE_CONFIG[type].sideTex = new int[] { 0, 1, 2, 3, 4, 5, 6, 7 };
		DEFAULT_SIDE_CONFIG[type].defaultSides = new byte[] { 3, 1, 2, 2, 2, 2 };

		String category = "Machine.Insolator";
		int basePower = MathHelper.clamp(ThermalExpansion.CONFIG.get(category, "BasePower", 20), 10, 500);
		ThermalExpansion.CONFIG.set(category, "BasePower", basePower);
		DEFAULT_ENERGY_CONFIG[type] = new EnergyConfig();
		DEFAULT_ENERGY_CONFIG[type].setParamsPower(basePower);

		SOUNDS[type] = CoreUtils.getSoundEvent(ThermalExpansion.modId, "blockMachineInsolator");

		GameRegistry.register(SOUNDS[type].setRegistryName(new ResourceLocation(ThermalExpansion.modId, "blockMachineInsolator")));
		GameRegistry.registerTileEntity(TileInsolator.class, "thermalexpansion.machineInsolator");
	}

	int inputTrackerPrimary;
	int inputTrackerSecondary;
	int outputTrackerPrimary;
	int outputTrackerSecondary;

	public boolean lockPrimary = false;

	FluidTankAdv tank = new FluidTankAdv(TEProps.MAX_FLUID_LARGE);

	public TileInsolator() {

		super(BlockMachine.Type.INSOLATOR);
		inventory = new ItemStack[2 + 1 + 1 + 1];
		tank.setLock(FluidRegistry.WATER);
	}

	@Override
	public void update() {

		if (ServerHelper.isClientWorld(worldObj)) {
			return;
		}
		boolean curActive = isActive;

		if (isActive) {
			if (processRem > 0) {
				int energy = calcEnergy();
				energyStorage.modifyEnergyStored(-energy * energyMod);
				processRem -= energy * processMod;
				tank.drain(energy * processMod / 10, true);
			}
			if (canFinish()) {
				processFinish();
				transferOutput();
				transferInput();
				energyStorage.modifyEnergyStored(-processRem * energyMod / processMod);

				if (!redstoneControlOrDisable() || !canStart()) {
					isActive = false;
					wasActive = true;
					tracker.markTime(worldObj);
				} else {
					processStart();
				}
			}
		} else if (redstoneControlOrDisable()) {
			if (timeCheck()) {
				transferOutput();
				transferInput();
			}
			if (timeCheckEighth() && canStart()) {
				processStart();
				int energy = calcEnergy();
				energyStorage.modifyEnergyStored(-energy * energyMod);
				processRem -= energy * processMod;
				tank.drain(energy * processMod / 10, true);
				isActive = true;
			}
		}
		updateIfChanged(curActive);
		chargeEnergy();
	}

	@Override
	public int getMaxInputSlot() {

		return 1;
	}

	@Override
	protected boolean canStart() {

		if (inventory[0] == null && inventory[1] == null) {
			return false;
		}
		RecipeInsolator recipe = InsolatorManager.getRecipe(inventory[0], inventory[1]);

		if (recipe == null || energyStorage.getEnergyStored() < recipe.getEnergy() * energyMod / processMod || tank.getFluidAmount() < recipe.getEnergy() / 10) {
			return false;
		}
		if (InsolatorManager.isRecipeReversed(inventory[0], inventory[1])) {
			if (recipe.getPrimaryInput().stackSize > inventory[1].stackSize || recipe.getSecondaryInput().stackSize > inventory[0].stackSize) {
				return false;
			}
		} else {
			if (recipe.getPrimaryInput().stackSize > inventory[0].stackSize || recipe.getSecondaryInput().stackSize > inventory[1].stackSize) {
				return false;
			}
		}
		ItemStack primaryItem = recipe.getPrimaryOutput();
		ItemStack secondaryItem = recipe.getSecondaryOutput();

		if (!augmentSecondaryNull && secondaryItem != null && inventory[3] != null) {
			if (!inventory[3].isItemEqual(secondaryItem)) {
				return false;
			}
			if (inventory[3].stackSize + secondaryItem.stackSize > secondaryItem.getMaxStackSize()) {
				return false;
			}
		}
		if (inventory[2] == null) {
			return true;
		}
		if (!inventory[2].isItemEqual(primaryItem)) {
			return false;
		}
		return inventory[2].stackSize + primaryItem.stackSize <= primaryItem.getMaxStackSize();
	}

	@Override
	protected boolean hasValidInput() {

		RecipeInsolator recipe = InsolatorManager.getRecipe(inventory[0], inventory[1]);

		if (recipe == null) {
			return false;
		}
		if (InsolatorManager.isRecipeReversed(inventory[0], inventory[1])) {
			if (recipe.getPrimaryInput().stackSize > inventory[1].stackSize || recipe.getSecondaryInput().stackSize > inventory[0].stackSize) {
				return false;
			}
		} else {
			if (recipe.getPrimaryInput().stackSize > inventory[0].stackSize || recipe.getSecondaryInput().stackSize > inventory[1].stackSize) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void processStart() {

		processMax = InsolatorManager.getRecipe(inventory[0], inventory[1]).getEnergy();
		processRem = processMax;
	}

	@Override
	protected void processFinish() {

		RecipeInsolator recipe = InsolatorManager.getRecipe(inventory[0], inventory[1]);

		if (recipe == null) {
			isActive = false;
			wasActive = true;
			tracker.markTime(worldObj);
			processRem = 0;
			return;
		}
		ItemStack primaryItem = ItemHelper.cloneStack(recipe.getPrimaryOutput());
		ItemStack secondaryItem = ItemHelper.cloneStack(recipe.getSecondaryOutput());
		if (inventory[2] == null) {
			inventory[2] = primaryItem;
		} else {
			inventory[2].stackSize += primaryItem.stackSize;
		}
		if (secondaryItem != null) {
			int recipeChance = recipe.getSecondaryOutputChance();
			if (recipeChance >= 100 || worldObj.rand.nextInt(secondaryChance) < recipeChance) {
				if (inventory[3] == null) {
					inventory[3] = secondaryItem;

					if (secondaryChance < recipeChance && worldObj.rand.nextInt(secondaryChance) < recipeChance - secondaryChance) {
						inventory[3].stackSize += secondaryItem.stackSize;
					}
				} else if (inventory[3].isItemEqual(secondaryItem)) {
					inventory[3].stackSize += secondaryItem.stackSize;

					if (secondaryChance < recipeChance && worldObj.rand.nextInt(secondaryChance) < recipeChance - secondaryChance) {
						inventory[3].stackSize += secondaryItem.stackSize;
					}
				}
				if (inventory[3].stackSize > inventory[3].getMaxStackSize()) {
					inventory[3].stackSize = inventory[3].getMaxStackSize();
				}
			}
		}
		if (InsolatorManager.isRecipeReversed(inventory[0], inventory[1])) {
			inventory[1].stackSize -= recipe.getPrimaryInput().stackSize;
			inventory[0].stackSize -= recipe.getSecondaryInput().stackSize;
		} else {
			inventory[0].stackSize -= recipe.getPrimaryInput().stackSize;
			inventory[1].stackSize -= recipe.getSecondaryInput().stackSize;
		}
		if (inventory[0].stackSize <= 0) {
			inventory[0] = null;
		}
		if (inventory[1].stackSize <= 0) {
			inventory[1] = null;
		}
	}

	@Override
	protected void transferInput() {

		if (!augmentAutoInput) {
			return;
		}
		int side;
		for (int i = inputTrackerPrimary + 1; i <= inputTrackerPrimary + 6; i++) {
			side = i % 6;
			if (sideCache[side] == 1 || sideCache[side] == 5) {
				if (extractItem(0, AUTO_TRANSFER[level], EnumFacing.VALUES[side])) {
					inputTrackerPrimary = side;
					break;
				}
			}
		}
		for (int i = inputTrackerPrimary + 1; i <= inputTrackerPrimary + 6; i++) {
			side = i % 6;
			if (sideCache[side] == 1 || sideCache[side] == 6) {
				if (extractItem(1, AUTO_TRANSFER[level], EnumFacing.VALUES[side])) {
					inputTrackerSecondary = side;
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
		int side;
		if (inventory[2] != null) {
			for (int i = outputTrackerPrimary + 1; i <= outputTrackerPrimary + 6; i++) {
				side = i % 6;

				if (sideCache[side] == 2 || sideCache[side] == 4) {
					if (transferItem(2, AUTO_TRANSFER[level], EnumFacing.VALUES[side])) {
						outputTrackerPrimary = side;
						break;
					}
				}
			}
		}
		if (inventory[3] == null) {
			return;
		}
		for (int i = outputTrackerSecondary + 1; i <= outputTrackerSecondary + 6; i++) {
			side = i % 6;

			if (sideCache[side] == 3 || sideCache[side] == 4) {
				if (transferItem(3, AUTO_TRANSFER[level], EnumFacing.VALUES[side])) {
					outputTrackerSecondary = side;
					break;
				}
			}
		}
	}

	@Override
	protected void onLevelChange() {

		super.onLevelChange();

		tank.setCapacity(TEProps.MAX_FLUID_LARGE * FLUID_CAPACITY[level]);
	}

	@Override
	protected boolean readPortableTagInternal(EntityPlayer player, NBTTagCompound tag) {

		if (!super.readPortableTagInternal(player, tag)) {
			return false;
		}
		lockPrimary = tag.getBoolean("SlotLock");
		return true;
	}

	@Override
	protected boolean writePortableTagInternal(EntityPlayer player, NBTTagCompound tag) {

		if (!super.writePortableTagInternal(player, tag)) {
			return false;
		}
		tag.setBoolean("SlotLock", lockPrimary);
		return true;
	}

	/* GUI METHODS */
	@Override
	public Object getGuiClient(InventoryPlayer inventory) {

		return new GuiInsolator(inventory, this);
	}

	@Override
	public Object getGuiServer(InventoryPlayer inventory) {

		return new ContainerInsolator(inventory, this);
	}

	@Override
	public void receiveGuiNetworkData(int i, int j) {

	}

	@Override
	public void sendGuiNetworkData(Container container, IContainerListener player) {

		super.sendGuiNetworkData(container, player);

		player.sendProgressBarUpdate(container, 0, tank.getFluidAmount());
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

		inputTrackerPrimary = nbt.getInteger("TrackIn1");
		inputTrackerSecondary = nbt.getInteger("TrackIn2");
		outputTrackerPrimary = nbt.getInteger("Tracker1");
		outputTrackerSecondary = nbt.getInteger("Tracker2");
		lockPrimary = nbt.getBoolean("SlotLock");
		tank.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		nbt.setInteger("TrackIn1", inputTrackerPrimary);
		nbt.setInteger("TrackIn2", inputTrackerSecondary);
		nbt.setInteger("Tracker1", outputTrackerPrimary);
		nbt.setInteger("Tracker2", outputTrackerSecondary);
		nbt.setBoolean("SlotLock", lockPrimary);
		tank.writeToNBT(nbt);

		return nbt;
	}

	/* NETWORK METHODS */
	@Override
	public PacketCoFHBase getGuiPacket() {

		PacketCoFHBase payload = super.getGuiPacket();

		payload.addBool(lockPrimary);
		payload.addInt(tank.getFluidAmount());

		return payload;
	}

	@Override
	public PacketCoFHBase getModePacket() {

		PacketCoFHBase payload = super.getModePacket();

		payload.addBool(lockPrimary);

		return payload;
	}

	@Override
	protected void handleGuiPacket(PacketCoFHBase payload) {

		super.handleGuiPacket(payload);

		lockPrimary = payload.getBool();
		tank.getFluid().amount = payload.getInt();
	}

	@Override
	protected void handleModePacket(PacketCoFHBase payload) {

		super.handleModePacket(payload);

		lockPrimary = payload.getBool();
		markDirty();
		callNeighborTileChange();
	}

	public void setMode(boolean mode) {

		boolean lastMode = lockPrimary;
		lockPrimary = mode;
		sendModePacket();
		lockPrimary = lastMode;
	}

	/* IInventory */
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {

		if (lockPrimary) {
			if (slot == 0) {
				return InsolatorManager.isItemFertilizer(stack);
			}
			if (slot == 1) {
				return !InsolatorManager.isItemFertilizer(stack) && InsolatorManager.isItemValid(stack);
			}
		}
		return slot <= 1 ? InsolatorManager.isItemValid(stack) : true;
	}

	/* IFluidHandler */
	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill) {

		return tank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {

		return null;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {

		return null;
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid) {

		return true;
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid) {

		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from) {

		return new FluidTankInfo[] { tank.getInfo() };
	}

}
