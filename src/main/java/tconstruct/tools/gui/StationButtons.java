package tconstruct.tools.gui;

import static tconstruct.tools.gui.InfoPanel.drawPanelHooks;
import static tconstruct.tools.gui.StationPanels.PANEL_X;
import static tconstruct.tools.gui.StationPanels.UPPER_Y;

import java.util.List;

import net.minecraft.client.gui.GuiButton;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import tconstruct.TConstruct;
import tconstruct.library.client.ToolGuiElement;
import tconstruct.util.network.ToolStationSelectionPacket;

/** The tool tab buttons, and the hooks that hang them and the side panels under the beam. */
@SideOnly(Side.CLIENT)
final class StationButtons {

    private StationButtons() {}

    /** The button column's width; the central panel starts this far right of guiLeft. */
    static final int COLUMN_W = 110;

    private static final int COLUMN_COUNT = 5, BUTTON_PITCH = 22, COLUMN_INSET = 2, BUTTON_TOP = 9;
    // the hook strip at (0, 248) of toolstation.png: a button's link in its top two rows, its hook in the bottom two,
    // and a panel's corner links at its 5 px and 9 px ends
    static final int HOOK_V = 248, HOOK_W = 18, HOOK_H = 2, PANEL_HOOK_L = 5, PANEL_HOOK_R = 9, PANEL_HOOK_H = 4,
            PANEL_HOOK_INSET = 5;

    /** A tab this screen has no button for falls back to Repair & Modify. */
    static void showRememberedTab(ToolStationGui screen) {
        for (Object o : screen.buttonList) {
            if (o instanceof GuiButtonTool button && button.element.tool == screen.toolSlots.selectedTool) {
                screen.showTab(button);
                return;
            }
        }
        screen.toolSlots.selectTool(null);
        TConstruct.packetPipeline.sendToServer(new ToolStationSelectionPacket(null));
    }

    static void createToolButtons(ToolStationGui screen) {
        List<ToolGuiElement> tabs = screen.tabs();
        for (int iter = 0; iter < tabs.size(); iter++) {
            ToolGuiElement element = tabs.get(iter);
            GuiButtonTool button = new GuiButtonTool(
                    iter,
                    screen.guiLeft + COLUMN_INSET + BUTTON_PITCH * (iter % COLUMN_COUNT),
                    screen.guiTop + BUTTON_TOP + BUTTON_PITCH * (iter / COLUMN_COUNT),
                    element.buttonIconX,
                    element.buttonIconY,
                    element.domain,
                    element.texture,
                    element);
            button.backgroundV = screen.theme().buttonV;
            screen.buttonList.add(button);
        }
    }

    /** TiC2's hooks over the tool buttons and at the panels' top corners. Expects toolstation.png bound. */
    static void drawHooks(ToolStationGui screen, StationTheme theme) {
        int u = theme.hookU, tools = 0;
        for (Object o : screen.buttonList) if (o instanceof GuiButtonTool) tools++;
        for (Object o : screen.buttonList) {
            if (!(o instanceof GuiButtonTool)) continue;
            GuiButton button = (GuiButton) o;
            int x = button.xPosition + (button.width - HOOK_W) / 2;
            screen.drawTexturedModalRect(x, button.yPosition - HOOK_H, u, HOOK_V + HOOK_H, HOOK_W, HOOK_H);
            // only a button with another one under it gets a link
            if (button.id < tools - COLUMN_COUNT) {
                screen.drawTexturedModalRect(x, button.yPosition + button.height, u, HOOK_V, HOOK_W, HOOK_H);
            }
        }
        int panelX = screen.guiLeft + PANEL_X;
        drawPanelHooks(screen, panelX, screen.guiTop + UPPER_Y, screen.panels.panelWidth, u);
        drawPanelHooks(screen, panelX, screen.guiTop + screen.panels.lowerY, screen.panels.panelWidth, u);
    }
}
