/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.serverconfig.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableMap;
import com.openexchange.capabilities.Capability;
import com.openexchange.java.Strings;
import com.openexchange.serverconfig.ClientServerConfigFilter;
import com.openexchange.serverconfig.NotificationMailConfig;
import com.openexchange.serverconfig.ServerConfig;

/**
 * {@link ServerConfigImpl}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.0
 */
public class ServerConfigImpl implements ServerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ServerConfigImpl.class);

    private final ImmutableMap<String, Object> mappings;
    private final List<ClientServerConfigFilter> clientServerConfigFilters;

    /**
     * Initializes a new {@link ServerConfigImpl}.
     *
     * @param mappings The collected mappings
     * @param clientServerConfigFilters The optional filters
     */
    public ServerConfigImpl(Map<String, Object> mappings, List<ClientServerConfigFilter> clientServerConfigFilters) {
        super();
        this.mappings = ImmutableMap.<String, Object> copyOf(mappings);
        this.clientServerConfigFilters = clientServerConfigFilters;
    }

    @Override
    public Map<String, Object> asMap() {
        return mappings;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<Capability> getCapabilities() {
        return (Set<Capability>) mappings.get("capabilities");
    }

    @Override
    public boolean isForceHttps() {
        return ((Boolean) mappings.get("forceHTTPS")).booleanValue();
    }

    @Override
    public String[] getHosts() {
        return (String[]) mappings.get("hosts");
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SimpleEntry<String, String>> getLanguages() {
        List<SimpleEntry<String,String>> retVal = new ArrayList<>();
        Object foundLanguages = mappings.get("languages");
        try {
            List<SimpleEntry<String, String>> languagesList = (List<SimpleEntry<String, String>>) mappings.get("languages");
            if (languagesList != null) {
                retVal = languagesList;
            }
        } catch (ClassCastException cce) {
            LOG.warn("Invalid language mapping found in ServerConfig. Expected List but got {}", foundLanguages.getClass(), cce);
        }
        return retVal;
    }

    @Override
    public String getServerVersion() {
        return (String) mappings.get("serverVersion");
    }

    @Override
    public String getServerBuildDate() {
        return (String) mappings.get("buildDate");
    }

    @Override
    public String getUIVersion() {
        return (String) mappings.get("version");
    }

    @Override
    public String getProductName() {
        String productName = (String) mappings.get("productName");
        if (Strings.isEmpty(productName)) {
            LOG.warn("No 'productName' config was found, please fix 'as-config.yml'. Falling back to default 'OX App Suite'.");
            return "OX App Suite";
        }

        return productName;
    }

    @Override
    public Map<String, Object> forClient() {
        Map<String, Object> mappingsForClient = new HashMap<>(mappings);

        // Apply possible filters
        for(ClientServerConfigFilter filter : clientServerConfigFilters) {
            filter.apply(mappingsForClient);
        }

        // Drop "notificationMails" settings
        mappingsForClient.remove("notificationMails");

        return mappingsForClient;
    }

    @SuppressWarnings("unchecked")
    @Override
    public NotificationMailConfig getNotificationMailConfig() {
        NotificationMailConfigImpl mailConfig = new NotificationMailConfigImpl();
        mailConfig.setButtonBackgroundColor("#3c61aa");
        mailConfig.setButtonBorderColor("#356697");
        mailConfig.setButtonTextColor("#ffffff");
        Map<String, Object> mailsMap = (Map<String, Object>) mappings.get("notificationMails");
        if (mailsMap == null) {
            LOG.warn("No 'notificationMails' config was found, please fix 'as-config.yml'. Falling back to default.");
        } else {
            Map<String, Object> buttonMap = (Map<String,Object>) mailsMap.get("button");
            if (buttonMap == null) {
                LOG.warn("No 'button' section was found for notification mails configuration, please fix 'as-config.yml'. Falling back to default.");
            } else {
                mailConfig.setButtonBackgroundColor(getColorCode(buttonMap, "backgroundColor", mailConfig.getButtonBackgroundColor()));
                mailConfig.setButtonBorderColor(getColorCode(buttonMap, "borderColor", mailConfig.getButtonBorderColor()));
                mailConfig.setButtonTextColor(getColorCode(buttonMap, "textColor", mailConfig.getButtonTextColor()));
            }

            Map<String, Object> footerMap = (Map<String,Object>) mailsMap.get("footer");
            if (footerMap != null) {
                mailConfig.setFooterImage((String) footerMap.get("image"));
                mailConfig.setFooterImageAltText(getProductName());
                mailConfig.setFooterText((String) footerMap.get("text"));
                // Hidden property. We can document this one if anybody starts asking for it. Until then
                // we continue to use the MIME structure as image container.
                mailConfig.setEmbedFooterImage(Boolean.parseBoolean((String) footerMap.get("embed")));
            }

            return mailConfig;
        }

        return mailConfig;
    }

    private static final Pattern COLOR_CODE = Pattern.compile("^\\s*#[0-9a-fA-F]{6}\\s*$");

    private static String getColorCode(Map<String, Object> map, String key, String defaultValue) {
        Object object = map.get(key);
        if (object != null) {
            if (object instanceof String) {
                String value = (String) object;
                if (COLOR_CODE.matcher(value).matches()) {
                    return value.trim();
                }

                LOG.warn("Color code for key '{}' has invalid syntax, please fix 'as-config.yml'. Falling back to default '{}'.", key, defaultValue);
            } else {
                LOG.warn("Color code for key '{}' is not a valid string, please fix 'as-config.yml'. Falling back to default '{}'.", key, defaultValue);
            }
        } else {
            LOG.warn("No color code for key '{}' is defined, please fix 'as-config.yml'. Falling back to default '{}'.", key, defaultValue);
        }

        return defaultValue;
    }

}
