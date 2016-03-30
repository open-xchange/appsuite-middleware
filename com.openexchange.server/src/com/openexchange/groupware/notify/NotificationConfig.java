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

        NOTIFY_ON_DELETE("notify_participants_on_delete"),
        OBJECT_LINK("object_link"),
        INTERNAL_IMIP("imipForInternalUsers"),
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
        if(!INSTANCE.isPropertiesLoadInternal()) {
            try {
                INSTANCE.loadPropertiesInternal();
            } catch (final OXException e) {
                LOG.error("", e);
                return def;
            }
        }
        if(!INSTANCE.isPropertiesLoadInternal()) {
            return def;
        }
        return INSTANCE.getPropertyInternal(prop.getName(), def);
    }

    public static boolean getPropertyAsBoolean(final NotificationProperty prop, final boolean def) {
        final String boolVal = getProperty(prop,null);
        if(boolVal == null) {
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
        if(!INSTANCE.isPropertiesLoadInternal()) {
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
