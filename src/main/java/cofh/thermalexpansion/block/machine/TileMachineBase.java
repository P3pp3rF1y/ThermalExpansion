package cofh.thermalexpansion.block.machine;

import cofh.api.energy.EnergyStorage;
import cofh.api.item.IAugmentItem;
import cofh.api.tileentity.ISidedTexture;
import cofh.core.network.PacketCoFHBase;
import cofh.lib.util.TimeTracker;
import cofh.lib.util.helpers.MathHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.block.BlockTEBase;
import cofh.thermalexpansion.block.TileAugmentable;
import cofh.thermalexpansion.core.TEProps;
import cofh.thermalexpansion.item.TEAugments;

import cofh.thermalexpansion.model.TextureLocations;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;

public abstract class TileMachineBase extends TileAugmentable implements ITickable, ISidedTexture {

	protected static final SideConfig[] DEFAULT_SIDE_CONFIG = new SideConfig[BlockMachine.Type.values().length];
	protected static final EnergyConfig[] DEFAULT_ENERGY_CONFIG = new EnergyConfig[BlockMachine.Type.values().length];
	public static final boolean[] SECURITY = new boolean[BlockMachine.Type.values().length];

	protected static final SoundEvent[] SOUNDS = new SoundEvent[BlockMachine.Type.values().length];
	protected static final boolean[] ENABLE_SOUND = new boolean[BlockMachine.Type.values().length];

	protected static final int RATE = 500;
	protected static final int[] AUGMENT_COUNT = new int[] { 3, 4, 5, 6 };
	protected static final int[] ENERGY_CAPACITY = new int[] { 2, 3, 4, 5 };
	protected static final int[] ENERGY_TRANSFER = new int[] { 3, 6, 12, 24 };
	protected static final int[] AUTO_TRANSFER = new int[] { 8, 16, 32, 64 };
	protected static final int[] FLUID_CAPACITY = new int[] { 1, 2, 4, 8 };

	public static void configure() {

		for (int i = 0; i < BlockMachine.Type.values().length; i++) {
			String name = StringHelper.titleCase(BlockMachine.Type.byMetadata(i).getName());
			String comment = "Enable this to allow for " + name + "s to be securable.";
			SECURITY[i] = ThermalExpansion.CONFIG.get("Security", "Machine." + name + ".Securable", true, comment);

			comment = "Enable sounds for the " + name + ".";
			ENABLE_SOUND[i] = ThermalExpansion.CONFIG_CLIENT.get("Machine." + name, "Sound.Enable", true, comment);
		}
	}

	int processMax;

	@Override
	public ISound getSound() {
		return super.getSound();
	}

	int processRem;
	boolean wasActive;

	protected final byte type;
	protected EnergyConfig energyConfig;
	protected TimeTracker tracker = new TimeTracker();

	boolean augmentSecondaryNull;

	byte level = 0;
	int processMod = 1;
	int energyMod = 1;
	int secondaryChance = 100;

	public TileMachineBase() {

		this(BlockMachine.Type.FURNACE);
		if (getClass() != TileMachineBase.class) {
			throw new IllegalArgumentException();
		}
	}

	public TileMachineBase(BlockMachine.Type type) {

		this.type = (byte) type.ordinal();

		sideConfig = DEFAULT_SIDE_CONFIG[this.type];
		energyConfig = DEFAULT_ENERGY_CONFIG[this.type].copy();
		energyStorage = new EnergyStorage(energyConfig.maxEnergy, energyConfig.maxPower * ENERGY_TRANSFER[level]);
		setDefaultSides();
	}

	@Override
	public String getName() {

		return "tile.thermalexpansion.machine." + BlockMachine.Type.byMetadata(type).getName() + ".name";
	}

	@Override
	public int getLightValue() {

		return isActive ? BlockMachine.Type.values()[type].getLight() : 0;
	}

	@Override
	public boolean enableSecurity() {

		return SECURITY[type];
	}

	protected int calcEnergy() {

		if (!isActive) {
			return 0;
		}
		if (energyStorage.getEnergyStored() > energyConfig.maxPowerLevel) {
			return energyConfig.maxPower;
		}
		if (energyStorage.getEnergyStored() < energyConfig.minPowerLevel) {
			return energyConfig.minPower;
		}
		return energyStorage.getEnergyStored() / energyConfig.energyRamp;
	}

	protected int getMaxInputSlot() {

		return 0;
	}

	protected boolean canStart() {

		return false;
	}

	protected boolean canFinish() {

		return processRem > 0 ? false : hasValidInput();
	}

	protected boolean hasValidInput() {

		return true;
	}

	protected void processStart() {

	}

	protected void processFinish() {

	}

	protected void transferInput() {

	}

	protected void transferOutput() {

	}

	protected void updateIfChanged(boolean curActive) {

		if (curActive != isActive && !wasActive) {
			if (BlockMachine.Type.values()[type].getLight() != 0) {
				updateLighting();
			}
			sendUpdatePacket(Side.CLIENT);
		} else if (wasActive && tracker.hasDelayPassed(worldObj, 100)) {
			wasActive = false;
			if (BlockMachine.Type.values()[type].getLight() != 0) {
				updateLighting();
			}
			sendUpdatePacket(Side.CLIENT);
		}
	}

	protected void onLevelChange() {

		augments = new ItemStack[AUGMENT_COUNT[level]];
		augmentStatus = new boolean[augments.length];
		energyConfig.setParams(energyConfig.minPower, energyConfig.maxPower, energyConfig.maxEnergy * ENERGY_CAPACITY[level] / 2);
		energyStorage.setCapacity(energyConfig.maxEnergy);
		energyStorage.setMaxTransfer(energyConfig.maxPower * ENERGY_TRANSFER[level]);
	}

	/* ITickable */
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
				isActive = true;
			}
		}
		updateIfChanged(curActive);
		chargeEnergy();
	}

	/* BLOCK STATE */
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {

		IExtendedBlockState exState = (IExtendedBlockState) state;

		return exState.withProperty(TEProps.ACTIVE, isActive).withProperty(TEProps.FACING, EnumFacing.VALUES[facing])
				.withProperty(TEProps.SIDE_CONFIG[0], BlockTEBase.EnumSideConfig.VALUES[sideCache[0]])
				.withProperty(TEProps.SIDE_CONFIG[1], BlockTEBase.EnumSideConfig.VALUES[sideCache[1]])
				.withProperty(TEProps.SIDE_CONFIG[2], BlockTEBase.EnumSideConfig.VALUES[sideCache[2]])
				.withProperty(TEProps.SIDE_CONFIG[3], BlockTEBase.EnumSideConfig.VALUES[sideCache[3]])
				.withProperty(TEProps.SIDE_CONFIG[4], BlockTEBase.EnumSideConfig.VALUES[sideCache[4]])
				.withProperty(TEProps.SIDE_CONFIG[5], BlockTEBase.EnumSideConfig.VALUES[sideCache[5]]);
	}

	/* GUI METHODS */
	@Override
	public int getScaledProgress(int scale) {

		if (!isActive || processMax <= 0 || processRem <= 0) {
			return 0;
		}
		return scale * (processMax - processRem) / processMax;
	}

	@Override
	public int getScaledSpeed(int scale) {

		if (!isActive) {
			return 0;
		}
		double power = energyStorage.getEnergyStored() / energyConfig.energyRamp;
		power = MathHelper.clip(power, energyConfig.minPower, energyConfig.maxPower);

		return MathHelper.round(scale * power / energyConfig.maxPower);
	}

	/* NBT METHODS */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		processMax = nbt.getInteger("ProcMax");
		processRem = nbt.getInteger("ProcRem");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		nbt.setInteger("ProcMax", processMax);
		nbt.setInteger("ProcRem", processRem);

		return nbt;
	}

	@Override
	public void readAugmentsFromNBT(NBTTagCompound nbt) {

		level = nbt.getByte("Level");
		onLevelChange();

		super.readAugmentsFromNBT(nbt);
	}

	@Override
	public void writeAugmentsToNBT(NBTTagCompound nbt) {

		nbt.setByte("Level", level);

		super.writeAugmentsToNBT(nbt);
	}

	/* NETWORK METHODS */
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {

		SPacketUpdateTileEntity packet = super.getUpdatePacket();
		NBTTagCompound nbt = packet.getNbtCompound();

		nbt.setByte("level", level);

		return packet;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {

		super.onDataPacket(net, pkt);

		if (net.getDirection() == EnumPacketDirection.CLIENTBOUND) {
			NBTTagCompound nbt = pkt.getNbtCompound();

			byte curLevel = level;
			level = nbt.getByte("level");

			if (curLevel != level) {
				onLevelChange();
			}
		}
	}

	@Override
	public PacketCoFHBase getPacket() {

		PacketCoFHBase payload = super.getPacket();

		payload.addByte(level);

		return payload;
	}

	@Override
	public PacketCoFHBase getGuiPacket() {

		PacketCoFHBase payload = super.getGuiPacket();

		payload.addInt(processMax);
		payload.addInt(processRem);
		payload.addInt(processMod);
		payload.addInt(energyMod);

		return payload;
	}

	@Override
	protected void handleGuiPacket(PacketCoFHBase payload) {

		super.handleGuiPacket(payload);

		processMax = payload.getInt();
		processRem = payload.getInt();
		processMod = payload.getInt();
		energyMod = payload.getInt();
	}

	/* ITilePacketHandler */
	@Override
	public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {

		super.handleTilePacket(payload, isServer);

		if (!isServer) {
			byte curLevel = level;
			level = payload.getByte();

			if (curLevel != level) {
				onLevelChange();
			}
		} else {
			payload.getByte();
		}
	}

	/* AUGMENT HELPERS */
	@Override
	protected boolean installAugment(int slot) {

		IAugmentItem augmentItem = (IAugmentItem) augments[slot].getItem();
		boolean installed = false;

		if (augmentItem.getAugmentLevel(augments[slot], TEAugments.MACHINE_SECONDARY) > 0) {
			int augLevel = Math.min(TEAugments.NUM_MACHINE_SECONDARY, augmentItem.getAugmentLevel(augments[slot], TEAugments.MACHINE_SECONDARY));

			if (augLevel > level) {
				return false;
			}
			if (hasDuplicateAugment(TEAugments.MACHINE_SECONDARY, augLevel, slot)) {
				return false;
			}
			if (hasAugmentChain(TEAugments.MACHINE_SECONDARY, augLevel)) {
				secondaryChance -= TEAugments.MACHINE_SECONDARY_MOD[augLevel];
				installed = true;
			} else {
				return false;
			}
		}
		if (augmentItem.getAugmentLevel(augments[slot], TEAugments.MACHINE_SPEED) > 0) {
			int augLevel = Math.min(TEAugments.NUM_MACHINE_SPEED, augmentItem.getAugmentLevel(augments[slot], TEAugments.MACHINE_SPEED));

			if (augLevel > level) {
				return false;
			}
			if (hasDuplicateAugment(TEAugments.MACHINE_SPEED, augLevel, slot)) {
				return false;
			}
			if (hasAugmentChain(TEAugments.MACHINE_SPEED, augLevel)) {
				processMod = Math.max(processMod, TEAugments.MACHINE_SPEED_PROCESS_MOD[augLevel]);
				energyMod = Math.max(energyMod, TEAugments.MACHINE_SPEED_ENERGY_MOD[augLevel]);
				installed = true;
			} else {
				return false;
			}
		}
		if (augmentItem.getAugmentLevel(augments[slot], TEAugments.ENERGY_STORAGE) > 0) {
			int augLevel = Math.min(TEAugments.NUM_ENERGY_STORAGE, augmentItem.getAugmentLevel(augments[slot], TEAugments.ENERGY_STORAGE));

			if (augLevel > level) {
				return false;
			}
			if (hasDuplicateAugment(TEAugments.ENERGY_STORAGE, augLevel, slot)) {
				return false;
			}
			if (hasAugmentChain(TEAugments.ENERGY_STORAGE, augLevel)) {
				energyStorage.setCapacity(Math.max(energyStorage.getMaxEnergyStored(), energyConfig.maxEnergy * TEAugments.ENERGY_STORAGE_MOD[augLevel]));
				installed = true;
			} else {
				return false;
			}
		}
		if (augmentItem.getAugmentLevel(augments[slot], TEAugments.MACHINE_NULL) > 0) {
			augmentSecondaryNull = true;
			installed = true;
		}
		if (augmentItem.getAugmentLevel(augments[slot], TEAugments.GENERAL_AUTO_OUTPUT) > 0) {
			augmentAutoOutput = true;
			installed = true;
		}
		if (augmentItem.getAugmentLevel(augments[slot], TEAugments.GENERAL_AUTO_INPUT) > 0) {
			augmentAutoInput = true;
			installed = true;
		}
		if (augmentItem.getAugmentLevel(augments[slot], TEAugments.GENERAL_RECONFIG_SIDES) > 0) {
			augmentReconfigSides = true;
			installed = true;
		}
		if (augmentItem.getAugmentLevel(augments[slot], TEAugments.GENERAL_REDSTONE_CONTROL) > 0) {
			augmentRedstoneControl = true;
			installed = true;
		}
		return installed;
	}

	@Override
	protected void onInstalled() {

		if (!augmentReconfigSides) {
			setDefaultSides();
			sideCache[facing] = 0;
		}
		if (!augmentRedstoneControl) {
			this.rsMode = ControlMode.DISABLED;
		}
		if (isActive && energyStorage.getMaxEnergyStored() > 0 && processRem * energyMod / processMod > energyStorage.getEnergyStored()) {
			processRem = 0;
			isActive = false;
			wasActive = true;
			tracker.markTime(worldObj);
		}
	}

	@Override
	protected void resetAugments() {

		super.resetAugments();

		augmentSecondaryNull = false;

		processMod = 1;
		energyMod = 1;
		secondaryChance = 100;
	}

	/* IInventory */
	@Override
	public ItemStack decrStackSize(int slot, int amount) {

		ItemStack stack = super.decrStackSize(slot, amount);

		if (ServerHelper.isServerWorld(worldObj) && slot <= getMaxInputSlot()) {
			if (isActive && (inventory[slot] == null || !hasValidInput())) {
				isActive = false;
				wasActive = true;
				tracker.markTime(worldObj);
				processRem = 0;
			}
		}
		return stack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {

		if (ServerHelper.isServerWorld(worldObj) && slot <= getMaxInputSlot()) {
			if (isActive && inventory[slot] != null) {
				if (stack == null || !stack.isItemEqual(inventory[slot]) || !hasValidInput()) {
					isActive = false;
					wasActive = true;
					tracker.markTime(worldObj);
					processRem = 0;
				}
			}
		}
		super.setInventorySlotContents(slot, stack);
	}

	@Override
	public void markDirty() {

		if (isActive && !hasValidInput()) {
			isActive = false;
			wasActive = true;
			tracker.markTime(worldObj);
			processRem = 0;
		}
		super.markDirty();
	}

	/* IEnergyInfo */
	@Override
	public int getInfoEnergyPerTick() {

		return calcEnergy() * energyMod;
	}

	@Override
	public int getInfoMaxEnergyPerTick() {

		return energyConfig.maxPower * energyMod;
	}

	/* IReconfigurableFacing */
	@Override
	public boolean setFacing(EnumFacing side) {

		int sideInt = side.ordinal();

		if (sideInt < 0 || sideInt > 5) {
			return false;
		}
		if (!allowYAxisFacing() && sideInt < 2) {
			return false;
		}
		facing = (byte) sideInt;
		sideCache[facing] = 0;
		markDirty();
		sendUpdatePacket(Side.CLIENT);
		return true;
	}

	/* ISidedTexture */
	@Override
	public ResourceLocation getTexture(EnumFacing side, int pass) {

		if (pass == 0) {
			if (side.ordinal() == facing) {
				return isActive ? TextureLocations.addTxFolder(TextureLocations.Machine.ACTIVE_FACE_MAP.get(BlockMachine.Type.byMetadata(type)))
						: TextureLocations.addTxFolder(TextureLocations.Machine.FACE_MAP.get(BlockMachine.Type.byMetadata(type)));
			} else {
				return TextureLocations.addTxFolder(TextureLocations.Machine.SIDE_MAP.get(side));
			}
		} else if (pass == 1) {
			return TextureLocations.addTxFolder(TextureLocations.Config.CONFIG_MAP.get(getSideConfig(side)));
		}
		return TextureLocations.addTxFolder(TextureLocations.Machine.SIDE);
	}

	/* ISoundSource */
	@Override
	public SoundEvent getSoundEvent() {

		return ENABLE_SOUND[type] ? SOUNDS[type] : null;
	}

}
