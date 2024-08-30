package lol.tgformat.ui.clickgui;

import lol.tgformat.module.Module;
import lol.tgformat.module.ModuleType;
import lol.tgformat.module.impl.render.ArrayListMod;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author TG_format
 * @since 2024/6/9 下午7:37
 */
@Setter
public class ModuleCollection {

    public static boolean reloadModules;

    private HashMap<Object, Module> modules = new HashMap<>();

    public List<Module> getModules() {
        return new ArrayList<>(this.modules.values());
    }

    public HashMap<Object, Module> getModuleMap() {
        return modules;
    }

    public List<Module> getModulesInCategory(ModuleType c) {
        return this.modules.values().stream().filter(m -> m.getCategory() == c).collect(Collectors.toList());
    }

    public Module get(Class<? extends Module> mod) {
        return this.modules.get(mod);
    }

    public <T extends Module> T getModule(Class<T> mod) {
        return (T) this.modules.get(mod);
    }

    public List<Module> getModulesThatContainText(String text) {
        return this.getModules().stream().filter(m -> m.getName().toLowerCase().contains(text.toLowerCase())).collect(Collectors.toList());
    }

    public Module getModuleByName(String name) {
        return this.modules.values().stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<Module> getModulesContains(String text) {
        return this.modules.values().stream().filter(m -> m.getName().toLowerCase().contains(text.toLowerCase())).collect(Collectors.toList());
    }

    public final List<Module> getToggledModules() {
        return this.modules.values().stream().filter(Module::isState).collect(Collectors.toList());
    }


    public final List<Module> getArraylistModules(ArrayListMod arraylistMod, List<Module> modules) {
        return modules.stream().filter(module -> module.isState() && !(arraylistMod.importantModules.isEnabled() && module.getCategory().equals(ModuleType.Render))).collect(Collectors.toList());
    }


}

