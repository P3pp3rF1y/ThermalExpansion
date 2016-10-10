package cofh.thermalexpansion.network;

import cofh.api.tileentity.IRedstoneControl;
import cofh.api.tileentity.IRedstoneControl.ControlMode;
import cofh.api.tileentity.ISecurable;
import cofh.api.tileentity.ISecurable.AccessMode;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.lib.gui.container.IAugmentableContainer;
import cofh.thermalexpansion.ThermalExpansion;
import cofh.thermalexpansion.gui.container.ISchematicContainer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PacketTEBase extends PacketCoFHBase {

	public static void initialize() {

		PacketHandler.instance.registerPacket(PacketTEBase.class);
	}

	public enum PacketTypes {
		RS_POWER_UPDATE, RS_CONFIG_UPDATE, SECURITY_UPDATE, TAB_AUGMENT, TAB_SCHEMATIC, CONFIG_SYNC
	}

	@Override
	public void handlePacket(EntityPlayer player, boolean isServer) {

		try {
			int type = getByte();

			switch (PacketTypes.values()[type]) {
			case RS_POWER_UPDATE:
				int coords[] = getCoords();
				IRedstoneControl rs = (IRedstoneControl) player.worldObj.getTileEntity(new BlockPos(coords[0], coords[1], coords[2]));
				rs.setPowered(getBool());
				return;
			case RS_CONFIG_UPDATE:
				coords = getCoords();
				rs = (IRedstoneControl) player.worldObj.getTileEntity(new BlockPos(coords[0], coords[1], coords[2]));
				rs.setControl(ControlMode.values()[getByte()]);
				return;
			case SECURITY_UPDATE:
				if (player.openContainer instanceof ISecurable) {
					((ISecurable) player.openContainer).setAccess(AccessMode.values()[getByte()]);
				}
				return;
			case TAB_AUGMENT:
				if (player.openContainer instanceof IAugmentableContainer) {
					((IAugmentableContainer) player.openContainer).setAugmentLock(getBool());
				}
				return;
			case TAB_SCHEMATIC:
				if (player.openContainer instanceof ISchematicContainer) {
					((ISchematicContainer) player.openContainer).writeSchematic();
				}
				return;
			case CONFIG_SYNC:
				ThermalExpansion.instance.handleConfigSync(this);
				return;
			default:
				ThermalExpansion.LOG.error("Unknown Packet! Internal: TE_PH, ID: " + type);
			}
		} catch (Exception e) {
			ThermalExpansion.LOG.error("Packet payload failure! Please check your configuration!");
			e.printStackTrace();
		}
	}

	public static void sendRSPowerUpdatePacketToClients(IRedstoneControl rs, World world, BlockPos pos) {

		PacketHandler.sendToAllAround(getPacket(PacketTypes.RS_POWER_UPDATE).addCoords(pos).addBool(rs.isPowered()), world, pos);
	}

	public static void sendRSConfigUpdatePacketToServer(IRedstoneControl rs, BlockPos pos) {

		PacketHandler.sendToServer(getPacket(PacketTypes.RS_CONFIG_UPDATE).addCoords(pos).addByte(rs.getControl().ordinal()));
	}

	public static void sendSecurityPacketToServer(ISecurable securable) {

		PacketHandler.sendToServer(getPacket(PacketTypes.SECURITY_UPDATE).addByte(securable.getAccess().ordinal()));
	}

	public static void sendTabAugmentPacketToServer(boolean lock) {

		PacketHandler.sendToServer(getPacket(PacketTypes.TAB_AUGMENT).addBool(lock));
	}

	public static void sendTabSchematicPacketToServer() {

		PacketHandler.sendToServer(getPacket(PacketTypes.TAB_SCHEMATIC));
	}

	public static void sendConfigSyncPacketToClient(EntityPlayer player) {

		PacketHandler.sendTo(ThermalExpansion.instance.getConfigSync(), player);
	}

	public static PacketCoFHBase getPacket(PacketTypes theType) {

		return new PacketTEBase().addByte(theType.ordinal());
	}

}
