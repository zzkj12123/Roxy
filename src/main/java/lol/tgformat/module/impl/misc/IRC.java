package lol.tgformat.module.impl.misc;

import lol.tgformat.Client;
import lol.tgformat.api.event.Listener;
import lol.tgformat.events.WorldEvent;
import lol.tgformat.module.Module;
import lol.tgformat.module.ModuleType;
import lol.tgformat.utils.client.LogUtil;
import tech.skidonion.obfuscator.annotations.Renamer;
import tech.skidonion.obfuscator.annotations.StringEncryption;

import java.io.IOException;

/**
 * @author TG_format
 * @since 2024/7/13 下午5:25
 */
@Renamer
@StringEncryption
public class IRC extends Module {
    public IRC() {
        super("IRC", ModuleType.Misc);
    }
    @Override
    public void onEnable() {
        Client.instance.getExecutor().execute(() -> {
            try {
                Client.instance.getIrcServer().getClient().connect("123.136.94.3", 45600);
            } catch (IOException e) {
                LogUtil.print("Failed to connect to the server: " + e.getMessage());
            }
        });
    }
    @Override
    public void onDisable() {
        Client.instance.getIrcServer().getClient().close();
    }
    @Listener
    public void onWorldChange(WorldEvent e) {
        //TODO: 发送IGN数据到IRC服务器
    }
}
