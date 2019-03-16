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

package com.openexchange.multifactor.storage.impl;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.multifactor.MultifactorDevice;
import com.openexchange.session.UserAndContext;

/**
 * {@link MemoryMultifactorDeviceStorage2} - A generic device storage which operates in memory
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.0
 */
public class MemoryMultifactorDeviceStorage<T extends MultifactorDevice> {

    private static final Logger LOG = LoggerFactory.getLogger(MemoryMultifactorDeviceStorage.class);

    public static final long UNLIMITED_REGISTRATION_LIFETIME = 0;
    public static final long DEFAULT_REGISTRATION_LIFETIME = TimeUnit.MINUTES.toMillis(5); // 5min in ms

    /**
     * Internal device registration
     */
    private static class DeviceRegistration<T extends MultifactorDevice> {

        private final T    device;
        private final long createdOn;

        DeviceRegistration(T device) {
            super();
            this.device = device;
            this.createdOn = System.currentTimeMillis();
        }

        public T getDevice() {
            return device;
        }

        public long getCreatedOn() {
            return createdOn;
        }
    }

    /**
     * Container holding device registration per "session"/"user"
     */
    private static class RegistrationContainer<T extends MultifactorDevice> {

        private final Collection<DeviceRegistration<T>> registrations;
        private final long registrationLifeTime;
        private boolean nonexistent;

        RegistrationContainer(long registrationLifeTime) {
            super();
            this.registrationLifeTime = registrationLifeTime;
            this.registrations = new ArrayList<>();
            nonexistent = false;
        }

        public void markNonExistent() {
            this.nonexistent = true;
        }

        public boolean isNonExistent() {
            return nonexistent;
        }

        public boolean cleanup() {
            if (registrationLifeTime <= 0) {
                return false;
            }
            final long now = System.currentTimeMillis();
            return registrations.removeIf(r -> now - r.getCreatedOn() > registrationLifeTime);
        }

        public RegistrationContainer<T> addDevices(T device) {
            registrations.add(new DeviceRegistration<T>(device));
            return this;
        }

        public boolean removeDevice(String id) {
            return registrations.removeIf(r -> r.getDevice().getId().equals(id));
        }

        public boolean isEmpty() {
            return registrations.isEmpty();
        }

        public Collection<T> getDevices() {
            return registrations.stream().map(r -> r.getDevice()).collect(Collectors.toList());
        }
    }

    private static UserAndContext getKey(int contextId,  int userId) {
        return UserAndContext.newInstance(userId, contextId);
    }

    // ------------------------------------------------------------------------------------------------------------------------

    private final ConcurrentHashMap<UserAndContext, RegistrationContainer<T>> registrations = new ConcurrentHashMap<>();
    private final long registrationLifeTime;

    /**
     * Initializes a new {@link MemoryMultifactorDeviceStorage} with the default lifetime.
     */
    public MemoryMultifactorDeviceStorage() {
        this(DEFAULT_REGISTRATION_LIFETIME);
    }

    /**
     * Initializes a new {@link MemoryMultifactorDeviceStorage}.
     *
     * @param registrationLifeTime The lifetime of pending registrations in ms
     */
    public MemoryMultifactorDeviceStorage(long registrationLifeTime) {
        this.registrationLifeTime = registrationLifeTime;
    }

    /**
     * Internal method to remove expired pending device registrations
     */
    private void cleanup() {
        int size = 0;
        for (Iterator<RegistrationContainer<T>> iterator = registrations.values().iterator(); iterator.hasNext();) {
            final RegistrationContainer<T> next = iterator.next();
            synchronized (next) {
                next.cleanup();
                if (next.isEmpty()) {
                    iterator.remove();
                    next.markNonExistent();
                } else {
                    size++;
                }
            }
        }
        LOG.debug("storage size: {}", I(size));
    }

    /**
     * Registers a new device to the storage
     *
     * @param contextId The context ID to register the device for
     * @param userId The user ID to register the device for
     * @param device The device to register
     */
    public void registerDevice(int contextId, int userId, T device) {
        T nonNullDevice = Objects.requireNonNull(device, "device  must not be null");
        cleanup();

        doRegisterDevice(contextId, userId, nonNullDevice);
    }

    private void doRegisterDevice(int contextId, int userId, T device) {
        UserAndContext key = getKey(contextId, userId);
        RegistrationContainer<T> container = registrations.get(key);
        if (container == null) {
            RegistrationContainer<T> newContainer = new RegistrationContainer<T>(registrationLifeTime);
            container = registrations.putIfAbsent(key, newContainer);
            if (container == null) {
                container = newContainer;
            }
        }

        boolean retry = false;
        synchronized (container) {
            if (container.isNonExistent()) {
                // Removed in the meantime
                retry = true;
            } else {
                container.addDevices(device);
            }
        }

        if (retry) {
            // Retry since RegistrationContainer has been removed in the meantime
            doRegisterDevice(contextId, userId, device);
        } else {
            LOG.debug("storage size: {}", I(registrations.size()));
        }
    }

    /**
     * Unregisters an existing device from the storage
     *
     * @param contextId The context ID to unregister the device for
     * @param userId The user ID to unregister the device for
     * @param device The device to unregister
     * @return <code>true</code> if the device was unregistered, <code>false</code> if the device was not found
     */
    public boolean unregisterDevice(int contextId, int userId, T device) {
        T nonNullDevice = Objects.requireNonNull(device, "device must not be null");
        return unregisterDevice(contextId, userId, nonNullDevice.getId());
    }

    /**
     * Unregisters an existing device from the storage
     *
     * @param contextId The context ID to unregister the device for
     * @param userId The user ID to unregister the device for
     * @param device The ID of the device to unregister
     * @return <code>true</code> if the device was unregistered, <code>false</code> if the device was not found
     */
    public boolean unregisterDevice(int contextId, int userId, String deviceId) {
        UserAndContext key = getKey(contextId, userId);

        RegistrationContainer<T> registrationsForSession = registrations.get(key);
        if (registrationsForSession == null) {
            return false;
        }

        boolean removed = false;
        boolean retry = false;
        synchronized (registrationsForSession) {
            if (registrationsForSession.isNonExistent()) {
                // Removed in the meantime
                retry = true;
            } else {
                removed = registrationsForSession.removeDevice(deviceId);
                if (removed && registrationsForSession.isEmpty()) {
                    registrations.remove(key);
                    registrationsForSession.markNonExistent();
                }
            }
        }

        if (retry) {
            // Retry since RegistrationContainer has been removed in the meantime
            return unregisterDevice(contextId, userId, deviceId);
        }

        LOG.debug("storage size: {}", I(registrations.size()));
        return removed;
    }

    /**
     * Gets all registered devices for a given session
     *
     * @param contextId The context ID to get the devices for
     * @param userId The user ID to get the devices for
     * @return A collection of registered devices related to the given session
     */
    public Collection<T> getDevices(int contextId, int userId) {
        cleanup();
        return doGetDevices(contextId, userId);
    }

    private Collection<T> doGetDevices(int contextId, int userId) {
        RegistrationContainer<T> registrationsForSession = registrations.get(getKey(contextId, userId));
        if (registrationsForSession == null) {
            return Collections.emptyList();
        }


        synchronized (registrationsForSession) {
            if (!registrationsForSession.isNonExistent()) {
                return Collections.unmodifiableCollection(registrationsForSession.getDevices());
            }
        }

        // Retry since RegistrationContainer has been removed in the meantime
        return doGetDevices(contextId, userId);
    }

    /**
     * Gets a specific device
     *
     * @param contextId The context ID to get the device for
     * @param userId The user ID to get the device for
     * @param deviceId The ID of the device to get
     * @return The device with the given ID or an empty optional if no such device was found
     */
    public Optional<T> getDevice(int contextId, int userId, String deviceId) {
        final Collection<T> devices = getDevices(contextId, userId);
        return devices.stream().filter(d -> d.getId().equals(deviceId)).findFirst();
    }
}
