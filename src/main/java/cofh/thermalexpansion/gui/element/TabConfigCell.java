package cofh.thermalexpansion.gui.element;

import cofh.lib.gui.GuiBase;
import cofh.lib.gui.element.tab.TabBase;
import cofh.lib.gui.element.tab.TabConfiguration;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermalexpansion.block.cell.TileCell;

import java.util.List;

import cofh.thermalfoundation.core.TFProps;
import net.minecraft.client.gui.GuiScreen;

import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class TabConfigCell extends TabBase {

	TileCell myTile;

	public TabConfigCell(GuiBase gui, TileCell theTile) {

		this(gui, TabConfiguration.defaultSide, theTile);
	}

	public TabConfigCell(GuiBase gui, int side, TileCell theTile) {

		super(gui, side);

		headerColor = TabConfiguration.defaultHeaderColor;
		subheaderColor = TabConfiguration.defaultSubHeaderColor;
		textColor = TabConfiguration.defaultTextColor;
		backgroundColor = TabConfiguration.defaultBackgroundColor;

		maxHeight = 92;
		maxWidth = 100;
		myTile = theTile;
	}

	@Override
	public void addTooltip(List<String> list) {

		if (!isFullyOpened()) {
			list.add(StringHelper.localize("info.cofh.configuration"));
		}
	}

	@Override
	public boolean onMousePressed(int mouseX, int mouseY, int mouseButton) {

		if (!isFullyOpened()) {
			return false;
		}
		if (side == LEFT) {
			mouseX += currentWidth;
		}
		mouseX -= currentShiftX;
		mouseY -= currentShiftY;

		if (mouseX < 16 || mouseX >= 80 || mouseY < 20 || mouseY >= 84) {
			return false;
		}
		if (40 <= mouseX && mouseX < 56 && 24 <= mouseY && mouseY < 40) {
			handleSideChange(EnumFacing.UP, mouseButton);
		} else if (20 <= mouseX && mouseX < 36 && 44 <= mouseY && mouseY < 60) {
			handleSideChange(BlockHelper.ENUM_SIDE_LEFT[myTile.getFacing()], mouseButton);
		} else if (40 <= mouseX && mouseX < 56 && 44 <= mouseY && mouseY < 60) {
			handleSideChange(EnumFacing.VALUES[myTile.getFacing()], mouseButton);
		} else if (60 <= mouseX && mouseX < 76 && 44 <= mouseY && mouseY < 60) {
			handleSideChange(BlockHelper.ENUM_SIDE_RIGHT[myTile.getFacing()], mouseButton);
		} else if (40 <= mouseX && mouseX < 56 && 64 <= mouseY && mouseY < 80) {
			handleSideChange(EnumFacing.DOWN, mouseButton);
		} else if (60 <= mouseX && mouseX < 76 && 64 <= mouseY && mouseY < 80) {
			handleSideChange(BlockHelper.ENUM_SIDE_OPPOSITE[myTile.getFacing()], mouseButton);
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
		gui.drawTexturedModalRect(posX() + 16, posY + 20, 16, 20, 64, 64);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		gui.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		for (int i = 0; i < 3; i++) {
			gui.drawTextureMapIcon(myTile.getTexture(EnumFacing.UP, i), posX() + 40, posY + 24);
			gui.drawTextureMapIcon(myTile.getTexture(BlockHelper.ENUM_SIDE_LEFT[myTile.getFacing()], i), posX() + 20, posY + 44);
			gui.drawTextureMapIcon(myTile.getTexture(EnumFacing.VALUES[myTile.getFacing()], i), posX() + 40, posY + 44);
			gui.drawTextureMapIcon(myTile.getTexture(BlockHelper.ENUM_SIDE_RIGHT[myTile.getFacing()], i), posX() + 60, posY + 44);
			gui.drawTextureMapIcon(myTile.getTexture(EnumFacing.DOWN, i), posX() + 40, posY + 64);
			gui.drawTextureMapIcon(myTile.getTexture(BlockHelper.ENUM_SIDE_OPPOSITE[myTile.getFacing()], i), posX() + 60, posY + 64);
		}

		//TODO is this needed? face gets drawn in the 2nd pass (index 1) so why here again?
		//gui.drawIcon(myTile.getTexture(myTile.getFacing(), 3), posX() + 40, posY + 44);

	}

	@Override
	protected void drawTabForeground() {

		drawTabIcon(TabConfiguration.TAB_ICON);
		if (!isFullyOpened()) {
			return;
		}
		getFontRenderer().drawStringWithShadow(StringHelper.localize("info.cofh.configuration"), posXOffset() + 18, posY + 6, headerColor);
	}

	void handleSideChange(EnumFacing side, int mouseButton) {

		if (GuiScreen.isShiftKeyDown()) {
			if (side.getIndex() == myTile.getFacing()) {
				if (myTile.resetSides()) {
					GuiBase.playSound(SoundEvents.UI_BUTTON_CLICK, 0.2F);
				}
			} else if (myTile.setSide(side, 0)) {
				GuiBase.playSound(SoundEvents.UI_BUTTON_CLICK, 0.4F);
			}
			return;
		}
		if (mouseButton == 0) {
			if (myTile.incrSide(side)) {
				GuiBase.playSound(SoundEvents.UI_BUTTON_CLICK, 0.8F);
			}
		} else if (mouseButton == 1) {
			if (myTile.decrSide(side)) {
				GuiBase.playSound(SoundEvents.UI_BUTTON_CLICK, 0.6F);
			}
		}
	}

}
