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

package com.openexchange.passwordchange.history.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.passwordchange.history.PasswordChangeRecorderRegistryService;
import com.openexchange.passwordchange.history.PasswordChangeRecorderException;
import com.openexchange.passwordchange.history.PasswordChangeRecorder;
import com.openexchange.user.UserService;

/**
 * {@link PasswordChangeTrackerRegistryImpl} - Implementation of {@link PasswordChangeRecorderRegistryService} as an {@link ServiceTrackerCustomizer}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class PasswordChangeRecorderRegistryServiceImpl implements ServiceTrackerCustomizer<PasswordChangeRecorder, PasswordChangeRecorder>, PasswordChangeRecorderRegistryService {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordChangeRecorderRegistryServiceImpl.class);

    private final ConcurrentMap<String, PasswordChangeRecorder> recorders;
    private final BundleContext context;
    private final ConfigViewFactory viewFactory;
    private final UserService userService;

    /**
     * Initializes a new {@link PasswordChangeTrackerRegistryImpl}.
     */
    public PasswordChangeRecorderRegistryServiceImpl(BundleContext context, ConfigViewFactory viewFactory, UserService userService) {
        super();
        this.viewFactory = viewFactory;
        this.userService = userService;
        this.recorders = new ConcurrentHashMap<>();
        this.context = context;
    }

    /**
     * Register a new {@link PasswordChangeRecorder}
     *
     * @param recorder The actual recorder
     * @return <code>true</code> if successfully added to this registry; otherwise <code>false</code>
     */
    public boolean register(PasswordChangeRecorder recorder) {
        if (null == recorder) {
            return false;
        }

        String symbolicName = recorder.getSymbolicName();
        if (Strings.isEmpty(symbolicName)) {
            LOG.debug("Could not add recorder for name {}", symbolicName);
            return false;
        }

        return null == this.recorders.putIfAbsent(symbolicName, recorder);
    }

    /**
     * Unregister a {@link PasswordChangeRecorder}
     *
     * @param symbolicName The name of the {@link PasswordChangeRecorder}
     */
    public void unregister(String symbolicName) {
        if (false == Strings.isEmpty(symbolicName)) {
            LOG.debug("Try to remove recorder with name {}", symbolicName);
            this.recorders.remove(symbolicName);
        }
    }

    @Override
    public Map<String, PasswordChangeRecorder> getRecorders() {
        return this.recorders;
    }

    @Override
    public PasswordChangeRecorder getRecorder(String symbolicName) {
        if (false == Strings.isEmpty(symbolicName)) {
            return this.recorders.get(symbolicName);
        }
        return null;
    }

    @Override
    public PasswordChangeRecorder getRecorderForUser(int userId, int contextId) throws OXException {
        // Check user
        User user = userService.getUser(userId, contextId);
        if (user.isGuest()) {
            throw PasswordChangeRecorderException.DENIED_FOR_GUESTS.create();
        }

        // Load config and get according tracker
        ConfigView view = viewFactory.getView(userId, contextId);

        // Enabled
        Boolean enabled;
        {
            ComposedConfigProperty<Boolean> enabledProperty = view.property(PasswordChangeRecorderProperties.ENABLE.getFQPropertyName(), Boolean.class);
            if (enabledProperty.isDefined()) {
                enabled = enabledProperty.get();
            } else {
                enabled = PasswordChangeRecorderProperties.ENABLE.getDefaultValue(Boolean.class);
            }
        }
        if (null == enabled || !enabled.booleanValue()) {
            throw PasswordChangeRecorderException.DISABLED.create(userId, contextId);
        }

        // Recorder name
        String recorderName;
        {
            ComposedConfigProperty<String> recorderNameProperty = view.property(PasswordChangeRecorderProperties.RECORDER.getFQPropertyName(), String.class);
            if (recorderNameProperty.isDefined()) {
                recorderName = recorderNameProperty.get();
            } else {
                recorderName = PasswordChangeRecorderProperties.RECORDER.getDefaultValue(String.class);
            }
        }
        if (Strings.isEmpty(recorderName)) {
            recorderName = PasswordChangeRecorderProperties.RECORDER.getDefaultValue(String.class);
            LOG.debug("No recorder found. Falling back to default value");
        }

        PasswordChangeRecorder recorder = getRecorder(recorderName);
        if (null == recorder) {
            // If no recorder available, there should be no tracking
            LOG.debug("Could not load {} for user {} in context {}", recorderName, userId, contextId);
            throw PasswordChangeRecorderException.MISSING_RECORDER.create(recorderName);
        }
        return recorder;
    }

    // ----------------------------------------------------- ServiceTracker stuff ----------------------------------------------------------

    @Override
    public PasswordChangeRecorder addingService(ServiceReference<PasswordChangeRecorder> reference) {
        PasswordChangeRecorder recorder = context.getService(reference);
        boolean added = register(recorder);
        if (added) {
            return recorder;
        }

        LOG.warn("Could not add {}. A recorder with symbolic name '{}' already exists", recorder.getClass().getName(), recorder.getSymbolicName());
        context.ungetService(reference);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference<PasswordChangeRecorder> reference, PasswordChangeRecorder recorder) {
        // Ignore
    }

    @Override
    public void removedService(ServiceReference<PasswordChangeRecorder> reference, PasswordChangeRecorder recorder) {
        unregister(recorder.getSymbolicName());
        context.ungetService(reference);
    }
}
