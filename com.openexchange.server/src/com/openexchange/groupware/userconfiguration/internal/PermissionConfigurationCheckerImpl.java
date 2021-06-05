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

package com.openexchange.groupware.userconfiguration.internal;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.l;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.userconfiguration.Permission;
import com.openexchange.groupware.userconfiguration.PermissionCheckerCodes;
import com.openexchange.groupware.userconfiguration.PermissionConfigurationChecker;
import com.openexchange.java.Strings;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link PermissionConfigurationCheckerImpl} - The implementation for {@link PermissionConfigurationChecker}.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
@SingletonService
public class PermissionConfigurationCheckerImpl implements PermissionConfigurationChecker {

    private static final Logger LOG = LoggerFactory.getLogger(PermissionConfigurationCheckerImpl.class);

    /**
     * An extra logger for capability checks, which is only invoked when cache accepts a value.<br>
     * That is when passed value is not yet contained or its cache entry is considered as expired.
     */
    private static final Logger CAPABILITY_LOG = LoggerFactory.getLogger(PermissionConfigurationChecker.class.getSimpleName() + "_capability");
    private static final long TIME_TO_WAIT = TimeUnit.HOURS.toMillis(1);
    private static final Cache<String, Long> CAPABILITY_LOG_CACHE = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

    private static final Set<String> ILLEGAL_PROPERTIES;
    private static final Set<String> ILLEGAL_CAPABILITIES;

    static {
        Permission[] permissions = Permission.values();

        ImmutableSet.Builder<String> illegalProps = ImmutableSet.builderWithExpectedSize(permissions.length);
        ImmutableSet.Builder<String> illegalCaps = ImmutableSet.builderWithExpectedSize(permissions.length);

        StringBuilder propNameBuilder = new StringBuilder("com.openexchange.capability.");
        int reslen = propNameBuilder.length();

        for (Permission perm: permissions) {
            propNameBuilder.setLength(reslen);
            illegalProps.add(propNameBuilder.append(perm.getCapabilityName()).toString());
            illegalCaps.add(perm.getCapabilityName());
        }

        ILLEGAL_PROPERTIES = illegalProps.build();
        ILLEGAL_CAPABILITIES = illegalCaps.build();
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void checkConfig(ConfigurationService configService) {
        try {
            check(configService);
        } catch (OXException e) {
            LOG.warn("Unable to check configuration for invalid properties. Unable to load properties: {}", e.getMessage(), e);
            return;
        }
    }

    /**
     * Checks if any permissions are configured as capabilities and logs an error for them.
     *
     * @param configService The {@link ConfigurationService}
     * @throws OXException If properties couldn't be loaded
     */
    private void check(ConfigurationService configService) throws OXException {
        Map<String, String> props = configService.getProperties((n, v) -> ILLEGAL_PROPERTIES.contains(n));
        if (props.isEmpty() == false) {
            props.forEach((name, v) -> LOG.error("Permissions must not be defined as properties. Please remove '{}'", name));
        }

        // Check context sets
        Map<String, Object> yamlInFolder = configService.getYamlInFolder("contextSets");
        // @formatter:off
        yamlInFolder.entrySet()
                    .stream()
                    .filter((e) -> e.getValue() != null)
                    .forEach((e) -> containsPermissions(e.getValue().toString()).ifPresent((perm) -> LOG.error("Permissions must not be defined in context sets. Please remove '{}' from {}", perm, e.getKey())));
        // @formatter:on
    }

    @Override
    public void checkAttributes(Map<String, String> attributes) throws OXException {
        if (attributes == null || attributes.isEmpty()) {
            // Nothing to do
            return;
        }

        String illegal = attributes.entrySet().stream()
                             .filter((e) -> e.getValue() != null)
                             .filter((e) -> ILLEGAL_PROPERTIES.contains(e.getKey()))
                             .map((e) -> e.getKey())
                             .collect(Collectors.joining(","));

        if (Strings.isNotEmpty(illegal)) {
            LOG.error("Setting the permission(s) '{}' via user attributes is not allowed!!", illegal);
            throw PermissionCheckerCodes.ILLEGAL_USER_ATTRIBUTE.create(illegal);
        }
    }

    @Override
    public boolean isLegal(String capability, int userId, int contextId) {
        if (!ILLEGAL_CAPABILITIES.contains(capability)) {
            return true;
        }

        // Contained in set of illegal capabilities.
        String key = getKey(contextId, userId, capability);
        if (userId <= 0 || contextId <= 0) {
            logCapabilityWarning(key, "Ignoring capability '{}' because it's in conflict with a permission.", capability);
            return false;
        } else {
            logCapabilityWarning(key, "Ignoring capability '{}' for user {} in context {} because it's in conflict with a permission.", capability, I(userId), I(contextId));
            return false;
        }
    }

    /**
     * Logs the capability with rate limiting.
     *
     * @param key The cache key. See {@link #getKey(int, int, String)}
     * @param message The message
     * @param args The message arguments
     */
    private void logCapabilityWarning(String key, String message, Object... args) {
        long now = System.currentTimeMillis();
        Long lastLogged = CAPABILITY_LOG_CACHE.getIfPresent(key);
        if (null == lastLogged || now - l(lastLogged) > TIME_TO_WAIT) {
            CAPABILITY_LOG_CACHE.put(key, L(now));
            CAPABILITY_LOG.warn(message, args);
        } else {
            CAPABILITY_LOG.debug(message, args);
        }
    }

    /**
     * Gets the cache key for the {@link #CAPABILITY_LOG_CACHE}
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param capability The capability
     * @return The cache key
     */
    private static String getKey(int contextId, int userId, String capability) {
        if (contextId <= 0) {
            return capability;
        }

        if (userId <= 0) {
            return new StringBuilder(16).append(contextId).append('_').append(capability).toString();
        }

        return new StringBuilder(24).append(contextId).append('_').append(userId).append('_').append(capability).toString();
    }

    @Override
    public void checkCapabilities(Set<String> caps) throws OXException {
        if (caps == null || caps.isEmpty()) {
            return;
        }
        String capString = caps.stream().filter((cap) -> ILLEGAL_CAPABILITIES.contains(cap)).collect(Collectors.joining(","));
        if (Strings.isNotEmpty(capString)) {
            LOG.error("The capabilities '{}' are in conflict with permissions! Permissions must not be defined as capabilities.", capString);
            throw PermissionCheckerCodes.ILLEGAL_CAPABILITY.create(capString);
        }
    }

    /**
     * Checks if the given text contains any permissions and returns the first found
     *
     * @param text The text to check
     * @return The optional found permission
     */
    private Optional<String> containsPermissions(String text) {
        return ILLEGAL_PROPERTIES.stream().filter((invalid) -> text.contains(invalid)).findAny();
    }

}
