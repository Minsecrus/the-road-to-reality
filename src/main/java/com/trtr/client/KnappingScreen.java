package com.trtr.client;

import com.trtr.TRTRItems;
import com.trtr.crafting.KnappingMenu;
import com.trtr.crafting.KnappingPattern;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** The target blade is marked; exposed unwanted cells can be chipped away. No inventory slots to desync. */
public final class KnappingScreen extends AbstractContainerScreen<KnappingMenu> {
    private static final int CELL = 20;
    private static final int GRID_X = 18;
    private static final int GRID_Y = 36;

    public KnappingScreen(KnappingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 236;
        imageHeight = 182;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("menu.trtr.new_flint"),
                button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId, KnappingMenu.NEW_FLINT))
                .bounds(leftPos + 128, topPos + 112, 98, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> onClose())
                .bounds(leftPos + 128, topPos + 138, 98, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF25282B);
        graphics.fill(leftPos + 1, topPos + 1, leftPos + imageWidth - 1, topPos + imageHeight - 1, 0xFFD1C9B8);
        for (int cell = 0; cell < 25; cell++) {
            int x = leftPos + GRID_X + cell % 5 * CELL;
            int y = topPos + GRID_Y + cell / 5 * CELL;
            boolean present = (menu.mask() & (1 << cell)) != 0;
            boolean target = (KnappingPattern.BLADE & (1 << cell)) != 0;
            int color = !present ? 0xFFB0A896 : target ? 0xFF656D72 : 0xFF938D82;
            graphics.fill(x, y, x + CELL - 1, y + CELL - 1, color);
            if (target) graphics.renderOutline(x + 2, y + 2, CELL - 5, CELL - 5, present ? 0xFFD8E3DE : 0xFFAC4139);
            if (present && mouseX >= x && mouseX < x + CELL && mouseY >= y && mouseY < y + CELL) {
                graphics.renderOutline(x, y, CELL - 1, CELL - 1, 0xFFFFFFFF);
            }
        }
        graphics.renderItem(TRTRItems.FLINT_BLADE.get().getDefaultInstance(), leftPos + 166, topPos + 43);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 12, 12, 0xFF25282B, false);
        graphics.drawWordWrap(font, Component.translatable("menu.trtr.knapping_hint"), 128, 67, 98, 0xFF25282B);
        String key = !menu.hasMaterial() ? "menu.trtr.knapping_done"
                : KnappingPattern.ruined(menu.mask()) ? "menu.trtr.knapping_ruined" : "menu.trtr.knapping_edge";
        graphics.drawWordWrap(font, Component.translatable(key), 18, 143, 102, 0xFF25282B);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (int) mouseX - leftPos - GRID_X;
        int y = (int) mouseY - topPos - GRID_Y;
        if (button == 0 && x >= 0 && y >= 0 && x < 5 * CELL && y < 5 * CELL) {
            if (minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, y / CELL * 5 + x / CELL);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }
}
