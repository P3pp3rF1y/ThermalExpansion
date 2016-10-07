package cofh.thermalexpansion.block.device;

import cofh.api.energy.EnergyStorage;
import cofh.core.entity.FakePlayerCoFH;
import cofh.core.network.PacketCoFHBase;
import cofh.lib.util.helpers.MathHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.gui.client.device.GuiActivator;
import cofh.thermalexpansion.gui.container.device.ContainerActivator;
import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;

public class TileActivator extends TileDeviceBase implements ITickable {

	static EnergyConfig energyConfig;
	static int ACTIVATION_ENERGY = 20;
	static int MAX_SLOT = 9;

	public static void initialize() {

		int type = BlockDevice.Type.ACTIVATOR.ordinal();

		DEFAULT_SIDE_CONFIG[type] = new SideConfig();
		DEFAULT_SIDE_CONFIG[type].numConfig = 4;
		DEFAULT_SIDE_CONFIG[type].slotGroups = new int[][] { {}, { 0, 1, 2, 3, 4, 5, 6, 7, 8 }, { 0, 1, 2, 3, 4, 5, 6, 7, 8 }, { 0, 1, 2, 3, 4, 5, 6, 7, 8 } };
		DEFAULT_SIDE_CONFIG[type].allowInsertionSide = new boolean[] { false, true, false, true };
		DEFAULT_SIDE_CONFIG[type].allowExtractionSide = new boolean[] { false, false, true, true };
		DEFAULT_SIDE_CONFIG[type].allowInsertionSlot = new boolean[] { true, true, true, true, true, true, true, true, true, false };
		DEFAULT_SIDE_CONFIG[type].allowExtractionSlot = new boolean[] { true, true, true, true, true, true, true, true, true, false };
		DEFAULT_SIDE_CONFIG[type].sideTex = new int[] { 0, 1, 4, 7 };
		DEFAULT_SIDE_CONFIG[type].defaultSides = new byte[] { 1, 1, 1, 1, 1, 1 };

		String category = "Device.Activator";
		int maxPower = MathHelper.clamp(ThermalExpansion.CONFIG.get(category, "BasePower", 20), 0, 500);
		ThermalExpansion.CONFIG.set("Device.Activator", "BasePower", maxPower);
		energyConfig = new EnergyConfig();
		energyConfig.setParamsPower(maxPower);

		String comment = "This value sets how much energy the Activator uses when it actually does something. Set to 0 to disable it requiring energy.";
		maxPower = MathHelper.clamp(ThermalExpansion.CONFIG.get(category, "ActivationEnergy", ACTIVATION_ENERGY, comment), 0, 500);
		ThermalExpansion.CONFIG.set("Device.Activator", "ActivationEnergy", maxPower);
		ACTIVATION_ENERGY = maxPower;

		GameRegistry.registerTileEntity(TileActivator.class, "thermalexpansion.Activator");
	}

	public boolean leftClick = false;
	public byte tickSlot = 0;
	public boolean actsSneaking = false;
	public byte angle = 1;

	FakePlayerCoFH myFakePlayer;
	int slotTracker = 0;
	int[] tracker;

	static final Predicate<Entity> selectAttackable = new Predicate<Entity>() {

		@Override
		public boolean apply(Entity e) {

			return e.canBeAttackedWithItem();
		}
	};

	public TileActivator() {

		super(BlockDevice.Type.ACTIVATOR);
		inventory = new ItemStack[10];
		energyStorage = new EnergyStorage(energyConfig.maxEnergy, energyConfig.maxPower * 3);
	}

	@Override
	public void cofh_validate() {

		if (ServerHelper.isServerWorld(worldObj)) {
			myFakePlayer = new FakePlayerCoFH((WorldServer) worldObj);
		}
		super.cofh_validate();
	}

	@Override
	public void setDefaultSides() {

		sideCache = getDefaultSides();
		sideCache[facing] = 0;
		sideCache[facing ^ 1] = 2;
	}

	// TODO: FIX
	//	@Override
	//	public void onRedstoneUpdate() {
	//
	//		if (ServerHelper.isClientWorld(worldObj)) {
	//			return;
	//		} else if (!inWorld) {
	//			cofh_validate();
	//		}
	//		if (!redstoneControlOrDisable() && myFakePlayer.itemInUse != null) {
	//			myFakePlayer.stopUsingItem();
	//		} else {
	//			int coords[] = BlockHelper.getAdjacentCoordinatesForSide(xCoord, yCoord, zCoord, facing);
	//			Block block = worldObj.getBlock(coords[0], coords[1], coords[2]);
	//
	//			if (block != null && block.isAir(worldObj, coords[0], coords[1], coords[2])) {
	//				doDeploy();
	//			}
	//		}
	//	}
	//
	//	public boolean doDeploy() {
	//
	//		int tickSlot = getNextStackIndex();
	//		ItemStack theStack = getStackInSlot(tickSlot);
	//		updateFakePlayer(tickSlot);
	//
	//		boolean r = false;
	//		if (leftClick) {
	//			r = simLeftClick(myFakePlayer, theStack, facing);
	//		} else {
	//			int coords[] = BlockHelper.getAdjacentCoordinatesForSide(xCoord, yCoord, zCoord, facing);
	//			r = simRightClick(myFakePlayer, theStack, coords[0], coords[1], coords[2], 1);
	//		}
	//		if (theStack != null && theStack.stackSize <= 0) {
	//			setInventorySlotContents(tickSlot, null);
	//		}
	//		checkItemsUpdated();
	//		return r;
	//	}
	//
	//	public void checkItemsUpdated() {
	//
	//		ItemStack[] pInventory = myFakePlayer.inventory.mainInventory;
	//		int i = 0;
	//		for (; i < MAX_SLOT; i++) {
	//			setInventorySlotContents(i, pInventory[i]);
	//			if (inventory[i] != null && inventory[i].stackSize <= 0) {
	//				inventory[i] = null;
	//				pInventory[i] = null;
	//			}
	//		}
	//		for (int e = pInventory.length; i < e; i++) {
	//			if (InventoryHelper.addItemStackToInventory(inventory, pInventory[i], 0, MAX_SLOT - 1)) {
	//				pInventory[i] = null;
	//			}
	//		}
	//	}
	//
	//	public int getNextStackIndex() {
	//
	//		// FIXME: is this called too frequently? round-robin is wrong
	//
	//		if ((leftClick && myFakePlayer.theItemInWorldManager.durabilityRemainingOnBlock > -1) || myFakePlayer.itemInUse != null) {
	//			return slotTracker;
	//		}
	//		if (tickSlot == 0) {
	//			return incrementTracker();
	//		} else if (tickSlot == 1) {
	//			return getRandomStackIndex();
	//		}
	//		return 0;
	//	}
	//
	//	public int getRandomStackIndex() {
	//
	//		int i = 0;
	//		tracker = new int[MAX_SLOT];
	//		// TODO: allocating this array is probably bad
	//
	//		for (int k = 0; k < MAX_SLOT; k++) {
	//			if (getStackInSlot(k) != null) {
	//				tracker[i++] = k; // track filled slots
	//			}
	//		}
	//		if (i == 0) {
	//			return incrementTracker();
	//		}
	//		int v = MathHelper.RANDOM.nextInt(i + 1); // +1 so that the tracker field is used in some cases (old behavior. wrong?)
	//		return i == v ? incrementTracker() : tracker[v];
	//	}
	//
	//	public int incrementTracker() {
	//
	//		slotTracker++;
	//		slotTracker %= MAX_SLOT;
	//
	//		for (int k = slotTracker; k < MAX_SLOT; k++) {
	//			if (this.inventory[k] != null) {
	//				slotTracker = k;
	//				return slotTracker;
	//			}
	//		}
	//		for (int k = 0; k < slotTracker; k++) {
	//			if (this.inventory[k] != null) {
	//				slotTracker = k;
	//				return slotTracker;
	//			}
	//		}
	//		slotTracker = 0;
	//		return slotTracker;
	//	}
	//
	//	public void updateFakePlayer(int tickSlot) {
	//
	//		myFakePlayer.inventory.mainInventory = new ItemStack[36];
	//		for (int i = 0; i < MAX_SLOT; i++) {
	//			myFakePlayer.inventory.mainInventory[i] = getStackInSlot(i);
	//		}
	//		double x = xCoord + 0.5D;
	//		double y = yCoord - 1.1D;
	//		double z = zCoord + 0.5D;
	//		float pitch = this.angle == 0 ? 45.0F : this.angle == 1 ? 0F : -45F;
	//		float yaw;
	//
	//		switch (facing) {
	//		case 0:
	//			pitch = this.angle == 0 ? -90.0F : this.angle == 1 ? 0F : 90F;
	//			yaw = 0.0F;
	//			y -= 0.51D;
	//			break;
	//		case 1:
	//			pitch = this.angle == 0 ? 90.0F : this.angle == 1 ? 0F : -90F;
	//			yaw = 0.0F;
	//			y += 1.51D;
	//			break;
	//		case 2:
	//			yaw = 180.0F;
	//			z -= 0.51D;
	//			y += .5D;
	//			break;
	//		case 3:
	//			yaw = 0.0F;
	//			z += 0.51D;
	//			y += .5D;
	//			break;
	//		case 4:
	//			yaw = 90.0F;
	//			x -= 0.51D;
	//			y += .5D;
	//			break;
	//		default:
	//			yaw = -90.0F;
	//			x += 0.51D;
	//			y += .5D;
	//		}
	//		myFakePlayer.setPositionAndRotation(x, y, z, yaw, pitch);
	//		myFakePlayer.isSneaking = actsSneaking;
	//		myFakePlayer.yOffset = -1.1F;
	//		myFakePlayer.setItemInHand(tickSlot);
	//
	//		myFakePlayer.onUpdate();
	//	}
	//
	//	@Override
	//	public boolean rotateBlock() {
	//
	//		if (inWorld && ServerHelper.isServerWorld(worldObj)) {
	//			int coords[] = BlockHelper.getAdjacentCoordinatesForSide(xCoord, yCoord, zCoord, facing);
	//			myFakePlayer.theItemInWorldManager.cancelDestroyingBlock(coords[0], coords[1], coords[2]);
	//			myFakePlayer.theItemInWorldManager.durabilityRemainingOnBlock = -1;
	//		}
	//		return super.rotateBlock();
	//	}
	//
	//	public boolean simLeftClick(EntityPlayer thePlayer, ItemStack deployingStack, int side) {
	//
	//		int coords[] = BlockHelper.getAdjacentCoordinatesForSide(xCoord, yCoord, zCoord, facing);
	//
	//		Block theBlock = worldObj.getBlock(coords[0], coords[1], coords[2]);
	//		if (!theBlock.isAir(worldObj, coords[0], coords[1], coords[2])) {
	//			if (myFakePlayer.theItemInWorldManager.durabilityRemainingOnBlock == -1) {
	//				myFakePlayer.theItemInWorldManager.onBlockClicked(coords[0], coords[1], coords[2], facing ^ 1);
	//			} else if (myFakePlayer.theItemInWorldManager.durabilityRemainingOnBlock >= 9) {
	//				myFakePlayer.theItemInWorldManager.uncheckedTryHarvestBlock(coords[0], coords[1], coords[2]);
	//				myFakePlayer.theItemInWorldManager.durabilityRemainingOnBlock = -1;
	//
	//				if (deployingStack != null) {
	//					deployingStack.getItem().onBlockDestroyed(deployingStack, worldObj, theBlock, coords[0], coords[1], coords[2], myFakePlayer);
	//				}
	//			}
	//		} else {
	//			myFakePlayer.theItemInWorldManager.cancelDestroyingBlock(coords[0], coords[1], coords[2]);
	//			myFakePlayer.theItemInWorldManager.durabilityRemainingOnBlock = -1;
	//			List<Entity> entities = worldObj.selectEntitiesWithinAABB(Entity.class, BlockHelper.getAdjacentAABBForSide(xCoord, yCoord, zCoord, facing),
	//					selectAttackable);
	//
	//			if (entities.size() == 0) {
	//				return false;
	//			}
	//			thePlayer.attackTargetEntityWithCurrentItem(entities.get(entities.size() > 1 ? MathHelper.RANDOM.nextInt(entities.size()) : 0));
	//		}
	//		return true;
	//	}
	//
	//	public boolean simRightClick(EntityPlayer thePlayer, ItemStack deployingStack, int blockX, int blockY, int blockZ, int side) {
	//
	//		if (thePlayer.itemInUse == null) {
	//			if (!simRightClick2(thePlayer, deployingStack, blockX, blockY, blockZ, side) && deployingStack != null) {
	//				List<Entity> entities = worldObj.getEntitiesWithinAABB(Entity.class, BlockHelper.getAdjacentAABBForSide(xCoord, yCoord, zCoord, facing));
	//
	//				if (entities.size() > 0 && thePlayer.interactWith(entities.get(entities.size() > 1 ? MathHelper.RANDOM.nextInt(entities.size() - 1) : 0))) {
	//					return true;
	//				}
	//				PlayerInteractEvent event = ForgeEventFactory.onPlayerInteract(thePlayer, Action.RIGHT_CLICK_AIR, 0, 0, 0, -1, worldObj);
	//				if (event.useItem == Event.Result.DENY) {
	//					return false;
	//				}
	//				ItemStack result = deployingStack.useItemRightClick(worldObj, thePlayer);
	//				thePlayer.inventory.setInventorySlotContents(myFakePlayer.inventory.currentItem, result == null || result.stackSize <= 0 ? null : result);
	//			}
	//		}
	//		return true;
	//	}
	//
	//	public boolean simRightClick2(EntityPlayer thePlayer, ItemStack deployingStack, int blockX, int blockY, int blockZ, int side) {
	//
	//		float f = 0.5F;
	//		float f1 = 0.5F;
	//		float f2 = 0.5F;
	//		int offsetY = facing == 1 ? 1 : -1;
	//
	//		if (facing > 1) {
	//			if (angle == 0) {
	//				blockY -= 1;
	//			}
	//			if (angle == 2) {
	//				blockY += 1;
	//			}
	//		}
	//
	//		PlayerInteractEvent event = ForgeEventFactory.onPlayerInteract(thePlayer, Action.RIGHT_CLICK_BLOCK, blockX, blockY, blockZ, side, worldObj);
	//		if (event.isCanceled()) {
	//			return false;
	//		}
	//
	//		Block block = worldObj.getBlock(blockX, blockY, blockZ);
	//
	//		boolean isAir = block.isAir(worldObj, blockX, blockY, blockZ);
	//
	//		if (deployingStack != null && deployingStack.getItem() != null
	//				&& deployingStack.getItem().onItemUseFirst(deployingStack, thePlayer, worldObj, blockX, blockY, blockZ, side, f, f1, f2)) {
	//			return true;
	//		}
	//		if (!thePlayer.isSneaking() || thePlayer.getHeldItem() == null) {
	//			if (block.onBlockActivated(worldObj, blockX, blockY, blockZ, thePlayer, side, f, f1, f2)) {
	//				return true;
	//			}
	//		}
	//		if (deployingStack == null) {
	//			return false;
	//		} else {
	//			if (deployingStack.getItem() instanceof ItemBlock) {
	//				if (!deployingStack.tryPlaceItemIntoWorld(thePlayer, worldObj, blockX, blockY + offsetY, blockZ, facing != 1 ? 1 : 0, f, f1, f2)) {
	//					if (isAir) {
	//						if (!deployingStack.tryPlaceItemIntoWorld(thePlayer, worldObj, blockX, blockY, blockZ, facing != 1 ? 1 : 0, f, f1, f2)) {
	//							return false;
	//						}
	//					} else {
	//						if (!deployingStack.tryPlaceItemIntoWorld(thePlayer, worldObj, blockX, blockY, blockZ, 0, f, f1, f2)) {
	//							return false;
	//						}
	//					}
	//				}
	//			} else {
	//				if (isAir) {
	//					if (!deployingStack.tryPlaceItemIntoWorld(thePlayer, worldObj, blockX, blockY, blockZ, facing != 1 ? 1 : 0, f, f1, f2)) {
	//						if (!deployingStack.tryPlaceItemIntoWorld(thePlayer, worldObj, blockX, blockY + offsetY, blockZ, facing != 1 ? 1 : 0, f, f1, f2)) {
	//							return false;
	//						}
	//					}
	//				} else {
	//					if (!deployingStack.tryPlaceItemIntoWorld(thePlayer, worldObj, blockX, blockY, blockZ, 0, f, f1, f2)) {
	//						if (!deployingStack.tryPlaceItemIntoWorld(thePlayer, worldObj, blockX, blockY + offsetY, blockZ, facing != 1 ? 1 : 0, f, f1, f2)) {
	//							return false;
	//						}
	//					}
	//				}
	//			}
	//			if (deployingStack.stackSize <= 0) {
	//				MinecraftForge.EVENT_BUS.post(new PlayerDestroyItemEvent(thePlayer, deployingStack));
	//				thePlayer.inventory.setInventorySlotContents(myFakePlayer.inventory.currentItem, null);
	//			}
	//			return true;
	//		}
	//	}
	//
	//	@Override
	//	protected boolean readPortableTagInternal(EntityPlayer player, NBTTagCompound tag) {
	//
	//		actsSneaking = tag.getBoolean("Sneaking");
	//		leftClick = tag.getBoolean("LeftClick");
	//		tickSlot = tag.getByte("TickSlot");
	//		angle = tag.getByte("Angle");
	//
	//		return true;
	//	}
	//
	//	@Override
	//	protected boolean writePortableTagInternal(EntityPlayer player, NBTTagCompound tag) {
	//
	//		tag.setBoolean("Sneaking", actsSneaking);
	//		tag.setBoolean("LeftClick", leftClick);
	//		tag.setByte("TickSlot", tickSlot);
	//		tag.setByte("Angle", angle);
	//
	//		return true;
	//	}
	//
	//	/* ITickable */
	//	@Override
	//	public void update() {
	//
	//		if (ServerHelper.isClientWorld(worldObj)) {
	//			return;
	//		} else if (!inWorld) {
	//			cofh_validate();
	//		}
	//		if (hasEnergy(ACTIVATION_ENERGY)) {
	//			if (!isActive) {
	//				worldObj.markBlockForUpdate(pos);
	//			}
	//			isActive = true;
	//			boolean work = false;
	//
	//			if (worldObj.getTotalWorldTime() % CoFHProps.TIME_CONSTANT_HALF == 0 && redstoneControlOrDisable()) {
	//				work = doDeploy();
	//			} else {
	//
	//				if (leftClick && myFakePlayer.theItemInWorldManager.durabilityRemainingOnBlock > -1) {
	//					work = true;
	//					int tickSlot = getNextStackIndex();
	//					myFakePlayer.theItemInWorldManager.updateBlockRemoving();
	//					if (myFakePlayer.theItemInWorldManager.durabilityRemainingOnBlock >= 9) {
	//						work = simLeftClick(myFakePlayer, getStackInSlot(tickSlot), facing);
	//					}
	//				} else if (!leftClick && myFakePlayer.itemInUse != null) {
	//					work = true;
	//					int slot = getNextStackIndex();
	//					myFakePlayer.inventory.currentItem = slot;
	//					myFakePlayer.tickItemInUse(getStackInSlot(slot));
	//					checkItemsUpdated();
	//				}
	//			}
	//			if (work) {
	//				drainEnergy(ACTIVATION_ENERGY);
	//			}
	//		} else {
	//			if (isActive) {
	//				worldObj.markBlockForUpdate(pos);
	//			}
	//			isActive = false;
	//		}
	//		chargeEnergy();
	//	}

	@Override
	public void update() {

	}

	/* GUI METHODS */
	@Override
	public Object getGuiClient(InventoryPlayer inventory) {

		return new GuiActivator(inventory, this);
	}

	@Override
	public Object getGuiServer(InventoryPlayer inventory) {

		return new ContainerActivator(inventory, this);
	}

	@Override
	public int getInvSlotCount() {

		return MAX_SLOT;
	}

	/* NBT METHODS */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		actsSneaking = nbt.getBoolean("Sneaking");
		leftClick = nbt.getBoolean("LeftClick");
		tickSlot = nbt.getByte("TickSlot");
		angle = nbt.getByte("Angle");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		nbt.setBoolean("Sneaking", actsSneaking);
		nbt.setBoolean("LeftClick", leftClick);
		nbt.setByte("TickSlot", tickSlot);
		nbt.setByte("Angle", angle);

		return nbt;
	}

	@Override
	public NBTTagCompound getUpdateTag() {

		NBTTagCompound nbt = super.getUpdateTag();

		nbt.setBoolean("leftClick", leftClick);
		nbt.setBoolean("actsSneaking", actsSneaking);
		nbt.setByte("tickSlot", tickSlot);
		nbt.setByte("angle", angle);

		return nbt;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {

		super.onDataPacket(net, pkt);

		NBTTagCompound nbt = pkt.getNbtCompound();

		leftClick = nbt.getBoolean("leftClick");
		actsSneaking = nbt.getBoolean("actsSneaking");
		tickSlot = nbt.getByte("tickSlot");
		angle = nbt.getByte("angle");
	}

	/* NETWORK METHODS */
	@Override
	public PacketCoFHBase getPacket() {

		PacketCoFHBase payload = super.getPacket();

		payload.addBool(leftClick);
		payload.addBool(actsSneaking);
		payload.addByte(tickSlot);
		payload.addByte(angle);

		return payload;
	}

	@Override
	public PacketCoFHBase getModePacket() {

		PacketCoFHBase payload = super.getModePacket();

		payload.addBool(leftClick);
		payload.addBool(actsSneaking);
		payload.addByte(tickSlot);
		payload.addByte(angle);

		return payload;
	}

	@Override
	protected void handleModePacket(PacketCoFHBase payload) {

		super.handleModePacket(payload);

		leftClick = payload.getBool();
		actsSneaking = payload.getBool();
		tickSlot = payload.getByte();
		angle = payload.getByte();
	}

	/* ITilePacketHandler */
	@Override
	public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {

		super.handleTilePacket(payload, isServer);

		leftClick = payload.getBool();
		actsSneaking = payload.getBool();
		tickSlot = payload.getByte();
		angle = payload.getByte();
	}

	/* IReconfigurableFacing */
	@Override
	public boolean setFacing(EnumFacing side) {

		int sideInt = side.ordinal();

		if (sideInt < 0 || sideInt > 5) {
			return false;
		}
		facing = (byte) sideInt;
		sideCache[facing] = 0;
		sideCache[facing ^ 1] = 2;
		markDirty();
		sendUpdatePacket(Side.CLIENT);
		return true;
	}

}
