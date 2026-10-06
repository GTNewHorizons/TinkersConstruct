package tconstruct.tools.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
enum StationTheme {

    WOOD(0, 0, 18, 180, 6),
    METAL(83, 7, 36, 198, 12);

    /** The panel skin's row in panel.png. */
    final int panelV;
    /** The beam's row in beams.png. */
    final int beamV;
    /** The hooks' column in toolstation.png. */
    final int hookU;
    /** The button frame's row in icons.png. */
    final int buttonV;
    /** The scroll bar's column in panel.png. */
    final int sliderU;

    StationTheme(int panelV, int beamV, int hookU, int buttonV, int sliderU) {
        this.panelV = panelV;
        this.beamV = beamV;
        this.hookU = hookU;
        this.buttonV = buttonV;
        this.sliderU = sliderU;
    }
}
