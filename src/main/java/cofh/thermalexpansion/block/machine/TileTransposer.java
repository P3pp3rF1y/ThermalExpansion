package cofh.thermalexpansion.block.machine;

import cofh.core.network.PacketCoFHBase;
import cofh.core.util.CoreUtils;
import cofh.core.util.fluid.FluidTankAdv;
import cofh.lib.util.helpers.FluidHelper;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.MathHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.core.TEProps;
import cofh.thermalexpansion.gui.client.machine.GuiTransposer;
import cofh.thermalexpansion.gui.container.machine.ContainerTransposer;
import cofh.thermalexpansion.util.crafting.TransposerManager;
import cofh.thermalexpansion.util.crafting.TransposerManager.RecipeTransposer;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TileTransposer extends TileMachineBase implements IFluidHandler {

	public static void initialize() {

		int type = BlockMachine.Type.TRANSPOSER.ordinal();

		DEFAULT_SIDE_CONFIG[type] = new SideConfig();
		DEFAULT_SIDE_CONFIG[type].numConfig = 6;
		DEFAULT_SIDE_CONFIG[type].slotGroups = new int[][] { {}, { 0 }, { 2 }, {}, { 2 }, { 0, 2 } };
		DEFAULT_SIDE_CONFIG[type].allowInsertionSide = new boolean[] { false, true, false, false, false, true };
		DEFAULT_SIDE_CONFIG[type].allowExtractionSide = new boolean[] { false, true, true, false, true, true };
		DEFAULT_SIDE_CONFIG[type].allowInsertionSlot = new boolean[] { true, false, false, false };
		DEFAULT_SIDE_CONFIG[type].allowExtractionSlot = new boolean[] { true, false, true, false };
		DEFAULT_SIDE_CONFIG[type].sideTex = new int[] { 0, 1, 2, 3, 4, 7 };
		DEFAULT_SIDE_CONFIG[type].defaultSides = new byte[] { 3, 1, 2, 2, 2, 2 };

		String category = "Machine.Transposer";
		int basePower = MathHelper.clamp(ThermalExpansion.CONFIG.get(category, "BasePower", 40), 10, 500);
		ThermalExpansion.CONFIG.set(category, "BasePower", basePower);
		DEFAULT_ENERGY_CONFIG[type] = new EnergyConfig();
		DEFAULT_ENERGY_CONFIG[type].setParamsPower(basePower);

		SOUNDS[type] = CoreUtils.getSoundEvent(ThermalExpansion.modId, "blockMachineTransposer");

		GameRegistry.register(SOUNDS[type].setRegistryName(new ResourceLocation(ThermalExpansion.modId, "blockMachineTransposer")));
		GameRegistry.registerTileEntity(TileTransposer.class, "thermalexpansion.machineTransposer");
	}

	int inputTracker;
	int outputTracker;
	int outputTrackerFluid;

	FluidStack outputBuffer;
	FluidStack renderFluid = new FluidStack(FluidRegistry.WATER, 0);
	FluidTankAdv tank = new FluidTankAdv(TEProps.MAX_FLUID_LARGE);
	IFluidContainerItem containerItem = null;

	public boolean reverseFlag;
	public boolean reverse;

	public TileTransposer() {

		super(BlockMachine.Type.TRANSPOSER);
		inventory = new ItemStack[1 + 1 + 1 + 1];
	}

	@Override
	public void update() {

		if (ServerHelper.isClientWorld(worldObj)) {
			if (inventory[1] == null) {
				processRem = 0;
				containerItem = null;
			} else if (FluidHelper.isFluidContainerItem(inventory[1])) {
				containerItem = (IFluidContainerItem) inventory[1].getItem();
			}
			return;
		}
		if (containerItem == null) {
			if (FluidHelper.isFluidContainerItem(inventory[1])) {
				updateContainerItem();
			}
		}
		if (reverse) {
			transferOutputFluid();
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
	public int getLightValue() {

		return super.getLightValue() != 0 ? renderFluid.getFluid().getLuminosity(renderFluid) : 0;
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
		if (FluidHelper.isFluidContainerItem(inventory[0])) {
			inventory[1] = ItemHelper.cloneStack(inventory[0], 1);
			inventory[0].stackSize--;

			if (inventory[0].stackSize <= 0) {
				inventory[0] = null;
			}
			return false;
		}
		if (!reverse) {
			if (tank.getFluidAmount() <= 0) {
				return false;
			}
			RecipeTransposer recipe = TransposerManager.getFillRecipe(inventory[0], tank.getFluid());

			if (recipe == null || tank.getFluidAmount() < recipe.getFluid().amount || energyStorage.getEnergyStored() < recipe.getEnergy()) {
				return false;
			}
			if (inventory[0].stackSize < recipe.getInput().stackSize) {
				return false;
			}
			if (inventory[2] == null) {
				return true;
			}
			ItemStack output = recipe.getOutput();

			if (!inventory[2].isItemEqual(output)) {
				return false;
			}
			int result = inventory[2].stackSize + output.stackSize;
			return result <= output.getMaxStackSize();
		} else {
			RecipeTransposer recipe = TransposerManager.getExtractionRecipe(inventory[0]);

			if (recipe == null || energyStorage.getEnergyStored() < recipe.getEnergy()) {
				return false;
			}
			if (inventory[0].stackSize < recipe.getInput().stackSize) {
				return false;
			}
			if (tank.fill(recipe.getFluid(), false) != recipe.getFluid().amount) {
				return false;
			}
			if (inventory[2] == null) {
				return true;
			}
			ItemStack output = recipe.getOutput();

			if (output == null) {
				return true;
			}
			if (!inventory[2].isItemEqual(output)) {
				return false;
			}
			return inventory[2].stackSize + output.stackSize <= output.getMaxStackSize();
		}
	}

	@Override
	protected boolean hasValidInput() {

		if (containerItem != null) {
			return true;
		}
		RecipeTransposer recipe;

		if (!reverse) {
			recipe = TransposerManager.getFillRecipe(inventory[1], tank.getFluid());
		} else {
			recipe = TransposerManager.getExtractionRecipe(inventory[1]);
		}
		if (recipe == null) {
			return false;
		}
		return recipe.getInput().stackSize <= inventory[1].stackSize;
	}

	@Override
	protected void processStart() {

		FluidStack prevFluid = renderFluid.copy();
		RecipeTransposer recipe;

		if (!reverse) {
			recipe = TransposerManager.getFillRecipe(inventory[0], tank.getFluid());
			processMax = recipe.getEnergy();
			renderFluid = tank.getFluid().copy();
		} else {
			recipe = TransposerManager.getExtractionRecipe(inventory[0]);
			processMax = recipe.getEnergy();
			renderFluid = recipe.getFluid().copy();
		}
		renderFluid.amount = 0;
		processRem = processMax;

		inventory[1] = ItemHelper.cloneStack(inventory[0], recipe.getInput().stackSize);
		inventory[0].stackSize -= recipe.getInput().stackSize;

		if (inventory[0].stackSize <= 0) {
			inventory[0] = null;
		}
		if (!prevFluid.equals(renderFluid)) {
			sendFluidPacket();
		}
	}

	@Override
	protected void processFinish() {

		if (!reverse) {
			RecipeTransposer recipe = TransposerManager.getFillRecipe(inventory[1], tank.getFluid());

			if (recipe == null) {
				isActive = false;
				wasActive = true;
				tracker.markTime(worldObj);
				processRem = 0;
				return;
			}
			ItemStack output = ItemHelper.cloneStack(recipe.getOutput());
			if (inventory[2] == null) {
				inventory[2] = output;
			} else {
				inventory[2].stackSize += output.stackSize;
			}
			inventory[1] = null;
			tank.drain(recipe.getFluid().amount, true);
		} else {
			RecipeTransposer recipe = TransposerManager.getExtractionRecipe(inventory[1]);

			if (recipe == null) {
				isActive = false;
				wasActive = true;
				tracker.markTime(worldObj);
				processRem = 0;
				return;
			}
			ItemStack output = recipe.getOutput();
			int recipeChance = recipe.getChance();
			if (recipeChance >= 100 || worldObj.rand.nextInt(secondaryChance) < recipeChance) {
				if (inventory[2] == null) {
					inventory[2] = output;
				} else {
					inventory[2].stackSize += output.stackSize;
				}
			}
			inventory[1] = null;
			tank.fill(recipe.getFluid(), true);
		}
		reverse = reverseFlag;
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
		if (containerItem == null && FluidHelper.isFluidContainerItem(inventory[0])) {
			inventory[1] = ItemHelper.cloneStack(inventory[0], 1);
			inventory[0].stackSize--;

			if (inventory[0].stackSize <= 0) {
				inventory[0] = null;
			}
		}
		int side;
		for (int i = outputTracker + 1; i <= outputTracker + 6; i++) {
			side = i % 6;

			if (sideCache[side] == 2 || sideCache[side] == 4) {
				if (transferItem(2, AUTO_TRANSFER[level], EnumFacing.VALUES[side])) {
					outputTracker = side;
					break;
				}
			}
		}
	}

	protected void transferOutputFluid() {

		if (!augmentAutoOutput) {
			return;
		}
		if (tank.getFluidAmount() <= 0) {
			return;
		}
		int side;
		outputBuffer = new FluidStack(tank.getFluid(), Math.min(tank.getFluidAmount(), RATE));
		for (int i = outputTrackerFluid + 1; i <= outputTrackerFluid + 6; i++) {
			side = i % 6;

			if (sideCache[side] == 3 || sideCache[side] == 4) {
				int toDrain = FluidHelper.fillAdjacentFluidHandler(this, EnumFacing.VALUES[side], outputBuffer, true);

				if (toDrain > 0) {
					tank.drain(toDrain, true);
					outputTrackerFluid = side;
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

	protected void processContainerItem() {

		if (isActive) {
			int energy = Math.min(energyStorage.getEnergyStored(), calcEnergy() * energyMod);

			if (!reverse) {
				updateContainerFill(energy * processMod);
			} else {
				updateContainerEmpty(energy * processMod);
			}
			if (!redstoneControlOrDisable()) {
				isActive = false;
				wasActive = true;
				tracker.markTime(worldObj);
			} else {
				if (containerItem == null) {
					if (FluidHelper.isFluidContainerItem(inventory[1])) {
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
				if (FluidHelper.isFluidContainerItem(inventory[1])) {
					updateContainerItem();
				}
			}
			if (containerItem != null) {
				isActive = true;
			}
		}
	}

	protected void updateContainerItem() {

		containerItem = (IFluidContainerItem) inventory[1].getItem();
		FluidStack containerStack = FluidHelper.getFluidStackFromContainerItem(inventory[1]);
		FluidStack prevFluid = renderFluid.copy();

		if (!reverse) {
			renderFluid = tank.getFluid() == null ? null : tank.getFluid().copy();
		} else {
			renderFluid = tank.getFluid() == null ? containerStack == null ? null : containerStack.copy() : tank.getFluid().copy();
		}
		if (renderFluid == null) {
			renderFluid = new FluidStack(FluidRegistry.WATER, 0);
		} else {
			renderFluid.amount = 0;
		}
		if (!reverse) {
			processMax = containerItem.getCapacity(inventory[1])
					- (containerItem.getFluid(inventory[1]) == null ? 0 : containerItem.getFluid(inventory[1]).amount);
			processRem = processMax;
		} else {
			processMax = containerItem.getFluid(inventory[1]) == null ? 0 : containerItem.getFluid(inventory[1]).amount;
			processRem = processMax;
		}
		if (!prevFluid.equals(renderFluid)) {
			sendFluidPacket();
		}
	}

	protected void updateContainerFill(int energy) {

		if (energy <= 0) {
			return;
		}
		int amount = Math.min(tank.getFluidAmount(), energy);
		int filled = tank.getFluid() == null ? 0 : containerItem.fill(inventory[1], new FluidStack(tank.getFluid(), amount), true);

		if (containerItem.getFluid(inventory[1]) != null) {
			processRem -= filled;
			tank.drain(filled, true);
			energyStorage.modifyEnergyStored(-filled);

			if (containerItem.getFluid(inventory[1]).amount >= containerItem.getCapacity(inventory[1])) {
				transferOutput();
				reverse = reverseFlag;

				if (!redstoneControlOrDisable()) {
					isActive = false;
					wasActive = true;
					tracker.markTime(worldObj);
				}
			}
		}
	}

	protected void updateContainerEmpty(int energy) {

		if (energy <= 0) {
			return;
		}
		int amount = Math.min(tank.getSpace(), energy);
		FluidStack drainStack = containerItem.drain(inventory[1], amount, true);
		int drained = drainStack == null ? 0 : drainStack.amount;
		processRem -= drained;
		tank.fill(drainStack, true);
		energyStorage.modifyEnergyStored(-drained);

		if (processRem <= 0) {
			transferOutput();
			reverse = reverseFlag;

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

		return new GuiTransposer(inventory, this);
	}

	@Override
	public Object getGuiServer(InventoryPlayer inventory) {

		return new ContainerTransposer(inventory, this);
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
		outputTracker = nbt.getInteger("TrackOut1");
		outputTrackerFluid = nbt.getInteger("TrackOut2");

		reverse = nbt.getBoolean("Rev");
		reverseFlag = reverse;
		tank.readFromNBT(nbt);

		if (tank.getFluid() != null) {
			renderFluid = tank.getFluid();
		} else if (TransposerManager.getExtractionRecipe(inventory[1]) != null) {
			renderFluid = TransposerManager.getExtractionRecipe(inventory[1]).getFluid().copy();
			renderFluid.amount = 0;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		nbt.setInteger("TrackIn", inputTracker);
		nbt.setInteger("TrackOut1", outputTracker);
		nbt.setInteger("TrackOut2", outputTrackerFluid);
		nbt.setBoolean("Rev", reverse);
		tank.writeToNBT(nbt);

		return nbt;
	}

	/* NETWORK METHODS */
	@Override
	public NBTTagCompound getUpdateTag() {

		NBTTagCompound nbt = super.getUpdateTag();

		nbt.setTag("renderFluid", renderFluid.writeToNBT(new NBTTagCompound()));

		return nbt;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {

		super.onDataPacket(net, pkt);

		if (net.getDirection() == EnumPacketDirection.CLIENTBOUND) {
			NBTTagCompound nbt = pkt.getNbtCompound();

			renderFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("fuelTank"));
			if (renderFluid == null) {
				renderFluid = new FluidStack(FluidRegistry.LAVA, FluidContainerRegistry.BUCKET_VOLUME);
			}
		}
	}

	@Override
	public PacketCoFHBase getPacket() {

		PacketCoFHBase payload = super.getPacket();

		payload.addFluidStack(renderFluid);
		return payload;
	}

	@Override
	public PacketCoFHBase getGuiPacket() {

		PacketCoFHBase payload = super.getGuiPacket();

		payload.addBool(reverse);
		payload.addBool(reverseFlag);

		if (tank.getFluid() == null) {
			payload.addFluidStack(renderFluid);
		} else {
			payload.addFluidStack(tank.getFluid());
		}
		return payload;
	}

	@Override
	public PacketCoFHBase getFluidPacket() {

		PacketCoFHBase payload = super.getFluidPacket();

		payload.addFluidStack(renderFluid);

		return payload;
	}

	@Override
	public PacketCoFHBase getModePacket() {

		PacketCoFHBase payload = super.getModePacket();

		payload.addBool(reverseFlag);

		return payload;
	}

	@Override
	protected void handleGuiPacket(PacketCoFHBase payload) {

		super.handleGuiPacket(payload);

		reverse = payload.getBool();
		reverseFlag = payload.getBool();
		tank.setFluid(payload.getFluidStack());
	}

	@Override
	protected void handleFluidPacket(PacketCoFHBase payload) {

		super.handleFluidPacket(payload);

		renderFluid = payload.getFluidStack();
		IBlockState state = worldObj.getBlockState(pos);
		worldObj.notifyBlockUpdate(pos, state, state, 3);
	}

	@Override
	protected void handleModePacket(PacketCoFHBase payload) {

		super.handleModePacket(payload);

		reverseFlag = payload.getBool();
		if (!isActive) {
			reverse = reverseFlag;
		}
		markDirty();
		callNeighborTileChange();
	}

	public void setMode(boolean mode) {

		boolean lastFlag = reverseFlag;
		reverseFlag = mode;
		sendModePacket();
		reverseFlag = lastFlag;
	}

	/* ITilePacketHandler */
	@Override
	public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {

		super.handleTilePacket(payload, isServer);

		if (!isServer) {
			renderFluid = payload.getFluidStack();
		} else {
			payload.getFluidStack();
		}
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
				reverse = reverseFlag;
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
			reverse = reverseFlag;
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
			reverse = reverseFlag;
		}
		super.markDirty();
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {

		return slot == 0 ? FluidHelper.isFluidContainerItem(stack) || TransposerManager.isItemValid(stack) : true;
	}

	/* IFluidHandler */
	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill) {

		if (reverse || from == null || sideCache[from.ordinal()] != 1) {
			return 0;
		}
		return tank.fill(resource, doFill);
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {

		if (!reverse || from == null || sideCache[from.ordinal()] != 3) {
			return null;
		}
		return tank.drain(resource, doDrain);
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {

		if (!reverse || from == null || sideCache[from.ordinal()] != 3) {
			return null;
		}
		return tank.drain(maxDrain, doDrain);
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid) {

		if (from == null) {
			return false;
		}
		return !reverse && sideCache[from.ordinal()] == 1;
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid) {

		if (from == null) {
			return false;
		}
		return reverse && sideCache[from.ordinal()] == 3;
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from) {

		// if (reverse) {
		// if (sideCache[from.ordinal()] != 3) {
		// return CoFHProps.EMPTY_TANK_INFO;
		// }
		// } else {
		// if (sideCache[from.ordinal()] != 1) {
		// return CoFHProps.EMPTY_TANK_INFO;
		// }
		// }
		return new FluidTankInfo[] { tank.getInfo() };
	}

}
