package com.quantify.avory.plugins;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
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

@Component
public class InstallAndUnInstallEventListener implements InitializingBean, DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(InstallAndUnInstallEventListener.class);
    private final static String PLUGIN_KEY = "com.quantify.avory.plugins.limited-plugin";
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

    /**
     * Called when the plugin has been enabled.
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("Enabling plugin");
        eventPublisher.register(this);
    }

    /**
     * Called when the plugin is being disabled or removed.
     *
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception {
        log.info("Disabling plugin");
        eventPublisher.unregister(this);
    }

    /**
     * Called after plugins finish initializing.
     * Starts first installation routine.
     */
    @EventListener
    public void onPluginInstall(final PluginEnabledEvent pluginEnabledEvent) {
        String startUpPluginKey = pluginEnabledEvent.getPlugin().getKey();
        if (PLUGIN_KEY.equals(startUpPluginKey)) {
            String uniqueID = UUID.randomUUID().toString();
            System.out.println("UUID: " + uniqueID);

            persistData(uniqueID);
            sendIDToService(uniqueID);
        }

    }

    /**
     * Sends generated ID to an external service
     */
    private void sendIDToService(String id) {

        HttpResponse<JsonNode> response = Unirest.post(postmanURL)
                .body("UUID: " + id)
                .asEmpty();

        if (response.getStatus() == 200) {
            System.out.println("Success!! Status Code: " + response.getStatusText());
        } else {
            System.out.println("Failure!! Status Code: " + response.getStatusText());
        }

    }

    /**
     * Persists the generated ID so it can be used in other componenets
     */
    private void persistData(String id) {

        PluginSettings globalSettings = pluginSettingsFactory.createGlobalSettings();
        globalSettings.put("uuid", id);

        System.out.println("persistData here");
    }
}

