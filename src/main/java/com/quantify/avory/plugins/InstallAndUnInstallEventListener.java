package com.quantify.avory.plugins;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.event.events.PluginDisabledEvent;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

// TODO: Refactor class. HttpRequests and UUID persistence should happen outside an event listener
// FIXME: logger is not catching logs in this class, using System.out.print as a naive alternative

@Component
public class InstallAndUnInstallEventListener implements InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(InstallAndUnInstallEventListener.class);

    // TODO: Remove hardcoded values from testing
    private final static String PLUGIN_KEY = "com.quantify.avory.plugins.limited-plugin";
    private final static String UUID_KEY = "uuid";
    private final static String postmanURL = "https://postman-echo.com/post";


    @JiraImport
    private final EventPublisher eventPublisher;

    @ComponentImport
    private final PluginSettingsFactory pluginSettingsFactory;


    @Autowired
    public InstallAndUnInstallEventListener(EventPublisher eventPublisher, PluginSettingsFactory pluginSettingsFactory) {
        this.eventPublisher = eventPublisher;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Enabling plugin");
        eventPublisher.register(this);
    }

    @Override
    public void destroy() throws Exception {
        log.info("Disabling plugin");
        eventPublisher.unregister(this);

    }

    /**
     * Begins "enable/installation" routine of plugin.
     * Routine is as follows:
     *  1. Generate UUID
     *  2. Persist UUID to Global Settings for use in other components
     *  3. Send an HTTP request to external service
     *
     *  FIXME: Routine executes on enable and not just on install
     */
    @EventListener
    public void onPluginInstall(final PluginEnabledEvent pluginEnabledEvent) {
        PluginSettings globalSettings = pluginSettingsFactory.createGlobalSettings();
        String startUpPluginKey = pluginEnabledEvent.getPlugin().getKey();

        // IF global settings does not have a UUID generate and persist one
        if (PLUGIN_KEY.equals(startUpPluginKey) && globalSettings.get(UUID_KEY) == null) {

            String uniqueID = UUID.randomUUID().toString();
            System.out.println("UUID: " + uniqueID);

            persistPluginData(uniqueID);
            sendIDToExternalService(uniqueID);

        } else if(PLUGIN_KEY.equals(startUpPluginKey)){
            System.out.println("Already has uuid: " + globalSettings.get(UUID_KEY));

        }

    }

    //TODO: Refactor into another class
    private void sendIDToExternalService(String id) {

        HttpResponse<JsonNode> response = Unirest.post(postmanURL)
                .body("UUID: " + id)
                .asEmpty();

        if (response.getStatus() == 200) {
            System.out.println("Success!! Status Code: " + response.getStatusText());
        } else {
            System.out.println("Failure!! Status Code: " + response.getStatusText());
        }

    }

    //TODO: Refactor into another class
    private void persistPluginData(String id) {

        PluginSettings globalSettings = pluginSettingsFactory.createGlobalSettings();
        globalSettings.put(UUID_KEY, id);
    }
}

