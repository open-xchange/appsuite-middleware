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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.multifactor.MultifactorDevice;

/**
 * {@link MemoryMultifactorDeviceStorage2} - A generic device storage which operates in memory
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.0
 */
public class MemoryMultifactorDeviceStorage<T extends MultifactorDevice> {

    private static final Logger LOG = LoggerFactory.getLogger(MemoryMultifactorDeviceStorage.class);
    private static final String KEY_DELIMITER = ":";
    public static final long UNLIMITED_REGISTRATION_LIFETIME = 0;
    public static final long DEFAULT_REGISTRATION_LIFETIME = TimeUnit.MINUTES.toMillis(5); // 5min in ms

    private final ConcurrentHashMap<String, RegistrationContainer> registrations = new ConcurrentHashMap<>();
    private final Object lock = new Object();
    final long registrationLifeTime;

    /**
     * {@link DeviceRegistration} - Internal device registration
     *
     * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
     * @since v7.10.2
     */
    private class DeviceRegistration {

        private final T    device;
        private final long createdOn;

        DeviceRegistration(T device) {
            this.device = device;
            this.createdOn = new Date().getTime();
        }

        public T getDevice() {
            return device;
        }

        public long getCreatedOn() {
            return createdOn;
        }
    }

    private String getKey(int contextId,  int userId) {
        return String.valueOf(contextId) + KEY_DELIMITER + String.valueOf(userId);
    }

    /**
     *
     * {@link RegistrationContainer} - Container holding device registration per "session"/"user"
     *
     * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
     * @since v7.10.2
     */
    private class RegistrationContainer {

        private final Collection<DeviceRegistration> registrations;

        RegistrationContainer() {
            this.registrations = new ArrayList<>();
        }

        public synchronized boolean cleanup() {
            final long now = System.currentTimeMillis();
            if (registrationLifeTime > 0) {
                return registrations.removeIf(r -> now - r.getCreatedOn() > registrationLifeTime);
            }
            return false;
        }

        public synchronized RegistrationContainer addDevices(T device) {
            registrations.add(new DeviceRegistration(device));
            return this;
        }

        public synchronized boolean removeDevice(String id) {
            return registrations.removeIf(r -> r.getDevice().getId().equals(id));
        }

        public synchronized int getSize() {
            return registrations.size();
        }

        public synchronized Collection<T> getDevices() {
            return registrations.stream().map(r -> r.getDevice()).collect(Collectors.toList());
        }
    }

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
        synchronized (lock) {
            final Iterator<Entry<String, RegistrationContainer>> iterator = registrations.entrySet().iterator();
            while (iterator.hasNext()) {
                final RegistrationContainer next = iterator.next().getValue();
                next.cleanup();
                if (next.getSize() == 0) {
                    iterator.remove();
                }
            }
            LOG.debug("storage size: " + registrations.size());
        }
    }

    /**
     * Registers a new device to the storage
     *
     * @param contextId The context ID to register the device for
     * @param userId The user ID to register the device for
     * @param device The device to register
     */
    public void registerDevice(int contextId, int userId, T device) {
        device = Objects.requireNonNull(device, "device  must not be null");
        cleanup();
        synchronized (lock) {
            RegistrationContainer existingContainer =
                registrations.putIfAbsent(getKey(contextId, userId), new RegistrationContainer().addDevices(device));
            if(existingContainer != null) {
               existingContainer.addDevices(device);
            }
            LOG.debug("storage size: " + registrations.size());
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
        device = Objects.requireNonNull(device, "device must not be null");
        return unregisterDevice(contextId, userId, device.getId());
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
        synchronized (lock) {
            final String key = getKey(contextId, userId);
            RegistrationContainer registrationsForSession = registrations.get(key);
            boolean removed = registrationsForSession.removeDevice(deviceId);
            if(removed && registrationsForSession.getSize() == 0) {
                registrations.remove(key);
            }
            LOG.debug("storage size: " + registrations.size());
            return removed;
        }
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
        RegistrationContainer registrationsForSession = registrations.get(getKey(contextId, userId));
        return registrationsForSession == null ?
            Collections.emptyList() :
            Collections.unmodifiableCollection(registrationsForSession.getDevices());
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
