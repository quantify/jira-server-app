package com.quantify.avory.plugins;

import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PluginDataManagerImpl implements PluginDataManager {

    private final static String UUID_KEY = "uuid";

    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;

    @Autowired
    public PluginDataManagerImpl(PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @Override
    public String getUUID() {
        PluginSettings globalSettings = pluginSettingsFactory.createGlobalSettings();
        return (String) globalSettings.get(UUID_KEY);
    }

    @Override
    public void setUUID(String uuid) {
        PluginSettings globalSettings = pluginSettingsFactory.createGlobalSettings();
        globalSettings.put(UUID_KEY, uuid);
    }

    @Override
    public void deleteUUID() {
        PluginSettings globalSettings = pluginSettingsFactory.createGlobalSettings();
        globalSettings.remove(UUID_KEY);
    }

}
