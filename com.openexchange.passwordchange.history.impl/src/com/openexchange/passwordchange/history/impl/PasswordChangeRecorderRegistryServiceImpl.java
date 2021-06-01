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
import com.openexchange.java.Strings;
import com.openexchange.passwordchange.history.PasswordChangeRecorder;
import com.openexchange.passwordchange.history.PasswordChangeRecorderException;
import com.openexchange.passwordchange.history.PasswordChangeRecorderRegistryService;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link PasswordChangeRecorderRegistryServiceImpl} - Implementation of {@link PasswordChangeRecorderRegistryService} as an {@link ServiceTrackerCustomizer}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class PasswordChangeRecorderRegistryServiceImpl implements ServiceTrackerCustomizer<PasswordChangeRecorder, PasswordChangeRecorder>, PasswordChangeRecorderRegistryService {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(PasswordChangeRecorderRegistryServiceImpl.class);

    private final ConcurrentMap<String, PasswordChangeRecorder> recorders;
    private final BundleContext context;
    private final ConfigViewFactory viewFactory;
    private final UserService userService;

    /**
     * Initializes a new {@link PasswordChangeRecorderRegistryServiceImpl}.
     *
     * @param context The {@link BundleContext}
     * @param viewFactory The {@link ConfigViewFactory}
     * @param userService The {@link UserService}
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
            LOGGER.debug("Could not add recorder for name {}", symbolicName);
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
        if (Strings.isNotEmpty(symbolicName)) {
            LOGGER.debug("Try to remove recorder with name {}", symbolicName);
            this.recorders.remove(symbolicName);
        }
    }

    @Override
    public Map<String, PasswordChangeRecorder> getRecorders() {
        return this.recorders;
    }

    @Override
    public PasswordChangeRecorder getRecorder(String symbolicName) {
        if (Strings.isNotEmpty(symbolicName)) {
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
            throw PasswordChangeRecorderException.DISABLED.create(Integer.valueOf(userId), Integer.valueOf(contextId));
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
            LOGGER.debug("No recorder found. Falling back to default value");
        }

        PasswordChangeRecorder recorder = getRecorder(recorderName);
        if (null == recorder) {
            // If no recorder available, there should be no tracking
            LOGGER.debug("Could not load {} for user {} in context {}", recorderName, Integer.valueOf(userId), Integer.valueOf(contextId));
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

        LOGGER.warn("Could not add {}. A recorder with symbolic name '{}' already exists", recorder.getClass().getName(), recorder.getSymbolicName());
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
