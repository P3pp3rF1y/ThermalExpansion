package cofh.thermalexpansion.block.machine;

import cofh.core.network.PacketCoFHBase;
import cofh.core.util.CoreUtils;
import cofh.core.util.fluid.FluidTankAdv;
import cofh.lib.util.helpers.FluidHelper;
import cofh.lib.util.helpers.MathHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.core.TEProps;
import cofh.thermalexpansion.gui.client.machine.GuiCrucible;
import cofh.thermalexpansion.gui.container.machine.ContainerCrucible;
import cofh.thermalexpansion.util.crafting.CrucibleManager;
import cofh.thermalexpansion.util.crafting.CrucibleManager.RecipeCrucible;

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

public class TileCrucible extends TileMachineBase implements IFluidHandler, IFluidFace {

	public static void initialize() {

		int type = BlockMachine.Type.CRUCIBLE.ordinal();

		DEFAULT_SIDE_CONFIG[type] = new SideConfig();
		DEFAULT_SIDE_CONFIG[type].numConfig = 4;
		DEFAULT_SIDE_CONFIG[type].slotGroups = new int[][] { {}, { 0 }, {}, { 0 } };
		DEFAULT_SIDE_CONFIG[type].allowInsertionSide = new boolean[] { false, true, false, true };
		DEFAULT_SIDE_CONFIG[type].allowExtractionSide = new boolean[] { false, true, false, true };
		DEFAULT_SIDE_CONFIG[type].allowInsertionSlot = new boolean[] { true, false };
		DEFAULT_SIDE_CONFIG[type].allowExtractionSlot = new boolean[] { true, false };
		DEFAULT_SIDE_CONFIG[type].sideTex = new int[] { 0, 1, 4, 7 };
		DEFAULT_SIDE_CONFIG[type].defaultSides = new byte[] { 1, 1, 2, 2, 2, 2 };

		String category = "Machine.Crucible";
		int basePower = MathHelper.clamp(ThermalExpansion.CONFIG.get(category, "BasePower", 400), 10, 500);
		ThermalExpansion.CONFIG.set(category, "BasePower", basePower);
		DEFAULT_ENERGY_CONFIG[type] = new EnergyConfig();
		DEFAULT_ENERGY_CONFIG[type].setParams(basePower / 10, basePower, Math.max(400000, basePower * 1000));

		SOUNDS[type] = CoreUtils.getSoundEvent(ThermalExpansion.modId, "blockMachineCrucible");

		GameRegistry.register(SOUNDS[type].setRegistryName(new ResourceLocation(ThermalExpansion.modId, "blockMachineCrucible")));
		GameRegistry.registerTileEntity(TileCrucible.class, "thermalexpansion.machineCrucible");
	}

	int inputTracker;
	int outputTrackerFluid;

	FluidTankAdv tank = new FluidTankAdv(TEProps.MAX_FLUID_LARGE);
	FluidStack outputBuffer;
	FluidStack renderFluid = new FluidStack(FluidRegistry.LAVA, 0);

	public TileCrucible() {

		super(BlockMachine.Type.CRUCIBLE);
		inventory = new ItemStack[1 + 1];
	}

	@Override
	protected boolean canStart() {

		if (inventory[0] == null) {
			return false;
		}
		RecipeCrucible recipe = CrucibleManager.getRecipe(inventory[0]);

		if (recipe == null || energyStorage.getEnergyStored() < recipe.getEnergy() * energyMod / processMod) {
			return false;
		}
		if (inventory[0].stackSize < recipe.getInput().stackSize) {
			return false;
		}
		FluidStack output = recipe.getOutput();
		return tank.fill(output, false) == output.amount;
	}

	@Override
	protected boolean hasValidInput() {

		RecipeCrucible recipe = CrucibleManager.getRecipe(inventory[0]);
		return recipe == null ? false : recipe.getInput().stackSize <= inventory[0].stackSize;
	}

	@Override
	protected void processStart() {

		processMax = CrucibleManager.getRecipe(inventory[0]).getEnergy();
		processRem = processMax;

		FluidStack prevFluid = renderFluid.copy();
		renderFluid = CrucibleManager.getRecipe(inventory[0]).getOutput().copy();
		renderFluid.amount = 0;

		if (!prevFluid.equals(renderFluid)) {
			sendFluidPacket();
		}
	}

	@Override
	protected void processFinish() {

		RecipeCrucible recipe = CrucibleManager.getRecipe(inventory[0]);

		if (recipe == null) {
			isActive = false;
			wasActive = true;
			tracker.markTime(worldObj);
			processRem = 0;
			return;
		}
		tank.fill(recipe.getOutput(), true);
		inventory[0].stackSize--;

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

			if (sideCache[side] == 2) {
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
	public void update() {

		if (ServerHelper.isClientWorld(worldObj)) {
			return;
		}
		transferOutputFluid();

		super.update();
	}

	@Override
	protected void onLevelChange() {

		super.onLevelChange();

		tank.setCapacity(TEProps.MAX_FLUID_LARGE * FLUID_CAPACITY[level]);
	}

	/* GUI METHODS */
	@Override
	public Object getGuiClient(InventoryPlayer inventory) {

		return new GuiCrucible(inventory, this);
	}

	@Override
	public Object getGuiServer(InventoryPlayer inventory) {

		return new ContainerCrucible(inventory, this);
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
		outputTrackerFluid = nbt.getInteger("TrackOut");
		tank.readFromNBT(nbt);

		if (tank.getFluid() != null) {
			renderFluid = tank.getFluid();
		} else if (CrucibleManager.getRecipe(inventory[0]) != null) {
			renderFluid = CrucibleManager.getRecipe(inventory[0]).getOutput().copy();
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		nbt.setInteger("TrackIn", inputTracker);
		nbt.setInteger("TrackOut", outputTrackerFluid);
		tank.writeToNBT(nbt);

		return nbt;
	}

	/* NETWORK METHODS */
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {

		SPacketUpdateTileEntity packet = super.getUpdatePacket();
		NBTTagCompound nbt = packet.getNbtCompound();

		nbt.setTag("renderFluid", renderFluid.writeToNBT(new NBTTagCompound()));

		return packet;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {

		super.onDataPacket(net, pkt);

		if (net.getDirection() == EnumPacketDirection.CLIENTBOUND) {
			NBTTagCompound nbt = pkt.getNbtCompound();

			renderFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("renderFluid"));
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
	protected void handleGuiPacket(PacketCoFHBase payload) {

		super.handleGuiPacket(payload);
		tank.setFluid(payload.getFluidStack());
	}

	@Override
	protected void handleFluidPacket(PacketCoFHBase payload) {

		super.handleFluidPacket(payload);
		renderFluid = payload.getFluidStack();
		IBlockState state = worldObj.getBlockState(pos);
		worldObj.notifyBlockUpdate(pos, state, state, 3);
	}

	/* IInventory */
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {

		return slot == 0 ? CrucibleManager.recipeExists(stack) : true;
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

	/* IFluidHandler */
	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill) {

		return 0;
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {

		if (from != null && sideCache[from.ordinal()] != 2) {
			return null;
		}
		if (resource == null || !resource.isFluidEqual(tank.getFluid())) {
			return null;
		}
		return tank.drain(resource.amount, doDrain);
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {

		if (from != null && sideCache[from.ordinal()] != 2) {
			return null;
		}
		return tank.drain(maxDrain, doDrain);
	}

	@Override
	public boolean canFill(EnumFacing from, Fluid fluid) {

		return false;
	}

	@Override
	public boolean canDrain(EnumFacing from, Fluid fluid) {

		// return sideCache[from.ordinal()] == 2;
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from) {

		// if (sideCache[from.ordinal()] != 2) {
		// return CoFHProps.EMPTY_TANK_INFO;
		// }
		return new FluidTankInfo[] { tank.getInfo() };
	}

	@Override
	public String getFluidTextureName() {
		return renderFluid != null ? renderFluid.getFluid().getStill().toString() : "";
	}
}
