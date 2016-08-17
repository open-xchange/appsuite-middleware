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

package com.openexchange.realtime;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.java.ConcurrentSet;
import com.openexchange.management.ManagementAware;
import com.openexchange.management.ManagementObject;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.management.RealtimeConfigMBean;
import com.openexchange.realtime.management.RealtimeConfigManagement;
import com.openexchange.realtime.osgi.RealtimeServiceRegistry;
import com.openexchange.server.Initialization;

/**
 * {@link RealtimeConfig} Collects and exposes configuration parameters needed by the realtime stack.
 *
 * @author <a href="mailto:marc .arens@open-xchange.com">Marc Arens</a>
 */
public class RealtimeConfig implements Initialization, ManagementAware<RealtimeConfigMBean>, Reloadable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RealtimeConfig.class);

    private static final RealtimeConfig instance = new RealtimeConfig();

    private final AtomicBoolean started = new AtomicBoolean();

    private static final String isTraceAllUsersEnabledPropertyName = "com.openexchange.realtime.isTraceAllUsersEnabled";

    private static final String usersToTracePropertyName = "com.openexchange.realtime.usersToTrace";

    private boolean isTraceAllUsersEnabled = false;

    private Set<String> usersToTrace = new ConcurrentSet<String>();

    private final String numberOfRunLoopsPropertyName = "com.openexchange.realtime.numberOfRunLoops";

    private int numberOfRunLoops = 0;

    ManagementObject<RealtimeConfigMBean> managementObject;

    private RealtimeConfig() {
        managementObject = new RealtimeConfigManagement(this);
    }

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

    public void setTraceAllUsersEnabled(boolean enabled) {
        isTraceAllUsersEnabled = enabled;
    }

    /**
     * Gets the usersToTrace
     *
     * @return Returns an unmodifiable view of the users to trace
     */
    public Set<String> getUsersToTrace() {
        return Collections.unmodifiableSet(usersToTrace);
    }

    /**
     * Set the users to trace
     * @param users The users to trace
     */
    public void setUsersToTrace(Set<String> users) {
        usersToTrace = new ConcurrentSkipListSet<String>(users);

    }

    /**
     * Add a user from the users that should add tracers to sent Stanzas
     * @param user The users that should be removed
     * @return True if the user was added, else false
     */
    public boolean addUserToTrace(String user) {
        return usersToTrace.add(user);
    }

    /**
     * Remove a user from the users that should add tracers to sent Stanzas
     * @param user The users that should be removed
     * @return True if the user was removed, else false
     */
    public boolean removeUserToTrace(String user) {
        return usersToTrace.remove(user);
    }

    /**
     * Get the number of {@link RunLoop}s to use per {@link Component}
     * @return the number of {@link RunLoop}s to use per {@link Component}
     */
    public int getNumberOfRunLoops() {
        return numberOfRunLoops;
    }

    @Override
    public void start() throws OXException {
        if (!started.compareAndSet(false, true)) {
            LOG.error("{} already started", this.getClass().getName());
            return;
        }
        init();
    }

    @Override
    public void stop() {
        if (!started.compareAndSet(true, false)) {
            LOG.error("{} cannot be stopped since it has no been started before", this.getClass().getName());
            return;
        }
    }

    /**
     * Reads the complete configuration.
     *
     * @throws OXException
     */
    private void init() throws OXException {
        ConfigurationService configService = RealtimeServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (configService == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(ConfigurationService.class.getSimpleName());
        }
        isTraceAllUsersEnabled = configService.getBoolProperty(isTraceAllUsersEnabledPropertyName,false);

        usersToTrace = new HashSet<String>(configService.getProperty(usersToTracePropertyName, "", ","));

        numberOfRunLoops = configService.getIntProperty(numberOfRunLoopsPropertyName, 16);
    }

    @Override
    public ManagementObject<RealtimeConfigMBean> getManagementObject() {
        return managementObject;
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        isTraceAllUsersEnabled = configService.getBoolProperty(isTraceAllUsersEnabledPropertyName,false);
        usersToTrace = new HashSet<String>(configService.getProperty(usersToTracePropertyName, "", ","));
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties(isTraceAllUsersEnabledPropertyName, usersToTracePropertyName);
    }

}
