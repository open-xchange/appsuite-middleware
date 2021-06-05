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

package com.openexchange.groupware.notify;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ConfigurationExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.server.Initialization;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.conf.AbstractConfig;

/**
 * DEPENDS ON: SystemConfig
 */
public class NotificationConfig extends AbstractConfig implements Initialization {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(NotificationConfig.class);

    public enum NotificationProperty{

        OBJECT_LINK("object_link"),
        FROM_SOURCE("com.openexchange.notification.fromSource");


        private final String name;

        private NotificationProperty(final String name){
            this.name = name;
        }

        public String getName(){
            return name;
        }

    }

    private static Map<String, String> overriddenProperties = null;

    private static NotificationConfig INSTANCE = new NotificationConfig();

    public static NotificationConfig getInstance() {
        return INSTANCE;
    }

    @Override
    protected String getPropertyFileName() throws OXException {
        ConfigurationService configService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
		final File file = configService.getFileByName("notification.properties");
        final String filename = null == file ? null : file.getPath();
        if (null == filename) {
            throw ConfigurationExceptionCodes.PROPERTY_MISSING.create("notification.properties");
        }
        return filename;
    }

    public static String getProperty(final NotificationProperty prop, final String def) {
    	if (overriddenProperties != null) {
    		String overridden = overriddenProperties.get(prop.name);
    		if (overridden != null) {
    			return overridden;
    		}
    	}
        if (!INSTANCE.isPropertiesLoadInternal()) {
            try {
                INSTANCE.loadPropertiesInternal();
            } catch (OXException e) {
                LOG.error("", e);
                return def;
            }
        }
        if (!INSTANCE.isPropertiesLoadInternal()) {
            return def;
        }
        return INSTANCE.getPropertyInternal(prop.getName(), def);
    }

    public static boolean getPropertyAsBoolean(final NotificationProperty prop, final boolean def) {
        final String boolVal = getProperty(prop,null);
        if (boolVal == null) {
            return def;
        }
        return Boolean.parseBoolean(boolVal);
    }

    public static void override(NotificationProperty prop, String value) {
    	if (overriddenProperties == null) {
    		overriddenProperties = new HashMap<String, String>();
    	}
    	overriddenProperties.put(prop.name, value);
    }

    public static void forgetOverrides() {
    	overriddenProperties = null;
    }

    @Override
    public void start() throws OXException {
        if (!INSTANCE.isPropertiesLoadInternal()) {
            INSTANCE.loadPropertiesInternal();
        }
        NotificationPool.getInstance().startup();
    }

    @Override
    public void stop() {
         NotificationPool.getInstance().shutdown();
        INSTANCE = new NotificationConfig();
    }

}
