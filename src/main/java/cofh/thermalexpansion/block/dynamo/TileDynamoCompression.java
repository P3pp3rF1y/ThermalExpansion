package cofh.thermalexpansion.block.dynamo;

import cofh.core.network.PacketCoFHBase;
import cofh.core.util.fluid.FluidTankAdv;
import cofh.thermalexpansion.gui.client.dynamo.GuiDynamoCompression;
import cofh.thermalexpansion.gui.container.ContainerTEBase;

import gnu.trove.map.hash.TObjectIntHashMap;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TileDynamoCompression extends TileDynamoBase implements IFluidHandler {

	public static void initialize() {

		GameRegistry.registerTileEntity(TileDynamoCompression.class, "thermalexpansion.dynamoCompression");
	}

	FluidTankAdv fuelTank = new FluidTankAdv(MAX_FLUID);
	FluidTankAdv coolantTank = new FluidTankAdv(MAX_FLUID);

	FluidStack renderFluid = new FluidStack(FluidRegistry.LAVA, FluidContainerRegistry.BUCKET_VOLUME);
	int coolantRF;

	@Override
	protected boolean canGenerate() {

		if (fuelRF > 0) {
			return coolantRF > 0 || coolantTank.getFluidAmount() >= 50;
		}
		if (coolantRF > 0) {
			return fuelTank.getFluidAmount() >= 50;
		}
		return fuelTank.getFluidAmount() >= 50 && coolantTank.getFluidAmount() >= 50;
	}

	@Override
	protected void generate() {

		if (fuelRF <= 0) {
			fuelRF = getFuelEnergy(fuelTank.getFluid()) * fuelMod / FUEL_MOD;
			fuelTank.drain(50, true);
		}
		if (coolantRF <= 0) {
			coolantRF = getCoolantEnergy(coolantTank.getFluid()) * fuelMod / FUEL_MOD;
			coolantTank.drain(50, true);
		}
		int energy = calcEnergy() * energyMod;
		energyStorage.modifyEnergyStored(energy);
		fuelRF -= energy;
		coolantRF -= energy;
	}

	/* GUI METHODS */
	@Override
	public Object getGuiClient(InventoryPlayer inventory) {

		return new GuiDynamoCompression(inventory, this);
	}

	@Override
	public Object getGuiServer(InventoryPlayer inventory) {

		return new ContainerTEBase(inventory, this);
	}

	@Override
	public FluidTankAdv getTank(int tankIndex) {

		if (tankIndex == 0) {
			return fuelTank;
		}
		return coolantTank;
	}

	/* NBT METHODS */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);
		coolantRF = nbt.getInteger("Coolant");
		fuelTank.readFromNBT(nbt.getCompoundTag("FuelTank"));
		coolantTank.readFromNBT(nbt.getCompoundTag("CoolantTank"));

		if (!isValidFuel(fuelTank.getFluid())) {
			fuelTank.setFluid(null);
		}
		if (!isValidCoolant(coolantTank.getFluid())) {
			coolantTank.setFluid(null);
		}
		if (fuelTank.getFluid() != null) {
			renderFluid = fuelTank.getFluid();
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);
		nbt.setInteger("Coolant", coolantRF);
		nbt.setTag("FuelTank", fuelTank.writeToNBT(new NBTTagCompound()));
		nbt.setTag("CoolantTank", coolantTank.writeToNBT(new NBTTagCompound()));

		return nbt;
	}

	/* NETWORK METHODS */
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {

		SPacketUpdateTileEntity packet = super.getUpdatePacket();
		NBTTagCompound nbt = packet.getNbtCompound();

		nbt.setTag("fuelTank", fuelTank.getFluid().writeToNBT(new NBTTagCompound()));

		return packet;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {

		super.onDataPacket(net, pkt);

		NBTTagCompound nbt = pkt.getNbtCompound();

		renderFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("fuelTank"));
		if (renderFluid == null) {
			renderFluid = new FluidStack(FluidRegistry.LAVA, FluidContainerRegistry.BUCKET_VOLUME);
		}
	}

	@Override
	public PacketCoFHBase getPacket() {

		PacketCoFHBase payload = super.getPacket();

		payload.addFluidStack(fuelTank.getFluid());

		return payload;
	}

	@Override
	public PacketCoFHBase getGuiPacket() {

		PacketCoFHBase payload = super.getGuiPacket();

		payload.addFluidStack(fuelTank.getFluid());
		payload.addFluidStack(coolantTank.getFluid());

		return payload;
	}

	@Override
	protected void handleGuiPacket(PacketCoFHBase payload) {

		super.handleGuiPacket(payload);

		fuelTank.setFluid(payload.getFluidStack());
		coolantTank.setFluid(payload.getFluidStack());
	}

	/* ITilePacketHandler */
	@Override
	public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {

		super.handleTilePacket(payload, isServer);

		renderFluid = payload.getFluidStack();
		if (renderFluid == null) {
			renderFluid = new FluidStack(FluidRegistry.LAVA, FluidContainerRegistry.BUCKET_VOLUME);
		}
	}

	/* IFluidHandler */
	@Override
	public int fill(EnumFacing from, FluidStack resource, boolean doFill) {

		if (resource == null || from != null && from.ordinal() == facing && !augmentCoilDuct) {
			return 0;
		}
		if (isValidFuel(resource)) {
			return fuelTank.fill(resource, doFill);
		}
		if (isValidCoolant(resource)) {
			return coolantTank.fill(resource, doFill);
		}
		return 0;
	}

	@Override
	public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {

		if (resource == null || !augmentCoilDuct && from != null && from.ordinal() == facing) {
			return null;
		}
		if (isValidFuel(resource)) {
			return fuelTank.drain(resource.amount, doDrain);
		}
		if (isValidCoolant(resource)) {
			return coolantTank.drain(resource.amount, doDrain);
		}
		return null;
	}

	@Override
	public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {

		if (!augmentCoilDuct && from != null && from.ordinal() == facing) {
			return null;
		}
		if (fuelTank.getFluidAmount() <= 0) {
			return coolantTank.drain(maxDrain, doDrain);
		}
		return fuelTank.drain(maxDrain, doDrain);
	}

	@Override
	public FluidTankInfo[] getTankInfo(EnumFacing from) {

		return new FluidTankInfo[] { fuelTank.getInfo(), coolantTank.getInfo() };
	}

	/* FUEL MANAGER */
	static TObjectIntHashMap<Fluid> fuels = new TObjectIntHashMap<Fluid>();
	static TObjectIntHashMap<Fluid> coolants = new TObjectIntHashMap<Fluid>();

	public static boolean isValidFuel(FluidStack stack) {

		return stack == null ? false : fuels.containsKey(stack.getFluid());
	}

	public static boolean isValidCoolant(FluidStack stack) {

		return stack == null ? false : coolants.containsKey(stack.getFluid());
	}

	public static boolean addFuel(Fluid fluid, int energy) {

		if (fluid == null || energy < 10000 || energy > 200000000) {
			return false;
		}
		fuels.put(fluid, energy / 20);
		return true;
	}

	public static boolean addCoolant(Fluid fluid, int cooling) {

		if (fluid == null || cooling < 10000 || cooling > 200000000) {
			return false;
		}
		coolants.put(fluid, cooling / 20);
		return true;
	}

	public static boolean removeFuel(Fluid fluid) {

		fuels.remove(fluid);
		return true;
	}

	public static boolean removeCoolant(Fluid fluid) {

		coolants.remove(fluid);
		return true;
	}

	public static int getFuelEnergy(FluidStack stack) {

		return stack == null ? 0 : fuels.get(stack.getFluid());
	}

	public static int getCoolantEnergy(FluidStack stack) {

		return stack == null ? 0 : coolants.get(stack.getFluid());
	}

}
