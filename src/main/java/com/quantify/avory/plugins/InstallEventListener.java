package com.quantify.avory.plugins;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.event.events.PluginEnabledEvent;
import com.atlassian.plugin.spring.scanner.annotation.imports.JiraImport;
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

// FIXME: logger is not catching logs in this class, using System.out.print as a naive alternative

@Component
public class InstallEventListener implements InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(InstallEventListener.class);

    // TODO: Remove hardcoded values from testing
    private final static String PLUGIN_KEY = "com.quantify.avory.plugins.limited-plugin";
    private final static String postmanURL = "https://postman-echo.com/post";


    @JiraImport
    private final EventPublisher eventPublisher;
    private final PluginDataManager pluginDataManager;


    @Autowired
    public InstallEventListener(EventPublisher eventPublisher, PluginDataManager pluginDataManager) {
        this.eventPublisher = eventPublisher;
        this.pluginDataManager = pluginDataManager;
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
     */
    @EventListener
    public void onPluginInstall(final PluginEnabledEvent event) {

        // if global settings does not have a UUID,  generate and persist one
        if (isCurrentPlugin(event) &&  pluginDataManager.getUUID() == null) {

            String uniqueID = UUID.randomUUID().toString();
            log.debug("UUID: " + uniqueID);

            pluginDataManager.setUUID(uniqueID); // persist id to global settings
            sendIDToExternalService(uniqueID);

        } else if(isCurrentPlugin(event)){
            log.debug("Already has uuid: " + pluginDataManager.getUUID());

        }

    }

    //TODO: Refactor into another class
    private void sendIDToExternalService(String id) {

        HttpResponse<JsonNode> response = Unirest.post(postmanURL)
                .body("UUID: " + id)
                .asEmpty();

        if (response.getStatus() == 200) {
            log.debug("Success!! Status Code: " + response.getStatusText());
        } else {
            log.error("Failure!! Status Code: " + response.getStatusText());
        }

    }

    /**
     * Helper function to verify that that plugin event is this plugin
     *
     * Otherwise this event will fire for EVERY plugin on jira
     */
    private Boolean isCurrentPlugin(PluginEnabledEvent event){
        String startUpPluginKey = event.getPlugin().getKey();
        return (PLUGIN_KEY.equals(startUpPluginKey));
    }

}

