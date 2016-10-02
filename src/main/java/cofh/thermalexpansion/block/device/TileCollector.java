package cofh.thermalexpansion.block.device;

import cofh.api.tileentity.IInventoryConnection;
import cofh.core.CoFHProps;
import cofh.core.util.RegistrySocial;
import cofh.lib.util.helpers.InventoryHelper;
import cofh.lib.util.helpers.SecurityHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermalexpansion.gui.client.device.GuiCollector;
import cofh.thermalexpansion.gui.container.ContainerTEBase;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class TileCollector extends TileDeviceBase implements IInventoryConnection, ITickable {

	static final float[] DEFAULT_ARMOR_DROP_CHANCES = new float[] { 1.0F, 1.0F, 1.0F, 1.0F };
	static final float[] DEFAULT_HANDS_DROP_CHANCES = new float[] { 1.0F, 1.0F };

	public static void initialize() {

		int type = BlockDevice.Type.COLLECTOR.ordinal();

		DEFAULT_SIDE_CONFIG[type] = new SideConfig();
		DEFAULT_SIDE_CONFIG[type].numConfig = 2;
		DEFAULT_SIDE_CONFIG[type].slotGroups = new int[][] { {}, {} };
		DEFAULT_SIDE_CONFIG[type].allowInsertionSide = new boolean[] { false, false };
		DEFAULT_SIDE_CONFIG[type].allowExtractionSide = new boolean[] { false, false };
		DEFAULT_SIDE_CONFIG[type].allowInsertionSlot = new boolean[] {};
		DEFAULT_SIDE_CONFIG[type].allowExtractionSlot = new boolean[] {};
		DEFAULT_SIDE_CONFIG[type].sideTex = new int[] { 0, 4 };
		DEFAULT_SIDE_CONFIG[type].defaultSides = new byte[] { 0, 0, 0, 0, 0, 0 };

		GameRegistry.registerTileEntity(TileCollector.class, "thermalexpansion.deviceCollector");
	}

	int areaMajor = 2;
	int areaMinor = 1;
	LinkedList<ItemStack> stuffedItems = new LinkedList<ItemStack>();

	boolean ignoreGuild = true;
	boolean ignoreFriends = true;
	boolean ignoreOwner = true;

	public boolean augmentEntityCollection;

	public TileCollector() {

		super(BlockDevice.Type.COLLECTOR);
	}

	@Override
	public void setDefaultSides() {

		sideCache = getDefaultSides();
		sideCache[facing ^ 1] = 1;
	}

	public boolean isEmpty() {

		return stuffedItems.size() == 0;
	}

	public boolean doNotCollectItemsFrom(EntityPlayer player) {

		String name = player.getName();

		UUID ownerID = owner.getId();
		UUID otherID = SecurityHelper.getID(player);
		if (ownerID.equals(otherID)) {
			return ignoreOwner;
		}
		return ignoreFriends && RegistrySocial.playerHasAccess(name, owner);
	}

	public void collectItems() {

		BlockPos adj = new BlockPos(pos).offset(EnumFacing.VALUES[facing]);
		stuffedItems.addAll(collectItemsInArea(worldObj, adj, facing, areaMajor, areaMinor));

		if (augmentEntityCollection) {
			stuffedItems.addAll(collectItemsFromEntities(worldObj, adj, facing, areaMajor, areaMinor));
		}
	}

	public void outputBuffer() {

		for (int i = 0; i < 6; i++) {
			if (i != facing && sideCache[i] == 1) {
				BlockPos adj = new BlockPos(pos).offset(EnumFacing.VALUES[i]);
				TileEntity theTile = worldObj.getTileEntity(adj);

				if (InventoryHelper.isInsertion(theTile)) {
					LinkedList<ItemStack> newStuffed = new LinkedList<ItemStack>();
					for (ItemStack curItem : stuffedItems) {
						if (curItem == null || curItem.getItem() == null) {
							curItem = null;
						} else {
							curItem = InventoryHelper.addToInsertion(theTile, curItem, EnumFacing.VALUES[i]);
						}
						if (curItem != null) {
							newStuffed.add(curItem);
						}
					}
					stuffedItems = newStuffed;
				}
			}
		}
	}

	public List<ItemStack> collectItemsInArea(World worldObj, BlockPos adj, int side, int areaMajor, int areaMinor) {

		int x = adj.getX();
		int y = adj.getY();
		int z = adj.getZ();

		int areaMajor2 = 1 + areaMajor;
		List<ItemStack> stacks = new ArrayList<ItemStack>();
		List<EntityItem> result;

		switch (side) {
		case 0:
		case 1:
			result = worldObj.getEntitiesWithinAABB(EntityItem.class,
					new AxisAlignedBB(x - areaMajor, y, z - areaMajor, x + areaMajor2, y + areaMinor, z + areaMajor2));
			break;
		case 2:
		case 3:
			result = worldObj.getEntitiesWithinAABB(EntityItem.class,
					new AxisAlignedBB(x - areaMajor, y - areaMajor, z, x + areaMajor2, y + areaMajor2, z + areaMinor));
			break;
		default:
			result = worldObj.getEntitiesWithinAABB(EntityItem.class,
					new AxisAlignedBB(x, y - areaMajor, z - areaMajor, x + areaMinor, y + areaMajor2, z + areaMajor2));
			break;
		}
		for (int i = 0; i < result.size(); i++) {
			EntityItem entity = result.get(i);
			if (entity.isDead || entity.getEntityItem().stackSize <= 0) {
				continue;
			}
			stacks.add(entity.getEntityItem());
			entity.worldObj.removeEntity(entity);
		}
		return stacks;
	}

	public List<ItemStack> collectItemsFromEntities(World worldObj, BlockPos adj, int side, int areaMajor, int areaMinor) {

		int x = adj.getX();
		int y = adj.getY();
		int z = adj.getZ();

		int areaMajor2 = 1 + areaMajor;
		List<ItemStack> stacks = new ArrayList<ItemStack>();
		List<EntityLivingBase> result;

		switch (side) {
		case 0:
		case 1:
			result = worldObj.getEntitiesWithinAABB(EntityLivingBase.class,
					new AxisAlignedBB(x - areaMajor, y, z - areaMajor, x + areaMajor2, y + areaMinor, z + areaMajor2));
			break;
		case 2:
		case 3:
			result = worldObj.getEntitiesWithinAABB(EntityLivingBase.class,
					new AxisAlignedBB(x - areaMajor, y - areaMajor, z, x + areaMajor2, y + areaMajor2, z + areaMinor));
			break;
		default:
			result = worldObj.getEntitiesWithinAABB(EntityLivingBase.class,
					new AxisAlignedBB(x, y - areaMajor, z - areaMajor, x + areaMinor, y + areaMajor2, z + areaMajor2));
			break;
		}
		for (int i = 0; i < result.size(); i++) {
			EntityLivingBase entity = result.get(i);
			float[] armorDropChances = DEFAULT_ARMOR_DROP_CHANCES;
			float[] handsDropChances = DEFAULT_HANDS_DROP_CHANCES;

			if (entity instanceof EntityLiving) {
				armorDropChances = ((EntityLiving) entity).inventoryArmorDropChances;
				handsDropChances = ((EntityLiving) entity).inventoryHandsDropChances;
			} else if (isSecured() && entity instanceof EntityPlayer) {
				if (doNotCollectItemsFrom((EntityPlayer) entity)) {
					continue;
				}
			}
			for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
				ItemStack equipmentInSlot = entity.getItemStackFromSlot(slot);

				if (equipmentInSlot != null) {
					if ((slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR && armorDropChances[slot.getIndex()] >= 1f)
							|| (slot.getSlotType() == EntityEquipmentSlot.Type.HAND && handsDropChances[slot.getIndex()] >= 1f)) {
						stacks.add(equipmentInSlot);
						entity.setItemStackToSlot(slot, null);
					}
				}
			}
		}
		return stacks;
	}

	/* ITickable */
	@Override
	public void update() {

		if (ServerHelper.isClientWorld(worldObj)) {
			return;
		}
		if (worldObj.getTotalWorldTime() % CoFHProps.TIME_CONSTANT_HALF == 0 && redstoneControlOrDisable()) {
			if (!isEmpty()) {
				outputBuffer();
			}
			if (isEmpty()) {
				collectItems();
			}
		}
	}

	/* GUI METHODS */
	@Override
	public Object getGuiClient(InventoryPlayer inventory) {

		return new GuiCollector(inventory, this);
	}

	@Override
	public Object getGuiServer(InventoryPlayer inventory) {

		return new ContainerTEBase(inventory, this);
	}

	/* NBT METHODS */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		NBTTagList list = nbt.getTagList("StuffedInv", 10);
		stuffedItems.clear();
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound compound = list.getCompoundTagAt(i);
			stuffedItems.add(ItemStack.loadItemStackFromNBT(compound));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		NBTTagList list = new NBTTagList();
		list = new NBTTagList();
		for (int i = 0; i < stuffedItems.size(); i++) {
			if (stuffedItems.get(i) != null) {
				NBTTagCompound compound = new NBTTagCompound();
				stuffedItems.get(i).writeToNBT(compound);
				list.appendTag(compound);
			}
		}
		nbt.setTag("StuffedInv", list);

		return nbt;
	}

	/* IInventoryConnection */
	@Override
	public ConnectionType canConnectInventory(EnumFacing from) {

		if (from != null && from.ordinal() != facing && sideCache[from.ordinal()] == 1) {
			return ConnectionType.FORCE;
		} else {
			return ConnectionType.DEFAULT;
		}
	}

}
