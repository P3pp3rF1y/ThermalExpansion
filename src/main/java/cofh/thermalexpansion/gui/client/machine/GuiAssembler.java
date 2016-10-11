package cofh.thermalexpansion.gui.client.machine;

import cofh.lib.gui.element.ElementBase;
import cofh.lib.gui.element.ElementEnergyStored;
import cofh.lib.gui.element.ElementFluidTank;
import cofh.thermalexpansion.core.TEProps;
import cofh.thermalexpansion.gui.client.GuiAugmentableBase;
import cofh.thermalexpansion.gui.container.ISchematicContainer;
import cofh.thermalexpansion.gui.container.machine.ContainerAssembler;
import cofh.thermalexpansion.gui.element.ElementSlotOverlay;
import cofh.thermalexpansion.gui.element.ElementSlotOverlayAssembler;
import cofh.thermalexpansion.gui.element.TabSchematic;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class GuiAssembler extends GuiAugmentableBase {

	static final ResourceLocation TEXTURE = new ResourceLocation(TEProps.PATH_GUI_MACHINE + "Assembler.png");

	ElementSlotOverlayAssembler[] slotInput = new ElementSlotOverlayAssembler[3];
	ElementBase slotOutput;
	ElementBase slotInputFluid;

	public GuiAssembler(InventoryPlayer inventory, TileEntity tile) {

		super(new ContainerAssembler(inventory, tile), tile, inventory.player, TEXTURE);

		generateInfo("tab.thermalexpansion.machine.assembler", 3);

		ySize = 205;
	}

	@Override
	public void initGui() {

		super.initGui();

		addTab(new TabSchematic(this, (ISchematicContainer) inventorySlots));

		slotInput[0] = (ElementSlotOverlayAssembler) addElement(new ElementSlotOverlayAssembler(this, 8, 74).setSlotInfo(0, 4));
		slotInput[1] = (ElementSlotOverlayAssembler) addElement(new ElementSlotOverlayAssembler(this, 8, 74).setSlotInfo(4, 0));
		slotInput[2] = (ElementSlotOverlayAssembler) addElement(new ElementSlotOverlayAssembler(this, 8, 74).setSlotInfo(5, 1));

		slotInputFluid = addElement(new ElementSlotOverlay(this, 152, 9).setSlotInfo(0, 3, 2));
		slotOutput = addElement(new ElementSlotOverlay(this, 112, 31).setSlotInfo(3, 1, 2));

		addElement(new ElementEnergyStored(this, 8, 8, myTile.getEnergyStorage()));
		addElement(new ElementFluidTank(this, 152, 9, myTile.getTank()).setGauge(1).setAlwaysShow(true));
	}

	@Override
	protected void updateElementInformation() {

		super.updateElementInformation();

		slotInput[0].setVisible(myTile.hasSide(1));
		slotInput[1].setVisible(myTile.hasSide(3));
		slotInput[2].setVisible(myTile.hasSide(4));
		slotInputFluid.setVisible(myTile.hasSide(1) || myTile.hasSide(3) || myTile.hasSide(4));

		slotOutput.setVisible(myTile.hasSide(2));

		if (!myTile.hasSide(1)) {
			slotInput[1].slotRender = 0;
			slotInput[2].slotRender = 1;
		} else {
			slotInput[1].slotRender = 2;
			slotInput[2].slotRender = 3;
		}
	}
}
