package lol.tgformat.ui.clickgui;

import lol.tgformat.module.ModuleType;
import lol.tgformat.ui.utils.Animation;
import lol.tgformat.ui.utils.DecelerateAnimation;
import lol.tgformat.ui.utils.Direction;
import lol.tgformat.ui.utils.EaseInOutQuad;
import net.minecraft.client.renderer.GlStateManager;
import net.netease.utils.ColorUtil;
import net.netease.utils.RenderUtil;

import java.awt.*;

/**
 * @author TG_format
 * @since 2024/6/9 下午7:04
 */
public class CategoryButton extends Component {
    public final ModuleType category;
    public ModuleType currentCategory;
    private Animation hoverAnimation;
    private Animation enableAnimation;
    public Animation expandAnimation;

    public CategoryButton(ModuleType category) {
        this.category = category;
    }

    @Override
    public void initGui() {
        hoverAnimation = new EaseInOutQuad(200, 1);
        enableAnimation = new DecelerateAnimation(250, 1);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {

        boolean hovering = RenderUtil.isHovering(x, y - 3, 83 -
                (expandAnimation.getDirection().forwards() ? 62 : 0), 18, mouseX, mouseY);

        hoverAnimation.setDirection(hovering ? Direction.FORWARDS : Direction.BACKWARDS);
        hoverAnimation.setDuration(hovering ? 200 : 350);
        enableAnimation.setDirection(currentCategory == category ? Direction.FORWARDS : Direction.BACKWARDS);

        int color = ColorUtil.interpolateColor(new Color(68, 71, 78), new Color(115, 115, 115), (float) hoverAnimation.getOutput().floatValue());
        color = ColorUtil.interpolateColor(new Color(color), new Color(-1), (float) enableAnimation.getOutput().floatValue());



        GlStateManager.color(1, 1, 1);
        GlStateManager.color(1, 1, 1);


        float xDiff = 10 * expandAnimation.getOutput().floatValue();
        tenacityFont24.drawString(category.name(), x + 27 + xDiff, y, color);


    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        boolean hovering = RenderUtil.isHovering(x, y - 3, 83 -
                (expandAnimation.getDirection().forwards() ? 62 : 0), 18, mouseX, mouseY);
        this.hovering = hovering && button == 0;

    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {

    }
}

