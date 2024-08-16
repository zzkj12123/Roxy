package lol.tgformat.ui.clickgui;

import lol.tgformat.Client;
import lol.tgformat.ui.font.CustomFont;
import lol.tgformat.ui.utils.Animation;
import lol.tgformat.ui.utils.DecelerateAnimation;
import lol.tgformat.ui.utils.Direction;
import lol.tgformat.ui.utils.TooltipObject;
import lombok.Getter;
import lombok.Setter;
import net.netease.utils.ColorUtil;

import java.awt.*;

/**
 * @author TG_format
 * @since 2024/6/9 下午8:24
 */
@Setter
public class IconButton implements Screen {
    @Getter
    private float x, y, alpha;
    public Color accentColor, textColor = new Color(191, 191, 191);
    private Runnable clickAction;
    private boolean clickable = true;
    private CustomFont iconFont = iconFont16;

    private final Animation hoverAnimation = new DecelerateAnimation(250, 1);

    @Getter
    private String icon;

    private TooltipObject tooltip;


    public IconButton(String icon) {
        this.icon = icon;
    }

    public IconButton(String icon, String tooltip) {
        this.icon = icon;
        this.tooltip = new TooltipObject(tooltip);
    }


    @Override
    public void initGui() {

    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        float iconWidth = iconFont.getStringWidth(icon);
        float iconHeight = iconFont.getHeight();
        boolean hovering = SideGUI.isHovering(x - 3, y - 3, iconWidth + 6, iconHeight + 6, mouseX, mouseY);
        hoverAnimation.setDirection(hovering ? Direction.FORWARDS : Direction.BACKWARDS);

        Color iconColor = ColorUtil.interpolateColorC(textColor, accentColor, hoverAnimation.getOutput().floatValue());

        iconFont.drawString(icon, x, y, ColorUtil.applyOpacity(iconColor, alpha));

        if (tooltip != null) {
            Client.instance.getSideGui().addTooltip(tooltip);
            tooltip.setHovering(hovering);
        }


    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        boolean hovering = SideGUI.isHovering(x - 3, y - 3, iconFont16.getStringWidth(icon) + 6, iconFont16.getHeight() + 6, mouseX, mouseY);
        if (clickable && button == 0 && hovering && clickAction != null) {
            clickAction.run();
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {

    }

    public float getWidth() {
        return iconFont.getStringWidth(icon);
    }

    public float getHeight() {
        return iconFont.getHeight();
    }

    public void setTooltip(String tooltipText) {
        if (tooltip == null) {
            tooltip = new TooltipObject(tooltipText);
        } else {
            tooltip.setTip(tooltipText);
        }
    }

}

