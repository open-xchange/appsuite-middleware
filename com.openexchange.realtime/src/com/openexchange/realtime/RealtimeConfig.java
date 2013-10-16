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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.realtime;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.logging.Log;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.PropertyEvent;
import com.openexchange.config.PropertyListener;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.osgi.RealtimeServiceRegistry;
import com.openexchange.server.Initialization;


/**
 * {@link RealtimeConfig} Collects and exposes configuration parameters needed by the realtime stack.
 * 
 * @author <a href="mailto:marc .arens@open-xchange.com">Marc Arens</a>
 */
public class RealtimeConfig implements Initialization {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(RealtimeConfig.class);

    private static final RealtimeConfig instance = new RealtimeConfig();

    private final AtomicBoolean started = new AtomicBoolean();
    
    private HashMap<String, ChangeListener> changeListeners = new HashMap<String, ChangeListener>(2);

    private static final String isTraceAllUsersEnabledPropertyName = "com.openexchange.realtime.isTraceAllUsersEnabled";

    private static final String usersToTracePropertyName = "com.openexchange.realtime.usersToTrace";

    private boolean isTraceAllUsersEnabled = false;

    private Set<String> usersToTrace = Collections.emptySet();


    public static RealtimeConfig getInstance() {
        return instance;
    }

    /**
     * Gets the started
     * 
     * @return The started
     */
    public AtomicBoolean getStarted() {
        return started;
    }

    /**
     * Gets the isTraceAllUsersEnabled
     * 
     * @return The isTraceAllUsersEnabled
     */
    public boolean isTraceAllUsersEnabled() {
        return isTraceAllUsersEnabled;
    }

    /**
     * Sets the isTraceAllUsersEnabled
     * 
     * @param isTraceAllUsersEnabled The isTraceAllUsersEnabled to set
     */
    public void setTraceAllUsersEnabled(boolean isTraceAllUsersEnabled) {
        this.isTraceAllUsersEnabled = isTraceAllUsersEnabled;
    }

    /**
     * Gets the usersToTrace
     * 
     * @return The usersToTrace
     */
    public Set<String> getUsersToTrace() {
        return usersToTrace;
    }

    @Override
    public void start() throws OXException {
        if (!started.compareAndSet(false, true)) {
            LOG.error(this.getClass().getName() + " already started");
            return;
        }
        init();
    }

    @Override
    public void stop() {
        if (!started.compareAndSet(true, false)) {
            LOG.error(this.getClass().getName() + " cannot be stopped since it has no been started before");
            return;
        }
        ConfigurationService configService = RealtimeServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (configService == null) {
            LOG.error(
                "Couldn't unregister PropertyListeners",
                RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(ConfigurationService.class.getSimpleName()));
        } else {
            for (Entry<String, ChangeListener> entry : changeListeners.entrySet()) {
                configService.removePropertyListener(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Reads the complete configuration and adds PropertyListeners so we are informed about property changes.
     * 
     * @throws OXException
     */
    private void init() throws OXException {
        ConfigurationService configService = RealtimeServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (configService == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(ConfigurationService.class.getSimpleName());
        }

        ChangeListener isTraceAllUsersEnabledChangeListener = new ChangeListener() {
            @Override
            void doUpdate(RealtimeConfig realtimeConfig, ConfigurationService configService) {
                realtimeConfig.isTraceAllUsersEnabled = configService.getBoolProperty(
                    isTraceAllUsersEnabledPropertyName,
                    false);
            }
        };
        changeListeners.put(isTraceAllUsersEnabledPropertyName, isTraceAllUsersEnabledChangeListener);
        isTraceAllUsersEnabled = configService.getBoolProperty(isTraceAllUsersEnabledPropertyName, false, isTraceAllUsersEnabledChangeListener);

        ChangeListener usersToTraceChangeListener = new ChangeListener() {
            @Override
            void doUpdate(RealtimeConfig realtimeConfig, ConfigurationService configService) {
                realtimeConfig.usersToTrace = new HashSet<String>(configService.getProperty(
                    usersToTracePropertyName,
                    "",
                    ","));
            }
        };
        changeListeners.put(usersToTracePropertyName, usersToTraceChangeListener);
        usersToTrace = new HashSet<String>(configService.getProperty(usersToTracePropertyName, "", usersToTraceChangeListener, ","));
    }

    /**
     * {@link ChangeListener} that reacts on property changes.
     *
     * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
     */
    private abstract class ChangeListener implements PropertyListener {

        @Override
        public void onPropertyChange(PropertyEvent event) {
            try {
                ConfigurationService configService = RealtimeServiceRegistry.getInstance().getService(ConfigurationService.class);
                if (configService == null) {
                    throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(ConfigurationService.class.getSimpleName());
                }
                doUpdate(RealtimeConfig.getInstance(), configService);
            } catch (OXException e) {
                LOG.error(e.getMessage(), e);
            }
        }

        /**
         * Update the config property we are listening on in the RealtimeConfiguration
         * 
         * @param realtimeConfig The config to update
         * @param configService the configService to use for getting the updated value
         */
        abstract void doUpdate(RealtimeConfig realtimeConfig, ConfigurationService configService);

    }
}
