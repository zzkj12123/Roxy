package lol.tgformat.module.impl.render;

import com.mojang.realmsclient.gui.ChatFormatting;
import lol.tgformat.Client;
import lol.tgformat.api.event.Listener;
import lol.tgformat.events.PreUpdateEvent;
import lol.tgformat.events.render.Render2DEvent;
import lol.tgformat.module.Module;
import lol.tgformat.module.ModuleManager;
import lol.tgformat.module.ModuleType;
import lol.tgformat.module.impl.combat.KillAura;
import lol.tgformat.module.values.impl.*;
import lol.tgformat.ui.font.AbstractFontRenderer;
import lol.tgformat.ui.font.CustomFont;
import lol.tgformat.ui.font.FontUtil;
import lol.tgformat.ui.font.Pair;
import lol.tgformat.ui.hud.AnimationType;
import lol.tgformat.ui.utils.MathUtils;
import lol.tgformat.ui.utils.RoundedUtil;
import lol.tgformat.utils.math.MathUtil;
import lol.tgformat.utils.network.ServerUtil;
import lol.tgformat.utils.player.RomanNumeralUtil;
import lol.tgformat.utils.render.*;
import lol.tgformat.verify.GuiLogin;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.netease.font.FontManager;
import net.netease.utils.*;
import org.lwjgl.opengl.GL11;
import tech.skidonion.obfuscator.annotations.Renamer;
import tech.skidonion.obfuscator.annotations.StringEncryption;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

import static lol.tgformat.ui.clickgui.Utils.*;
import static lol.tgformat.utils.math.MathUtil.DF_1;
import static lol.tgformat.utils.render.Easing.EASE_OUT_ELASTIC;
import static lol.tgformat.utils.render.Easing.EASE_OUT_SINE;
import static net.netease.font.FontManager.*;

/**
 * @Author KuChaZi
 * @Date 2024/6/9 9:39
 * @ClassName: HUD
 */

@Renamer
@StringEncryption
public class HUD extends Module {
    public static final StringSetting clientName = new StringSetting("Client Name");
    public static final ModeSetting theme = Theme.getModeSetting("Theme Selection", "MINT_BLUE");
    private final BooleanSetting target = new BooleanSetting("TargetHUD", true);
    public final ModeSetting targetMode = new ModeSetting("TargetHUDMods","Naven","Acrimony", "Naven", "Lovely","Rise");
    private final NumberSetting X_post = new NumberSetting("TatgetHUD-X", 0,640,-520, 1);
    private final NumberSetting Y_post = new NumberSetting("TargetHUD-Y", 0,345,-305, 1);
    public final BooleanSetting hotBar = new BooleanSetting("HotBar", false);
    public static final BooleanSetting button = new BooleanSetting("Button", false);
    private final BooleanSetting health = new BooleanSetting("Health", false);
    private final BooleanSetting potion = new BooleanSetting("Potion", false);
    private static final BooleanSetting potionlow = new BooleanSetting("Lowercase", false);
    private final BooleanSetting armor = new BooleanSetting("ArmorHUD", true);
    private final BooleanSetting info = new BooleanSetting("Info", true);
    public static final ColorSetting color1 = new ColorSetting("Color-1", new Color(126, 0, 252, 203));
    public static final ColorSetting color2 = new ColorSetting("Color-2", new Color(191, 220, 238, 156));
    private static final Animation openingAnimation = new DecelerateAnimation(175, 1, Direction.BACKWARDS);
    private final Random random = new Random();
    private final lol.tgformat.ui.hud.Animation animation = new lol.tgformat.ui.hud.Animation();
    EntityPlayer targets;
    private int width = 0;
    public HUD() {
        super("HUD", ModuleType.Render);
        animation.setAnimDuration(400);
        animation.setAnimType(AnimationType.SLIDE);

        targetMode.addParent(target, a -> target.isEnabled());
        X_post.addParent(target, a -> target.isEnabled());
        Y_post.addParent(target, a -> target.isEnabled());

    }

    @Listener
    public void onRender2D(Render2DEvent event) {
        if (isNull()) return;
        HUD hud = ModuleManager.getModule(HUD.class);
        boolean inChat = mc.currentScreen instanceof GuiChat;
        if (hud.isState() && target.isEnabled()) {
            if (inChat) {
                animation.getTimer().setTimeElapsed(400);
                render(mc.thePlayer, true);

                targets = null;
            } else if (this.isState()) {
                boolean canRender = ModuleManager.getModule(KillAura.class).isState() && KillAura.getTarget() != null && KillAura.getTarget() instanceof EntityPlayer;

                if (ModuleManager.getModule(KillAura.class).isState() && KillAura.getTarget() != null && KillAura.getTarget() instanceof EntityPlayer) {
                    targets = (EntityPlayer) KillAura.getTarget();
                }


                render(targets, canRender);
            } else {
                animation.getTimer().setTimeElapsed(0);
            }
        }

        if (hotBar.isEnabled() && hud.isState()) {
            if (mc.theWorld != null) {
                int middleScreen = sr.getScaledWidth() / 2;
                RoundedUtils.drawRound(middleScreen - 91, sr.getScaledHeight() - 22, 182, 20, 3, new Color(0, 0, 0, 160));
                RoundedUtils.drawRound(middleScreen - 91 + mc.thePlayer.inventory.currentItem * 20, sr.getScaledHeight() - 22, 20, 20, 3, new Color(255, 255, 255, 80));
                GlStateManager.enableRescaleNormal();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                RenderHelper.enableGUIStandardItemLighting();

                for (int j = 0; j < 9; ++j) {
                    int k = sr.getScaledWidth() / 2 - 90 + j * 20 + 2;
                    int l = sr.getScaledHeight() - 16 - 3;
                    renderHotBarItem(j, k, l, event.getPartialTicks(), mc.thePlayer);
                }

                RenderHelper.disableStandardItemLighting();
                GlStateManager.disableRescaleNormal();
                GlStateManager.disableBlend();
            }
        }

        if (info.isEnabled() && hud.isState()) {
            float x2 = sr.getScaledWidth() - 3;
            float y2 = sr.getScaledHeight() - 10;
            String uname = GuiLogin.uid;
            String date = Client.instance.getDate();
            String version = Client.instance.getVersion();


            mc.fontRendererObj.drawStringWithShadow(ChatFormatting.GRAY + version + " - " + ChatFormatting.WHITE + date + ChatFormatting.GRAY + " - "  + uname,
                    x2 - mc.fontRendererObj.getStringWidth(ChatFormatting.GRAY + version + " - " + ChatFormatting.WHITE + date + ChatFormatting.GRAY + " - " + uname),
                    y2,
                    -1
            );
        }

        if (health.isEnabled() && hud.isState()) {
            renderHealth();
        }

        if (armor.isEnabled() && hud.isState()) {
            drawArmor(sr);
        }

        if (potion.isEnabled() && hud.isState()) {
            renderpotion();
        }
    }
    public static Color color(int tick) {
        return new Color(RenderUtil.colorSwitch(getClientColors().getFirst(), getClientColors().getSecond(),2000.0f, -(tick * 200) / 40, 75L, 2.0));
    }
    public static void drawLine(double x, double y, double width, double height, Color color){
        Gui.drawRect(x, y, x + width, y + height, color.getRGB());
    }
    public static int offsetValue = 0;
    private void renderpotion() {
        float yOffset = (float) (14.5 * openingAnimation.getOutput());
        List<PotionEffect> potions = new ArrayList<>(mc.thePlayer.getActivePotionEffects());
        potions.sort(Comparator.comparingDouble(e -> -fr.getStringWidth(I18n.format(e.getEffectName()))));

        int count = 0;
        for (PotionEffect effect : potions) {
            Potion potion = Potion.potionTypes[effect.getPotionID()];
            String name = I18n.format(potion.getName()) + (effect.getAmplifier() > 0 ? " " + RomanNumeralUtil.generate(effect.getAmplifier() + 1) : "");
            Color c = new Color(potion.getLiquidColor());
            String str = get(name + " §7- " + Potion.getDurationString(effect));
            fr.drawString(str, sr.getScaledWidth() - fr.getStringWidth(str) - 2,
                    -10 + sr.getScaledHeight() - fr.getHeight() + (7 - (10 * (count + 1))) - yOffset,
                    new Color(c.getRed(), c.getGreen(), c.getBlue(), 255).getRGB(), true);
            count++;
        }

        offsetValue = count * fr.getHeight();
    }

    public static String get(String text) {
        return potionlow.isEnabled() ? text.toLowerCase() : text;
    }
    
    private void renderHealth() {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        GuiScreen screen = mc.currentScreen;
        float absorptionHealth = mc.thePlayer.getAbsorptionAmount();
        String string = MathUtil.DF_1D.format(mc.thePlayer.getHealth() / 2.0F) + "§c❤ " + (absorptionHealth <= 0.0F ? "" : "§e" + MathUtil.DF_1D.format(absorptionHealth / 2.0F) + "§6❤");
        int offsetY = 0;
        if (mc.thePlayer.getHealth() >= 0.0F && mc.thePlayer.getHealth() < 10.0F || mc.thePlayer.getHealth() >= 10.0F && mc.thePlayer.getHealth() < 100.0F) {
            this.width = 3;
        }

        if (screen instanceof GuiInventory) {
            offsetY = 70;
        } else if (screen instanceof GuiContainerCreative) {
            offsetY = 80;
        } else if (screen instanceof GuiChest) {
            offsetY = ((GuiChest)screen).ySize / 2 - 15;
        }

        int x = (new ScaledResolution(mc)).getScaledWidth() / 2 - this.width;
        int y = (new ScaledResolution(mc)).getScaledHeight() / 2 + 25 + offsetY;
        Color color = Colors.blendColors(new float[]{0.0F, 0.5F, 1.0F}, new Color[]{new Color(255, 37, 0), Color.YELLOW, Color.GREEN}, mc.thePlayer.getHealth() / mc.thePlayer.getMaxHealth());
        mc.fontRendererObj.drawString(string, absorptionHealth > 0.0F ? (float)x - 15.5F : (float)x - 3.5F, (float)y, color.getRGB(), true);
        GL11.glPushMatrix();
        mc.getTextureManager().bindTexture(Gui.icons);
        this.random.setSeed((long) mc.ingameGUI.getUpdateCounter() * 312871L);
        float width = (float)scaledResolution.getScaledWidth() / 2.0F - mc.thePlayer.getMaxHealth() / 2.5F * 10.0F / 2.0F;
        float maxHealth = mc.thePlayer.getMaxHealth();
        int lastPlayerHealth = mc.ingameGUI.lastPlayerHealth;
        int healthInt = MathHelper.ceiling_float_int(mc.thePlayer.getHealth());
        int l2 = -1;
        boolean flag = mc.ingameGUI.healthUpdateCounter > (long) mc.ingameGUI.getUpdateCounter() && (mc.ingameGUI.healthUpdateCounter - (long) mc.ingameGUI.getUpdateCounter()) / 3L % 2L == 1L;
        if (mc.thePlayer.isPotionActive(Potion.regeneration)) {
            l2 = mc.ingameGUI.getUpdateCounter() % MathHelper.ceiling_float_int(maxHealth + 5.0F);
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        for(int i6 = MathHelper.ceiling_float_int(maxHealth / 2.0F) - 1; i6 >= 0; --i6) {
            int xOffset = 16;
            if (mc.thePlayer.isPotionActive(Potion.poison)) {
                xOffset += 36;
            } else if (mc.thePlayer.isPotionActive(Potion.wither)) {
                xOffset += 72;
            }

            int k3 = 0;
            if (flag) {
                k3 = 1;
            }

            float renX = width + (float)(i6 % 10 * 8);
            float renY = (float)scaledResolution.getScaledHeight() / 2.0F + 15.0F + (float)offsetY;
            if (healthInt <= 4) {
                renY += (float)this.random.nextInt(2);
            }

            if (i6 == l2) {
                renY -= 2.0F;
            }

            int yOffset = 0;
            if (mc.theWorld.getWorldInfo().isHardcoreModeEnabled()) {
                yOffset = 5;
            }

            Gui.drawTexturedModalRect2(renX, renY, 16 + k3 * 9, 9 * yOffset, 9, 9);
            if (flag) {
                if (i6 * 2 + 1 < lastPlayerHealth) {
                    Gui.drawTexturedModalRect2(renX, renY, xOffset + 54, 9 * yOffset, 9, 9);
                }

                if (i6 * 2 + 1 == lastPlayerHealth) {
                    Gui.drawTexturedModalRect2(renX, renY, xOffset + 63, 9 * yOffset, 9, 9);
                }
            }

            if (i6 * 2 + 1 < healthInt) {
                Gui.drawTexturedModalRect2(renX, renY, xOffset + 36, 9 * yOffset, 9, 9);
            }

            if (i6 * 2 + 1 == healthInt) {
                Gui.drawTexturedModalRect2(renX, renY, xOffset + 45, 9 * yOffset, 9, 9);
            }
        }

        GL11.glPopMatrix();
    }

    private void drawArmor(ScaledResolution sr) {
        List<ItemStack> equipment = new ArrayList<>();
        boolean inWater = mc.thePlayer.isEntityAlive() && mc.thePlayer.isInsideOfMaterial(Material.water);
        int x = -94;

        ItemStack armorPiece;
        for (int i = 3; i >= 0; i--) {
            if ((armorPiece = mc.thePlayer.inventory.armorInventory[i]) != null) {
                equipment.add(armorPiece);
            }
        }
        Collections.reverse(equipment);

        for (ItemStack itemStack : equipment) {
            armorPiece = itemStack;
            RenderHelper.enableGUIStandardItemLighting();
            x += 15;
            GlStateManager.pushMatrix();
            GlStateManager.disableAlpha();
            GlStateManager.clear(256);
            mc.getRenderItem().zLevel = -150.0F;
            int s = mc.thePlayer.capabilities.isCreativeMode ? 15 : 0;
            mc.getRenderItem().renderItemAndEffectIntoGUI(armorPiece, -x + sr.getScaledWidth() / 2 - 4,
                    (int) (sr.getScaledHeight() - (inWater ? 65 : 55) + s - (16 * openingAnimation.getOutput())));
            mc.getRenderItem().zLevel = 0.0F;
            GlStateManager.disableBlend();
            GlStateManager.disableDepth();
            GlStateManager.disableLighting();
            GlStateManager.enableDepth();
            GlStateManager.enableAlpha();
            GlStateManager.popMatrix();
            armorPiece.getEnchantmentTagList();
        }
    }

    private void render(EntityPlayer entity, boolean render) {
        ScaledResolution sr = new ScaledResolution(mc);
        int x = sr.getScaledWidth() / 2 - X_post.getValue().intValue();
        int y = sr.getScaledHeight() / 2 - Y_post.getValue().intValue();

        animation.updateState(render);
        animation.setAnimDuration(400);
        animation.setAnimType(AnimationType.SLIDE);

        if(entity == null) return;

        if(animation.isRendered() || !animation.isAnimDone()) {
            animation.render(() -> {
                switch (targetMode.getMode()) {
                    case "Lovely": {
                        float getMaxHel = Math.min(entity.getMaxHealth(), 20.0f);
                        float width = 5.9F;
                        float height = 56.0F;

                        GlowUtils.drawGlow(x, y, 120.0F, 30.0F, 6, new Color(0, 0, 0, 60));
                        DrawUtil.drawRoundedRect(x, y, x + 80 + 40, y + 34.5, 2.0, Integer.MIN_VALUE);
                        DrawUtil.drawRoundedRect(x, y, x, y, 0.0, new Color(255, 255, 255, 255).getRGB());
                        mc.fontRendererObj.drawStringWithShadow("Target: " + entity.getName(), x + 5, y + 5, new Color(255, 255, 255).getRGB());
                        mc.fontRendererObj.drawStringWithShadow("Health: " + DF_1.format(entity.getHealth()), x + 5.0F, y + 18.0F, Color.WHITE.getRGB());
                        mc.fontRendererObj.drawStringWithShadow(entity.getHealth() <= mc.thePlayer.getHealth() ? "W" : "L", x + 110, y + 14, entity.getHealth() <= mc.thePlayer.getHealth() ? Color.GREEN.getRGB() : Color.RED.getRGB());

                        DrawUtil.drawRoundedRect(x, y, x, y, 0.0, new Color(255, 255, 255, 255).getRGB());
                        entity.animatedHealthBar = AnimationUtil.animate(entity.animatedHealthBar, entity.getHealth(), 0.15f);

                        RoundedUtils.drawRound(x + 1.0F, y + height - 25.3F, entity.animatedHealthBar / entity.getMaxHealth() * Math.max(getMaxHel * width, (float) arial18.getStringWidth(entity.getName())), 3.0F, 0.0F, new Color(126, 0, 252, 203));
                        //  RoundedUtils.drawRound(x + 1.0F, y + height - 25.3F, entity.animatedHealthBar / entity.getMaxHealth() * Math.max(getMaxHel * width, (float)FontManager.arial18.getStringWidth(entity.getName())), 3.0F, 0.0F, new Color(126, 0, 252, 203));

                        // DrawUtil.drawRoundedRect(x, y, x, y, 0.0, new Color(255, 255, 255, 255).getRGB());

                        break;
                    }
                    case "Naven": {
//                        float getMaxHel = Math.min(entity.getMaxHealth(), 20.0f);
//                        float width = 6.4F;
//                        float height = 56.0F;
//                        GlowUtils.drawGlow(x, y, 140.0F, 58.0F, 6, new Color(0, 0, 0, 60));
//                        DrawUtil.drawRoundedRect(x, y, x + 120 + 20, y + 55, 6.0, Integer.MIN_VALUE);
//                        DrawUtil.drawRoundedRect(x, y, x, y, 0.0, new Color(255, 255, 255, 255).getRGB());
//
//                        FontManager.edit20.drawStringWithShadow(entity.getName(), x + 46, y + 12, new Color(255, 255, 255).getRGB());
//                        FontManager.arial16.drawStringWithShadow("Health: " + DF_1.format(entity.getHealth()), x + 46.0F, y + 24.0F, Color.WHITE.getRGB());
//                        FontManager.arial16.drawStringWithShadow("Distance: " + MathUtil.round(mc.thePlayer.getDistanceToEntity(entity), 1), x + 46.0F, y + 31.0F, Color.WHITE.getRGB());
//                        FontManager.arial16.drawStringWithShadow("Block: " + entity.isBlocking(), x + 46.0F, y + 38.0F, Color.WHITE.getRGB());//+ entity.isBlocking()
//
//                        DrawUtil.drawRoundedRect(x, y, x, y, 0.0, new Color(255, 255, 255, 255).getRGB());
//
//                        entity.animatedHealthBar = AnimationUtil.animate(entity.animatedHealthBar, entity.getHealth(), 0.25f);
//
//                        RoundedUtils.drawRound(x + 6.0F, y + height - 7.0F, entity.animatedHealthBar / entity.getMaxHealth() * Math.max(getMaxHel * width, (float)FontManager.arial18.getStringWidth(entity.getName())), 3.0F, 2.0F, new Color(160, 42, 42));
//
//                        DrawUtil.drawRoundedRect(x, y, x, y, 0.0, new Color(255, 255, 255, 255).getRGB());
//
//                        DrawUtil.drawHead(((AbstractClientPlayer)entity).getLocationSkin(), x + 6, y + 8, 36, 36);
                        float width = 130;
                        float height = 50;

                        RoundedUtil.drawRound(x, y, width, height, 5, new Color(10, 10, 30, 120));
                        DrawUtil.drawHead(((AbstractClientPlayer)entity).getLocationSkin(), x + 7, y + 7, 30, 30);
                        RoundedUtil.drawRound(x + 5, y + height - 7, (entity.getHealth() / entity.getMaxHealth()) * width - 10, 3, 2, new Color(160, 42, 42));
                        DecimalFormat decimalFormat = new DecimalFormat("0.00");
                        arial18.drawString(entity.getName(), x + 40, y + 10, -1);
                        arial16.drawString("Health: " + decimalFormat.format(entity.getHealth()), x + 40, y + 22, -1);
                        arial16.drawString("Distance: " + decimalFormat.format(entity.getDistanceToEntity(mc.thePlayer)), x + 40, y + 30, -1);

                        break;
                    }
                    case "Rise":{
                        float width;
                        float height;
                        height = 32;
                        width = Math.max(120.0F, arial20.getStringWidth(target.getName()) + 51.0F);
                        float healthPercentme = MathHelper.clamp_float((mc.thePlayer.getHealth() + mc.thePlayer.getAbsorptionAmount()) / (mc.thePlayer.getMaxHealth() + mc.thePlayer.getAbsorptionAmount()), 0, 1);
                        RoundedUtil.drawRound(x,y,width,height,5, new Color(10, 10, 30, 120));
                        RoundedUtil.drawGradientHorizontal(x + 34f, (y + height - 13), width - 37.2F, 8, 1, new Color(0, 0, 0, 150), new Color(0, 0, 0, 85));
                        RoundedUtil.drawGradientHorizontal(x + 34f, (y + height - 13), entity.getHealth() / entity.getMaxHealth() * (width - 37.2F), 8, 1, new Color(181, 112, 255, 203), new Color(190, 109, 255, 203));
                        final int scaleOffset = (int)(entity.hurtTime * 0.7f);
                        DrawUtil.drawHead(((AbstractClientPlayer)entity).getLocationSkin(), x + 3 + scaleOffset / 2, y + 2 + scaleOffset / 2,27 - scaleOffset, 27 - scaleOffset);
                        StencilUtil.uninitStencilBuffer();
                        GlStateManager.disableBlend();
                        arial20.drawString(entity.getName(), x + 33f, (float) (y + 3.5) + 1f, Color.WHITE.getRGB());
                        float healthPercent = MathHelper.clamp_float((entity.getHealth() + entity.getAbsorptionAmount()) / (entity.getMaxHealth() + entity.getAbsorptionAmount()), 0, 1);
                        String healthText = (int) MathUtils.round(healthPercent * 100, .01) + ".0%";
                        tenacityBoldFont16.drawString(healthText, x + 63, y + 20.8f, Color.WHITE.getRGB());

                        break;
                    }
                    case "Acrimony": {
                        DrawUtil.drawRoundedRect(x, y, x + 140 + 20, y + 50 + 10, 2.0, Integer.MIN_VALUE);
                        DrawUtil.drawRoundedRect(x, y, x, y, 0.0, new Color(255, 255, 255, 255).getRGB());
                        FontManager.arial16.drawStringWithShadow("" + Math.round((double)entity.getHealth() - 0.5), (float)x + 44.5f, y + 26, new Color(255, 255, 255).getRGB());
                        FontManager.arial10.drawStringWithShadow(Math.round(entity.posX - 0.5) + "", (float)x + 85.5f, (double)y + 28.4, new Color(255, 255, 255).getRGB());
                        FontManager.arial14.drawStringWithShadow("" + Math.round((double)mc.thePlayer.getDistanceToEntity(entity) - 0.5), (float)x + 67.2f, y + 26, new Color(255, 255, 255).getRGB());
                        RenderUtil.drawLoadingCircleNormal(x + 50, y + 29, new Color(246, 174, 90, 200));
                        RenderUtil.drawLoadingCircleFast(x + 50, y + 29, new Color(183, 211, 82, 200));
                        RenderUtil.drawLoadingCircleSlow(x + 70, y + 29, new Color(227, 103, 103, 200));
                        RenderUtil.drawLoadingCircleFast(x + 70, y + 29, new Color(182, 182, 84, 200));
                        RenderUtil.drawLoadingCircleNormal(x + 90, y + 29, new Color(199, 19, 19, 200));
                        DrawUtil.drawRoundedRect(x, y, x, y, 0.0, new Color(255, 255, 255, 255).getRGB());
                        Gui.drawRect(x, y + 50 + 8, x + 160, y + 50 + 10, new Color(1, 1, 1, 200).getRGB());
                        DrawUtil.drawRoundedRect(x, y, x, y, 0.0, new Color(255, 255, 255, 255).getRGB());
                        GuiInventory.drawEntityOnScreen(x + 22, y + 54, 26, -entity.rotationYaw, entity.rotationPitch, entity);
                        FontManager.arial26.drawStringWithShadow(entity.getName(), x + 44, y + 8, new Color(255, 255, 255).getRGB());
                        FontManager.arial26.drawStringWithShadow(entity.getHealth() <= mc.thePlayer.getHealth() ? "Winning" : "Losing", x + 44, y + 44, entity.getHealth() <= mc.thePlayer.getHealth() ? Color.GREEN.getRGB() : Color.RED.getRGB());
                        break;
                    }



                }

            }, x, y, x, y);
        }

    }


    private void renderHotBarItem(final int index, final int xPos, final int yPos, final float partialTicks, final EntityPlayer entityPlayer) {
        final ItemStack itemstack = entityPlayer.inventory.mainInventory[index];
        final RenderItem itemRenderer = mc.getRenderItem();

        if (itemstack != null) {
            final float f = (float) itemstack.animationsToGo - partialTicks;

            if (f > 0.0F) {
                GlStateManager.pushMatrix();
                final float f1 = 1.0F + f / 5.0F;
                GlStateManager.translate((float) (xPos + 8), (float) (yPos + 12), 0.0F);
                GlStateManager.scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                GlStateManager.translate((float) (-(xPos + 8)), (float) (-(yPos + 12)), 0.0F);
            }

            itemRenderer.renderItemAndEffectIntoGUI(itemstack, xPos, yPos);

            if (f > 0.0F) {
                GlStateManager.popMatrix();
            }

            itemRenderer.renderItemOverlays(mc.fontRendererObj, itemstack, xPos, yPos);
        }
    }

    
    public static String name() {
        String name = "Rise";
        if (!clientName.getString().isEmpty()) {
            name = clientName.getString().replace("%time%", new SimpleDateFormat("HH:mm").format(new Date()));
        }

        return name;
    }
    public static Pair<Color, Color> getClientColors() {
        return Theme.getThemeColors(theme.getMode());
    }
}