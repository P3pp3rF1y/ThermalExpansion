package cofh.thermalexpansion.gui.element;

import cofh.lib.gui.GuiBase;
import cofh.lib.gui.GuiProps;
import cofh.lib.gui.element.tab.TabBase;
import cofh.lib.util.helpers.RenderHelper;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermalexpansion.core.TEProps;
import cofh.thermalexpansion.gui.container.ISchematicContainer;
import cofh.thermalexpansion.network.PacketTEBase;
import cofh.thermalfoundation.core.TFProps;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.List;

public class TabSchematic extends TabBase {

	public static int defaultSide = TabBase.LEFT;
	public static ResourceLocation GRID_TEXTURE = new ResourceLocation(TEProps.PATH_ELEMENTS + "slot_grid_schematic.png");
	public static ResourceLocation OUTPUT_TEXTURE = new ResourceLocation(TEProps.PATH_ELEMENTS + "slot_output_schematic.png");
	public static final ResourceLocation TAB_ICON = new ResourceLocation(TFProps.PATH_GFX + "items/tool/schematic.png");

	ISchematicContainer myContainer;

	public TabSchematic(GuiBase gui, ISchematicContainer theTile) {

		this(gui, defaultSide, theTile);
	}

	public TabSchematic(GuiBase gui, int side, ISchematicContainer theTile) {

		super(gui, side);

		myContainer = theTile;
		maxHeight = 92;
		maxWidth = 112;
		backgroundColor = 0x2020B0;

		for (int i = 0; i < myContainer.getCraftingSlots().length; i++) {
			myContainer.getCraftingSlots()[i].xDisplayPosition = -gui.getGuiLeft() - 16;
			myContainer.getCraftingSlots()[i].yDisplayPosition = -gui.getGuiTop() - 16;
		}
		myContainer.getResultSlot().xDisplayPosition = -gui.getGuiLeft() - 16;
		myContainer.getResultSlot().yDisplayPosition = -gui.getGuiTop() - 16;
	}

	@Override
	public void addTooltip(List<String> list) {

		if (!isFullyOpened()) {
			list.add(StringHelper.localize("item.thermalexpansion.diagram.schematic.name"));
		}
	}

	@Override
	public boolean onMousePressed(int mouseX, int mouseY, int mouseButton) throws IOException {

		if (!isFullyOpened()) {
			return false;
		}
		if (side == LEFT) {
			mouseX += currentWidth;
		}
		mouseX -= currentShiftX;
		mouseY -= currentShiftY;

		if (mouseX < 8 || mouseX >= 102 || mouseY < 20 || mouseY >= 84) {
			return false;
		}
		if (77 < mouseX && mouseX < 93 && 60 < mouseY && mouseY < 76) {

			if (myContainer.canWriteSchematic()) {
				writeSchematic();
			}
		} else {
			gui.mouseClicked(mouseButton);
		}
		return true;
	}

	@Override
	protected void drawTabBackground() {

		super.drawTabBackground();

		if (!isFullyOpened()) {
			return;
		}
		float colorR = (backgroundColor >> 16 & 255) / 255.0F * 0.6F;
		float colorG = (backgroundColor >> 8 & 255) / 255.0F * 0.6F;
		float colorB = (backgroundColor & 255) / 255.0F * 0.6F;
		GL11.glColor4f(colorR, colorG, colorB, 1.0F);
		gui.drawTexturedModalRect(8, 20, 16, 20, 94, 64);

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		gui.bindTexture(GRID_TEXTURE);
		gui.drawSizedTexturedModalRect(13, 25, 5, 5, 54, 54, 64, 64);
		gui.bindTexture(OUTPUT_TEXTURE);
		gui.drawSizedTexturedModalRect(72, 25, 3, 3, 26, 26, 32, 32);


		if (myContainer.canWriteSchematic()) {
			gui.drawIcon(GuiProps.ICON_BUTTON, 77, 60);
			gui.drawIcon(GuiProps.ICON_ACCEPT, 77, 60);
		} else {
			gui.drawIcon(GuiProps.ICON_BUTTON_INACTIVE, 77, 60);
			gui.drawIcon(GuiProps.ICON_ACCEPT_INACTIVE, 77, 60);
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	protected void drawTabForeground() {

		drawTabIcon(TAB_ICON);
		if (!isFullyOpened()) {
			return;
		}
		getFontRenderer().drawStringWithShadow(StringHelper.localize("item.thermalexpansion.diagram.schematic.name"), sideOffset() + 18, 6, headerColor);

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public void setFullyOpen() {

		super.setFullyOpen();
		for (int i = 0; i < myContainer.getCraftingSlots().length; i++) {
			myContainer.getCraftingSlots()[i].xDisplayPosition = posX() + 14 + 18 * (i % 3);
			myContainer.getCraftingSlots()[i].yDisplayPosition = posY + 26 + 18 * (i / 3);
		}
		myContainer.getResultSlot().xDisplayPosition = posX() + 77;
		myContainer.getResultSlot().yDisplayPosition = posY + 30;
	}

	@Override
	public void toggleOpen() {

		if (open) {
			for (int i = 0; i < myContainer.getCraftingSlots().length; i++) {
				myContainer.getCraftingSlots()[i].xDisplayPosition = -gui.getGuiLeft() - 16;
				myContainer.getCraftingSlots()[i].yDisplayPosition = -gui.getGuiTop() - 16;
			}
			myContainer.getResultSlot().xDisplayPosition = -gui.getGuiLeft() - 16;
			myContainer.getResultSlot().yDisplayPosition = -gui.getGuiTop() - 16;
		}
		super.toggleOpen();
	}

	private boolean writeSchematic() {

		if (myContainer.canWriteSchematic()) {
			PacketTEBase.sendTabSchematicPacketToServer();
			myContainer.writeSchematic();
			return true;
		}
		return false;
	}
}
