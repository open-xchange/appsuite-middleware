/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.serverconfig.impl;

import java.util.AbstractMap.SimpleEntry;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.capabilities.Capability;
import com.openexchange.java.Strings;
import com.openexchange.serverconfig.ClientServerConfigFilter;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.NotificationMailConfig;

/**
 * {@link ServerConfigImpl}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 * @since v7.8.0
 */
public class ServerConfigImpl implements ServerConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ServerConfigImpl.class);

    private final Map<String, Object> mappings;
    private final List<ClientServerConfigFilter> clientServerConfigFilters;

    public ServerConfigImpl(Map<String, Object> mappings, List<ClientServerConfigFilter> clientServerConfigFilters) {
        this.mappings = mappings;
        this.clientServerConfigFilters = clientServerConfigFilters;
    }

    @Override
    public Map<String, Object> asMap() {
        return new HashMap<>(mappings);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<Capability> getCapabilities() {
        return (Set<Capability>) mappings.get("capabilities");
    }

    @Override
    public boolean isForceHttps() {
        return (boolean) mappings.get("forceHttps");
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
            LOG.warn("Invalid language mapping found in ServerConfig. Expected List but got {}", foundLanguages.getClass());
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
        Map<String, Object> forClient = new HashMap<>(mappings);
        for(ClientServerConfigFilter filter : clientServerConfigFilters) {
            filter.apply(forClient);
        }

        forClient.remove("notificationMails");
        return forClient;
    }

    @Override
    public NotificationMailConfig getNotificationMailConfig() {
        NotificationMailConfigImpl mailConfig = new NotificationMailConfigImpl();
        mailConfig.setButtonBackgroundColor("#3c73aa");
        mailConfig.setButtonBorderColor("#356697");
        mailConfig.setButtonTextColor("#ffffff");
        Map<String, Object> mailsMap = (Map<String, Object>) mappings.get("notificationMails");
        if(mailsMap == null) {
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
                } else {
                    LOG.warn("Color code for key '{}' has invalid syntax, please fix 'as-config.yml'. Falling back to default '{}'.", key, defaultValue);
                }
            } else {
                LOG.warn("Color code for key '{}' is not a valid string, please fix 'as-config.yml'. Falling back to default '{}'.", key, defaultValue);
            }
        } else {
            LOG.warn("No color code for key '{}' is defined, please fix 'as-config.yml'. Falling back to default '{}'.", key, defaultValue);
        }

        return defaultValue;
    }

}
