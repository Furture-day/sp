package chao.android.gradle.plugin.dependencies

import chao.android.gradle.plugin.base.PluginException
import chao.android.gradle.plugin.util.StringUtils
import org.gradle.api.initialization.Settings
import org.gradle.initialization.DefaultSettings

import java.util.function.Function
import java.util.stream.Collectors

/**
 * @author qinchao
 * @since 2019/5/29
 */
class ModuleHandler {

    private DefaultSettings settings

    private Map<String, ModuleBuilder> builders

    private static ModuleHandler sInstance

    static ModuleHandler instance() {
        if (sInstance == null) {
            synchronized (ModuleHandler.class) {
                if (sInstance == null) {
                    sInstance = new ModuleHandler()
                }
            }
        }
        return sInstance
    }

    private ModuleHandler() {
        builders = new HashMap<>()
    }

    void setSettings(DefaultSettings settings) {
        this.settings = settings
    }

    DefaultSettings getSettings() {
        return this.settings
    }

    ModuleBuilder module(String moduleName, String remoteName, String projectName) {
        checkModuleName(moduleName)
        if (StringUtils.isEmpty(moduleName)) {
            throw new PluginException("invilid module: ${moduleName} -> ${remoteName} -> ${projectName}" )
        }
        ModuleBuilder moduleBuilder = new ModuleBuilder(this)
        moduleBuilder.name(moduleName).remote(remoteName).project(projectName)
        if (StringUtils.isEmpty(remoteName)) {
            project(moduleName, projectName)
        } else if (StringUtils.isEmpty(projectName)) {
            remote(moduleName, remoteName)
        }
        builders.put(moduleName, moduleBuilder)
        return moduleBuilder
    }

    ModuleBuilder project(String moduleName, String project) {
        checkModuleName(moduleName)
        if (StringUtils.isEmpty(project)) {
            throw new PluginException("invilid module: ${moduleName} -> ${project}" )
        }
        ModuleBuilder moduleBuilder = builders.get(moduleName)
        if (!moduleBuilder) {
            moduleBuilder = new ModuleBuilder(this)
            builders.put(moduleName, moduleBuilder)
        }
        moduleBuilder.name(moduleName).project(project)
        return moduleBuilder
    }

    ModuleBuilder module(String moduleName) {
        checkModuleName(moduleName)
        ModuleBuilder moduleBuilder = builders.get(moduleName)
        if (!moduleBuilder) {
            moduleBuilder = new ModuleBuilder(this)
            builders.put(moduleName, moduleBuilder)
        }
        moduleBuilder.name(moduleName)
        return moduleBuilder
    }

    ModuleBuilder module(String moduleName, String remoteName) {
        checkModuleName(moduleName)
        remote(moduleName, remoteName)
    }

    ModuleBuilder remote(String moduleName, String remoteName) {
        checkModuleName(moduleName)
        if (StringUtils.isEmpty(remoteName)) {
            throw new PluginException("invalid module: ${moduleName} -> ${remoteName}" )
        }
        ModuleBuilder moduleBuilder = builders.get(moduleName)
        if (!moduleBuilder) {
            moduleBuilder = new ModuleBuilder(this)
            builders.put(moduleName, moduleBuilder)
        }
        moduleBuilder.name(moduleName).remote(remoteName)
        return moduleBuilder
    }

    List<Module> getModules() {
        return new ArrayList<Module>(builders.values()).stream().map(new Function<ModuleBuilder, Module>() {
            @Override
            Module apply(ModuleBuilder moduleBuilder) {
                return moduleBuilder.build()
            }
        }).collect(Collectors.toList())

    }

    private static void checkModuleName(String name) {
        if (StringUtils.isEmpty(name)) {
            throw new RuntimeException("module name should not be empty.")
        }
        if (!name.matches("^[a-zA-Z0-9_]*\$")) {
            throw new RuntimeException("module name only contains (0-9, a-z, A-Z and _) characters") ///字符
        }
    }

}
