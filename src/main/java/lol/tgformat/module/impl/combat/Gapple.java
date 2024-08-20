package lol.tgformat.module.impl.combat;

import lol.tgformat.api.event.Listener;
import lol.tgformat.events.TickEvent;
import lol.tgformat.events.render.Render2DEvent;
import lol.tgformat.module.Module;
import lol.tgformat.module.ModuleManager;
import lol.tgformat.module.ModuleType;
import lol.tgformat.module.values.impl.BooleanSetting;
import lol.tgformat.module.values.impl.NumberSetting;
import lol.tgformat.ui.font.FontUtil;
import lol.tgformat.utils.client.LogUtil;
import lol.tgformat.component.MovementComponent;
import lol.tgformat.component.PacketStoringComponent;
import lol.tgformat.utils.network.PacketUtil;
import lol.tgformat.utils.player.InventoryUtil;
import lol.tgformat.utils.render.GlowUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.*;
import net.netease.font.FontManager;
import net.netease.utils.RoundedUtils;

import java.awt.*;

import static lol.tgformat.module.impl.combat.KillAura.target;

/**
 * @author TG_format
 * @since 2024/7/28 下午8:26
 */
public class Gapple extends Module {
    public NumberSetting duringSendTicks = new NumberSetting("DuringSendTicks", 1, 10,0,1);
    public NumberSetting delay = new NumberSetting("Delay", 9, 10,0,1);
    public BooleanSetting auto = new BooleanSetting("Auto", false);
    private final boolean stopMove = true;
    public boolean noCancelC02 = false;
    public boolean noC02 = false;
    private int slot = -1;
    private int c03s = 0;
    private int c02s = 0;
    private boolean canStart = false;
    public static boolean eating = false;
    public static boolean pulsing = false;
    public static boolean restart = false;

    public Gapple() {
        super("Gapple", ModuleType.Misc);
    }

    @Override
    public void onEnable() {
        this.c03s = 0;
        this.slot = InventoryUtil.findItem(36, 45, Items.golden_apple);
        if (this.slot != -1) {
            this.slot -= 36;
        }
    }

    @Override
    public void onDisable() {
        eating = false;

        if (this.canStart) {
            pulsing = false;
            PacketStoringComponent.stopStoringPackets();
        }
        if (this.stopMove) {
            MovementComponent.resetMove();
        }
    }

    @Listener
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.thePlayer.isDead) {
            PacketStoringComponent.stopStoringPackets();
            this.setState(false);
            return;
        }
        if (this.slot == -1) {
            LogUtil.addChatMessage("没苹果玩你妈逼");
            this.setState(false);
            return;
        }
        if (eating) {
            if (this.stopMove) {
                MovementComponent.cancelMove();
            }
            if (!PacketStoringComponent.storing) {
                PacketStoringComponent.startStoringPackets(C09PacketHeldItemChange.class, C0EPacketClickWindow.class, C0DPacketCloseWindow.class);
                PacketStoringComponent.setCanelPackets(C07PacketPlayerDigging.class, it -> ((C07PacketPlayerDigging)it).getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM);
                PacketStoringComponent.setCanelPackets(C08PacketPlayerBlockPlacement.class, it -> ((C08PacketPlayerBlockPlacement)it).getPosition().getY() == -1);
                PacketStoringComponent.setCanelPackets(C02PacketUseEntity.class, it -> this.noCancelC02);
                PacketStoringComponent.setCanelPackets(C0APacketAnimation.class, it -> this.noCancelC02);
                PacketStoringComponent.setCancelAction(C03PacketPlayer.class, packet -> ++this.c03s);
                PacketStoringComponent.setReleaseAction(C03PacketPlayer.class, packet -> --this.c03s);
                PacketStoringComponent.setReleaseMap(C02PacketUseEntity.class, packet -> !eating && this.noC02);
                PacketStoringComponent.setCancelAction(C02PacketUseEntity.class, packet -> ++this.c02s);
                PacketStoringComponent.setReleaseAction(C02PacketUseEntity.class, packet -> --this.c02s);
                this.canStart = true;
            }
        } else {
            eating = true;
        }
        if (this.c03s >= 32) {
            eating = false;
            pulsing = true;
            PacketStoringComponent.resetBlackList();
            PacketStoringComponent.send(new C09PacketHeldItemChange(this.slot), true);
            PacketStoringComponent.send(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventoryContainer.getSlot(this.slot + 36).getStack()), true);
            PacketStoringComponent.stopStoringPackets();
            PacketStoringComponent.send(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem), true);
            pulsing = false;
            this.setState(false);
            LogUtil.addChatMessage("Eat");
            if (auto.isEnabled()){
                if (target.getName()!=null){
                    LogUtil.addChatMessage("Stop");
                    restart = true;
                    if (restart){
                        setState(true);
                        restart = false;
                        LogUtil.addChatMessage("Restart");
                    }
                }
            }else {
                restart = false;
            }
            return;
        }
        if (mc.thePlayer.ticksExisted % delay.getValue() == 0) {
            for (int i = 0; i < duringSendTicks.getValue(); ++i) {
                PacketStoringComponent.releasePacket(true);
            }
        }
    }
    @Listener
    public void onRender2D(Render2DEvent event) {
        float target = (float)(120.0f * (this.c03s / 32.0)) * ((float) 100 / 120);
        int startX = sr.getScaledWidth() / 2 - 68;
        int startY = sr.getScaledHeight() / 2 + 30;
        GlStateManager.disableAlpha();
        String text = target + "%";
        FontUtil.tenacityFont18.drawString(text, startX + 10 + 60 - FontUtil.tenacityFont18.getStringWidth(text) / 2, startY + 20, new Color(225, 225, 225, 100).getRGB());
        RoundedUtils.drawGradientRound(startX + 10, (float) (startY + 7.5), 120.0f, 2.0f, 3.0f, new Color(0, 0, 0, 200), new Color(0, 0, 0, 150), new Color(0, 0, 0, 150), new Color(0, 0, 0, 150));
        RoundedUtils.drawGradientRound(startX + 10, (float) (startY + 7.5), Math.min(target, 120.0f), 2.0f, 3.0f,new Color(241, 59, 232, 170), new Color(241, 59, 232, 170), new Color(241, 59, 232, 170), new Color(241, 59, 232, 170));
        GlStateManager.disableAlpha();
    }
}